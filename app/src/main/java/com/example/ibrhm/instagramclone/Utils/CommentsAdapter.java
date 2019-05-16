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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Comments;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.widget.SocialTextView;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Context mContext;
    private List<Comments> mCommentsList;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private String post_user_id, post_id;

    private static int ACTIVITY_NO = 0;

    public CommentsAdapter(Context context, List<Comments> commentsList, String post_user_id, String post_id, int activityNo){
        mContext = context;
        mCommentsList = commentsList;
        this.post_user_id = post_user_id;
        this.post_id = post_id;
        ACTIVITY_NO = activityNo;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_layout, viewGroup, false);
        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int i) {
        commentsViewHolder.setData(mCommentsList.get(i));
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
                    final Users user = dataSnapshot.getValue(Users.class);

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

                    mCommentContent.setOnMentionClickListener(new Function2<TextView, String, Unit>() {
                        @Override
                        public Unit invoke(TextView textView, final String s) {
                            mRootRef.child("Users").orderByChild("user_name").equalTo(s).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                                            if(ds.getKey().equals(mCurrentUser.getUid())){
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
}
