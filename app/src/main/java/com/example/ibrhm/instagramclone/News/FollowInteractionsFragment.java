package com.example.ibrhm.instagramclone.News;


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
import com.example.ibrhm.instagramclone.Utils.RequestAdapter;
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
public class FollowInteractionsFragment extends Fragment {

    private View mView;
    private ImageView mBackView;
    private LottieAnimationView mLoadingAnim;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private RecyclerView mIntView;
    private List<String> mIntList;
    private RequestAdapter mIntAdapter;

    public FollowInteractionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_follow_interactions, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        setupView();
        onBack();

        mLoadingAnim.setVisibility(View.VISIBLE);
        mLoadingAnim.playAnimation();
        mIntView.setVisibility(View.GONE);

        return mView;
    }

    private void getFollowInteractions(){
        mIntList = new ArrayList<>();
        mIntAdapter = new RequestAdapter(getActivity(), mIntList, false);
        mIntView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mIntView.setHasFixedSize(true);
        mIntView.setAdapter(mIntAdapter);

        mRootRef.child("News").child("Follow_interactions").child(mCurrentUser.getUid()).orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mIntList.clear();
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        mIntList.add(ds.getKey());
                    }
                    Collections.reverse(mIntList);
                    mIntAdapter.notifyDataSetChanged();

                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mLoadingAnim.setVisibility(View.GONE);
                            mLoadingAnim.pauseAnimation();
                            mIntView.setVisibility(View.VISIBLE);
                        }
                    },1200);
                }
                else{
                    getActivity().getSupportFragmentManager().popBackStack();
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
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupView(){
        mBackView = mView.findViewById(R.id.f_interaction_back_button);
        mIntView = mView.findViewById(R.id.f_interaction_container);
        mLoadingAnim = mView.findViewById(R.id.f_interaction_loading_anim);
    }

    @Override
    public void onStart() {
        super.onStart();
        getFollowInteractions();
    }
}
