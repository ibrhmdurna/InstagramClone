package com.example.ibrhm.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ibrhm.instagramclone.Models.Messages;
import com.example.ibrhm.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private Context mContext;
    private List<Messages> mMessagesList;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    private String mUserName;

    public MessagesAdapter(Context context, List<Messages> messagesList, String userName){
        mContext = context;
        mMessagesList = messagesList;
        mUserName = userName;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout, viewGroup,false);
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder messagesViewHolder, int i) {

        if(i > 0){
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy 0:00 aa");
            String message_time = simpleDateFormat.format(new Date(mMessagesList.get(i).getTime()));

            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy 0:00 aa");
            String old_message_time = simpleDateFormat2.format(new Date(mMessagesList.get(i - 1).getTime()));

            if(message_time.equals(old_message_time)){
                mMessagesList.get(i).setTimeVisibility(false);
            }
            else{
                mMessagesList.get(i).setTimeVisibility(true);
            }
        }
        else{
            mMessagesList.get(i).setTimeVisibility(true);
        }

        for(int x = 0; x < mMessagesList.size() - 1; x++){
            if(mMessagesList.get(x).getFrom().equals(mMessagesList.get(x + 1).getFrom())){
                mMessagesList.get(x).setVisibility(false);
                mMessagesList.get(x + 1).setVisibility(true);
            }
            else{
                mMessagesList.get(x).setVisibility(true);
            }
        }

        mMessagesList.get(mMessagesList.size() - 1).setVisibility(true);

        messagesViewHolder.setData(mMessagesList.get(i));
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder{

        CircleImageView mProfileImage;
        EmojiTextView mMessageContent;
        LinearLayout mMessageLayout;
        TextView mTimeView;
        LinearLayout mTypingLayout, mSeenLayout;
        TextView mSeenUserName;
        CircleImageView mTypingImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.message_profile_image);
            mMessageContent = itemView.findViewById(R.id.message_content);
            mMessageLayout = itemView.findViewById(R.id.message_layout);
            mTimeView = itemView.findViewById(R.id.message_time_view);
            mTypingLayout = itemView.findViewById(R.id.typing_layout);
            mSeenLayout = itemView.findViewById(R.id.message_seen_layout);
            mSeenUserName = itemView.findViewById(R.id.message_seen_username);
            mTypingImage = itemView.findViewById(R.id.typing_image);
        }

        public void setData(Messages message){

            if(!message.isSeenLayout()){
                mSeenLayout.setVisibility(View.GONE);
                mMessageLayout.setVisibility(View.VISIBLE);
                if(message.isTimeVisibility()){
                    mTimeView.setVisibility(View.VISIBLE);
                    String time = GetTimeAgo.getMessageAgo(message.getTime());
                    mTimeView.setText(time);
                }
                else{
                    mTimeView.setVisibility(View.GONE);
                }

                if(message.getFrom().equals(mCurrentUser.getUid())){
                    mMessageLayout.setGravity(Gravity.END);
                    mProfileImage.setVisibility(View.INVISIBLE);
                    mMessageContent.setBackground(mContext.getDrawable(R.drawable.current_message_layout));
                }
                else{
                    mMessageLayout.setGravity(Gravity.START);
                    mMessageContent.setBackground(mContext.getDrawable(R.drawable.external_message_background));

                    if(message.isVisibility()){
                        mProfileImage.setVisibility(View.VISIBLE);

                        mRootRef.child("Users").child(message.getFrom()).child("details").child("profile_image").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String url = dataSnapshot.getValue().toString();
                                UniversalImageLoader.setImage(url, mProfileImage, null,"");
                                UniversalImageLoader.setImage(url, mTypingImage, null, "");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    else{
                        mProfileImage.setVisibility(View.INVISIBLE);
                    }
                }

                if(message.getType().equals("like"))
                {
                    //mMessageContent.setTextSize(50f);
                    mMessageContent.setBackground(mContext.getDrawable(android.R.color.transparent));
                }
                else{
                    //mMessageContent.setTextSize(15f);
                }

                mMessageContent.setText(message.getMessage());
            }
            else{
                mMessageLayout.setVisibility(View.GONE);
                mTimeView.setVisibility(View.GONE);
                mSeenLayout.setVisibility(View.VISIBLE);
                mSeenUserName.setText(mUserName);
            }
        }
    }
}
