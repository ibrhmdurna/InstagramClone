package com.example.ibrhm.instagramclone.News;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ibrhm.instagramclone.Local.CommentActivity;
import com.example.ibrhm.instagramclone.Local.LikeActivity;
import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.CommentLike;
import com.example.ibrhm.instagramclone.Models.Comments;
import com.example.ibrhm.instagramclone.Models.Interactions;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.CommentsAdapter;
import com.example.ibrhm.instagramclone.Utils.CommentsExtraAdapter;
import com.example.ibrhm.instagramclone.Utils.GetTimeAgo;
import com.example.ibrhm.instagramclone.Utils.GridImageView;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.CameraAnimation;
import com.github.kshitij_jain.instalike.InstaLikeView;
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
import com.universalvideoview.UniversalVideoView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class CommentsInteractionActivity extends AppCompatActivity {

    private ImageView mBackView;
    private CircleImageView mCommentProfileImage;
    private SocialAutoCompleteTextView mCommentInput;
    private TextView mCommentPostBtn;
    private LottieAnimationView mLoadingAnim;

    private UserPosts mPost;
    private Interactions mInteraction;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    private List<Comments> mCommentsList;
    private RecyclerView mCommentsView;
    private CommentsExtraAdapter mCommentsAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_interaction);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mInteraction = (Interactions) getIntent().getSerializableExtra("interaction");

        mCommentsList = new ArrayList<>();
        setupView();
        onBack();
        getData();

        commentWatcher();
        getComments();
        postComment();

        mCommentsView.setVisibility(View.INVISIBLE);
        mLoadingAnim.setVisibility(View.VISIBLE);
        mLoadingAnim.playAnimation();

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCommentsView.setVisibility(View.VISIBLE);
                mLoadingAnim.setVisibility(View.GONE);
                mLoadingAnim.pauseAnimation();
            }
        }, 1200);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                commentFocus();
            }
        }, 1400);

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


    private void commentFocus(){
        mCommentsList.get(1).setFocus(true);
        mCommentsAdapter.notifyItemChanged(1);
        mCommentsView.smoothScrollToPosition(5);
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCommentsList.get(1).setFocus(false);
                mCommentsAdapter.notifyItemChanged(1);
            }
        }, 2000);

        mCommentInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentsView.smoothScrollToPosition(mCommentsList.size());
            }
        });
    }

    private void postComment(){
        mCommentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String comment_key = mRootRef.child("Posts").child(mInteraction.getPost_user_id()).child(mInteraction.getPost_id()).child("comments").push().getKey();
                final String comment = mCommentInput.getText().toString();

                Map commentMap = new HashMap();
                commentMap.put("comment", mCommentInput.getText().toString());
                commentMap.put("time", ServerValue.TIMESTAMP);
                commentMap.put("user_id", mCurrentUser.getUid());
                commentMap.put("comment_id", comment_key);
                commentMap.put("caption", false);
                commentMap.put("send", false);

                mRootRef.child("Posts").child(mInteraction.getPost_user_id()).child(mInteraction.getPost_id()).child("comments").child(comment_key).setValue(commentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRootRef.child("Posts").child(mInteraction.getPost_user_id()).child(mInteraction.getPost_id()).child("comments").child(comment_key).child("send").setValue(true);

                        mRootRef.child("News").child("Interactions").child(mInteraction.getPost_user_id()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        String new_key = mRootRef.child("News").child("Interactions").child(mInteraction.getPost_user_id()).child(mInteraction.getPost_id()).push().getKey();

                                        Map newsMap = new HashMap();
                                        newsMap.put("News/Interactions/"+mInteraction.getPost_user_id()+"/"+new_key+"/user_id", mCurrentUser.getUid());
                                        newsMap.put("News/Interactions/"+mInteraction.getPost_user_id()+"/"+new_key+"/post_id", mInteraction.getPost_id());
                                        newsMap.put("News/Interactions/"+mInteraction.getPost_user_id()+"/"+new_key+"/post_user_id", mInteraction.getPost_user_id());
                                        newsMap.put("News/Interactions/"+mInteraction.getPost_user_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                        newsMap.put("News/Interactions/"+mInteraction.getPost_user_id()+"/"+new_key+"/action", "comment");
                                        newsMap.put("News/Interactions/"+mInteraction.getPost_user_id()+"/"+new_key+"/content", comment);
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

    private void getComments(){
        mCommentsList = new ArrayList<>();
        mCommentsAdapter = new CommentsExtraAdapter(this, mCommentsList, mInteraction.getPost_user_id(), mInteraction.getPost_id(), mInteraction, mPost, 3);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mCommentsView.setLayoutManager(mLayoutManager);
        mCommentsView.setHasFixedSize(true);
        mCommentsView.setAdapter(mCommentsAdapter);

        mRootRef.child("Posts").child(mInteraction.getPost_user_id()).child(mInteraction.getPost_id()).child("comments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comments comment = dataSnapshot.getValue(Comments.class);
                if(!comment.isCaption()){
                    if(findInteraction(comment)){
                        mCommentsList.add(0, comment);
                    }
                    else{
                        mCommentsList.add(comment);
                    }
                }

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

        mCommentsAdapter.setOnItemClickListener(new CommentsExtraAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                mCommentInput.requestFocus();
                mCommentsView.smoothScrollToPosition(mCommentsList.size());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }
        });
    }

    private boolean findInteraction(Comments value){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:00");
        String commentTime = simpleDateFormat.format(new Date(value.getTime()));

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy hh:00");
        String interactionTime = simpleDateFormat2.format(new Date(mInteraction.getTime()));

        return interactionTime.equals(commentTime) && value.getComment().equals(mInteraction.getContent()) && value.getUser_id().equals(mInteraction.getUser_id());
    }

    private int findPosition(String id){
        for(int i = 0; i < mCommentsList.size(); i++){
            if(mCommentsList.get(i) != null){
                if(mCommentsList.get(i).getComment_id().equals(id)){
                    return i;
                }
            }
        }
        return -1;
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

    private void onBack(){
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentsInteractionActivity.super.onBackPressed();
                finish();
            }
        });
    }

    private void setupView(){
        mBackView = findViewById(R.id.comments_int_back_button);
        mCommentInput = findViewById(R.id.comment_input);
        mCommentProfileImage = findViewById(R.id.comment_profile_image);
        mCommentPostBtn = findViewById(R.id.comment_post_btn);
        mCommentsView = findViewById(R.id.comments_int_container);
        mLoadingAnim = findViewById(R.id.comments_int_loading_anim);
    }
}
