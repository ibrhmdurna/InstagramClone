package com.example.ibrhm.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Interactions;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InteractionsAdapter extends RecyclerView.Adapter<InteractionsAdapter.InteractionsViewHolder> {

    private Activity mContext;
    private List<Interactions> mIntList;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    private OnItemClickListener mListener;

    public InteractionsAdapter(Activity context, List<Interactions> intList){
        mContext = context;
        mIntList = intList;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    public interface OnItemClickListener{
        void onItemLikeClick(int position);
        void onItemCommentClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @NonNull
    @Override
    public InteractionsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.interaction_layout, viewGroup, false);
        return new InteractionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InteractionsViewHolder interactionsViewHolder, int i) {
        interactionsViewHolder.setData(mIntList.get(i));
    }

    @Override
    public int getItemCount() {
        return mIntList.size();
    }

    public class InteractionsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView mProfileImage;
        EmojiTextView mContent;
        ImageView mPostImage;
        RelativeLayout mTwoProfileLayout;
        CircleImageView m1stPhoto, m2thPhoto;

        public InteractionsViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.int_one_profile_image_view);
            mContent = itemView.findViewById(R.id.int_content);
            mPostImage = itemView.findViewById(R.id.int_post_image);
            mTwoProfileLayout = itemView.findViewById(R.id.int_two_profile_image_view);
            m1stPhoto = itemView.findViewById(R.id.int_1st_photo);
            m2thPhoto = itemView.findViewById(R.id.int_2th_photo);
        }

        public void setData(final Interactions interaction){

            if(interaction.getAction().equals("comment")){
                mRootRef.child("Posts").child(interaction.getPost_user_id()).child(interaction.getPost_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Posts post = dataSnapshot.getValue(Posts.class);
                            UniversalImageLoader.setImage(post.getPost_url(), mPostImage, null, "");
                            dataSnapshot.getRef().child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot dataSnapshot2) {
                                    if(dataSnapshot2.exists()){
                                        String uid = null;
                                        for(DataSnapshot ds : dataSnapshot2.getChildren()){
                                            if(!ds.child("user_id").getValue().toString().equals(mCurrentUser.getUid()) &&
                                                    ds.child("user_id").getValue().toString().equals(interaction.getUser_id())&&
                                                    ds.child("comment").getValue().toString().equals(interaction.getContent())){
                                                uid = ds.child("user_id").getValue().toString();
                                            }
                                        }
                                        final String finalUid = uid;
                                        mRootRef.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot3) {
                                                Users user = dataSnapshot3.getValue(Users.class);

                                                String time = GetTimeAgo.getTimeAgoSmall(interaction.getTime());
                                                UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");
                                                mContent.setText(Html.fromHtml("<b>"+user.getUser_name()+"</b> commented: "+ interaction.getContent()+" "+time));

                                                SpannableString ss = new SpannableString(mContent.getText());

                                                ss.setSpan(new ClickableSpan() {
                                                    @Override
                                                    public void onClick(@NonNull View widget) {
                                                        Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                        profileIntent.putExtra("activity_no", 3);
                                                        profileIntent.putExtra("user_id", finalUid);
                                                        mContext.startActivity(profileIntent);
                                                    }

                                                    @Override
                                                    public void updateDrawState(@NonNull TextPaint ds) {
                                                        super.updateDrawState(ds);
                                                        ds.setColor(Color.BLACK);
                                                        ds.setUnderlineText(false);
                                                    }
                                                }, 0, user.getUser_name().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                ss.setSpan(new ClickableSpan() {
                                                    @Override
                                                    public void onClick(@NonNull View widget) {
                                                        if(mListener != null){
                                                            int position = getAdapterPosition();
                                                            if(position != RecyclerView.NO_POSITION){
                                                                mListener.onItemCommentClick(position);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void updateDrawState(@NonNull TextPaint ds) {
                                                        super.updateDrawState(ds);
                                                        ds.setColor(Color.BLACK);
                                                        ds.setUnderlineText(false);
                                                    }
                                                }, user.getUser_name().length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                ForegroundColorSpan fcsGray = new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorDarkGray));
                                                ss.setSpan(fcsGray, mContent.getText().length() - 3, mContent.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                mContent.setText(ss);
                                                mContent.setMovementMethod(LinkMovementMethod.getInstance());
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        mProfileImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                profileIntent.putExtra("activity_no", 3);
                                                profileIntent.putExtra("user_id", finalUid);
                                                mContext.startActivity(profileIntent);
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mListener != null){
                            int position = getAdapterPosition();
                            if(position != RecyclerView.NO_POSITION){
                                mListener.onItemCommentClick(position);
                            }
                        }
                    }
                });
            }
            else if(interaction.getAction().equals("like")) {
                final List<String> mLikeList = new ArrayList<>();
                mRootRef.child("Posts").child(interaction.getPost_user_id()).child(interaction.getPost_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final Posts post = dataSnapshot.getValue(Posts.class);

                            if(post.getType().equals("Video")){
                                UniversalImageLoader.setImage(post.getThumb_image(), mPostImage, null, "");
                            }
                            else{
                                UniversalImageLoader.setImage(post.getPost_url(), mPostImage, null, "");
                            }
                            dataSnapshot.getRef().child("likes").orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot dataSnapshot2) {
                                    if(dataSnapshot2.exists()){
                                        mLikeList.clear();
                                        for(DataSnapshot ds : dataSnapshot2.getChildren()){
                                            if(!ds.getKey().equals(mCurrentUser.getUid())){
                                                mLikeList.add(ds.getKey());
                                            }
                                        }
                                        Collections.reverse(mLikeList);
                                        if(mLikeList.size() == 1){
                                            mRootRef.child("Users").child(mLikeList.get(0)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @SuppressLint("SetTextI18n")
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot3) {
                                                    Users user = dataSnapshot3.getValue(Users.class);

                                                    String time = GetTimeAgo.getTimeAgoSmall(interaction.getTime());
                                                    UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");
                                                    mContent.setText(Html.fromHtml("<b>"+user.getUser_name()+"</b> liked your post. "+time));

                                                    SpannableString ss = new SpannableString(mContent.getText());

                                                    ss.setSpan(new ClickableSpan() {
                                                        @Override
                                                        public void onClick(@NonNull View widget) {
                                                            Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                            profileIntent.putExtra("activity_no", 3);
                                                            profileIntent.putExtra("user_id", mLikeList.get(0));
                                                            mContext.startActivity(profileIntent);
                                                        }

                                                        @Override
                                                        public void updateDrawState(@NonNull TextPaint ds) {
                                                            super.updateDrawState(ds);
                                                            ds.setColor(Color.BLACK);
                                                            ds.setUnderlineText(false);
                                                        }
                                                    }, 0, user.getUser_name().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                    ss.setSpan(new ClickableSpan() {
                                                        @Override
                                                        public void onClick(@NonNull View widget) {
                                                            if(mListener != null){
                                                                int position = getAdapterPosition();
                                                                if(position != RecyclerView.NO_POSITION){
                                                                    mListener.onItemLikeClick(position);
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void updateDrawState(@NonNull TextPaint ds) {
                                                            super.updateDrawState(ds);
                                                            ds.setColor(Color.BLACK);
                                                            ds.setUnderlineText(false);
                                                        }
                                                    }, user.getUser_name().length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                    ForegroundColorSpan fcsGray = new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorDarkGray));
                                                    ss.setSpan(fcsGray, mContent.getText().length() - 3, mContent.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                    mContent.setText(ss);
                                                    mContent.setMovementMethod(LinkMovementMethod.getInstance());
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                            mProfileImage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                    profileIntent.putExtra("activity_no", 3);
                                                    profileIntent.putExtra("user_id", mLikeList.get(0));
                                                    mContext.startActivity(profileIntent);
                                                }
                                            });
                                        }
                                        else if (mLikeList.size() > 1){
                                            mProfileImage.setVisibility(View.GONE);
                                            mTwoProfileLayout.setVisibility(View.VISIBLE);
                                            mRootRef.child("Users").child(mLikeList.get(0)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @SuppressLint("SetTextI18n")
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot3) {
                                                    final Users user = dataSnapshot3.getValue(Users.class);

                                                    final String time = GetTimeAgo.getTimeAgoSmall(interaction.getTime());
                                                    UniversalImageLoader.setImage(user.getDetails().getProfile_image(), m1stPhoto, null, "");

                                                    mRootRef.child("Users").child(mLikeList.get(1)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot4) {
                                                            Users user2 = dataSnapshot4.getValue(Users.class);

                                                            UniversalImageLoader.setImage(user2.getDetails().getProfile_image(), m2thPhoto, null, "");

                                                            String start_content = user.getUser_name()+", "+user2.getUser_name();

                                                            if(mLikeList.size() == 2){
                                                                mContent.setText(Html.fromHtml("<b>"+start_content+"</b> liked your post. "+time));
                                                            }
                                                            else if(mLikeList.size() > 2){
                                                                mContent.setText(Html.fromHtml("<b>"+start_content+"</b> and <b>"+(mLikeList.size() - 2)+" others </b> liked your post. "+time));
                                                            }

                                                            SpannableString ss = new SpannableString(mContent.getText());

                                                            ss.setSpan(new ClickableSpan() {
                                                                @Override
                                                                public void onClick(@NonNull View widget) {
                                                                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                                    profileIntent.putExtra("activity_no", 3);
                                                                    profileIntent.putExtra("user_id", mLikeList.get(0));
                                                                    mContext.startActivity(profileIntent);
                                                                }

                                                                @Override
                                                                public void updateDrawState(@NonNull TextPaint ds) {
                                                                    super.updateDrawState(ds);
                                                                    ds.setColor(Color.BLACK);
                                                                    ds.setUnderlineText(false);
                                                                }
                                                            }, 0, user.getUser_name().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                            ss.setSpan(new ClickableSpan() {
                                                                @Override
                                                                public void onClick(@NonNull View widget) {
                                                                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                                    profileIntent.putExtra("activity_no", 3);
                                                                    profileIntent.putExtra("user_id", mLikeList.get(1));
                                                                    mContext.startActivity(profileIntent);
                                                                }

                                                                @Override
                                                                public void updateDrawState(@NonNull TextPaint ds) {
                                                                    super.updateDrawState(ds);
                                                                    ds.setColor(Color.BLACK);
                                                                    ds.setUnderlineText(false);
                                                                }
                                                            }, user.getUser_name().length() + 2, user.getUser_name().length() + user2.getUser_name().length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                            if(mLikeList.size() == 2){
                                                                ss.setSpan(new ClickableSpan() {
                                                                    @Override
                                                                    public void onClick(@NonNull View widget) {
                                                                        if(mListener != null){
                                                                            int position = getAdapterPosition();
                                                                            if(position != RecyclerView.NO_POSITION){
                                                                                mListener.onItemLikeClick(position);
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void updateDrawState(@NonNull TextPaint ds) {
                                                                        super.updateDrawState(ds);
                                                                        ds.setColor(Color.BLACK);
                                                                        ds.setUnderlineText(false);
                                                                    }
                                                                }, start_content.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                            }
                                                            else if(mLikeList.size() > 2){
                                                                ss.setSpan(new ClickableSpan() {
                                                                    @Override
                                                                    public void onClick(@NonNull View widget) {
                                                                        if(mListener != null){
                                                                            int position = getAdapterPosition();
                                                                            if(position != RecyclerView.NO_POSITION){
                                                                                mListener.onItemLikeClick(position);
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void updateDrawState(@NonNull TextPaint ds) {
                                                                        super.updateDrawState(ds);
                                                                        ds.setColor(Color.BLACK);
                                                                        ds.setUnderlineText(false);
                                                                    }
                                                                }, start_content.length(), start_content.length() + 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                                String count = (mLikeList.size() - 2)+"";

                                                                ss.setSpan(new ClickableSpan() {
                                                                    @Override
                                                                    public void onClick(@NonNull View widget) {
                                                                        if(mListener != null){
                                                                            int position = getAdapterPosition();
                                                                            if(position != RecyclerView.NO_POSITION){
                                                                                mListener.onItemLikeClick(position);
                                                                            }
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void updateDrawState(@NonNull TextPaint ds) {
                                                                        super.updateDrawState(ds);
                                                                        ds.setColor(Color.BLACK);
                                                                        ds.setUnderlineText(false);
                                                                    }
                                                                }, start_content.length() + 5 + count.length() + 8, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                            }

                                                            ForegroundColorSpan fcsGray = new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorDarkGray));
                                                            ss.setSpan(fcsGray, mContent.getText().length() - 3, mContent.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                            mContent.setText(ss);
                                                            mContent.setMovementMethod(LinkMovementMethod.getInstance());
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
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

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mListener != null){
                            int position = getAdapterPosition();
                            if(position != RecyclerView.NO_POSITION){
                                mListener.onItemLikeClick(position);
                            }
                        }
                    }
                });
            }
        }
    }
}
