package com.example.ibrhm.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ibrhm.instagramclone.Home.ChatActivity;
import com.example.ibrhm.instagramclone.Models.Chats;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class DirectAdapter extends RecyclerView.Adapter<DirectAdapter.DirectViewHolder> {

    private Activity mActivity;
    private List<Chats> mChatsList;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    public DirectAdapter(Activity activity, List<Chats> chatsList){
        mActivity = activity;
        mChatsList = chatsList;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public DirectViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.direct_layout, viewGroup, false);
        return new DirectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DirectViewHolder directViewHolder, int i) {
        directViewHolder.setData(mChatsList.get(i));
    }

    @Override
    public int getItemCount() {
        return mChatsList.size();
    }

    public class DirectViewHolder extends RecyclerView.ViewHolder{

        CircleImageView mProfileImage;
        EmojiTextView mUserName, mLastMessage;
        TextView mTime;
        ImageView mSeenView;

        public DirectViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.direct_profile_image);
            mUserName = itemView.findViewById(R.id.direct_layout_user_name);
            mLastMessage = itemView.findViewById(R.id.direct_last_message);
            mTime = itemView.findViewById(R.id.direct_time);
            mSeenView = itemView.findViewById(R.id.direct_seen_view);
        }

        @SuppressLint("SetTextI18n")
        public void setData(final Chats chat){

            if(chat.isSeen()){
                mSeenView.setVisibility(View.GONE);
                mUserName.setTypeface(null, Typeface.NORMAL);
                mLastMessage.setTypeface(null, Typeface.NORMAL);
                mLastMessage.setTextColor(mActivity.getResources().getColor(R.color.colorLightBlack));
                mTime.setTextColor(mActivity.getResources().getColor(R.color.colorGray));
            }
            else{
                mSeenView.setVisibility(View.VISIBLE);
                mUserName.setTypeface(null, Typeface.BOLD);
                mLastMessage.setTypeface(null, Typeface.BOLD);
                mLastMessage.setTextColor(mActivity.getResources().getColor(android.R.color.black));
                mTime.setTextColor(mActivity.getResources().getColor(android.R.color.black));
            }

            if(chat.getUser_id() != null){
                mRootRef.child("Users").child(chat.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Users user = dataSnapshot.getValue(Users.class);

                            UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null,"");
                            mUserName.setText(user.getUser_name());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            String time = GetTimeAgo.getTimeAgoSmall(chat.getTime());
            mTime.setText(time);
            mLastMessage.setText(chat.getLast_message() + "  •");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chatIntent = new Intent(mActivity, ChatActivity.class);
                    chatIntent.putExtra("user_id", chat.getUser_id());
                    chatIntent.putExtra("user_name", mUserName.getText().toString());
                    mActivity.startActivity(chatIntent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dialogDirect(mUserName.getText().toString(), chat.getUser_id());
                    return true;
                }
            });
        }

        private void dialogDirect(String user_name, final String uid){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(mActivity);
            View view = mActivity.getLayoutInflater().inflate(R.layout.dialog_direct, null);
            mBuilder.setView(view);
            final AlertDialog dialog = mBuilder.create();

            TextView mTitle = view.findViewById(R.id.dialog_direct_title);
            mTitle.setText(user_name);

            Button mDeleteBtn = view.findViewById(R.id.dialog_direct_delete_btn);
            mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    mRootRef.child("Messages").child(mCurrentUser.getUid()).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRootRef.child("Chats").child(mCurrentUser.getUid()).child(uid).removeValue();
                        }
                    });
                }
            });

            Button mMuteBtn = view.findViewById(R.id.dialog_direct_mute_btn);
            mMuteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
    }
}
