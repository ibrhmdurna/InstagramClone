package com.example.ibrhm.instagramclone.Local;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ibrhm.instagramclone.Home.ChatActivity;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;
import com.example.ibrhm.instagramclone.Utils.PostsAdapter;
import com.example.ibrhm.instagramclone.Utils.ProfilePostsAdapter;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.CenterLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hoanganhtuan95ptit.autoplayvideorecyclerview.AutoPlayVideoRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserProfileFragment extends Fragment {

    private static int ACTIVITY_NO = 0;

    private View mView;

    private ImageView mBackView;

    private CircleImageView mProfileImage;
    private TextView mUserName, mFullName, mBio, mWebSite;
    private TextView mPost, mFollowers, mFollowing;

    private ImageView mGridView, mVerticalView, mTagView;

    private AutoPlayVideoRecyclerView mPostsView;
    private List<UserPosts> mPostsList;
    private ProfilePostsAdapter mPostsAdapter;
    private PostsAdapter mPostsVerticalAdapter;
    private LinearLayout mPostsLayout, mPostsNotFoundLayout, mPrivateLayout, mReqLayout, mUserInfoLayout;
    private TextView mPostNotBody, mReqTitle;
    private TextView mReqConfirmBtn, mReqDeleteBtn;
    private LottieAnimationView mLoadingAnim;
    private LinearLayout mFollowersLayout, mFollowingLayout;

    private TextView mFollowBtn;
    private TextView mReqCancelBtn;
    private TextView mMessageBtn;
    private LinearLayout mUnFollowBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private String user_id;

    private boolean isPrivateAccount = false;
    private boolean isFollow = false;
    private boolean isBackground = false;

    public UserProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        setupView();
        onBack();

        ACTIVITY_NO = getActivity().getIntent().getIntExtra("activity_no", 0);

        if(!user_id.equals("not_found")){
            isBackground = App.background.isThereBackground("User_Profile");

            if(!isBackground){
                mUserInfoLayout.setVisibility(View.GONE);
                mPostsView.setVisibility(View.GONE);
                mLoadingAnim.setVisibility(View.VISIBLE);
                mLoadingAnim.playAnimation();
                App.background.getBackgroundList().add("User_Profile");
            }

            mPostsList = new ArrayList<>();
            getData();
            selectGrid();
            getPosts(mPostsAdapter);

            mGridView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mGridView.isSelected()){
                        selectGrid();
                    }
                }
            });

            mVerticalView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mVerticalView.isSelected()){
                        selectVertical();
                    }
                }
            });

            mTagView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mTagView.isSelected()){
                        selectTag();
                    }
                }
            });

            setupFollow();
            setupUnFollow();
            setupUnReq();
            isFollowReqState();

            mMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                    chatIntent.putExtra("user_id", user_id);
                    startActivity(chatIntent);
                }
            });

            setupFollowers();
            setupFollowing();
        }
        else{
            mUserInfoLayout.setVisibility(View.GONE);
            mPostsView.setVisibility(View.GONE);

            mUserName.setText("Instagramer");
        }

        return mView;
    }

    private void setupFollowers(){
        mFollowersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.user_profile_container, new FollowersFragment(user_id, ACTIVITY_NO));
                transaction.addToBackStack("Followers");
                transaction.commit();
            }
        });
    }

    private void setupFollowing(){
        mFollowingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.user_profile_container, new FollowingFragment(user_id, ACTIVITY_NO));
                transaction.addToBackStack("Following");
                transaction.commit();
            }
        });
    }

    private void setupReqConfirm(){
        mReqConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map followReqMap = new HashMap();
                followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", null);
                followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/time", null);
                followReqMap.put("Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", null);
                mRootRef.updateChildren(followReqMap);

                String currentDate = java.text.DateFormat.getDateInstance().format(new Date());
                Map followMap = new HashMap();
                followMap.put("Followers/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                followMap.put("Followers/"+mCurrentUser.getUid()+"/"+user_id+"/date", currentDate);
                followMap.put("Following/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                followMap.put("Following/"+user_id+"/"+mCurrentUser.getUid()+"/date", currentDate);
                mRootRef.updateChildren(followMap);

                Map newsMap = new HashMap();
                newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+user_id+"/date", currentDate);
                newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+user_id+"/time", ServerValue.TIMESTAMP);
                mRootRef.updateChildren(newsMap);

                String news_key = mRootRef.child("News").child("All_users").child("Interactions").child(user_id).push().getKey();
                Map allNewsMap = new HashMap();
                allNewsMap.put("News/All_users/Interactions/"+user_id+"/"+news_key+"/action", "following");
                allNewsMap.put("News/All_users/Interactions/"+user_id+"/"+news_key+"/time", ServerValue.TIMESTAMP);
                allNewsMap.put("News/All_users/Interactions/"+user_id+"/"+news_key+"/content", "");
                allNewsMap.put("News/All_users/Interactions/"+user_id+"/"+news_key+"/post_id", "");
                allNewsMap.put("News/All_users/Interactions/"+user_id+"/"+news_key+"/post_user_id", mCurrentUser.getUid());
                allNewsMap.put("News/All_users/Interactions/"+user_id+"/"+news_key+"/user_id", user_id);
                mRootRef.updateChildren(allNewsMap);

                mFollowBtn.setVisibility(View.GONE);
                mMessageBtn.setVisibility(View.VISIBLE);
                mUnFollowBtn.setVisibility(View.VISIBLE);
                mReqLayout.setVisibility(View.GONE);
                getPrivateData();
            }
        });
    }

    private void setupReqDelete(){
        mReqDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map followReqMap = new HashMap();
                followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", null);
                followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/time", null);
                followReqMap.put("Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", null);
                mRootRef.updateChildren(followReqMap);
                mReqLayout.setVisibility(View.GONE);
            }
        });
    }

    private void isFollowReqState(){
        mRootRef.child("Follow_req").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild(mCurrentUser.getUid())){
                        mReqLayout.setVisibility(View.VISIBLE);
                        mReqTitle.setText(Html.fromHtml("<b>"+mUserName.getText()+"</b> wants to follow you"));
                        setupReqConfirm();
                        setupReqDelete();
                    }
                }
                else{
                    mReqLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isFollowing(){
        if(!isFollow){
            if(isPrivateAccount){
                mPrivateLayout.setVisibility(View.VISIBLE);
                mPostsLayout.setVisibility(View.GONE);
                mPostsNotFoundLayout.setVisibility(View.GONE);
                if(mPostsList != null){
                    mPostsList.clear();
                }
                if(mPostsView.getHandingVideoHolder() != null){
                    mPostsView.getHandingVideoHolder().stopVideo();
                }
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mUserInfoLayout.setVisibility(View.VISIBLE);
                        mPostsView.setVisibility(View.VISIBLE);
                        mLoadingAnim.setVisibility(View.GONE);
                        mLoadingAnim.pauseAnimation();
                    }
                }, 500);
            }
            else{
                mPrivateLayout.setVisibility(View.GONE);
                selectGrid();
                if(mPostsView.getHandingVideoHolder() != null){
                    mPostsView.getHandingVideoHolder().playVideo();
                }
            }
        }
        else{
            mPrivateLayout.setVisibility(View.GONE);
            selectGrid();
            if(mPostsView.getHandingVideoHolder() != null){
                mPostsView.getHandingVideoHolder().playVideo();
            }
        }
    }

    private void setupFollow(){
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

                    mFollowBtn.setVisibility(View.GONE);
                    mMessageBtn.setVisibility(View.VISIBLE);
                    mUnFollowBtn.setVisibility(View.VISIBLE);
                    mReqCancelBtn.setVisibility(View.GONE);
                }
                else{
                    Map followReqMap = new HashMap();
                    followReqMap.put("News/Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                    followReqMap.put("News/Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);
                    followReqMap.put("Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                    mRootRef.updateChildren(followReqMap);
                    mFollowBtn.setVisibility(View.GONE);
                    mMessageBtn.setVisibility(View.GONE);
                    mUnFollowBtn.setVisibility(View.GONE);
                    mReqCancelBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupUnFollow(){
        mUnFollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUnFollow();
            }
        });
    }

    private void setupUnReq(){
        mReqCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map followReqMap = new HashMap();
                followReqMap.put("News/Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", null);
                followReqMap.put("News/Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/time", null);
                followReqMap.put("Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", null);
                mRootRef.updateChildren(followReqMap);
                mFollowBtn.setVisibility(View.VISIBLE);
                mMessageBtn.setVisibility(View.GONE);
                mUnFollowBtn.setVisibility(View.GONE);
                mReqCancelBtn.setVisibility(View.GONE);
                getPrivateData();
            }
        });
    }

    private void getFollowersCount(){
        mRootRef.child("Followers").child(user_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mFollowers.setText(dataSnapshot.getChildrenCount()+"");

                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        if(ds.getRef().getKey().equals(mCurrentUser.getUid())){
                            isPrivateAccount = false;
                            break;
                        }
                    }
                }
                else{
                    mFollowers.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount(){
        mRootRef.child("Following").child(user_id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mFollowing.setText(dataSnapshot.getChildrenCount()+"");
                }
                else{
                    mFollowing.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowState(){
        mRootRef.child("Followers").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user_id)){
                    // Beni takip ediyor
                    mRootRef.child("Following").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(user_id)){
                                // Takip ediyorum
                                isFollow = true;
                                mFollowBtn.setVisibility(View.GONE);
                                mMessageBtn.setVisibility(View.VISIBLE);
                                mUnFollowBtn.setVisibility(View.VISIBLE);
                            }else{
                                // Takip etmiyorum
                                isFollow = false;
                                mMessageBtn.setVisibility(View.GONE);
                                mUnFollowBtn.setVisibility(View.GONE);
                                mFollowBtn.setText(getString(R.string.follow_back));
                                mRootRef.child("Follow_req").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(user_id)){
                                            mFollowBtn.setVisibility(View.GONE);
                                            mReqCancelBtn.setVisibility(View.VISIBLE);
                                        }
                                        else{
                                            mFollowBtn.setVisibility(View.VISIBLE);
                                            mReqCancelBtn.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            isFollowing();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    // Beni takip etmiyor
                    mRootRef.child("Following").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(user_id)){
                                // Takip ediyorum
                                isFollow = true;
                                mFollowBtn.setVisibility(View.GONE);
                                mMessageBtn.setVisibility(View.VISIBLE);
                                mUnFollowBtn.setVisibility(View.VISIBLE);
                            }else{
                                // Takip etmiyorum
                                isFollow = false;
                                mMessageBtn.setVisibility(View.GONE);
                                mUnFollowBtn.setVisibility(View.GONE);
                                mFollowBtn.setText(getString(R.string.follow));
                                mRootRef.child("Follow_req").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(user_id)){
                                            mFollowBtn.setVisibility(View.GONE);
                                            mReqCancelBtn.setVisibility(View.VISIBLE);
                                        }
                                        else{
                                            mFollowBtn.setVisibility(View.VISIBLE);
                                            mReqCancelBtn.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            isFollowing();
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

    private void dialogUnFollow(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_un_follow, null);
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

                mFollowBtn.setVisibility(View.VISIBLE);
                mMessageBtn.setVisibility(View.GONE);
                mUnFollowBtn.setVisibility(View.GONE);
                getPrivateData();
            }
        });

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void selectGrid(){
        mGridView.setSelected(true);
        mVerticalView.setSelected(false);
        mTagView.setSelected(false);
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().stopVideo();
        }

        mPostsAdapter = new ProfilePostsAdapter(mPostsList, getContext());
        mPostsView.setAdapter(mPostsAdapter);
        mPostsView.setHasFixedSize(true);
        mPostsView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        mPostsAdapter.setOnItemClickListener(new ProfilePostsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent postIntent = new Intent(getContext(), PostActivity.class);
                postIntent.putExtra("activity_no", ACTIVITY_NO);
                postIntent.putExtra("user_post", mPostsList.get(position));
                startActivity(postIntent);
            }
        });
    }

    private void selectVertical(){
        mGridView.setSelected(false);
        mVerticalView.setSelected(true);
        mTagView.setSelected(false);
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().playVideo();
        }
        mPostsVerticalAdapter = new PostsAdapter(mPostsList, getActivity(), 0);
        mPostsView.setLayoutManager(new CenterLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mPostsView.setAdapter(mPostsVerticalAdapter);
        mPostsView.setHasFixedSize(true);
    }

    private void selectTag(){
        mGridView.setSelected(false);
        mVerticalView.setSelected(false);
        mTagView.setSelected(true);
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().stopVideo();
        }
    }

    private void getPosts(final RecyclerView.Adapter adapter){
        mRootRef.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String user_name = dataSnapshot.getValue(Users.class).getUser_name();
                final String profile_image = dataSnapshot.getValue(Users.class).getDetails().getProfile_image();

                mRootRef.child("Posts").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            mPostsLayout.setVisibility(View.VISIBLE);
                            mPostsNotFoundLayout.setVisibility(View.GONE);
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                UserPosts userPosts = new UserPosts();

                                userPosts.setUser_id(user_id);
                                userPosts.setUser_name(user_name);
                                userPosts.setProfile_image(profile_image);
                                userPosts.setPost_id(ds.getValue(Posts.class).getPost_id());
                                userPosts.setPost_url(ds.getValue(Posts.class).getPost_url());
                                userPosts.setType(ds.getValue(Posts.class).getType());
                                userPosts.setThumb_image(ds.getValue(Posts.class).getThumb_image());
                                userPosts.setCaption(ds.getValue(Posts.class).getCaption());
                                userPosts.setTime(ds.getValue(Posts.class).getTime());

                                mPostsList.add(userPosts);
                            }

                            sortArrayList(adapter);

                            mPost.setText(dataSnapshot.getChildrenCount()+"");
                        }else{
                            mPost.setText("0");
                            mPostsLayout.setVisibility(View.GONE);
                            mPostsNotFoundLayout.setVisibility(View.VISIBLE);
                            mPostNotBody.setText("When "+user_name+" posts, you'll see their photos and videos here.");
                        }

                        if(!isBackground){
                            Handler h = new Handler();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mUserInfoLayout.setVisibility(View.VISIBLE);
                                    mPostsView.setVisibility(View.VISIBLE);
                                    mLoadingAnim.setVisibility(View.GONE);
                                    mLoadingAnim.pauseAnimation();
                                }
                            }, 500);
                        }
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

    private void sortArrayList(RecyclerView.Adapter adapter){
        Collections.sort(mPostsList, new Comparator<UserPosts>() {
            @Override
            public int compare(UserPosts o1, UserPosts o2) {
                return Long.compare(o2.getTime(), o1.getTime());
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void getPrivateData(){
        mRootRef.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);

                isPrivateAccount = user.isPrivate_account();

                getFollowState();
                getFollowersCount();
                getFollowingCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getData(){
        mRootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);

                mUserName.setText(user.getUser_name());

                String imgUrl = user.getDetails().getProfile_image();
                UniversalImageLoader.setImage(imgUrl, mProfileImage, null, "");

                if(!user.getFull_name().equals("")){
                    mFullName.setText(user.getFull_name());
                    mFullName.setVisibility(View.VISIBLE);
                }
                else{
                    mFullName.setVisibility(View.GONE);
                }

                if(!user.getDetails().getBiography().equals("")){
                    mBio.setText(user.getDetails().getBiography());
                    mBio.setVisibility(View.VISIBLE);
                }
                else{
                    mBio.setVisibility(View.GONE);
                }

                if(!user.getDetails().getWeb_site().equals("")){
                    mWebSite.setText(user.getDetails().getWeb_site());
                    mWebSite.setVisibility(View.VISIBLE);
                }
                else{
                    mWebSite.setVisibility(View.GONE);
                }

                isPrivateAccount = user.isPrivate_account();

                getFollowState();
                getFollowersCount();
                getFollowingCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupView(){
        mProfileImage = mView.findViewById(R.id.user_profile_image);
        mUserName = mView.findViewById(R.id.userToolbarUserName);
        mFullName = mView.findViewById(R.id.user_profile_full_name);
        mBio = mView.findViewById(R.id.user_profile_biography);
        mWebSite = mView.findViewById(R.id.user_profile_website);
        mPost = mView.findViewById(R.id.user_posts_count);
        mFollowers = mView.findViewById(R.id.user_followers_count);
        mFollowing = mView.findViewById(R.id.user_following_count);
        mGridView = mView.findViewById(R.id.user_grid_view);
        mVerticalView = mView.findViewById(R.id.user_vertical_view);
        mTagView = mView.findViewById(R.id.user_tag_view);
        mPostsView = mView.findViewById(R.id.user_profile_posts_container);
        mPostsLayout = mView.findViewById(R.id.user_posts_layout);
        mPostsNotFoundLayout = mView.findViewById(R.id.user_posts_not_layout);
        mPostNotBody = mView.findViewById(R.id.user_not_posts_body);
        mFollowBtn = mView.findViewById(R.id.follow_btn);
        mMessageBtn = mView.findViewById(R.id.message_btn);
        mUnFollowBtn = mView.findViewById(R.id.un_follow_btn);
        mReqCancelBtn = mView.findViewById(R.id.req_cancel_btn);
        mPrivateLayout = mView.findViewById(R.id.user_private_account_layout);
        mReqLayout = mView.findViewById(R.id.profile_req_layout);
        mReqTitle = mView.findViewById(R.id.profile_req_title);
        mReqConfirmBtn = mView.findViewById(R.id.profile_req_confirm_btn);
        mReqDeleteBtn = mView.findViewById(R.id.profile_req_delete_btn);
        mUserInfoLayout = mView.findViewById(R.id.user_info_layout);
        mLoadingAnim = mView.findViewById(R.id.user_profile_loading_anim);
        mFollowersLayout = mView.findViewById(R.id.followers_layout);
        mFollowingLayout = mView.findViewById(R.id.following_layout);
    }

    @Subscribe(sticky = true)
    public void OnValueEvent(EventBusDataEvents.SendUID event){
        user_id = event.getUser_id();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void onBack(){
        mBackView = mView.findViewById(R.id.user_profile_back);
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().playVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().stopVideo();
        }
        for (int i = 0; i < App.background.getBackgroundList().size(); i++){
            if(App.background.getBackgroundList().get(i).equals("User_Profile")){
                App.background.getBackgroundList().remove(i);
                isBackground = false;
                break;
            }
        }
    }
}
