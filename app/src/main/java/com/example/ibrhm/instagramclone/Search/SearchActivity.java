package com.example.ibrhm.instagramclone.Search;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ibrhm.instagramclone.Local.PostActivity;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.ibrhm.instagramclone.Utils.ExploreAdapter;
import com.example.ibrhm.instagramclone.Utils.ProfilePostsAdapter;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
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

public class SearchActivity extends AppCompatActivity {

    private static int ACTIVITY_NO = 1;
    private static String TAG = "SearchActivity";

    private BottomNavigationView mBottomNavigationView;
    private Toolbar mSearchToolbar;
    private LottieAnimationView mLoadingAnim;

    private List<String> mFollowingList;
    private List<UserPosts> mAllPostsList;
    private ArrayList[] mLayoutList;
    private AutoPlayVideoRecyclerView mExploreView;
    private ExploreAdapter mExploreAdapter;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private boolean isBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mFollowingList = new ArrayList<>();
        mAllPostsList = new ArrayList<>();

        mSearchToolbar = findViewById(R.id.search_toolbar);
        mExploreView = findViewById(R.id.explore_container);
        mLoadingAnim = findViewById(R.id.explore_loading_anim);

        onSearchAlgolia();

        isBackground = App.background.isThereBackground("Explore");
        getMyFollowing();
    }

    private void getMyFollowing(){
        if(!isBackground){
            mLoadingAnim.setVisibility(View.VISIBLE);
            mLoadingAnim.playAnimation();
            mExploreView.setVisibility(View.GONE);
        }

        mRootRef.child("Following").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        mFollowingList.add(ds.getKey());
                    }
                    getMyFollowingGetFollowing();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(!isBackground){
                    mLoadingAnim.setVisibility(View.GONE);
                    mLoadingAnim.pauseAnimation();
                }
                Toast.makeText(getApplicationContext(), "Couldn't refresh feed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMyFollowingGetFollowing() {
        final int followingCount = mFollowingList.size() -1;
        for(int i = 0; i < mFollowingList.size(); i++){
            final int finalI = i;
            mRootRef.child("Following").child(mFollowingList.get(i)).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int userTotalFollowingCount = (int) dataSnapshot.getChildrenCount();
                        int totalUserCount = mFollowingList.size() + userTotalFollowingCount;

                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            if(!mFollowingList.contains(ds.getKey())){
                                mFollowingList.add(ds.getKey());
                            }
                            else{
                                totalUserCount--;
                            }
                        }

                        if(totalUserCount == mFollowingList.size() && finalI == followingCount){
                            getExplore();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if(!isBackground){
                        mLoadingAnim.setVisibility(View.GONE);
                        mLoadingAnim.pauseAnimation();
                    }
                    Toast.makeText(getApplicationContext(), "Couldn't refresh feed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getExplore() {

        mFollowingList.remove(mCurrentUser.getUid());
        final int[] totalPostCount = {0};

        for(int i = 0; i < mFollowingList.size(); i++){
            mExploreView.removeAllViews();
            final int finalI = i;
            mRootRef.child("Users").child(mFollowingList.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        final Users user = dataSnapshot.getValue(Users.class);

                        mRootRef.child("Posts").child(user.getUser_id()).orderByChild("time").limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    int userPostCount = (int) dataSnapshot.getChildrenCount();
                                    totalPostCount[0] += userPostCount;

                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        Posts post = ds.getValue(Posts.class);
                                        UserPosts userPosts = new UserPosts();
                                        userPosts.setUser_id(user.getUser_id());
                                        userPosts.setProfile_image(user.getDetails().getProfile_image());
                                        userPosts.setUser_name(user.getUser_name());
                                        userPosts.setType(post.getType());
                                        userPosts.setThumb_image(post.getThumb_image());
                                        userPosts.setPost_id(post.getPost_id());
                                        userPosts.setPost_url(post.getPost_url());
                                        userPosts.setCaption(post.getCaption());
                                        userPosts.setTime(post.getTime());
                                        userPosts.setLikes(post.getLikes());

                                        mAllPostsList.add(userPosts);
                                    }

                                    if(mAllPostsList.size() == totalPostCount[0] && finalI == mFollowingList.size() - 1){
                                        Collections.sort(mAllPostsList, new Comparator<UserPosts>() {
                                            @Override
                                            public int compare(UserPosts o1, UserPosts o2) {
                                                return Long.compare(o2.getTime(), o1.getTime());
                                            }
                                        });

                                        int layoutCount;

                                        if(mAllPostsList.size() % 9 == 0){
                                            layoutCount = (mAllPostsList.size() / 9);
                                        }
                                        else{
                                            if((mAllPostsList.size() % 9) < 3){
                                                layoutCount = (mAllPostsList.size() / 9);
                                            }
                                            else{
                                                layoutCount = (mAllPostsList.size() / 9) + 1;
                                            }
                                        }

                                        mLayoutList = new ArrayList[layoutCount];

                                        for(int i = 0; i < mLayoutList.length; i++){
                                            mLayoutList[i] = new ArrayList<UserPosts>();
                                        }

                                        Log.e("Post Size", mAllPostsList.size()+"");
                                        Log.e("Layout Length", mLayoutList.length+"");

                                        int count = 1;

                                        for(int i = 0; i < mAllPostsList.size(); i++){
                                            if(i < count * 9){
                                                mLayoutList[count - 1].add(mAllPostsList.get(i));
                                            }
                                            else{
                                                int result = mAllPostsList.size() - (count * 9);
                                                if(result > 2){
                                                    count++;
                                                    mLayoutList[count - 1].add(mAllPostsList.get(i));
                                                }
                                            }
                                        }

                                        setupExplore();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                if(!isBackground){
                                    mLoadingAnim.setVisibility(View.GONE);
                                    mLoadingAnim.pauseAnimation();
                                }
                                Toast.makeText(getApplicationContext(), "Couldn't refresh feed", Toast.LENGTH_SHORT).show();
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

    private void setupExplore(){
        mExploreView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mExploreAdapter = new ExploreAdapter(mLayoutList, this);
        mExploreView.setHasFixedSize(true);
        mExploreView.setAdapter(mExploreAdapter);

        if(!isBackground){
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLoadingAnim.setVisibility(View.GONE);
                    mLoadingAnim.pauseAnimation();
                    mExploreView.setVisibility(View.VISIBLE);
                }
            },1000);
            App.background.getBackgroundList().add("Explore");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNavigationView();
        if(mExploreView.getHandingVideoHolder() != null){
            mExploreView.getHandingVideoHolder().playVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mExploreView.getHandingVideoHolder() != null){
            mExploreView.getHandingVideoHolder().stopVideo();
        }
    }

    private void onSearchAlgolia(){
        mSearchToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent algoliaIntent = new Intent(SearchActivity.this, AlgoliaSearchActivity.class);
                startActivity(algoliaIntent);
            }
        });
    }

    private void setupNavigationView(){
        mBottomNavigationView = findViewById(R.id.searchBottomNavigationView);

        BottomNavigationViewHelper.SetupNavigation(this, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NO);
        menuItem.setChecked(true);
    }
}
