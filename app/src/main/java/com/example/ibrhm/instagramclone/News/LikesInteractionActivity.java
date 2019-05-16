package com.example.ibrhm.instagramclone.News;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Local.CommentActivity;
import com.example.ibrhm.instagramclone.Local.LikeActivity;
import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Comments;
import com.example.ibrhm.instagramclone.Models.Interactions;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.ibrhm.instagramclone.Utils.GetTimeAgo;
import com.example.ibrhm.instagramclone.Utils.GridImageView;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.CameraAnimation;
import com.github.kshitij_jain.instalike.InstaLikeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.widget.SocialTextView;
import com.universalvideoview.UniversalVideoView;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikesInteractionActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNavigationView;

    private ImageView mBackView;
    private CircleImageView mProfileImage;
    private TextView mUserName;
    private TextView mPostTitle;
    private LinearLayout mUserLayout;
    private GridImageView mPostImage;
    private UniversalVideoView mVideoView;
    private SocialTextView mCaptionText;
    private TextView mTimeText;
    private ImageView mLikeView, mCommentView, mShareView;
    private TextView mLikeCount, mCommentCount;
    private InstaLikeView mBigLikeView;
    private CameraAnimation mCameraAnimation;
    private ImageView mPostOptions;

    private List<Comments> mCommentsList;

    private UserPosts mPost;
    private Interactions mInteraction;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes_interaction);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mInteraction = (Interactions) getIntent().getSerializableExtra("interaction");

        mCommentsList = new ArrayList<>();
        setupView();
        onBack();
        setupPost();
    }

    private void setupPost(){

        final String[] timeAgoSmall = {null};

        mRootRef.child("Posts").child(mInteraction.getPost_user_id()).child(mInteraction.getPost_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    final Posts post = dataSnapshot.getValue(Posts.class);

                    final String timeAgo = GetTimeAgo.getTimeAgo(post.getTime());
                    timeAgoSmall[0] = GetTimeAgo.getTimeAgoSmall(post.getTime());

                    mTimeText.setText(timeAgo);

                    String post_path = post.getPost_url();
                    String post_type = post_path.substring(post_path.lastIndexOf("."), post_path.lastIndexOf(".") + 4);

                    if (post_type.equals(".mp4") | post_type.equals(".3gp")) {
                        mVideoView.setVisibility(View.VISIBLE);
                        mCameraAnimation.setVisibility(View.VISIBLE);
                        mPostImage.setVisibility(View.GONE);
                        mVideoView.setVideoPath(post.getPost_url());
                        mVideoView.start();
                        mPostTitle.setText(getString(R.string.video_small));
                    } else {
                        mVideoView.setVisibility(View.GONE);
                        mCameraAnimation.setVisibility(View.GONE);
                        mPostImage.setVisibility(View.VISIBLE);
                        UniversalImageLoader.setImage(post.getPost_url(), mPostImage, null, "");
                        mPostTitle.setText(getString(R.string.photo_small));
                    }

                    mRootRef.child("Users").child(post.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshotE) {
                            final Users user = dataSnapshotE.getValue(Users.class);

                            UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");
                            mUserName.setText(user.getUser_name());

                            mPost = new UserPosts(user.getDetails().getProfile_image(), user.getUser_id(), user.getUser_name(), post.getCaption(), post.getPost_url(), post.getPost_id(), post.getType(), post.getThumb_image(), post.getTime(), post.getLikes());

                            if(!mPost.getCaption().equals("")){

                                mCaptionText.setVisibility(View.VISIBLE);

                                mCaptionText.setText(Html.fromHtml("<b>"+mPost.getUser_name()+"</b> "+mPost.getCaption()));

                                SpannableString ss = new SpannableString(mCaptionText.getText());

                                ss.setSpan(new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        if(mPost.getUser_id().equals(mCurrentUser.getUid())){
                                            for(int i = 0; i < App.background.getBackgroundList().size(); i++){
                                                if(App.background.getBackgroundList().get(i).equals("Profile")){
                                                    App.background.getBackgroundList().remove(i);
                                                    break;
                                                }
                                            }
                                            Intent profileIntent = new Intent(LikesInteractionActivity.this, ProfileActivity.class);
                                            startActivity(profileIntent);
                                        }
                                        else{
                                            Intent profileIntent = new Intent(LikesInteractionActivity.this, UserProfileActivity.class);
                                            profileIntent.putExtra("activity_no", 3);
                                            profileIntent.putExtra("user_id", mPost.getUser_id());
                                            startActivity(profileIntent);
                                        }
                                    }

                                    @Override
                                    public void updateDrawState(@NonNull TextPaint ds) {
                                        super.updateDrawState(ds);
                                        ds.setColor(Color.BLACK);
                                        ds.setUnderlineText(false);
                                    }
                                }, 0, mPost.getUser_name().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                mCaptionText.setText(ss);
                                mCaptionText.setMovementMethod(LinkMovementMethod.getInstance());

                                mCaptionText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent commentsIntent = new Intent(LikesInteractionActivity.this, CommentActivity.class);
                                        commentsIntent.putExtra("post_user_id", mPost.getUser_id());
                                        commentsIntent.putExtra("post_id", mPost.getPost_id());
                                        commentsIntent.putExtra("profile_image", mPost.getProfile_image());
                                        commentsIntent.putExtra("user_name", mPost.getUser_name());
                                        commentsIntent.putExtra("caption", mPost.getCaption());
                                        commentsIntent.putExtra("time", timeAgoSmall[0]);
                                        commentsIntent.putExtra("activity_no", 3);
                                        CommentActivity.focus = false;
                                        startActivity(commentsIntent);
                                    }
                                });
                            }else{
                                mCaptionText.setVisibility(View.GONE);
                            }

                            mCommentView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent commentIntent = new Intent(LikesInteractionActivity.this, CommentActivity.class);
                                    commentIntent.putExtra("post_user_id", mPost.getUser_id());
                                    commentIntent.putExtra("post_id", mPost.getPost_id());
                                    commentIntent.putExtra("profile_image", mPost.getProfile_image());
                                    commentIntent.putExtra("user_name", mPost.getUser_name());
                                    commentIntent.putExtra("caption", mPost.getCaption());
                                    commentIntent.putExtra("time", timeAgoSmall[0]);
                                    commentIntent.putExtra("activity_no", 3);
                                    CommentActivity.focus = true;
                                    startActivity(commentIntent);
                                }
                            });

                            mRootRef.child("Posts").child(mPost.getUser_id()).child(mPost.getPost_id()).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        mCommentsList.clear();
                                        mCommentCount.setVisibility(View.VISIBLE);
                                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                                            Comments comment = ds.getValue(Comments.class);
                                            mCommentsList.add(comment);
                                        }

                                        if(mCommentsList.get(0).isCaption()){
                                            if(mCommentsList.size() == 1){
                                                mCommentCount.setVisibility(View.GONE);
                                            }
                                            else{
                                                int count = mCommentsList.size() - 1;
                                                mCommentCount.setText("View all "+count+" comments");
                                            }
                                        }
                                        else{
                                            if(mCommentsList.size() == 1){
                                                mCommentCount.setText("View 1 comment");
                                            }
                                            else{
                                                int count = mCommentsList.size();
                                                mCommentCount.setText("View all "+count+" comments");
                                            }
                                        }

                                        mCommentCount.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent commentsIntent = new Intent(LikesInteractionActivity.this, CommentActivity.class);
                                                commentsIntent.putExtra("post_user_id", mPost.getUser_id());
                                                commentsIntent.putExtra("post_id", mPost.getPost_id());
                                                commentsIntent.putExtra("profile_image", mPost.getProfile_image());
                                                commentsIntent.putExtra("user_name", mPost.getUser_name());
                                                commentsIntent.putExtra("caption", mPost.getCaption());
                                                commentsIntent.putExtra("time", timeAgoSmall[0]);
                                                commentsIntent.putExtra("activity_no", 3);
                                                CommentActivity.focus = false;
                                                startActivity(commentsIntent);
                                            }
                                        });
                                    }else{
                                        mCommentCount.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            mLikeView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mRootRef.child("Posts").child(mPost.getUser_id()).child(mPost.getPost_id()).child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                if(dataSnapshot.hasChild(mCurrentUser.getUid())){
                                                    dataSnapshot.child(mCurrentUser.getUid()).getRef().removeValue();
                                                    mLikeView.setBackground(getResources().getDrawable(R.drawable.ufi_heart));

                                                    mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                                if(ds.child("user_id").getValue().toString().equals(mCurrentUser.getUid())
                                                                        && ds.child("action").getValue().toString().equals("like")
                                                                        && ds.child("post_id").getValue().toString().equals(mPost.getPost_id())){
                                                                    Map newsMap = new HashMap();
                                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+ds.getKey()+"/user_id", null);
                                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+ds.getKey()+"/post_id", null);
                                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+ds.getKey()+"/post_user_id", null);
                                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+ds.getKey()+"/time", null);
                                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+ds.getKey()+"/action", null);
                                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+ds.getKey()+"/content", null);
                                                                    mRootRef.updateChildren(newsMap);
                                                                    break;
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                    mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.exists()){
                                                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                                    if(ds.child("action").getValue().toString().equals("like") &&
                                                                            ds.child("user_id").getValue().toString().equals(mCurrentUser.getUid()) &&
                                                                            ds.child("post_id").getValue().toString().equals(mPost.getPost_id())){
                                                                        ds.getRef().removeValue();
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                    Map postsLiked = new HashMap();
                                                    postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/post_id", null);
                                                    postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/time", null);
                                                    mRootRef.updateChildren(postsLiked);
                                                }
                                                else{
                                                    Map likeMap = new HashMap();
                                                    likeMap.put("user_id", mCurrentUser.getUid());
                                                    likeMap.put("time", ServerValue.TIMESTAMP);

                                                    dataSnapshot.child(mCurrentUser.getUid()).getRef().updateChildren(likeMap);
                                                    mLikeView.setBackground(getResources().getDrawable(R.drawable.ic_like_active_icon));

                                                    if(!mPost.getUser_id().equals(mCurrentUser.getUid())){
                                                        String new_key = mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).child(mPost.getPost_id()).push().getKey();

                                                        Map newsMap = new HashMap();
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/user_id", mCurrentUser.getUid());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_id", mPost.getPost_id());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_user_id", mPost.getUser_id());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/action", "like");
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/content", "");
                                                        mRootRef.updateChildren(newsMap);

                                                        String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).push().getKey();

                                                        Map allNewsMap = new HashMap();
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/action", "like");
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/content", "");
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_id", mPost.getPost_id());
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_user_id", mPost.getUser_id());
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/user_id", mCurrentUser.getUid());
                                                        mRootRef.updateChildren(allNewsMap);
                                                    }

                                                    Map postsLiked = new HashMap();
                                                    postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/post_id", mPost.getPost_id());
                                                    postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                                    mRootRef.updateChildren(postsLiked);
                                                }
                                            }else{
                                                Map likeMap = new HashMap();
                                                likeMap.put("user_id", mCurrentUser.getUid());
                                                likeMap.put("time", ServerValue.TIMESTAMP);

                                                dataSnapshot.child(mCurrentUser.getUid()).getRef().updateChildren(likeMap);
                                                mLikeView.setBackground(getResources().getDrawable(R.drawable.ic_like_active_icon));

                                                if(!mPost.getUser_id().equals(mCurrentUser.getUid())){
                                                    String new_key = mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).child(mPost.getPost_id()).push().getKey();

                                                    Map newsMap = new HashMap();
                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/user_id", mCurrentUser.getUid());
                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_id", mPost.getPost_id());
                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_user_id", mPost.getUser_id());
                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/action", "like");
                                                    newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/content", "");
                                                    mRootRef.updateChildren(newsMap);

                                                    String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).push().getKey();

                                                    Map allNewsMap = new HashMap();
                                                    allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/action", "like");
                                                    allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                                    allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/content", "");
                                                    allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_id", mPost.getPost_id());
                                                    allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_user_id", mPost.getUser_id());
                                                    allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/user_id", mCurrentUser.getUid());
                                                    mRootRef.updateChildren(allNewsMap);
                                                }

                                                Map postsLiked = new HashMap();
                                                postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/post_id", mPost.getPost_id());
                                                postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                                mRootRef.updateChildren(postsLiked);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });

                            final long[] firstClick = {0};
                            final long[] lastClick = {0};

                            mPostImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    firstClick[0] = lastClick[0];
                                    lastClick[0] = System.currentTimeMillis();

                                    if(lastClick[0] - firstClick[0] < 300){

                                        mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                boolean isThere = true;
                                                if(dataSnapshot.exists()){
                                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                        if (ds.child("user_id").getValue().toString().equals(mCurrentUser.getUid())
                                                                && ds.child("action").getValue().toString().equals("like")
                                                                && ds.child("post_id").getValue().toString().equals(mPost.getPost_id())){
                                                            isThere = false;
                                                            break;
                                                        }
                                                    }

                                                    if(isThere){
                                                        Map likeMap = new HashMap();
                                                        likeMap.put("Posts/"+mPost.getUser_id()+"/"+mPost.getPost_id()+"/likes/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                                                        likeMap.put("Posts/"+mPost.getUser_id()+"/"+mPost.getPost_id()+"/likes/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);

                                                        mRootRef.updateChildren(likeMap);

                                                        if(!mPost.getUser_id().equals(mCurrentUser.getUid())){
                                                            String new_key = mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).child(mPost.getPost_id()).push().getKey();

                                                            Map newsMap = new HashMap();
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/user_id", mCurrentUser.getUid());
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_id", mPost.getPost_id());
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_user_id", mPost.getUser_id());
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/action", "like");
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/content", "");
                                                            mRootRef.updateChildren(newsMap);

                                                            String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).push().getKey();

                                                            Map allNewsMap = new HashMap();
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/action", "like");
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/content", "");
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_id", mPost.getPost_id());
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_user_id", mPost.getUser_id());
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/user_id", mCurrentUser.getUid());
                                                            mRootRef.updateChildren(allNewsMap);
                                                        }

                                                        Map postsLiked = new HashMap();
                                                        postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/post_id", mPost.getPost_id());
                                                        postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                                        mRootRef.updateChildren(postsLiked);
                                                    }
                                                }
                                                else{
                                                    Map likeMap = new HashMap();
                                                    likeMap.put("Posts/"+mPost.getUser_id()+"/"+mPost.getPost_id()+"/likes/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                                                    likeMap.put("Posts/"+mPost.getUser_id()+"/"+mPost.getPost_id()+"/likes/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);

                                                    mRootRef.updateChildren(likeMap);

                                                    if(!mPost.getUser_id().equals(mCurrentUser.getUid())){
                                                        String new_key = mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).child(mPost.getPost_id()).push().getKey();

                                                        Map newsMap = new HashMap();
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/user_id", mCurrentUser.getUid());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_id", mPost.getPost_id());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_user_id", mPost.getUser_id());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/action", "like");
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/content", "");
                                                        mRootRef.updateChildren(newsMap);

                                                        String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).push().getKey();

                                                        Map allNewsMap = new HashMap();
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/action", "like");
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/content", "");
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_id", mPost.getPost_id());
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_user_id", mPost.getUser_id());
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/user_id", mCurrentUser.getUid());
                                                        mRootRef.updateChildren(allNewsMap);
                                                    }

                                                    Map postsLiked = new HashMap();
                                                    postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/post_id", mPost.getPost_id());
                                                    postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                                    mRootRef.updateChildren(postsLiked);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        mLikeView.setBackground(getResources().getDrawable(R.drawable.ic_like_active_icon));
                                        mBigLikeView.start();
                                        lastClick[0] = 0;
                                    }
                                }
                            });

                            mVideoView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    firstClick[0] = lastClick[0];
                                    lastClick[0] = System.currentTimeMillis();

                                    if(lastClick[0] - firstClick[0] < 300){

                                        mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                boolean isThere = true;
                                                if(dataSnapshot.exists()){
                                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                        if (ds.child("user_id").getValue().toString().equals(mCurrentUser.getUid())
                                                                && ds.child("action").getValue().toString().equals("like")
                                                                && ds.child("post_id").getValue().toString().equals(mPost.getPost_id())){
                                                            isThere = false;
                                                            break;
                                                        }
                                                    }

                                                    if(isThere){
                                                        Map likeMap = new HashMap();
                                                        likeMap.put("Posts/"+mPost.getUser_id()+"/"+mPost.getPost_id()+"/likes/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                                                        likeMap.put("Posts/"+mPost.getUser_id()+"/"+mPost.getPost_id()+"/likes/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);

                                                        mRootRef.updateChildren(likeMap);

                                                        if(!mPost.getUser_id().equals(mCurrentUser.getUid())){
                                                            String new_key = mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).child(mPost.getPost_id()).push().getKey();

                                                            Map newsMap = new HashMap();
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/user_id", mCurrentUser.getUid());
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_id", mPost.getPost_id());
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_user_id", mPost.getUser_id());
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/action", "like");
                                                            newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/content", "");
                                                            mRootRef.updateChildren(newsMap);

                                                            String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).push().getKey();

                                                            Map allNewsMap = new HashMap();
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/action", "like");
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/content", "");
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_id", mPost.getPost_id());
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_user_id", mPost.getUser_id());
                                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/user_id", mCurrentUser.getUid());
                                                            mRootRef.updateChildren(allNewsMap);
                                                        }

                                                        Map postsLiked = new HashMap();
                                                        postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/post_id", mPost.getPost_id());
                                                        postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                                        mRootRef.updateChildren(postsLiked);
                                                    }
                                                }
                                                else{
                                                    Map likeMap = new HashMap();
                                                    likeMap.put("Posts/"+mPost.getUser_id()+"/"+mPost.getPost_id()+"/likes/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                                                    likeMap.put("Posts/"+mPost.getUser_id()+"/"+mPost.getPost_id()+"/likes/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);

                                                    mRootRef.updateChildren(likeMap);

                                                    if(!mPost.getUser_id().equals(mCurrentUser.getUid())){
                                                        String new_key = mRootRef.child("News").child("Interactions").child(mPost.getUser_id()).child(mPost.getPost_id()).push().getKey();

                                                        Map newsMap = new HashMap();
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/user_id", mCurrentUser.getUid());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_id", mPost.getPost_id());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/post_user_id", mPost.getUser_id());
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/action", "like");
                                                        newsMap.put("News/Interactions/"+mPost.getUser_id()+"/"+new_key+"/content", "");
                                                        mRootRef.updateChildren(newsMap);

                                                        String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).push().getKey();

                                                        Map allNewsMap = new HashMap();
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/action", "like");
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/content", "");
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_id", mPost.getPost_id());
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/post_user_id", mPost.getUser_id());
                                                        allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+all_new_key+"/user_id", mCurrentUser.getUid());
                                                        mRootRef.updateChildren(allNewsMap);
                                                    }

                                                    Map postsLiked = new HashMap();
                                                    postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/post_id", mPost.getPost_id());
                                                    postsLiked.put("Posts_liked/"+mCurrentUser.getUid()+"/"+mPost.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                                    mRootRef.updateChildren(postsLiked);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        mLikeView.setBackground(getResources().getDrawable(R.drawable.ic_like_active_icon));
                                        mBigLikeView.start();
                                        lastClick[0] = 0;
                                    }
                                }
                            });

                            mUserLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(mPost.getUser_id().equals(mCurrentUser.getUid())){
                                        for(int i = 0; i < App.background.getBackgroundList().size(); i++){
                                            if(App.background.getBackgroundList().get(i).equals("Profile")){
                                                App.background.getBackgroundList().remove(i);
                                                break;
                                            }
                                        }
                                        Intent profileIntent = new Intent(LikesInteractionActivity.this, ProfileActivity.class);
                                        startActivity(profileIntent);
                                    }else{
                                        for(int i = 0; i < App.background.getBackgroundList().size(); i++){
                                            if(App.background.getBackgroundList().get(i).equals("User_Profile")){
                                                App.background.getBackgroundList().remove(i);
                                                break;
                                            }
                                        }
                                        Intent profileIntent = new Intent(LikesInteractionActivity.this, UserProfileActivity.class);
                                        profileIntent.putExtra("user_id", mPost.getUser_id());
                                        profileIntent.putExtra("activity_no", 3);
                                        startActivity(profileIntent);
                                    }
                                }
                            });

                            mPostOptions.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getApplicationContext(), "More Options", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    dataSnapshot.child("likes").getRef().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {

                            if(dataSnapshot2.exists()){
                                mLikeCount.setVisibility(View.VISIBLE);

                                for (DataSnapshot ds : dataSnapshot2.getChildren()){
                                    if(ds.child("user_id").getValue().equals(mCurrentUser.getUid())){
                                        mLikeView.setBackground(getResources().getDrawable(R.drawable.ic_like_active_icon));
                                        break;
                                    }else{
                                        mLikeView.setBackground(getResources().getDrawable(R.drawable.ufi_heart));
                                    }
                                }

                                if(dataSnapshot2.getChildrenCount() > 1){
                                    mLikeCount.setText(Html.fromHtml("<b>"+dataSnapshot2.getChildrenCount() + " likes</b>"));
                                    mLikeCount.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent likesIntent = new Intent(LikesInteractionActivity.this, LikeActivity.class);
                                            likesIntent.putExtra("post_user_id", post.getUser_id());
                                            likesIntent.putExtra("post_id", post.getPost_id());
                                            likesIntent.putExtra("activity_no", 3);
                                            startActivity(likesIntent);
                                        }
                                    });
                                }
                                else if(dataSnapshot2.getChildrenCount() == 1){
                                    dataSnapshot2.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot3) {
                                            for (final DataSnapshot ds : dataSnapshot3.getChildren()){
                                                mRootRef.child("Users").child(ds.getRef().getKey()).child("user_name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        mLikeCount.setText(Html.fromHtml("Liked by <b>"+dataSnapshot.getValue().toString()+"</b>"));
                                                        mLikeCount.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if(mCurrentUser.getUid().equals(ds.getRef().getKey())){
                                                                    for(int i = 0; i < App.background.getBackgroundList().size(); i++){
                                                                        if(App.background.getBackgroundList().get(i).equals("Profile")){
                                                                            App.background.getBackgroundList().remove(i);
                                                                            break;
                                                                        }
                                                                    }
                                                                    Intent profileIntent = new Intent(LikesInteractionActivity.this, ProfileActivity.class);
                                                                    startActivity(profileIntent);
                                                                }
                                                                else{
                                                                    Intent userProfileIntent = new Intent(LikesInteractionActivity.this, UserProfileActivity.class);
                                                                    userProfileIntent.putExtra("user_id", ds.getKey());
                                                                    userProfileIntent.putExtra("activity_no", 3);
                                                                    startActivity(userProfileIntent);
                                                                }
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                            else{
                                mLikeCount.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupNavigationView(){
        mBottomNavigationView = findViewById(R.id.intBottomNavigationView);

        BottomNavigationViewHelper.SetupNavigation(this, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupNavigationView();
    }

    private void onBack(){
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikesInteractionActivity.super.onBackPressed();
            }
        });
    }

    private void setupView(){
        mBackView = findViewById(R.id.post_back_button);
        mProfileImage = findViewById(R.id.post_profile_image_f);
        mUserName = findViewById(R.id.post_user_name_f);
        mUserLayout = findViewById(R.id.post_user_layout_f);
        mCaptionText = findViewById(R.id.post_caption_f);
        mPostImage = findViewById(R.id.post_image_view_f);
        mTimeText = findViewById(R.id.post_time_f);
        mLikeView = findViewById(R.id.post_like_view_f);
        mCommentView = findViewById(R.id.post_comment_view_f);
        mShareView = findViewById(R.id.post_share_view_f);
        mLikeCount = findViewById(R.id.post_like_count_f);
        mBigLikeView = findViewById(R.id.like_view_f);
        mPostOptions = findViewById(R.id.post_more_options_view_f);
        mCommentCount = findViewById(R.id.post_comment_count_f);
        mVideoView = findViewById(R.id.post_video_view_f);
        mCameraAnimation = findViewById(R.id.cameraAnimation_f);
        mPostTitle = findViewById(R.id.post_title);
    }
}
