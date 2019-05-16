package com.example.ibrhm.instagramclone.Utils;

import android.app.Activity;
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
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Interactions;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InteractionsExtraAdapter extends RecyclerView.Adapter<InteractionsExtraAdapter.InteractionsViewHolder> {

    private Activity mContext;
    private List<Interactions> mIntList;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    private OnItemClickListener mListener;

    public InteractionsExtraAdapter(Activity context, List<Interactions> intList){
        mContext = context;
        mIntList = intList;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    public interface OnItemClickListener{
        void onItemLikeClick(int position);
        void onItemFollowClick(int position);
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
    public void onBindViewHolder(@NonNull InteractionsViewHolder interactionsViewHolder, final int i) {
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

        public InteractionsViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.int_one_profile_image_view);
            mContent = itemView.findViewById(R.id.int_content);
            mPostImage = itemView.findViewById(R.id.int_post_image);
        }

        public void setData(final Interactions interaction){

            if(interaction.getAction().equals("like")){
                // Etkileşim beğenme ise
                mRootRef.child("Posts").child(interaction.getPost_user_id()).child(interaction.getPost_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Posts post = dataSnapshot.getValue(Posts.class);

                        if(post.getType().equals("Video")){
                            UniversalImageLoader.setImage(post.getThumb_image(), mPostImage, null, "");
                        }
                        else{
                            UniversalImageLoader.setImage(post.getPost_url(), mPostImage, null, "");
                        }

                        mRootRef.child("Users").child(interaction.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    final Users user = dataSnapshot.getValue(Users.class);

                                    UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");

                                    mRootRef.child("Users").child(interaction.getPost_user_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                final Users postUser = dataSnapshot.getValue(Users.class);

                                                mContent.setText(Html.fromHtml("<b>"+user.getUser_name()+"</b> liked <b>"+postUser.getUser_name()+"</b>'s post. "+GetTimeAgo.getTimeAgoSmall(interaction.getTime())));

                                                SpannableString ss = new SpannableString(mContent.getText());

                                                ss.setSpan(new ClickableSpan() {
                                                    @Override
                                                    public void onClick(@NonNull View widget) {
                                                        if(user.getUser_id().equals(mCurrentUser.getUid())){
                                                            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                                            mContext.startActivity(profileIntent);
                                                        }
                                                        else{
                                                            Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                            profileIntent.putExtra("activity_no", 3);
                                                            profileIntent.putExtra("user_id", user.getUser_id());
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

                                                ss.setSpan(new ClickableSpan() {
                                                    @Override
                                                    public void onClick(@NonNull View widget) {
                                                        if(postUser.getUser_id().equals(mCurrentUser.getUid())){
                                                            Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                                            mContext.startActivity(profileIntent);
                                                        }
                                                        else{
                                                            Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                            profileIntent.putExtra("activity_no", 3);
                                                            profileIntent.putExtra("user_id", postUser.getUser_id());
                                                            mContext.startActivity(profileIntent);
                                                        }
                                                    }

                                                    @Override
                                                    public void updateDrawState(@NonNull TextPaint ds) {
                                                        super.updateDrawState(ds);
                                                        ds.setColor(Color.BLACK);
                                                        ds.setUnderlineText(false);
                                                    }
                                                }, user.getUser_name().length() + 7, user.getUser_name().length() + 7 + postUser.getUser_name().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
                                                }, user.getUser_name().length(), user.getUser_name().length() + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
                                                }, user.getUser_name().length() + 7 + postUser.getUser_name().length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                ForegroundColorSpan fcsGray = new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorDarkGray));
                                                ss.setSpan(fcsGray, mContent.getText().length() - 3, mContent.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                mContent.setText(ss);
                                                mContent.setMovementMethod(LinkMovementMethod.getInstance());
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

                        mProfileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                profileIntent.putExtra("activity_no", 3);
                                profileIntent.putExtra("user_id", interaction.getUser_id());
                                mContext.startActivity(profileIntent);
                            }
                        });
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
            else if(interaction.getAction().equals("following")){
                // Etkileşim takip etme ise

                mPostImage.setVisibility(View.GONE);
                mRootRef.child("Users").child(interaction.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final Users user = dataSnapshot.getValue(Users.class);

                            UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null,"");
                            mRootRef.child("Users").child(interaction.getPost_user_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        final Users user2 = dataSnapshot.getValue(Users.class);

                                        mContent.setText(Html.fromHtml("<b>"+user.getUser_name()+"</b> started following <b>"+user2.getUser_name()+"</b> "+GetTimeAgo.getTimeAgoSmall(interaction.getTime())));

                                        SpannableString ss = new SpannableString(mContent.getText());

                                        ss.setSpan(new ClickableSpan() {
                                            @Override
                                            public void onClick(@NonNull View widget) {
                                                if(user.getUser_id().equals(mCurrentUser.getUid())){
                                                    Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                                    mContext.startActivity(profileIntent);
                                                }
                                                else{
                                                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                    profileIntent.putExtra("activity_no", 3);
                                                    profileIntent.putExtra("user_id", user.getUser_id());
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

                                        ss.setSpan(new ClickableSpan() {
                                            @Override
                                            public void onClick(@NonNull View widget) {
                                                if(user2.getUser_id().equals(mCurrentUser.getUid())){
                                                    Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                                    mContext.startActivity(profileIntent);
                                                }
                                                else{
                                                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                                    profileIntent.putExtra("activity_no", 3);
                                                    profileIntent.putExtra("user_id", user2.getUser_id());
                                                    mContext.startActivity(profileIntent);
                                                }
                                            }

                                            @Override
                                            public void updateDrawState(@NonNull TextPaint ds) {
                                                super.updateDrawState(ds);
                                                ds.setColor(Color.BLACK);
                                                ds.setUnderlineText(false);
                                            }
                                        }, user.getUser_name().length() + 19, user.getUser_name().length() + 19 + user2.getUser_name().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                        ss.setSpan(new ClickableSpan() {
                                            @Override
                                            public void onClick(@NonNull View widget) {
                                                if(mListener != null){
                                                    int position = getAdapterPosition();
                                                    if(position != RecyclerView.NO_POSITION){
                                                        mListener.onItemFollowClick(position);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void updateDrawState(@NonNull TextPaint ds) {
                                                super.updateDrawState(ds);
                                                ds.setColor(Color.BLACK);
                                                ds.setUnderlineText(false);
                                            }
                                        }, user.getUser_name().length(), user.getUser_name().length() + 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                        ss.setSpan(new ClickableSpan() {
                                            @Override
                                            public void onClick(@NonNull View widget) {
                                                if(mListener != null){
                                                    int position = getAdapterPosition();
                                                    if(position != RecyclerView.NO_POSITION){
                                                        mListener.onItemFollowClick(position);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void updateDrawState(@NonNull TextPaint ds) {
                                                super.updateDrawState(ds);
                                                ds.setColor(Color.BLACK);
                                                ds.setUnderlineText(false);
                                            }
                                        }, user.getUser_name().length() + 19 + user2.getUser_name().length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                        ForegroundColorSpan fcsGray = new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorDarkGray));
                                        ss.setSpan(fcsGray, mContent.getText().length() - 3, mContent.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                        mContent.setText(ss);
                                        mContent.setMovementMethod(LinkMovementMethod.getInstance());
                                    }
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
                                    profileIntent.putExtra("user_id", interaction.getUser_id());
                                    mContext.startActivity(profileIntent);
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
                                mListener.onItemFollowClick(position);
                            }
                        }
                    }
                });
            }
            else if(interaction.getAction().equals("comment_like")){
                // Etkileşim yorum yapma ise


            }
        }
    }
}
