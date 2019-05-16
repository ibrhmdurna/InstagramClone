package com.example.ibrhm.instagramclone.Utils;

import android.app.Activity;
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
import android.widget.TextView;

import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Users;
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

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ReqViewHolder> {

    private Activity mContext;
    private List<String> mReqList;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private boolean isRequest;

    public RequestAdapter(Activity context, List<String> reqList, boolean request){
        mContext = context;
        mReqList = reqList;
        isRequest = request;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ReqViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.request_layout, viewGroup, false);
        return new ReqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReqViewHolder reqViewHolder, int i) {
        reqViewHolder.setData(mReqList.get(i), i);
    }

    @Override
    public int getItemCount() {
        return mReqList.size();
    }

    public class ReqViewHolder extends RecyclerView.ViewHolder{

        CircleImageView mProfileImage;
        TextView mUserName;
        EmojiTextView mFullName;
        TextView mConfirm, mDelete, mFollow, mFollowing, mRequested;

        public ReqViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.req_profile_image);
            mUserName = itemView.findViewById(R.id.req_user_name);
            mFullName = itemView.findViewById(R.id.req_full_name);
            mConfirm = itemView.findViewById(R.id.req_confirm_btn);
            mDelete = itemView.findViewById(R.id.req_delete_btn);
            mFollow = itemView.findViewById(R.id.req_follow_btn);
            mFollowing = itemView.findViewById(R.id.req_following_btn);
            mRequested = itemView.findViewById(R.id.req_requested_btn);
        }

        public void setData(final String uid, final int position){

            if(isRequest){
                mConfirm.setVisibility(View.VISIBLE);
                mDelete.setVisibility(View.VISIBLE);
            }
            else{
                mConfirm.setVisibility(View.GONE);
                mConfirm.setVisibility(View.GONE);
                getFollowState(uid);
            }

            final String[] img_url = new String[1];
            final String[] user_name = new String[1];
            mRootRef.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users user = dataSnapshot.getValue(Users.class);

                    UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");
                    mUserName.setText(user.getUser_name());

                    if(!user.getFull_name().equals("")){
                        mFullName.setVisibility(View.VISIBLE);
                        mFullName.setText(user.getFull_name());
                    }

                    img_url[0] = user.getDetails().getProfile_image();
                    user_name[0] = user.getUser_name();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // Istegi Kabul Et
            mConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map followReqMap = new HashMap();
                    followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+uid+"/user_id", null);
                    followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+uid+"/time", null);
                    followReqMap.put("Follow_req/"+uid+"/"+mCurrentUser.getUid()+"/user_id", null);
                    mRootRef.updateChildren(followReqMap);

                    String currentDate = java.text.DateFormat.getDateInstance().format(new Date());
                    Map followMap = new HashMap();
                    followMap.put("Followers/"+mCurrentUser.getUid()+"/"+uid+"/user_id", uid);
                    followMap.put("Followers/"+mCurrentUser.getUid()+"/"+uid+"/date", currentDate);
                    followMap.put("Following/"+uid+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                    followMap.put("Following/"+uid+"/"+mCurrentUser.getUid()+"/date", currentDate);
                    mRootRef.updateChildren(followMap);

                    Map newsMap = new HashMap();
                    newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+uid+"/user_id", uid);
                    newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+uid+"/date", currentDate);
                    newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+uid+"/time", ServerValue.TIMESTAMP);
                    mRootRef.updateChildren(newsMap);

                    String news_key = mRootRef.child("News").child("All_users").child("Interactions").child(uid).push().getKey();
                    Map allNewsMap = new HashMap();
                    allNewsMap.put("News/All_users/Interactions/"+uid+"/"+news_key+"/action", "following");
                    allNewsMap.put("News/All_users/Interactions/"+uid+"/"+news_key+"/time", ServerValue.TIMESTAMP);
                    allNewsMap.put("News/All_users/Interactions/"+uid+"/"+news_key+"/content", "");
                    allNewsMap.put("News/All_users/Interactions/"+uid+"/"+news_key+"/post_id", "");
                    allNewsMap.put("News/All_users/Interactions/"+uid+"/"+news_key+"/post_user_id", mCurrentUser.getUid());
                    allNewsMap.put("News/All_users/Interactions/"+uid+"/"+news_key+"/user_id", uid);
                    mRootRef.updateChildren(allNewsMap);

                    getFollowState(uid);
                }
            });

            // Istegi Iptal Et
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map followReqMap = new HashMap();
                    followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+uid+"/user_id", null);
                    followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+uid+"/time", null);
                    followReqMap.put("Follow_req/"+uid+"/"+mCurrentUser.getUid()+"/user_id", null);
                    mRootRef.updateChildren(followReqMap);
                    mReqList.remove(position);
                    notifyItemRemoved(position);
                }
            });

            // Takip Et
            mFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRootRef.child("Users").child(uid).child("private_account").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue().equals(true)){
                                Map followReqMap = new HashMap();
                                followReqMap.put("News/Follow_req/"+uid+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                                followReqMap.put("News/Follow_req/"+uid+"/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);
                                followReqMap.put("Follow_req/"+mCurrentUser.getUid()+"/"+uid+"/user_id", uid);
                                mRootRef.updateChildren(followReqMap);
                                mFollow.setVisibility(View.GONE);
                                mFollowing.setVisibility(View.GONE);
                                mRequested.setVisibility(View.VISIBLE);
                            }
                            else{
                                String currentDate = java.text.DateFormat.getDateInstance().format(new Date());
                                Map followMap = new HashMap();
                                followMap.put("Following/"+mCurrentUser.getUid()+"/"+uid+"/user_id", uid);
                                followMap.put("Following/"+mCurrentUser.getUid()+"/"+uid+"/date", currentDate);
                                followMap.put("Followers/"+uid+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                                followMap.put("Followers/"+uid+"/"+mCurrentUser.getUid()+"/date", currentDate);
                                mRootRef.updateChildren(followMap);

                                String news_key = mRootRef.child("News").child("All_users").child("Interactions").child(uid).push().getKey();
                                Map allNewsMap = new HashMap();
                                allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/action", "following");
                                allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/time", ServerValue.TIMESTAMP);
                                allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/content", "");
                                allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/post_id", "");
                                allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/post_user_id", uid);
                                allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/user_id", mCurrentUser.getUid());
                                mRootRef.updateChildren(allNewsMap);

                                mFollow.setVisibility(View.GONE);
                                mFollowing.setVisibility(View.VISIBLE);
                                mRequested.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            // Istegi Iptal Et
            mRequested.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map followReqMap = new HashMap();
                    followReqMap.put("News/Follow_req/"+uid+"/"+mCurrentUser.getUid()+"/user_id", null);
                    followReqMap.put("News/Follow_req/"+uid+"/"+mCurrentUser.getUid()+"/time", null);
                    followReqMap.put("Follow_req/"+mCurrentUser.getUid()+"/"+uid+"/user_id", null);
                    mRootRef.updateChildren(followReqMap);
                    mFollow.setVisibility(View.VISIBLE);
                    mFollowing.setVisibility(View.GONE);
                    mRequested.setVisibility(View.GONE);
                }
            });

            // Takip Cek
            mFollowing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogUnFollow(img_url[0], user_name[0], uid);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(mContext, UserProfileActivity.class);
                    profileIntent.putExtra("activity_no", 3);
                    profileIntent.putExtra("user_id", uid);
                    mContext.startActivity(profileIntent);
                }
            });
        }

        private void dialogUnFollow(String image_url, String user_name, final String uid){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
            View view = mContext.getLayoutInflater().inflate(R.layout.dialog_un_follow, null);
            mBuilder.setView(view);
            final AlertDialog dialog = mBuilder.create();

            CircleImageView mImage = view.findViewById(R.id.dialog_un_follow_profile_image);
            UniversalImageLoader.setImage(image_url, mImage, null,"");

            TextView body = view.findViewById(R.id.dialog_un_follow_body);
            body.setText(Html.fromHtml("Unfollow <b>@"+user_name+"</b>?"));

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
                    followMap.put("Following/"+mCurrentUser.getUid()+"/"+uid, null);
                    followMap.put("Followers/"+uid+"/"+mCurrentUser.getUid(), null);
                    mRootRef.updateChildren(followMap);

                    Map newsMap = new HashMap();
                    newsMap.put("News/Follow_interactions/"+uid+"/"+mCurrentUser.getUid()+"/user_id", null);
                    newsMap.put("News/Follow_interactions/"+uid+"/"+mCurrentUser.getUid()+"/date", null);
                    newsMap.put("News/Follow_interactions/"+uid+"/"+mCurrentUser.getUid()+"/time", null);
                    mRootRef.updateChildren(newsMap);

                    mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    if(ds.child("action").getValue().toString().equals("following") &&
                                            ds.child("post_user_id").getValue().toString().equals(uid)){
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

                    mFollow.setVisibility(View.VISIBLE);
                    mFollowing.setVisibility(View.GONE);
                    mRequested.setVisibility(View.GONE);
                }
            });

            dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }

        private void getFollowState(final String uid){
            mRootRef.child("Following").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(uid)){
                        // Takip ediyorum
                        mConfirm.setVisibility(View.GONE);
                        mDelete.setVisibility(View.GONE);
                        mFollow.setVisibility(View.GONE);
                        mFollowing.setVisibility(View.VISIBLE);
                        mRequested.setVisibility(View.GONE);
                    }else{
                        // Takip etmiyorum
                        mConfirm.setVisibility(View.GONE);
                        mDelete.setVisibility(View.GONE);
                        mFollowing.setVisibility(View.GONE);
                        mRootRef.child("Follow_req").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(uid)){
                                    mFollow.setVisibility(View.GONE);
                                    mRequested.setVisibility(View.VISIBLE);
                                }
                                else{
                                    mFollow.setVisibility(View.VISIBLE);
                                    mRequested.setVisibility(View.GONE);
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
