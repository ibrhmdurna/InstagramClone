package com.example.ibrhm.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Local.CommentActivity;
import com.example.ibrhm.instagramclone.Local.LikeActivity;
import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Comments;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.CameraAnimation;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.Video;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.VideoView;
import com.github.kshitij_jain.instalike.InstaLikeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.widget.SocialTextView;
import com.hoanganhtuan95ptit.autoplayvideorecyclerview.VideoHolder;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder> {

    private Activity mContext;
    private List<UserPosts> mPostsList;
    private List<Comments> mCommentsList;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private static int ACTIVITY_NO;

    public PostsAdapter(List<UserPosts> postsList, Activity context, int activity_no){
        mContext = context;
        mPostsList = postsList;
        ACTIVITY_NO = activity_no;

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCommentsList = new ArrayList<>();
    }

    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_layout, viewGroup, false);
        return new PostsViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, int i) {

        boolean video = false;

        if(mPostsList.get(i).getType().equals("Video")){
            video = true;
            postsViewHolder.getVideoLayout();
        }

        postsViewHolder.setData(mPostsList.get(i), video);
    }

    @Override
    public int getItemCount() {
        return mPostsList.size();
    }

    public class PostsViewHolder extends VideoHolder {

        CircleImageView mProfileImage;
        TextView mUserName;
        SocialTextView mCaptionText;
        GridImageView mPostImage;
        TextView mTimeText;
        ImageView mLikeView, mCommentView, mShareView;
        TextView mLikeCount, mCommentCount;
        InstaLikeView mBigLikeView;
        LinearLayout mPostUserLayout;
        ImageView mPostOptions;
        VideoView mVideoView;
        CameraAnimation mCameraAnimation;

        boolean isVideo = false;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.post_profile_image);
            mUserName = itemView.findViewById(R.id.post_user_name);
            mCaptionText = itemView.findViewById(R.id.post_caption);
            mPostImage = itemView.findViewById(R.id.post_image_view);
            mTimeText = itemView.findViewById(R.id.post_time);
            mLikeView = itemView.findViewById(R.id.post_like_view);
            mCommentView = itemView.findViewById(R.id.post_comment_view);
            mShareView = itemView.findViewById(R.id.post_share_view);
            mLikeCount = itemView.findViewById(R.id.post_like_count);
            mBigLikeView = itemView.findViewById(R.id.like_view);
            mPostUserLayout = itemView.findViewById(R.id.post_user_layout);
            mPostOptions = itemView.findViewById(R.id.post_more_options_view);
            mCommentCount = itemView.findViewById(R.id.post_comment_count);
            mVideoView = itemView.findViewById(R.id.post_video_view);
            mCameraAnimation = itemView.findViewById(R.id.cameraAnimation);
        }

        public void setData(final UserPosts posts, boolean video){

            isVideo = video;

            if(isVideo){
                mVideoView.setVisibility(View.VISIBLE);
                mCameraAnimation.setVisibility(View.VISIBLE);
                mPostImage.setVisibility(View.GONE);
                mVideoView.setVideo(new Video(posts.getPost_url(), 0));
            }else{
                mVideoView.setVisibility(View.GONE);
                mCameraAnimation.setVisibility(View.GONE);
                mPostImage.setVisibility(View.VISIBLE);
                UniversalImageLoader.setImage(posts.getPost_url(), mPostImage, null, "");
            }

            UniversalImageLoader.setImage(posts.getProfile_image(), mProfileImage, null, "");

            mUserName.setText(posts.getUser_name());

            final String timeAgo = GetTimeAgo.getTimeAgo(posts.getTime());
            final String timeAgoSmall = GetTimeAgo.getTimeAgoSmall(posts.getTime());

            mTimeText.setText(timeAgo);

            mRootRef.child("Posts").child(posts.getUser_id()).child(posts.getPost_id()).child("likes").addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){

                        mLikeCount.setVisibility(View.VISIBLE);
                        mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ufi_heart));

                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            if(ds.child("user_id").getValue().equals(mAuth.getCurrentUser().getUid())){
                                mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));
                                break;
                            }else{
                                mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ufi_heart));
                            }
                        }

                        if(dataSnapshot.getChildrenCount() > 1){
                            mLikeCount.setText(Html.fromHtml("<b>"+dataSnapshot.getChildrenCount() + " likes</b>"));
                            mLikeCount.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent likesIntent = new Intent(mContext, LikeActivity.class);
                                    likesIntent.putExtra("post_user_id", posts.getUser_id());
                                    likesIntent.putExtra("post_id", posts.getPost_id());
                                    likesIntent.putExtra("activity_no", ACTIVITY_NO);
                                    mContext.startActivity(likesIntent);
                                }
                            });
                        }
                        else{
                            dataSnapshot.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                    for (final DataSnapshot ds : dataSnapshot2.getChildren()){
                                        mRootRef.child("Users").child(ds.getRef().getKey()).child("user_name").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                mLikeCount.setText(Html.fromHtml("Liked by <b>"+dataSnapshot.getValue().toString()+"</b>"));
                                                mLikeCount.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if(mAuth.getCurrentUser().getUid().equals(ds.getRef().getKey())){
                                                            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                                            mContext.startActivity(profileIntent);
                                                        }
                                                        else{
                                                            Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                            profileIntent.putExtra("user_id", ds.getRef().getKey());
                                                            profileIntent.putExtra("activity_no", ACTIVITY_NO);
                                                            mContext.startActivity(profileIntent);
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

                            mLikeCount.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                        }
                    }else{
                        mLikeCount.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if(!posts.getCaption().equals("")){

                mCaptionText.setVisibility(View.VISIBLE);

                mCaptionText.setText(Html.fromHtml("<b>"+posts.getUser_name()+"</b> "+posts.getCaption()));

                SpannableString ss = new SpannableString(mCaptionText.getText());

                ss.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if(posts.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                            mContext.startActivity(profileIntent);
                        }
                        else{
                            Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                            profileIntent.putExtra("activity_no", ACTIVITY_NO);
                            profileIntent.putExtra("user_id", posts.getUser_id());
                            mContext.startActivity(profileIntent);
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(Color.BLACK);
                        ds.setUnderlineText(false);
                    }
                }, 0, posts.getUser_name().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                mCaptionText.setText(ss);
                mCaptionText.setMovementMethod(LinkMovementMethod.getInstance());

                mCaptionText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(mContext, CommentActivity.class);
                        commentsIntent.putExtra("post_user_id", posts.getUser_id());
                        commentsIntent.putExtra("post_id", posts.getPost_id());
                        commentsIntent.putExtra("activity_no", ACTIVITY_NO);
                        CommentActivity.focus = false;
                        mContext.startActivity(commentsIntent);
                    }
                });

                mCaptionText.setOnMentionClickListener(new Function2<TextView, String, Unit>() {
                    @Override
                    public Unit invoke(TextView textView, String s) {
                        mRootRef.child("Users").orderByChild("user_name").equalTo(s).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        if(ds.getKey().equals(mAuth.getCurrentUser().getUid())){
                                            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                            mContext.startActivity(profileIntent);
                                        }
                                        else{
                                            Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                            profileIntent.putExtra("user_id", ds.getKey());
                                            profileIntent.putExtra("activity_no", ACTIVITY_NO);
                                            mContext.startActivity(profileIntent);
                                        }
                                    }
                                }
                                else{
                                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                    profileIntent.putExtra("user_id", "not_found");
                                    profileIntent.putExtra("activity_no", ACTIVITY_NO);
                                    mContext.startActivity(profileIntent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return null;
                    }
                });
            }else{
                mCaptionText.setVisibility(View.GONE);
            }

            mCommentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent commentIntent = new Intent(mContext, CommentActivity.class);
                    commentIntent.putExtra("post_user_id", posts.getUser_id());
                    commentIntent.putExtra("post_id", posts.getPost_id());
                    commentIntent.putExtra("activity_no", ACTIVITY_NO);
                    CommentActivity.focus = true;
                    mContext.startActivity(commentIntent);
                }
            });

            mRootRef.child("Posts").child(posts.getUser_id()).child(posts.getPost_id()).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                Intent commentsIntent = new Intent(mContext, CommentActivity.class);
                                commentsIntent.putExtra("post_user_id", posts.getUser_id());
                                commentsIntent.putExtra("post_id", posts.getPost_id());
                                commentsIntent.putExtra("activity_no", ACTIVITY_NO);
                                CommentActivity.focus = false;
                                mContext.startActivity(commentsIntent);
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
                    mRootRef.child("Posts").child(posts.getUser_id()).child(posts.getPost_id()).child("likes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                if(dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                    dataSnapshot.child(mAuth.getCurrentUser().getUid()).getRef().removeValue();
                                    mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ufi_heart));

                                    mRootRef.child("News").child("Interactions").child(posts.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                if(ds.child("user_id").getValue().toString().equals(mAuth.getCurrentUser().getUid())
                                                        && ds.child("action").getValue().toString().equals("like")
                                                        && ds.child("post_id").getValue().toString().equals(posts.getPost_id())){
                                                    Map newsMap = new HashMap();
                                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+ds.getKey()+"/user_id", null);
                                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+ds.getKey()+"/post_id", null);
                                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+ds.getKey()+"/post_user_id", null);
                                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+ds.getKey()+"/time", null);
                                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+ds.getKey()+"/action", null);
                                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+ds.getKey()+"/content", null);
                                                    mRootRef.updateChildren(newsMap);
                                                    break;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    mRootRef.child("News").child("All_users").child("Interactions").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                                    if(ds.child("action").getValue().toString().equals("like") &&
                                                            ds.child("user_id").getValue().toString().equals(mAuth.getCurrentUser().getUid()) &&
                                                            ds.child("post_id").getValue().toString().equals(posts.getPost_id())){
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
                                    postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/post_id", null);
                                    postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/time", null);
                                    mRootRef.updateChildren(postsLiked);
                                }
                                else{
                                    Map likeMap = new HashMap();
                                    likeMap.put("user_id", mAuth.getCurrentUser().getUid());
                                    likeMap.put("time", ServerValue.TIMESTAMP);

                                    dataSnapshot.child(mAuth.getCurrentUser().getUid()).getRef().updateChildren(likeMap);
                                    mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));

                                    if(!posts.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                                        String new_key = mRootRef.child("News").child("Interactions").child(posts.getUser_id()).child(posts.getPost_id()).push().getKey();

                                        Map newsMap = new HashMap();
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_id", posts.getPost_id());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_user_id", posts.getUser_id());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/action", "like");
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/content", "");
                                        mRootRef.updateChildren(newsMap);

                                        String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mAuth.getCurrentUser().getUid()).push().getKey();

                                        Map allNewsMap = new HashMap();
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/action", "like");
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/content", "");
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_id", posts.getPost_id());
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_user_id", posts.getUser_id());
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                        mRootRef.updateChildren(allNewsMap);
                                    }

                                    Map postsLiked = new HashMap();
                                    postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/post_id", posts.getPost_id());
                                    postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                    mRootRef.updateChildren(postsLiked);
                                }
                            }else{
                                Map likeMap = new HashMap();
                                likeMap.put("user_id", mAuth.getCurrentUser().getUid());
                                likeMap.put("time", ServerValue.TIMESTAMP);

                                dataSnapshot.child(mAuth.getCurrentUser().getUid()).getRef().updateChildren(likeMap);
                                mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));

                                if(!posts.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                                    String new_key = mRootRef.child("News").child("Interactions").child(posts.getUser_id()).child(posts.getPost_id()).push().getKey();

                                    Map newsMap = new HashMap();
                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_id", posts.getPost_id());
                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_user_id", posts.getUser_id());
                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/action", "like");
                                    newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/content", "");
                                    mRootRef.updateChildren(newsMap);

                                    String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mAuth.getCurrentUser().getUid()).push().getKey();

                                    Map allNewsMap = new HashMap();
                                    allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/action", "like");
                                    allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                    allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/content", "");
                                    allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_id", posts.getPost_id());
                                    allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_user_id", posts.getUser_id());
                                    allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                    mRootRef.updateChildren(allNewsMap);
                                }

                                Map postsLiked = new HashMap();
                                postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/post_id", posts.getPost_id());
                                postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/time", ServerValue.TIMESTAMP);
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

                        mRootRef.child("News").child("Interactions").child(posts.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean isThere = true;
                                if(dataSnapshot.exists()){
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        if (ds.child("user_id").getValue().toString().equals(mAuth.getCurrentUser().getUid())
                                                && ds.child("action").getValue().toString().equals("like")
                                                && ds.child("post_id").getValue().toString().equals(posts.getPost_id())){
                                            isThere = false;
                                            break;
                                        }
                                    }

                                    if(isThere){
                                        Map likeMap = new HashMap();
                                        likeMap.put("Posts/"+posts.getUser_id()+"/"+posts.getPost_id()+"/likes/"+mAuth.getCurrentUser().getUid()+"/user_id", mAuth.getCurrentUser().getUid());
                                        likeMap.put("Posts/"+posts.getUser_id()+"/"+posts.getPost_id()+"/likes/"+mAuth.getCurrentUser().getUid()+"/time", ServerValue.TIMESTAMP);

                                        mRootRef.updateChildren(likeMap);

                                        if(!posts.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                                            String new_key = mRootRef.child("News").child("Interactions").child(posts.getUser_id()).child(posts.getPost_id()).push().getKey();

                                            Map newsMap = new HashMap();
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_id", posts.getPost_id());
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_user_id", posts.getUser_id());
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/action", "like");
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/content", "");
                                            mRootRef.updateChildren(newsMap);

                                            String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mAuth.getCurrentUser().getUid()).push().getKey();

                                            Map allNewsMap = new HashMap();
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/action", "like");
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/content", "");
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_id", posts.getPost_id());
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_user_id", posts.getUser_id());
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                            mRootRef.updateChildren(allNewsMap);
                                        }

                                        Map postsLiked = new HashMap();
                                        postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/post_id", posts.getPost_id());
                                        postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                        mRootRef.updateChildren(postsLiked);
                                    }
                                }
                                else{
                                    Map likeMap = new HashMap();
                                    likeMap.put("Posts/"+posts.getUser_id()+"/"+posts.getPost_id()+"/likes/"+mAuth.getCurrentUser().getUid()+"/user_id", mAuth.getCurrentUser().getUid());
                                    likeMap.put("Posts/"+posts.getUser_id()+"/"+posts.getPost_id()+"/likes/"+mAuth.getCurrentUser().getUid()+"/time", ServerValue.TIMESTAMP);

                                    mRootRef.updateChildren(likeMap);

                                    if(!posts.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                                        String new_key = mRootRef.child("News").child("Interactions").child(posts.getUser_id()).child(posts.getPost_id()).push().getKey();

                                        Map newsMap = new HashMap();
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_id", posts.getPost_id());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_user_id", posts.getUser_id());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/action", "like");
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/content", "");
                                        mRootRef.updateChildren(newsMap);

                                        String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mAuth.getCurrentUser().getUid()).push().getKey();

                                        Map allNewsMap = new HashMap();
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/action", "like");
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/content", "");
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_id", posts.getPost_id());
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_user_id", posts.getUser_id());
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                        mRootRef.updateChildren(allNewsMap);
                                    }

                                    Map postsLiked = new HashMap();
                                    postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/post_id", posts.getPost_id());
                                    postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                    mRootRef.updateChildren(postsLiked);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));
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

                        mRootRef.child("News").child("Interactions").child(posts.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean isThere = true;
                                if(dataSnapshot.exists()){
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        if (ds.child("user_id").getValue().toString().equals(mAuth.getCurrentUser().getUid())
                                                && ds.child("action").getValue().toString().equals("like")
                                                && ds.child("post_id").getValue().toString().equals(posts.getPost_id())){
                                            isThere = false;
                                            break;
                                        }
                                    }

                                    if(isThere){
                                        Map likeMap = new HashMap();
                                        likeMap.put("Posts/"+posts.getUser_id()+"/"+posts.getPost_id()+"/likes/"+mAuth.getCurrentUser().getUid()+"/user_id", mAuth.getCurrentUser().getUid());
                                        likeMap.put("Posts/"+posts.getUser_id()+"/"+posts.getPost_id()+"/likes/"+mAuth.getCurrentUser().getUid()+"/time", ServerValue.TIMESTAMP);

                                        mRootRef.updateChildren(likeMap);

                                        if(!posts.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                                            String new_key = mRootRef.child("News").child("Interactions").child(posts.getUser_id()).child(posts.getPost_id()).push().getKey();

                                            Map newsMap = new HashMap();
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_id", posts.getPost_id());
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_user_id", posts.getUser_id());
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/action", "like");
                                            newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/content", "");
                                            mRootRef.updateChildren(newsMap);

                                            String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mAuth.getCurrentUser().getUid()).push().getKey();

                                            Map allNewsMap = new HashMap();
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/action", "like");
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/content", "");
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_id", posts.getPost_id());
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_user_id", posts.getUser_id());
                                            allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                            mRootRef.updateChildren(allNewsMap);
                                        }


                                        Map postsLiked = new HashMap();
                                        postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/post_id", posts.getPost_id());
                                        postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                        mRootRef.updateChildren(postsLiked);
                                    }
                                }
                                else{
                                    Map likeMap = new HashMap();
                                    likeMap.put("Posts/"+posts.getUser_id()+"/"+posts.getPost_id()+"/likes/"+mAuth.getCurrentUser().getUid()+"/user_id", mAuth.getCurrentUser().getUid());
                                    likeMap.put("Posts/"+posts.getUser_id()+"/"+posts.getPost_id()+"/likes/"+mAuth.getCurrentUser().getUid()+"/time", ServerValue.TIMESTAMP);

                                    mRootRef.updateChildren(likeMap);

                                    if(!posts.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                                        String new_key = mRootRef.child("News").child("Interactions").child(posts.getUser_id()).child(posts.getPost_id()).push().getKey();

                                        Map newsMap = new HashMap();
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_id", posts.getPost_id());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/post_user_id", posts.getUser_id());
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/time", ServerValue.TIMESTAMP);
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/action", "like");
                                        newsMap.put("News/Interactions/"+posts.getUser_id()+"/"+new_key+"/content", "");
                                        mRootRef.updateChildren(newsMap);

                                        String all_new_key = mRootRef.child("News").child("All_users").child("Interactions").child(mAuth.getCurrentUser().getUid()).push().getKey();

                                        Map allNewsMap = new HashMap();
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/action", "like");
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/time", ServerValue.TIMESTAMP);
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/content", "");
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_id", posts.getPost_id());
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/post_user_id", posts.getUser_id());
                                        allNewsMap.put("News/All_users/Interactions/"+mAuth.getCurrentUser().getUid()+"/"+all_new_key+"/user_id", mAuth.getCurrentUser().getUid());
                                        mRootRef.updateChildren(allNewsMap);
                                    }

                                    Map postsLiked = new HashMap();
                                    postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/post_id", posts.getPost_id());
                                    postsLiked.put("Posts_liked/"+mAuth.getCurrentUser().getUid()+"/"+posts.getPost_id()+"/time", ServerValue.TIMESTAMP);
                                    mRootRef.updateChildren(postsLiked);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));
                        mBigLikeView.start();
                        lastClick[0] = 0;
                    }
                }
            });

            mPostUserLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(posts.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                        Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                        mContext.startActivity(profileIntent);
                    }else{
                        Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                        profileIntent.putExtra("user_id", posts.getUser_id());
                        profileIntent.putExtra("activity_no", ACTIVITY_NO);
                        mContext.startActivity(profileIntent);
                    }
                }
            });

            mPostOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "More Options", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public View getVideoLayout() {
            return mVideoView;
        }

        @Override
        public void playVideo() {
            if(isVideo){
                mCameraAnimation.setVisibility(View.VISIBLE);
                mCameraAnimation.start();
                mVideoView.play(new VideoView.OnPreparedListener() {
                    @Override
                    public void onPrepared() {
                        mPostImage.setVisibility(View.GONE);
                        mCameraAnimation.setVisibility(View.GONE);
                        mCameraAnimation.stop();
                    }
                });
            }
        }

        @Override
        public void stopVideo() {
            if(isVideo){
                mCameraAnimation.stop();
                mVideoView.stop();
            }
        }
    }
}
