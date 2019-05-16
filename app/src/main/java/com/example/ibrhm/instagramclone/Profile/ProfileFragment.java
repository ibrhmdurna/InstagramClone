package com.example.ibrhm.instagramclone.Profile;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ibrhm.instagramclone.Local.FollowersFragment;
import com.example.ibrhm.instagramclone.Local.FollowingFragment;
import com.example.ibrhm.instagramclone.Local.PostActivity;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;
import com.example.ibrhm.instagramclone.Utils.PostsAdapter;
import com.example.ibrhm.instagramclone.Utils.ProfilePostsAdapter;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanganhtuan95ptit.autoplayvideorecyclerview.AutoPlayVideoRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private View mView;
    private ImageView mMoreOptions;
    private TextView mEditProfile;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    private TextView mToolbarUserName;
    private TextView mPost, mFollowers, mFollowing;
    private TextView mFullName, mBio, mWebSite;
    private CircleImageView mProfileImage;
    private LinearLayout mFollowersLayout, mFollowingLayout;

    private ImageView mGridView, mVerticalView, mTagView;

    private AutoPlayVideoRecyclerView mPostsView;
    private List<UserPosts> mPostsList;
    private ProfilePostsAdapter mPostsAdapter;
    private LinearLayout mPostsNotFoundLayout;
    private RelativeLayout mProfileInfoLayout;
    private LottieAnimationView mLoadingAnim;

    private PostsAdapter mPostsVerticalAdapter;

    private boolean isBackground = false;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.fragment_profile, container, false);

        isBackground = App.background.isThereBackground("Profile");

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        setupView();
        setupMoreOptions();
        setupEditProfile();

        mPostsList = new ArrayList<>();

        if(!isBackground){
            mProfileInfoLayout.setVisibility(View.GONE);
            mPostsNotFoundLayout.setVisibility(View.GONE);
            mPostsView.setVisibility(View.GONE);
            mLoadingAnim.setVisibility(View.VISIBLE);
            mLoadingAnim.playAnimation();
            App.background.getBackgroundList().add("Profile");

            selectGrid();
            getPosts(mPostsAdapter);
        }
        else{
            mPost.setText(App.backgroundProfile.getPostCount());
            mFollowers.setText(App.backgroundProfile.getFollowersCount());
            mFollowing.setText(App.backgroundProfile.getFollowingCount());

            if(App.backgroundProfile.getPostLayoutState().equals("Grid")){
                selectGrid();
                getPosts(mPostsAdapter);
            }
            else if (App.backgroundProfile.getPostLayoutState().equals("Vertical")){
                selectVertical();
                getPosts(mPostsVerticalAdapter);
            }
            else if(App.backgroundProfile.getPostLayoutState().equals("Tag")){
                selectTag();
            }
        }

        getUserInformation();

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

        setupFollowing();
        setupFollowers();

        return mView;
    }

    private void setupFollowers(){
        mFollowersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.profile_container, new FollowersFragment(mAuth.getCurrentUser().getUid(), 4));
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
                transaction.replace(R.id.profile_container, new FollowingFragment(mAuth.getCurrentUser().getUid(), 4));
                transaction.addToBackStack("Following");
                transaction.commit();
            }
        });
    }

    private void getFollowersCount(){
        mRootRef.child("Followers").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mFollowers.setText(dataSnapshot.getChildrenCount()+"");
                }
                else{
                    mFollowers.setText("0");
                }

                if(!isBackground){
                    App.backgroundProfile.setFollowersCount(mFollowers.getText().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount(){
        mRootRef.child("Following").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mFollowing.setText(dataSnapshot.getChildrenCount()+"");
                }
                else{
                    mFollowing.setText("0");
                }

                if(!isBackground){
                    App.backgroundProfile.setFollowingCount(mFollowing.getText().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        App.backgroundProfile.setPostLayoutState("Grid");

        mPostsAdapter.setOnItemClickListener(new ProfilePostsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent postIntent = new Intent(getContext(), PostActivity.class);
                postIntent.putExtra("activity_no", 4);
                postIntent.putExtra("user_post", mPostsList.get(position));
                startActivity(postIntent);
            }
        });
    }

    private void selectVertical(){
        mGridView.setSelected(false);
        mVerticalView.setSelected(true);
        mTagView.setSelected(false);
        mPostsVerticalAdapter = new PostsAdapter(mPostsList, getActivity(), 4);
        mPostsView.setAdapter(mPostsVerticalAdapter);
        mPostsView.setHasFixedSize(true);
        mPostsView.setLayoutManager(new LinearLayoutManager(getContext()));
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().playVideo();
        }
        App.backgroundProfile.setPostLayoutState("Vertical");
    }

    private void selectTag(){
        mGridView.setSelected(false);
        mVerticalView.setSelected(false);
        mTagView.setSelected(true);
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().stopVideo();
        }
        App.backgroundProfile.setPostLayoutState("Tag");
    }

    private void getPosts(final RecyclerView.Adapter adapter){
        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String user_name = dataSnapshot.getValue(Users.class).getUser_name();
                final String profile_image = dataSnapshot.getValue(Users.class).getDetails().getProfile_image();

                mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                UserPosts userPosts = new UserPosts();

                                userPosts.setUser_id(mAuth.getCurrentUser().getUid());
                                userPosts.setUser_name(user_name);
                                userPosts.setProfile_image(profile_image);
                                userPosts.setType(ds.getValue(Posts.class).getType());
                                userPosts.setThumb_image(ds.getValue(Posts.class).getThumb_image());
                                userPosts.setPost_id(ds.getValue(Posts.class).getPost_id());
                                userPosts.setPost_url(ds.getValue(Posts.class).getPost_url());
                                userPosts.setCaption(ds.getValue(Posts.class).getCaption());
                                userPosts.setTime(ds.getValue(Posts.class).getTime());

                                mPostsList.add(userPosts);
                            }

                            sortArrayList(adapter);
                            mPost.setText(dataSnapshot.getChildrenCount()+"");

                            if(!isBackground){
                                Handler h = new Handler();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPostsNotFoundLayout.setVisibility(View.GONE);
                                        mProfileInfoLayout.setVisibility(View.VISIBLE);
                                        mPostsView.setVisibility(View.VISIBLE);
                                        mLoadingAnim.setVisibility(View.GONE);
                                        mLoadingAnim.pauseAnimation();
                                    }
                                }, 1000);
                                if(!isBackground){
                                    App.backgroundProfile.setPostCount(mPost.getText().toString());
                                }
                            }
                        }else{
                            if(!isBackground){
                                Handler h = new Handler();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPostsNotFoundLayout.setVisibility(View.VISIBLE);
                                        mProfileInfoLayout.setVisibility(View.VISIBLE);
                                        mPostsView.setVisibility(View.VISIBLE);
                                        mLoadingAnim.setVisibility(View.GONE);
                                        mLoadingAnim.pauseAnimation();
                                    }
                                }, 1000);
                            }
                            else{
                                mPostsNotFoundLayout.setVisibility(View.VISIBLE);
                            }
                            mPost.setText("0");
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

    private void setupMoreOptions(){
        mMoreOptions = mView.findViewById(R.id.profile_settings);

        mMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.profile_container, new ProfileSettingsFragment());
                transaction.addToBackStack("Profile_Settings");
                transaction.commit();
                if(mPostsView.getHandingVideoHolder() != null){
                    mPostsView.getHandingVideoHolder().stopVideo();
                }
            }
        });
    }

    private void setupEditProfile(){
        mEditProfile = mView.findViewById(R.id.edit_profile);

        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.profile_container, new ProfileEditFragment());
                transaction.addToBackStack("Edit_Profile");
                transaction.commit();
                if(mPostsView.getHandingVideoHolder() != null){
                    mPostsView.getHandingVideoHolder().stopVideo();
                }
            }
        });
    }

    private void getUserInformation(){
        final String user_id = mAuth.getCurrentUser().getUid();

        mRootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Users user = dataSnapshot.getValue(Users.class);

                    EventBusDataEvents.SendUserInfo event = new EventBusDataEvents.SendUserInfo(user);
                    EventBus.getDefault().postSticky(event);

                    mToolbarUserName.setText(user.getUser_name());

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

                    getFollowingCount();
                    getFollowersCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupView(){
        mToolbarUserName = mView.findViewById(R.id.toolbarUserName);
        mPost = mView.findViewById(R.id.posts_count);
        mFollowers = mView.findViewById(R.id.followers_count);
        mFollowing = mView.findViewById(R.id.following_count);
        mFullName = mView.findViewById(R.id.profile_full_name);
        mBio = mView.findViewById(R.id.profile_biography);
        mWebSite = mView.findViewById(R.id.profile_website);
        mProfileImage = mView.findViewById(R.id.profile_image);
        mGridView = mView.findViewById(R.id.grid_view);
        mVerticalView = mView.findViewById(R.id.vertical_view);
        mTagView = mView.findViewById(R.id.tag_view);
        mPostsView = mView.findViewById(R.id.profile_posts_container);
        mPostsNotFoundLayout = mView.findViewById(R.id.posts_not_found_layout);
        mProfileInfoLayout = mView.findViewById(R.id.profile_info_layout);
        mLoadingAnim = mView.findViewById(R.id.profile_loading_anim);
        mFollowersLayout = mView.findViewById(R.id.followers_layout);
        mFollowingLayout = mView.findViewById(R.id.following_layout);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().stopVideo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().playVideo();
        }
    }
}
