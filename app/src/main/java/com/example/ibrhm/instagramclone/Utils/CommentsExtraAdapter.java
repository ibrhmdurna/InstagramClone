package com.example.ibrhm.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Local.LikeActivity;
import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Comments;
import com.example.ibrhm.instagramclone.Models.Interactions;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.News.CommentsInteractionActivity;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsExtraAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Comments> mCommentsList;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private String post_user_id, post_id;

    private UserPosts mPost;
    private Interactions mInteraction;

    private OnItemClickListener mListener;

    private static int ACTIVITY_NO = 0;

    public CommentsExtraAdapter(Context context, List<Comments> commentsList, String post_user_id, String post_id, Interactions interactions, UserPosts userPosts, int activityNo){
        mContext = context;
        mCommentsList = commentsList;
        this.post_user_id = post_user_id;
        this.post_id = post_id;
        mInteraction = interactions;
        mPost = userPosts;
        ACTIVITY_NO = activityNo;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i == 0){
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comments_interaction_layout, viewGroup, false);
            return new InteractionViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_layout, viewGroup, false);
            return new CommentsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        if(i == 0){
            mCommentsList.add(0, null);
            ((InteractionViewHolder) viewHolder).setData();
        }
        else{
            ((CommentsViewHolder) viewHolder).setData(mCommentsList.get(i));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return 0;
        }
        else return position;
    }

    @Override
    public int getItemCount() {
        return mCommentsList.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout mCommentLayout;
        CircleImageView mProfileImage;
        SocialTextView mCommentContent;
        TextView mTimeView, mLikeView, mReplyView;
        ImageView mLikeAction;
        View mCaptionLayout;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            mCommentLayout = itemView.findViewById(R.id.comment_content_layout);
            mProfileImage = itemView.findViewById(R.id.comment_profile_image);
            mCommentContent = itemView.findViewById(R.id.comment_content);
            mTimeView = itemView.findViewById(R.id.comment_time);
            mLikeView = itemView.findViewById(R.id.comment_like);
            mReplyView = itemView.findViewById(R.id.comment_reply);
            mLikeAction = itemView.findViewById(R.id.comment_like_view);
            mCaptionLayout = itemView.findViewById(R.id.caption_bottom_layout);
        }

        public void setData(final Comments comment) {

            mRootRef.child("Users").child(comment.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users user = dataSnapshot.getValue(Users.class);

                    UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");
                    mCommentContent.setText(Html.fromHtml("<b>"+user.getUser_name() + "</b> " + comment.getComment()));

                    SpannableString ss = new SpannableString(mCommentContent.getText());

                    ss.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            if(comment.getUser_id().equals(mCurrentUser.getUid())){
                                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                mContext.startActivity(profileIntent);
                            }
                            else{
                                Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                profileIntent.putExtra("activity_no", ACTIVITY_NO);
                                profileIntent.putExtra("user_id", comment.getUser_id());
                                mContext.startActivity(profileIntent);
                            }
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(Color.BLACK);
                            ds.setUnderlineText(false);
                        }
                    }, 0, user.getUser_name().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    mCommentContent.setText(ss);
                    mCommentContent.setMovementMethod(LinkMovementMethod.getInstance());

                    mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(comment.getUser_id().equals(mCurrentUser.getUid())){
                                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                mContext.startActivity(profileIntent);
                            }
                            else{
                                Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                profileIntent.putExtra("activity_no", ACTIVITY_NO);
                                profileIntent.putExtra("user_id", comment.getUser_id());
                                mContext.startActivity(profileIntent);
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if (comment.isCaption()) {
                mCommentLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
                mLikeAction.setVisibility(View.GONE);
                mCaptionLayout.setVisibility(View.VISIBLE);
                mLikeView.setVisibility(View.GONE);
                mReplyView.setVisibility(View.GONE);

                String time = GetTimeAgo.getTimeAgoSmall(comment.getTime());
                mTimeView.setText(time);
            } else {

                mCaptionLayout.setVisibility(View.GONE);

                if (comment.isSend()) {
                    String time = GetTimeAgo.getTimeAgoSmall(comment.getTime());
                    mTimeView.setText(time);
                    mCommentLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));

                    mLikeAction.setVisibility(View.VISIBLE);

                    mReplyView.setVisibility(View.VISIBLE);
                    mLikeView.setVisibility(View.GONE);
                    mLikeAction.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_default_icon));

                    if (comment.getComment_like() != null) {
                        mRootRef.child("Posts").child(post_user_id).child(post_id).child("comments").child(comment.getComment_id()).child("comment_like").addListenerForSingleValueEvent(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean like = false;

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (ds.getKey().equals(mCurrentUser.getUid())) {
                                        like = true;
                                        break;
                                    }
                                }

                                if (like) {
                                    mLikeAction.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));
                                } else {
                                    mLikeAction.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_default_icon));
                                }

                                if (dataSnapshot.getChildrenCount() > 1) {
                                    mLikeView.setVisibility(View.VISIBLE);
                                    mLikeView.setText(dataSnapshot.getChildrenCount() + " likes");
                                } else if (dataSnapshot.getChildrenCount() == 1) {
                                    mLikeView.setVisibility(View.VISIBLE);
                                    mLikeView.setText(dataSnapshot.getChildrenCount() + " like");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        mLikeView.setVisibility(View.GONE);
                    }


                    mLikeAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mRootRef.child("Posts").child(post_user_id).child(post_id).child("comments").child(comment.getComment_id()).child("comment_like").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.hasChild(mCurrentUser.getUid())) {
                                            dataSnapshot.child(mCurrentUser.getUid()).getRef().removeValue();
                                        } else {
                                            dataSnapshot.child(mCurrentUser.getUid()).getRef().child("user_id").setValue(mCurrentUser.getUid());
                                        }
                                    } else {
                                        dataSnapshot.child(mCurrentUser.getUid()).getRef().child("user_id").setValue(mCurrentUser.getUid());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                    if(comment.isFocus()){
                        mCommentLayout.setBackgroundColor(mContext.getResources().getColor(R.color.colorCommentTypingColor));
                    }
                    else{
                        mCommentLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
                    }
                } else {
                    mCommentLayout.setBackgroundColor(mContext.getResources().getColor(R.color.colorCommentTypingColor));
                    mLikeView.setVisibility(View.GONE);
                    mReplyView.setVisibility(View.GONE);
                    mLikeAction.setVisibility(View.GONE);
                    mTimeView.setText("Posting...");
                }
            }
        }
    }

    public class InteractionViewHolder extends RecyclerView.ViewHolder{

        CircleImageView mProfileImage;
        TextView mUserName;
        LinearLayout mUserLayout;
        GridImageView mPostImage;
        UniversalVideoView mVideoView;
        SocialTextView mCaptionText;
        TextView mTimeText;
        ImageView mLikeView, mCommentView, mShareView;
        TextView mLikeCount;
        InstaLikeView mBigLikeView;
        CameraAnimation mCameraAnimation;
        ImageView mPostOptions;

        public InteractionViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.comments_int_profile_image);
            mUserName = itemView.findViewById(R.id.comments_int_user_name);
            mUserLayout = itemView.findViewById(R.id.comments_int_user_layout);
            mCaptionText = itemView.findViewById(R.id.comments_int_caption);
            mPostImage = itemView.findViewById(R.id.comments_int_image_view);
            mTimeText = itemView.findViewById(R.id.comments_int_time);
            mLikeView = itemView.findViewById(R.id.comments_int_like_view);
            mCommentView = itemView.findViewById(R.id.comments_int_comment_view);
            mShareView = itemView.findViewById(R.id.comments_int_share_view);
            mLikeCount = itemView.findViewById(R.id.comments_int_like_count);
            mBigLikeView = itemView.findViewById(R.id.comments_int_big_like_view);
            mPostOptions = itemView.findViewById(R.id.comments_int_more_options_view);
            mVideoView = itemView.findViewById(R.id.comments_int_video_view);
            mCameraAnimation = itemView.findViewById(R.id.comments_int_cameraAnimation);
        }

        public void setData(){

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
                        } else {
                            mVideoView.setVisibility(View.GONE);
                            mCameraAnimation.setVisibility(View.GONE);
                            mPostImage.setVisibility(View.VISIBLE);
                            UniversalImageLoader.setImage(post.getPost_url(), mPostImage, null, "");
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
                                                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                                mContext.startActivity(profileIntent);
                                            }
                                            else{
                                                Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                profileIntent.putExtra("activity_no", ACTIVITY_NO);
                                                profileIntent.putExtra("user_id", mPost.getUser_id());
                                                mContext.startActivity(profileIntent);
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
                                }else{
                                    mCaptionText.setVisibility(View.GONE);
                                }

                                mCommentView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(mListener != null){
                                            int position = getAdapterPosition();
                                            if(position != RecyclerView.NO_POSITION){
                                                mListener.onItemClick(position);
                                            }
                                        }
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
                                                        mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ufi_heart));

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
                                                        mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));

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
                                                    mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));

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

                                            mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));
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
                                            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                            mContext.startActivity(profileIntent);
                                        }else{
                                            for(int i = 0; i < App.background.getBackgroundList().size(); i++){
                                                if(App.background.getBackgroundList().get(i).equals("User_Profile")){
                                                    App.background.getBackgroundList().remove(i);
                                                    break;
                                                }
                                            }
                                            Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                            profileIntent.putExtra("user_id", mPost.getUser_id());
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
                                            mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ic_like_active_icon));
                                            break;
                                        }else{
                                            mLikeView.setBackground(mContext.getResources().getDrawable(R.drawable.ufi_heart));
                                        }
                                    }

                                    if(dataSnapshot2.getChildrenCount() > 1){
                                        mLikeCount.setText(Html.fromHtml("<b>"+dataSnapshot2.getChildrenCount() + " likes</b>"));
                                        mLikeCount.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent likesIntent = new Intent(mContext, LikeActivity.class);
                                                likesIntent.putExtra("post_user_id", post.getUser_id());
                                                likesIntent.putExtra("post_id", post.getPost_id());
                                                likesIntent.putExtra("activity_no", ACTIVITY_NO);
                                                mContext.startActivity(likesIntent);
                                            }
                                        });
                                    }
                                    else{
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
                                                                        Intent profileIntent = new Intent(mContext, ProfileActivity.class);
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
    }
}
