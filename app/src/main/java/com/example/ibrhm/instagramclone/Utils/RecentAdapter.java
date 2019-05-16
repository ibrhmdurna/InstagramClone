package com.example.ibrhm.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Search.AlgoliaSearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.RecentViewHolder> {

    private Context mContext;
    private List<String> mUsersList;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    public RecentAdapter(Context context, List<String> usersList){
        mContext = context;
        mUsersList = usersList;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.hits_item, viewGroup, false);
        return new RecentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder recentViewHolder, int i) {
        recentViewHolder.setData(mUsersList.get(i), i);
    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }

    public class RecentViewHolder extends RecyclerView.ViewHolder{

        CircleImageView mProfileImage;
        TextView mUserName, mRecent;
        EmojiTextView mFullName;

        public RecentViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.product_profile);
            mUserName = itemView.findViewById(R.id.product_name);
            mFullName = itemView.findViewById(R.id.product_price);
            mRecent = itemView.findViewById(R.id.product_recent);

        }

        public void setData(final String uid, int position){

            if(position == 0){
                mRecent.setVisibility(View.VISIBLE);
            }

            mRootRef.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users user = dataSnapshot.getValue(Users.class);

                    UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");
                    mUserName.setText(user.getUser_name());
                    mFullName.setText(user.getFull_name());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(uid.equals(mCurrentUser.getUid())){
                        Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                        mContext.startActivity(profileIntent);
                    }else{
                        Intent userProfileIntent = new Intent(mContext, UserProfileActivity.class);
                        userProfileIntent.putExtra("activity_no", 1);
                        userProfileIntent.putExtra("user_id", uid);
                        mContext.startActivity(userProfileIntent);
                    }

                    Map recentMap = new HashMap();
                    recentMap.put("Recent/"+mCurrentUser.getUid()+"/"+uid+"/user_id", uid);
                    recentMap.put("Recent/"+mCurrentUser.getUid()+"/"+uid+"/time", ServerValue.TIMESTAMP);

                    mRootRef.updateChildren(recentMap);
                }
            });
        }
    }
}
