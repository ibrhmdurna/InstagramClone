package com.example.ibrhm.instagramclone.Local;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.UsersAdapter;
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
public class FollowersFragment extends Fragment {

    private View mView;

    private ImageView mBackView;
    private LottieAnimationView mLoadingAnim;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private List<String> mFollowersList;
    private RecyclerView mFollowersView;
    private UsersAdapter mFollowersAdapter;

    private static int ACTIVITY_NO = 0;
    private String uid;

    public FollowersFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public FollowersFragment(String uid, int activityNo){
        ACTIVITY_NO = activityNo;
        this.uid = uid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_followers, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        setupView();
        onBack();

        mLoadingAnim.setVisibility(View.VISIBLE);
        mLoadingAnim.playAnimation();
        mFollowersView.setVisibility(View.GONE);
        getFollowers();

        return mView;
    }

    private void getFollowers(){
        mFollowersList = new ArrayList<>();
        mFollowersAdapter = new UsersAdapter(getActivity(), mFollowersList, ACTIVITY_NO, true);
        mFollowersView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mFollowersView.setHasFixedSize(true);
        mFollowersView.setAdapter(mFollowersAdapter);

        mRootRef.child("Followers").child(uid).orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mFollowersList.clear();
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String uid = ds.getKey();
                        mFollowersList.add(uid);
                    }

                    Collections.reverse(mFollowersList);
                    mFollowersAdapter.notifyDataSetChanged();

                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mLoadingAnim.setVisibility(View.GONE);
                            mLoadingAnim.pauseAnimation();
                            mFollowersView.setVisibility(View.VISIBLE);
                        }
                    }, 1200);
                }
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
        mBackView = mView.findViewById(R.id.followers_back_button);
        mFollowersView = mView.findViewById(R.id.followers_container);
        mLoadingAnim = mView.findViewById(R.id.followers_loading_anim);
    }
}
