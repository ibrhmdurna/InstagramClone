package com.example.ibrhm.instagramclone.Home;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.example.ibrhm.instagramclone.Local.CommentActivity;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.ibrhm.instagramclone.Utils.PostsAdapter;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.CenterLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoanganhtuan95ptit.autoplayvideorecyclerview.AutoPlayVideoRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static int ACTIVITY_NO = 0;

    private View mView;
    private BottomNavigationView mBottomNavigationView;
    private ImageView mCameraView, mDirectView;
    private ViewPager mViewPager;
    private PullRefreshLayout mRefreshLayout;
    private RelativeLayout mDirectFillView;
    private TextView mDirectCount;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private List<UserPosts> mFirstPostsList;
    private List<UserPosts> mPostsList;
    private List<String> mPostListKey;
    private AutoPlayVideoRecyclerView mPostsView;
    private PostsAdapter mPostsAdapter;

    private static int POST_COUNT = 10;
    private static int PAGE_NUMBER = 1;

    private static boolean theEndPosts = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        mPostsView = mView.findViewById(R.id.posts_container);
        mViewPager = getActivity().findViewById(R.id.homeViewPager);
        mRefreshLayout = mView.findViewById(R.id.post_refresh_layout);
        mDirectFillView = mView.findViewById(R.id.imgDirectFill);
        mDirectCount = mView.findViewById(R.id.directCount);

        mFirstPostsList = new ArrayList<>();
        mPostsList = new ArrayList<>();
        mPostListKey = new ArrayList<>();

        mPostsAdapter = new PostsAdapter(mFirstPostsList, getActivity(), 0);
        mPostsView.setHasFixedSize(true);
        mPostsView.setAdapter(mPostsAdapter);
        mPostsView.setLayoutManager(new CenterLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        getCamera();
        getDirect();
        getFollowingPosts();

        mRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                theEndPosts = false;
                mPostsList.clear();
                mFirstPostsList.clear();
                getPosts();
            }
        });

        mPostsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                CenterLayoutManager layoutManager = (CenterLayoutManager) mPostsView.getLayoutManager();

                assert layoutManager != null;
                if(dy > 0 && layoutManager.findLastVisibleItemPosition() == mPostsView.getAdapter().getItemCount() - 1){
                    if(!theEndPosts)
                        addNewPosts();
                }
            }
        });

        getDirectCount();

        return mView;
    }

    private void getDirectCount(){
        mRootRef.child("Chats").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int count = 0;
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        Log.e("Chat ", ds.getKey());
                        if(ds.child("seen").getValue().equals(false)){
                            count++;
                        }
                    }

                    if(count > 0){
                        mDirectFillView.setVisibility(View.VISIBLE);
                        mDirectView.setVisibility(View.GONE);
                        mDirectCount.setText(count+"");
                    }
                    else{
                        mDirectFillView.setVisibility(View.GONE);
                        mDirectView.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    mDirectFillView.setVisibility(View.GONE);
                    mDirectView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNewPosts() {
        int getPostCount = PAGE_NUMBER * POST_COUNT;
        int limit = (++PAGE_NUMBER) * POST_COUNT;

        for(int i = getPostCount; i < limit; i++){
            if(mFirstPostsList.size() <= mPostsList.size() - 1){
                mFirstPostsList.add(mPostsList.get(i));
            }
            else{
                PAGE_NUMBER = 1;
                theEndPosts = true;
                break;
            }
        }

        mPostsAdapter.notifyDataSetChanged();
    }

    private void getFollowingPosts(){
        mPostListKey.add(mCurrentUser.getUid());

        mRootRef.child("Following").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        mPostListKey.add(ds.getKey());
                    }

                    getPosts();
                }
                else{
                    getPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPosts(){

        for (int i = 0; i < mPostListKey.size(); i++){
            final String user_id = mPostListKey.get(i);

            mRootRef.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final String user_name = dataSnapshot.getValue(Users.class).getUser_name();
                    final String profile_image = dataSnapshot.getValue(Users.class).getDetails().getProfile_image();

                    mRootRef.child("Posts").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            if(dataSnapshot2.exists()){
                                for(DataSnapshot ds : dataSnapshot2.getChildren()){
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

                                sortArrayList();
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
    }

    private void sortArrayList(){
        Collections.sort(mPostsList, new Comparator<UserPosts>() {
            @Override
            public int compare(UserPosts o1, UserPosts o2) {
                return Long.compare(o2.getTime(), o1.getTime());
            }
        });

        mFirstPostsList.clear();
        if(mPostsList.size() >= POST_COUNT){
            for(int i = 0; i < POST_COUNT; i++){
                mFirstPostsList.add(mPostsList.get(i));
            }
        }
        else {
            mFirstPostsList.addAll(mPostsList);
        }

        Log.e("Posts Size : ", mPostsList.size()+"");
        Log.e("First Size : ", mFirstPostsList.size()+"");

        mPostsAdapter.notifyDataSetChanged();
        mRefreshLayout.setRefreshing(false);
    }

    private void setupNavigationView(){
        mBottomNavigationView = mView.findViewById(R.id.bottomNavigationView);

        BottomNavigationViewHelper.SetupNavigation(getActivity(), mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NO);
        menuItem.setChecked(true);
    }

    private void getCamera(){
        mCameraView = mView.findViewById(R.id.imgCamera);

        mCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0, true);
                if(mPostsView.getHandingVideoHolder() != null){
                    mPostsView.getHandingVideoHolder().stopVideo();
                }
            }
        });
    }

    private void getDirect(){
        mDirectView = mView.findViewById(R.id.imgDirect);

        mDirectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(2, true);
                if(mPostsView.getHandingVideoHolder() != null){
                    mPostsView.getHandingVideoHolder().stopVideo();
                }
            }
        });

        mDirectFillView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(2, true);
                if(mPostsView.getHandingVideoHolder() != null){
                    mPostsView.getHandingVideoHolder().stopVideo();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupNavigationView();
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().playVideo();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().stopVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mPostsView.getHandingVideoHolder() != null){
            mPostsView.getHandingVideoHolder().stopVideo();
        }
    }
}
