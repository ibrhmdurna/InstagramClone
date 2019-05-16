package com.example.ibrhm.instagramclone.Home;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.baoyz.widget.PullRefreshLayout;
import com.example.ibrhm.instagramclone.Models.CommentLike;
import com.example.ibrhm.instagramclone.Models.Messages;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.MessagesAdapter;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private ImageView mBackView;
    private CircleImageView mProfileImage;
    private TextView mUserName;
    private CircleImageView mChatCameraMedium, mChatCameraSmall;
    private EmojiEditText mMessageInput;
    private ImageView mChatLikeView;
    private TextView mSendView;
    private PullRefreshLayout mRefreshLayout;
    private CircleImageView mTypingImage;
    private LinearLayout mTypingLayout;
    private LinearLayout mSeenLayout;
    private TextView mSeenUserName;
    private LottieAnimationView mLoadingAnim;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    private List<Messages> mMessagesList;
    private RecyclerView mMessagesView;
    private MessagesAdapter mMessagesAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    private String user_id;
    private String user_name;

    private static int TOTAL_LOAD_MESSAGE_COUNT = 50;
    private static int CURRENT_POSITION = 0;
    private static int CURRENT_MORE_POSITION = 0;

    private static String MESSAGE_LAST_KEY;
    private static String MESSAGE_PREVIEW_KEY;

    private boolean isTyping = true;
    private boolean isSeen = false;

    public static boolean isOpenChat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        user_id = getIntent().getStringExtra("user_id");
        user_name = getIntent().getStringExtra("user_name");

        setupView();
        onBack();
        getData();
        messageWatcher();

        mMessagesList = new ArrayList<>();
        mMessagesAdapter = new MessagesAdapter(this, mMessagesList, user_name);
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessagesView.setLayoutManager(mLinearLayoutManager);
        mMessagesView.setHasFixedSize(true);
        mMessagesView.setAdapter(mMessagesAdapter);

        getMessages();
        messageLastSeen();
        isOpenChat = true;
        sendMessage();
        setupLike();

        mRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() > mMessagesList.size()){
                            CURRENT_MORE_POSITION = 0;
                            getMoreMessages();
                        }
                        else{
                            mRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void setupLike(){
        mChatLikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMessagesList.get(mMessagesList.size() - 1).getFrom().equals(mCurrentUser.getUid())){
                    if(!mMessagesList.get(mMessagesList.size() - 1).getType().equals("like")){
                        int unicode = 0x2764;
                        String emoji = new String(Character.toChars(unicode));

                        mSeenLayout.setVisibility(View.GONE);

                        Map chatMap = new HashMap();
                        chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/last_message", emoji);
                        chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/seen", true);
                        chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/time", ServerValue.TIMESTAMP);
                        chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                        chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/typing", false);
                        chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/last_message", emoji);
                        chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/seen", false);
                        chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);
                        chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());

                        mRootRef.child("Chats").updateChildren(chatMap);

                        Map messageMap = new HashMap();
                        messageMap.put("from", mCurrentUser.getUid());
                        messageMap.put("message", emoji);
                        messageMap.put("seen", false);
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("type", "like");

                        final String message_key = mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).push().getKey();

                        Map messagesMap = new HashMap();
                        messagesMap.put(mCurrentUser.getUid()+"/"+user_id+"/"+message_key, messageMap);
                        messagesMap.put(user_id+"/"+mCurrentUser.getUid()+"/"+message_key, messageMap);

                        mRootRef.child("Messages").updateChildren(messagesMap);
                    }
                }
                else{
                    int unicode = 0x2764;
                    String emoji = new String(Character.toChars(unicode));

                    mSeenLayout.setVisibility(View.GONE);

                    Map chatMap = new HashMap();
                    chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/last_message", emoji);
                    chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/seen", true);
                    chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/time", ServerValue.TIMESTAMP);
                    chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                    chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/typing", false);
                    chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/last_message", emoji);
                    chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/seen", false);
                    chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);
                    chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());

                    mRootRef.child("Chats").updateChildren(chatMap);

                    Map messageMap = new HashMap();
                    messageMap.put("from", mCurrentUser.getUid());
                    messageMap.put("message", emoji);
                    messageMap.put("seen", false);
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("type", "like");

                    final String message_key = mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).push().getKey();

                    Map messagesMap = new HashMap();
                    messagesMap.put(mCurrentUser.getUid()+"/"+user_id+"/"+message_key, messageMap);
                    messagesMap.put(user_id+"/"+mCurrentUser.getUid()+"/"+message_key, messageMap);

                    mRootRef.child("Messages").updateChildren(messagesMap);
                }
            }
        });
    }

    private void typingWatcher(){
        mMessageInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(isTyping && s.toString().trim().length() > 1){
                    isTyping = false;
                    mRootRef.child("Chats").child(mCurrentUser.getUid()).child(user_id).child("typing").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                dataSnapshot.getRef().setValue(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else if (s.toString().trim().length() == 0){
                    isTyping = true;
                    mRootRef.child("Chats").child(mCurrentUser.getUid()).child(user_id).child("typing").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                dataSnapshot.getRef().setValue(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    private void getMoreMessages(){

        Query messageQuery = mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).orderByKey().endAt(MESSAGE_LAST_KEY).limitToLast(TOTAL_LOAD_MESSAGE_COUNT);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                if(!MESSAGE_PREVIEW_KEY.equals(dataSnapshot.getKey())){
                    mMessagesList.add(CURRENT_MORE_POSITION++, message);
                    mMessagesAdapter.notifyItemInserted(CURRENT_MORE_POSITION);
                    mLinearLayoutManager.scrollToPositionWithOffset(CURRENT_MORE_POSITION + 1, 0);
                }
                else{
                    MESSAGE_PREVIEW_KEY = MESSAGE_LAST_KEY;
                }

                if(CURRENT_MORE_POSITION == 1){
                    MESSAGE_LAST_KEY = dataSnapshot.getKey();
                }

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                /*Messages message = dataSnapshot.getValue(Messages.class);
                int position = findPosition(message.getTime());
                if(position != -1){
                    mMessagesList.remove(position);
                    mMessagesAdapter.notifyItemRemoved(position);
                }*/
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMessages(){

        Query messageQuery = mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).limitToLast(TOTAL_LOAD_MESSAGE_COUNT);

        messageQuery.addChildEventListener(childEventListener);
    }

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
            Messages message = dataSnapshot.getValue(Messages.class);
            message.setSeenLayout(false);
            mMessagesList.add(message);

            if(CURRENT_POSITION == 0){
                MESSAGE_LAST_KEY = dataSnapshot.getKey();
                MESSAGE_PREVIEW_KEY = dataSnapshot.getKey();
            }

            CURRENT_POSITION++;

            if(mMessagesList.size() > 1){
                Messages oldMessage = mMessagesList.get(mMessagesList.size() - 2);
                if(message.getFrom().equals(oldMessage.getFrom())){
                    mMessagesList.get(mMessagesList.size() - 2).setVisibility(false);
                    mMessagesAdapter.notifyItemChanged(mMessagesList.size() - 2);
                }
            }

            mMessagesAdapter.notifyItemInserted(mMessagesList.size() - 1);
            mMessagesView.smoothScrollToPosition(mMessagesList.size() - 1);

            messageSeen(dataSnapshot.getKey());
            //messageLastSeen(dataSnapshot.getKey());
            typingWatcher();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            /*Messages message = dataSnapshot.getValue(Messages.class);
            int position = findPosition(message.getTime());
            if(position != -1){
                mMessagesList.remove(position);
                mMessagesAdapter.notifyItemRemoved(position);
            }*/
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void messageLastSeen() {
        mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                Messages message = ds.getValue(Messages.class);

                                if(message.isSeen() && message.getFrom().equals(mCurrentUser.getUid())){
                                    isSeen = true;
                                    message.setSeenLayout(true);
                                    mMessagesList.add(message);
                                    mMessagesAdapter.notifyItemInserted(mMessagesList.size() - 1);
                                    mMessagesView.smoothScrollToPosition(mMessagesList.size() - 1);
                                    break;
                                }
                                else{
                                    isSeen = false;
                                    for(int i = 0; i < mMessagesList.size(); i++){
                                        if(mMessagesList.get(i).isSeenLayout()){
                                            mMessagesList.remove(i);
                                            mMessagesAdapter.notifyItemRemoved(i);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void currentSeenState(){
        final List<Messages> tempList = new ArrayList<>();
        mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).limitToLast(2)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            tempList.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                Messages message = ds.getValue(Messages.class);
                                tempList.add(message);
                            }

                            if(tempList.get(1).isSeen() && tempList.get(1).getFrom().equals(mCurrentUser.getUid())){
                                mMessagesList.get(mMessagesList.size() - 1).setSeenLayout(true);
                                mMessagesList.get(mMessagesList.size() - 1).setTypingLayout(false);
                                mMessagesAdapter.notifyItemChanged(mMessagesList.size() - 1);
                            }
                            else{
                                if(tempList.get(1).isTypingLayout()){
                                    mMessagesList.remove(mMessagesList.size() - 1);
                                    mMessagesAdapter.notifyItemRemoved(mMessagesList.size() - 1);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private ValueEventListener typingEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                if(dataSnapshot.getValue().equals(true)){
                    if(isSeen){
                        /*if(mMessagesList.get(mMessagesList.size() - 1).isSeenLayout()){
                            mMessagesList.get(mMessagesList.size() - 1).setSeenLayout(false);
                            mMessagesList.remove(mMessagesList.size() - 1);
                            mMessagesAdapter.notifyItemRemoved(mMessagesList.size() - 1);
                        }*/
                    }
                    /*
                    if(mMessagesList.get(mMessagesList.size() - 1).isSeenLayout()){
                        mMessagesList.get(mMessagesList.size() - 1).setSeenLayout(false);
                        mMessagesAdapter.notifyItemChanged(mMessagesList.size() - 1);
                        mMessagesList.remove(mMessagesList.size() - 1);
                        mMessagesAdapter.notifyItemRemoved(mMessagesList.size() - 1);
                    }*/

                    mTypingLayout.setVisibility(View.VISIBLE);
                    mTypingLayout.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, android.R.anim.fade_in));

                }
                else
                {
                    if(isSeen){
                        /*Messages message = mMessagesList.get(mMessagesList.size() - 1);
                        if(!message.isSeenLayout()){
                            message.setSeenLayout(true);
                            mMessagesList.add(message);
                            mMessagesAdapter.notifyItemInserted(mMessagesList.size() - 1);
                        }*/
                    }

                    mTypingLayout.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, android.R.anim.fade_out));
                    mTypingLayout.getAnimation().setFillAfter(true);
                    mTypingLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private int findPosition(long time){
        for(int i = 0; i < mMessagesList.size(); i++){
            if(mMessagesList.get(i).getTime() == time){
                return i;
            }
        }
        return -1;
    }

    private void messageSeen(String key){
        mRootRef.child("Messages").child(user_id).child(mCurrentUser.getUid())
                .child(key).child("seen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    dataSnapshot.getRef().setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRootRef.child("Chats").child(mCurrentUser.getUid()).child(user_id).child("seen").setValue(true);
                            /*if(mMessagesList.get(mMessagesList.size() - 1).getFrom().equals(mCurrentUser.getUid())){
                                mMessagesList.get(mMessagesList.size() - 1).setSeen(true);
                                mMessagesAdapter.notifyItemChanged;
                            }*/
                        }
                    });
                }
                else{
                    mRootRef.child("Chats").child(mCurrentUser.getUid()).child(user_id).child("seen").setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(){
        mSendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSeenLayout.setVisibility(View.GONE);
                final String message = mMessageInput.getText().toString();

                Map chatMap = new HashMap();
                chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/last_message", message);
                chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/seen", true);
                chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/time", ServerValue.TIMESTAMP);
                chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                chatMap.put(mCurrentUser.getUid()+"/"+user_id+"/typing", false);
                chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/last_message", message);
                chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/seen", false);
                chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);
                chatMap.put(user_id+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());

                mRootRef.child("Chats").updateChildren(chatMap);

                Map messageMap = new HashMap();
                messageMap.put("from", mCurrentUser.getUid());
                messageMap.put("message", message);
                messageMap.put("seen", false);
                messageMap.put("time", ServerValue.TIMESTAMP);
                messageMap.put("type", "text");

                final String message_key = mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).push().getKey();

                Map messagesMap = new HashMap();
                messagesMap.put(mCurrentUser.getUid()+"/"+user_id+"/"+message_key, messageMap);
                messagesMap.put(user_id+"/"+mCurrentUser.getUid()+"/"+message_key, messageMap);

                mRootRef.child("Messages").updateChildren(messagesMap);
                mMessageInput.getText().clear();
            }
        });
    }

    private void messageWatcher(){
        mMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mMessageInput.getText().length() > 0){
                    mChatCameraMedium.setVisibility(View.GONE);
                    mChatCameraSmall.setVisibility(View.VISIBLE);
                    mChatLikeView.setVisibility(View.GONE);
                    mSendView.setVisibility(View.VISIBLE);
                }
                else{
                    mChatCameraMedium.setVisibility(View.VISIBLE);
                    mChatCameraSmall.setVisibility(View.GONE);
                    mChatLikeView.setVisibility(View.VISIBLE);
                    mSendView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void getData(){
        mRootRef.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);

                UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mProfileImage, null, "");
                UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mTypingImage, null, "");
                mUserName.setText(user.getUser_name());
                mSeenUserName.setText(user.getUser_name());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onBack(){
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.super.onBackPressed();
            }
        });
    }

    private void setupView(){
        mBackView = findViewById(R.id.chat_back_button);
        mProfileImage = findViewById(R.id.chat_profile_image);
        mUserName = findViewById(R.id.chat_user_name);
        mMessageInput = findViewById(R.id.message_input);
        mChatCameraMedium = findViewById(R.id.chat_camera_medium);
        mChatCameraSmall = findViewById(R.id.chat_camera_small);
        mChatLikeView = findViewById(R.id.chat_like_view);
        mSendView = findViewById(R.id.message_send_view);
        mMessagesView = findViewById(R.id.messages_container);
        mRefreshLayout = findViewById(R.id.messages_refresh_layout);
        mTypingImage = findViewById(R.id.typing_image);
        mTypingLayout = findViewById(R.id.typing_layout);
        mSeenLayout = findViewById(R.id.message_seen_layout);
        mSeenUserName = findViewById(R.id.message_seen_username);
        mLoadingAnim = findViewById(R.id.chat_loading_anim);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TOTAL_LOAD_MESSAGE_COUNT = 15;
        CURRENT_POSITION = 0;
        CURRENT_MORE_POSITION = 0;
        isTyping = true;

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(user_id, 2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRootRef.child("Chats").child(user_id).child(mCurrentUser.getUid()).child("typing").addValueEventListener(typingEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOpenChat = false;
        mRootRef.child("Messages").child(mCurrentUser.getUid()).child(user_id).removeEventListener(childEventListener);
        mRootRef.child("Chats").child(mCurrentUser.getUid()).child(user_id).child("typing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    dataSnapshot.getRef().setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRootRef.child("Chats").child(user_id).child(mCurrentUser.getUid()).removeEventListener(typingEventListener);
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
