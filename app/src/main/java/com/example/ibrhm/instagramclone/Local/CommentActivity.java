package com.example.ibrhm.instagramclone.Local;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.example.ibrhm.instagramclone.Models.CommentLike;
import com.example.ibrhm.instagramclone.Models.Comments;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.CommentsAdapter;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.widget.Mention;
import com.hendraanggrian.widget.MentionAdapter;
import com.hendraanggrian.widget.SocialAutoCompleteTextView;
import com.hendraanggrian.widget.SocialEditText;
import com.vanniktech.emoji.EmojiEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class CommentActivity extends AppCompatActivity {

    private static int ACTIVITY_NO = 0;
    private ImageView mBackView;

    private String post_user_id, post_id;

    private CircleImageView mCommentProfileImage;
    private SocialAutoCompleteTextView mCommentInput;
    private TextView mCommentPostBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private List<Comments> mCommentsList;
    private RecyclerView mCommentsView;
    private CommentsAdapter mCommentsAdapter;

    public static boolean focus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mBackView = findViewById(R.id.comments_back_button);
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentActivity.super.onBackPressed();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCommentProfileImage = findViewById(R.id.comment_profile_image);
        mCommentInput = findViewById(R.id.comment_input);
        mCommentPostBtn = findViewById(R.id.comment_post_btn);
        mCommentsView = findViewById(R.id.comments_container);

        if(getIntent().getExtras() != null){
            post_user_id = getIntent().getStringExtra("post_user_id");
            post_id = getIntent().getStringExtra("post_id");
            ACTIVITY_NO = getIntent().getIntExtra("activity_no", 0);

            getData();
        }

        getComments();

        postComment();
        commentWatcher();

        if(focus){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            mCommentInput.requestFocus();
        }else{
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        final MentionAdapter mentionAdapter = new MentionAdapter(this);

        mCommentInput.setMentionTextChangedListener(new Function2<MultiAutoCompleteTextView, String, Unit>() {
            @Override
            public Unit invoke(MultiAutoCompleteTextView multiAutoCompleteTextView, String s) {
                FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("user_name").startAt(s).endAt(s+"\uf8ff")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        mentionAdapter.clear();
                                        Users user = ds.getValue(Users.class);
                                        if(user.getDetails().getProfile_image().equals("")){
                                            mentionAdapter.add(new Mention(user.getUser_name(), user.getFull_name(), R.drawable.ic_default_avatar));
                                        }
                                        else{
                                            mentionAdapter.add(new Mention(user.getUser_name(), user.getFull_name(), user.getDetails().getProfile_image()));
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                return null;
            }
        });

        mCommentInput.setMentionAdapter(mentionAdapter);
    }

    private void postComment(){
        mCommentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String comment_key = mRootRef.child("Posts").child(post_user_id).child(post_id).child("comments").push().getKey();
                final String comment = mCommentInput.getText().toString();

                Map commentMap = new HashMap();
                commentMap.put("comment", mCommentInput.getText().toString());
                commentMap.put("time", ServerValue.TIMESTAMP);
                commentMap.put("user_id", mCurrentUser.getUid());
                commentMap.put("comment_id", comment_key);
                commentMap.put("caption", false);
                commentMap.put("send", false);

                mRootRef.child("Posts").child(post_user_id).child(post_id).child("comments").child(comment_key).setValue(commentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRootRef.child("Posts").child(post_user_id).child(post_id).child("comments").child(comment_key).child("send").setValue(true);

                        mRootRef.child("News").child("Interactions").child(post_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    boolean isTrigger = true;
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        if(ds.child("post_user_id").getValue().toString().equals(mCurrentUser.getUid()) &&
                                                ds.child("action").getValue().toString().equals("comment")){
                                            isTrigger = false;
                                            break;
                                        }
                                    }

                                    if(isTrigger){
                                        String new_key = mRootRef.child("News").child("Interactions").child(post_user_id).child(post_id).push().getKey();

                                        Map newsMap = new HashMap();
                                        newsMap.put("News/Interactions/"+post_user_id+"/"+new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                        newsMap.put("News/Interactions/"+post_user_id+"/"+new_key+"/post_id", post_id);
                                        newsMap.put("News/Interactions/"+post_user_id+"/"+new_key+"/post_user_id", post_user_id);
                                        newsMap.put("News/Interactions/"+post_user_id+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                        newsMap.put("News/Interactions/"+post_user_id+"/"+new_key+"/action", "comment");
                                        newsMap.put("News/Interactions/"+post_user_id+"/"+new_key+"/content", comment);
                                        mRootRef.updateChildren(newsMap);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                mCommentInput.getText().clear();
                mCommentsView.smoothScrollToPosition(mCommentsList.size());
            }
        });
    }

    private void commentWatcher(){
        mCommentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mCommentInput.getText().length() > 0){
                    mCommentPostBtn.setEnabled(true);
                }
                else{
                    mCommentPostBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void getData(){
        mRootRef.child("Users").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                mCommentInput.setHint("Comment as "+user.getUser_name()+"...");
                UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mCommentProfileImage, null, "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getComments(){
        mCommentsList = new ArrayList<>();
        mCommentsAdapter = new CommentsAdapter(CommentActivity.this, mCommentsList, post_user_id, post_id, ACTIVITY_NO);
        mCommentsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mCommentsView.setHasFixedSize(true);
        mCommentsView.setAdapter(mCommentsAdapter);

        mRootRef.child("Posts").child(post_user_id).child(post_id).child("comments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comments comment = dataSnapshot.getValue(Comments.class);
                mCommentsList.add(comment);
                mCommentsAdapter.notifyItemInserted(mCommentsList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comments comment = dataSnapshot.getValue(Comments.class);
                if(comment.isSend()){
                    int position = findPosition(dataSnapshot.getKey());
                    if(position != -1){
                        CommentLike commentLike = new CommentLike(mCurrentUser.getUid());
                        if(commentLike.getUser_id() != null){
                            mCommentsList.get(position).setSend(true);
                            mCommentsList.get(position).setComment_like(commentLike);
                            mCommentsAdapter.notifyItemChanged(position);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int position = findPosition(dataSnapshot.getKey());
                mCommentsList.remove(position);
                mCommentsAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int findPosition(String id){
        for(int i = 0; i < mCommentsList.size(); i++){
            if(mCommentsList.get(i).getComment_id().equals(id)){
                return i;
            }
        }
        return -1;
    }
}
