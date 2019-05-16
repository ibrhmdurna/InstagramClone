package com.example.ibrhm.instagramclone.Home;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.baoyz.widget.PullRefreshLayout;
import com.example.ibrhm.instagramclone.Models.Chats;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.DirectAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DirectFragment extends Fragment {

    private View mView;

    private ViewPager mViewPager;
    private ImageView mBackView, mAddView;
    private EmojiTextView mUserName;
    private LinearLayout mSearchLayout, mCameraLayout, mDirectNotLayout;
    private LottieAnimationView mLoadingAnim;
    private PullRefreshLayout mRefreshLayout;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private List<Long> mChatTimeList;
    private List<Chats> mChatsList;
    private RecyclerView mDirectView;
    private DirectAdapter mDirectAdapter;

    public static boolean isOpenDirect = false;

    public DirectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_direct, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        setupView();
        onBack();
        setupSearchView();
        goCamera();

        mRefreshLayout.setEnabled(false);
        mLoadingAnim.setVisibility(View.VISIBLE);
        mLoadingAnim.playAnimation();
        mDirectView.setVisibility(View.GONE);
        mDirectNotLayout.setVisibility(View.GONE);
        getData();
        getChats();

        mRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mDirectView.setVisibility(View.GONE);
                mDirectNotLayout.setVisibility(View.GONE);
                getData();
                getChats();
            }
        });

        return mView;
    }

    private void goCamera(){
        mCameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0,true);
            }
        });
    }

    private void setupSearchView(){
        mSearchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getActivity(), DirectSearchActivity.class);
                startActivity(searchIntent);
            }
        });
    }

    private void getChats(){
        mChatsList = new ArrayList<>();
        mChatTimeList = new ArrayList<>();
        mDirectAdapter = new DirectAdapter(getActivity(), mChatsList);
        mDirectView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mDirectView.setHasFixedSize(true);
        mDirectView.setAdapter(mDirectAdapter);

        mChatsList.clear();
        mRootRef.child("Chats").child(mCurrentUser.getUid()).orderByChild("time").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Chats chat = dataSnapshot.getValue(Chats.class);
                mChatsList.add(0, chat);
                mChatTimeList.add(0, chat.getTime());
                mDirectAdapter.notifyItemInserted(0);

                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setEnabled(true);
                        mRefreshLayout.setRefreshing(false);
                        mLoadingAnim.setVisibility(View.GONE);
                        mLoadingAnim.pauseAnimation();
                        mDirectView.setVisibility(View.VISIBLE);
                        if(mDirectAdapter.getItemCount() > 0){
                            mDirectNotLayout.setVisibility(View.GONE);
                        }
                        else{
                            mDirectNotLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }, 1000);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                int position = findPosition(dataSnapshot.getKey());
                if(position != -1){
                    Chats chat = dataSnapshot.getValue(Chats.class);
                    if(chat.getTime() != mChatTimeList.get(position)){
                        mChatsList.remove(position);
                        mChatTimeList.remove(position);
                        mDirectAdapter.notifyItemRemoved(position);
                        mChatsList.add(0, chat);
                        mChatTimeList.add(0, chat.getTime());
                        mDirectAdapter.notifyItemInserted(0);
                    }
                    else if(chat.isSeen()){
                        mChatsList.get(position).setSeen(true);
                        mDirectAdapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int position = findPosition(dataSnapshot.getKey());
                if(position != -1){
                    mChatsList.remove(position);
                    mChatTimeList.remove(position);
                    mDirectAdapter.notifyItemRemoved(position);

                    if(mDirectAdapter.getItemCount() > 0){
                        mDirectNotLayout.setVisibility(View.GONE);
                    }
                    else{
                        mDirectNotLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int findPosition(String id){
        for(int i = 0; i < mChatsList.size(); i++){
            if(mChatsList.get(i).getUser_id().equals(id)){
                return i;
            }
        }
        return -1;
    }

    private void getData(){
        mRootRef.child("Users").child(mCurrentUser.getUid()).child("user_name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user_name = dataSnapshot.getValue().toString();
                mUserName.setText(user_name);
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
                mViewPager.setCurrentItem(1, true);
            }
        });
    }

    private void setupView(){
        mViewPager = getActivity().findViewById(R.id.homeViewPager);
        mBackView = mView.findViewById(R.id.direct_back_button);
        mAddView = mView.findViewById(R.id.direct_add_button);
        mUserName = mView.findViewById(R.id.direct_user_name);
        mDirectView = mView.findViewById(R.id.direct_container);
        mSearchLayout = mView.findViewById(R.id.direct_search_layout);
        mCameraLayout = mView.findViewById(R.id.direct_camera_layout);
        mDirectNotLayout = mView.findViewById(R.id.direct_not_layout);
        mLoadingAnim = mView.findViewById(R.id.direct_loading_anim);
        mRefreshLayout = mView.findViewById(R.id.direct_refresh_layout);
    }

    @Override
    public void onPause() {
        super.onPause();
        isOpenDirect = false;
    }
}
