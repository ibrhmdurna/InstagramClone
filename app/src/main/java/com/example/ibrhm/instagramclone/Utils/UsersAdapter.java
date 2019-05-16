package com.example.ibrhm.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiTextView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private static int ACTIVITY_NO = 0;

    private Activity mContext;
    private List<String> mUserList;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private boolean isFollowers = false;

    public UsersAdapter(Activity context, List<String> usersList, int activityNo, boolean isFollowers){
        mContext = context;
        mUserList = usersList;

        this.isFollowers = isFollowers;
        ACTIVITY_NO = activityNo;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_layout, viewGroup, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i) {
        userViewHolder.setData(mUserList.get(i));
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{

        private boolean isPrivateAccount = false;

        CircleImageView mProfileImage;
        TextView mUserName;
        EmojiTextView mFullName;
        TextView mFollowBtn, mFollowingBtn, mRequestedBtn;
        ImageView mMoreOptions;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.user_profile_image);
            mUserName = itemView.findViewById(R.id.user_name);
            mFullName = itemView.findViewById(R.id.user_full_name);
            mFollowBtn = itemView.findViewById(R.id.follow_view);
            mFollowingBtn = itemView.findViewById(R.id.following_view);
            mRequestedBtn = itemView.findViewById(R.id.requested_view);
            mMoreOptions = itemView.findViewById(R.id.user_more_options);
        }

        public void setData(final String uid){

            mRootRef.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users user = dataSnapshot.getValue(Users.class);

                    UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");
                    mUserName.setText(user.getUser_name());
                    mFullName.setText(user.getFull_name());

                    isPrivateAccount = user.isPrivate_account();

                    isFollowing(uid);

                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(uid.equals(mCurrentUser.getUid())){
                                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                                mContext.startActivity(profileIntent);
                            }
                            else{
                                Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                                profileIntent.putExtra("user_id", uid);
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

        private void isFollowing(final String user_id) {

            if(!user_id.equals(mCurrentUser.getUid()))
            {
                mRootRef.child("Following").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            // Takip ediyorum
                            mFollowBtn.setVisibility(View.GONE);
                            mFollowingBtn.setVisibility(View.VISIBLE);
                            mRequestedBtn.setVisibility(View.GONE);
                        }else{
                            // Takip etmiyorum
                            mFollowingBtn.setVisibility(View.GONE);
                            mFollowBtn.setText(mContext.getString(R.string.follow));
                            mRootRef.child("Follow_req").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mFollowBtn.setVisibility(View.GONE);
                                        mFollowingBtn.setVisibility(View.GONE);
                                        mRequestedBtn.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        mFollowBtn.setVisibility(View.VISIBLE);
                                        mFollowingBtn.setVisibility(View.GONE);
                                        mRequestedBtn.setVisibility(View.GONE);
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

                mFollowingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogUnFollow(user_id);
                    }
                });

                mFollowBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!isPrivateAccount){
                            String currentDate = java.text.DateFormat.getDateInstance().format(new Date());
                            Map followMap = new HashMap();
                            followMap.put("Following/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                            followMap.put("Following/"+mCurrentUser.getUid()+"/"+user_id+"/date", currentDate);
                            followMap.put("Followers/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                            followMap.put("Followers/"+user_id+"/"+mCurrentUser.getUid()+"/date", currentDate);
                            mRootRef.updateChildren(followMap);

                            String news_key = mRootRef.child("News").child("All_users").child("Interactions").child(user_id).push().getKey();
                            Map allNewsMap = new HashMap();
                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/action", "following");
                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/time", ServerValue.TIMESTAMP);
                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/content", "");
                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/post_id", "");
                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/post_user_id", user_id);
                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/user_id", mCurrentUser.getUid());
                            mRootRef.updateChildren(allNewsMap);

                            isFollowing(user_id);
                        }
                        else{
                            Map followReqMap = new HashMap();
                            followReqMap.put("News/Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                            followReqMap.put("News/Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);
                            followReqMap.put("Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                            mRootRef.updateChildren(followReqMap);

                            isFollowing(user_id);
                        }
                    }
                });

                mRequestedBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map followReqMap = new HashMap();
                        followReqMap.put("News/Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", null);
                        followReqMap.put("News/Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/time", null);
                        followReqMap.put("Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", null);
                        mRootRef.updateChildren(followReqMap);

                        isFollowing(user_id);
                    }
                });

                if(isFollowers){
                    mMoreOptions.setVisibility(View.VISIBLE);

                    mMoreOptions.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mContext, "More Options", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        private void dialogUnFollow(final String user_id){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
            View view = mContext.getLayoutInflater().inflate(R.layout.dialog_un_follow, null);
            mBuilder.setView(view);
            final AlertDialog dialog = mBuilder.create();

            CircleImageView mImage = view.findViewById(R.id.dialog_un_follow_profile_image);
            mImage.setImageDrawable(mProfileImage.getDrawable());

            TextView body = view.findViewById(R.id.dialog_un_follow_body);
            body.setText(Html.fromHtml("Unfollow <b>@"+mUserName.getText()+"</b>?"));

            Button mCancelBtn = view.findViewById(R.id.dialog_un_follow_cancel_btn);
            mCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            Button mUnFollowButton = view.findViewById(R.id.dialog_un_follow_btn);
            mUnFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Map followMap = new HashMap();
                    followMap.put("Following/"+mCurrentUser.getUid()+"/"+user_id, null);
                    followMap.put("Followers/"+user_id+"/"+mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(followMap);

                    Map newsMap = new HashMap();
                    newsMap.put("News/Follow_interactions/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", null);
                    newsMap.put("News/Follow_interactions/"+user_id+"/"+mCurrentUser.getUid()+"/date", null);
                    newsMap.put("News/Follow_interactions/"+user_id+"/"+mCurrentUser.getUid()+"/time", null);
                    mRootRef.updateChildren(newsMap);

                    mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    if(ds.child("action").getValue().toString().equals("following") &&
                                            ds.child("post_user_id").getValue().toString().equals(user_id)){
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

                    isFollowing(user_id);
                }
            });

            dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
    }
}
