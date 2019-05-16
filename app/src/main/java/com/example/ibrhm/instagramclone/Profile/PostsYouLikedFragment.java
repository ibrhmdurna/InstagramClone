package com.example.ibrhm.instagramclone.Profile;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.baoyz.widget.PullRefreshLayout;
import com.example.ibrhm.instagramclone.Local.PostActivity;
import com.example.ibrhm.instagramclone.Models.Posts;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.ProfilePostsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostsYouLikedFragment extends Fragment {

    private View mView;

    private ImageView mBackView;
    private LottieAnimationView mLoadingAnim;
    private LinearLayout mNotPostsLayout;
    private PullRefreshLayout mRefreshLayout;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private List<UserPosts> mLikedList;
    private RecyclerView mLikedView;
    private ProfilePostsAdapter mLikedAdapter;


    public PostsYouLikedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_posts_you_liked, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        setupView();
        onBack();

        mLoadingAnim.setVisibility(View.VISIBLE);
        mLoadingAnim.playAnimation();
        mLikedView.setVisibility(View.GONE);
        mNotPostsLayout.setVisibility(View.GONE);
        mRefreshLayout.setEnabled(false);
        getPostsLiked();

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mLikedView.getAdapter().getItemCount() > 0){
                    mRefreshLayout.setEnabled(true);
                    mRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            getPostsLiked();
                        }
                    });
                }
            }
        }, 1550);

        return mView;
    }

    private void getPostsLiked(){
        mLikedList = new ArrayList<>();
        mLikedAdapter = new ProfilePostsAdapter(mLikedList, getActivity());
        mLikedView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mLikedView.setHasFixedSize(true);
        mLikedView.setAdapter(mLikedAdapter);

        final int[] liked_count = {0};

        mRootRef.child("Posts_liked").child(mCurrentUser.getUid()).orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    liked_count[0] = (int) dataSnapshot.getChildrenCount();
                    for(final DataSnapshot ds : dataSnapshot.getChildren()){
                        mRootRef.child("Posts").orderByChild(ds.getKey()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                if(dataSnapshot2.exists()){
                                    mLikedList.clear();
                                    for(DataSnapshot ds2 : dataSnapshot2.getChildren()){
                                        final Posts posts = ds2.child(ds.getKey()).getValue(Posts.class);

                                        mRootRef.child("Users").child(ds2.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    Users user = dataSnapshot.getValue(Users.class);

                                                    UserPosts userPosts = new UserPosts(user.getDetails().getProfile_image(),
                                                                                        user.getUser_id(),
                                                                                        user.getUser_name(),
                                                                                        posts.getCaption(),
                                                                                        posts.getPost_url(),
                                                                                        posts.getPost_id(),
                                                                                        posts.getType(),
                                                                                        posts.getThumb_image(),
                                                                                        posts.getTime(),
                                                                                        posts.getLikes());

                                                    mLikedList.add(userPosts);
                                                    if(liked_count[0] == mLikedList.size()){
                                                        Collections.reverse(mLikedList);
                                                        mLikedAdapter.notifyDataSetChanged();

                                                        mLikedAdapter.setOnItemClickListener(new ProfilePostsAdapter.OnItemClickListener() {
                                                            @Override
                                                            public void onItemClick(int position) {
                                                                Intent postIntent = new Intent(getContext(), PostActivity.class);
                                                                postIntent.putExtra("activity_no", 4);
                                                                postIntent.putExtra("user_post", mLikedList.get(position));
                                                                startActivity(postIntent);
                                                            }
                                                        });

                                                        Handler h = new Handler();
                                                        h.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mLoadingAnim.setVisibility(View.GONE);
                                                                mLoadingAnim.pauseAnimation();
                                                                mLikedView.setVisibility(View.VISIBLE);
                                                            }
                                                        }, 1500);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }else{
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mLoadingAnim.setVisibility(View.GONE);
                            mLoadingAnim.pauseAnimation();
                            mLikedView.setVisibility(View.GONE);
                            mNotPostsLayout.setVisibility(View.VISIBLE);
                        }
                    }, 800);
                }

                mRefreshLayout.setRefreshing(false);
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
                getFragmentManager().popBackStack();
            }
        });
    }

    private void setupView(){
        mBackView = mView.findViewById(R.id.posts_liked_back_button);
        mLikedView = mView.findViewById(R.id.posts_liked_container);
        mLoadingAnim = mView.findViewById(R.id.posts_liked_loading_anim);
        mNotPostsLayout = mView.findViewById(R.id.not_posts_layout);
        mRefreshLayout = mView.findViewById(R.id.posts_liked_refresh_layout);
    }
}
