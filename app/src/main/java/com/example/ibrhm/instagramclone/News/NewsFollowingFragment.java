package com.example.ibrhm.instagramclone.News;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.baoyz.widget.PullRefreshLayout;
import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Interactions;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.InteractionsExtraAdapter;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFollowingFragment extends Fragment {

    private View mView;

    private List<Interactions> mIntList;
    private RecyclerView mIntView;
    private InteractionsExtraAdapter mIntAdapter;
    private PullRefreshLayout mRefreshLayout;
    private LottieAnimationView mLoadingAnim;
    private LinearLayout mNewsNotLayout;

    private List<String> mFollowingList;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    private boolean isBackground = false;

    public NewsFollowingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_news_following, container, false);

        isBackground = App.background.isThereBackground("News_Following");

        setupView();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        if(!isBackground){
            mLoadingAnim.setVisibility(View.VISIBLE);
            mLoadingAnim.playAnimation();
            mIntView.setVisibility(View.GONE);
            App.background.getBackgroundList().add("News_Following");
        }

        getFollowingInteractions();

        mRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mIntView.setVisibility(View.GONE);
                getFollowingInteractions();
            }
        });

        return mView;
    }

    private void getFollowingInteractions(){
        mFollowingList = new ArrayList<>();
        mIntList = new ArrayList<>();
        mIntAdapter = new InteractionsExtraAdapter(getActivity(), mIntList);
        mIntView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mIntView.setHasFixedSize(true);
        mIntView.setAdapter(mIntAdapter);

        getFollowing();

    }

    private void getFollowing(){
        mRootRef.child("Following").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mNewsNotLayout.setVisibility(View.GONE);
                    mFollowingList.clear();
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        mFollowingList.add(ds.getKey());
                    }

                    Collections.reverse(mFollowingList);
                    if(mFollowingList.size() > 0){
                        for(int i = 0; i < mFollowingList.size(); i++){
                            mRootRef.child("News").child("All_users").child("Interactions").child(mFollowingList.get(i)).orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                                            Interactions interaction = ds.getValue(Interactions.class);
                                            assert interaction != null;
                                            if(!interaction.getPost_user_id().equals(mCurrentUser.getUid())){
                                                mIntList.add(interaction);
                                            }
                                        }

                                        sortArrayList();

                                        for(int i = 0; i < mIntList.size(); i++){
                                            Log.e("["+i+"] ", mIntList.get(i).getUser_id());
                                        }
                                    }

                                    if(!isBackground){
                                        Handler h = new Handler();
                                        h.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mIntView.setVisibility(View.VISIBLE);
                                                mLoadingAnim.pauseAnimation();
                                                mLoadingAnim.setVisibility(View.GONE);
                                                mRefreshLayout.setEnabled(true);
                                                mRefreshLayout.setRefreshing(false);
                                            }
                                        },1500);
                                    }
                                    else{
                                        Handler h = new Handler();
                                        h.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mIntView.setVisibility(View.VISIBLE);
                                                mRefreshLayout.setRefreshing(false);
                                            }
                                        }, 1500);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
                else{
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIntView.setVisibility(View.VISIBLE);
                            mLoadingAnim.pauseAnimation();
                            mLoadingAnim.setVisibility(View.GONE);
                            mRefreshLayout.setEnabled(true);
                            mNewsNotLayout.setVisibility(View.VISIBLE);
                            mRefreshLayout.setRefreshing(false);
                        }
                    },1500);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mIntAdapter.setOnItemClickListener(new InteractionsExtraAdapter.OnItemClickListener() {
            @Override
            public void onItemLikeClick(int position) {
                Intent likeIntent = new Intent(getContext(), LikesInteractionActivity.class);
                likeIntent.putExtra("interaction", mIntList.get(position));
                startActivity(likeIntent);
            }

            @Override
            public void onItemFollowClick(int position) {
                Intent profileIntent = new Intent(getContext(), UserProfileActivity.class);
                profileIntent.putExtra("activity_no", 3);
                profileIntent.putExtra("user_id", mIntList.get(position).getUser_id());
                startActivity(profileIntent);
            }
        });
    }

    private void sortArrayList(){
        Collections.sort(mIntList, new Comparator<Interactions>() {
            @Override
            public int compare(Interactions o1, Interactions o2) {
                return Long.compare(o2.getTime(), o1.getTime());
            }
        });


        mIntAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setupView(){
        mIntView = mView.findViewById(R.id.news_following_container);
        mRefreshLayout = mView.findViewById(R.id.news_following_refresh_layout);
        mLoadingAnim = mView.findViewById(R.id.news_following_loading_anim);
        mNewsNotLayout = mView.findViewById(R.id.news_following_not_layout);
    }
}
