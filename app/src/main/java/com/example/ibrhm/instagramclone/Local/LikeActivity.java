package com.example.ibrhm.instagramclone.Local;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.ibrhm.instagramclone.Utils.UsersAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LikeActivity extends AppCompatActivity {

    private static int ACTIVITY_NO = 0;

    private ImageView mBackView;
    private BottomNavigationView mBottomNavigationView;
    private LottieAnimationView mLoadingAnim;

    private String post_user_id, post_id;

    private List<String> mLikeList;
    private RecyclerView mLikeView;
    private UsersAdapter mLikeAdapter;

    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);

        mLoadingAnim = findViewById(R.id.like_loading_anim);
        mLikeView = findViewById(R.id.likes_container);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        ACTIVITY_NO = getIntent().getIntExtra("activity_no", 0);
        post_user_id = getIntent().getStringExtra("post_user_id");
        post_id = getIntent().getStringExtra("post_id");

        onBack();

        mLoadingAnim.setVisibility(View.VISIBLE);
        mLoadingAnim.playAnimation();
        mLikeView.setVisibility(View.GONE);
        getLikes();
    }

    private void getLikes(){
        mLikeList = new ArrayList<>();
        mLikeAdapter = new UsersAdapter(this, mLikeList, ACTIVITY_NO, false);
        mLikeView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mLikeView.setHasFixedSize(true);
        mLikeView.setAdapter(mLikeAdapter);

        mRootRef.child("Posts").child(post_user_id).child(post_id).child("likes").orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mLikeList.clear();
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String uid = ds.getKey();
                        mLikeList.add(uid);
                    }

                    Collections.reverse(mLikeList);
                    mLikeAdapter.notifyDataSetChanged();
                }

                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingAnim.setVisibility(View.GONE);
                        mLoadingAnim.pauseAnimation();
                        mLikeView.setVisibility(View.VISIBLE);
                    }
                }, 1200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupNavigationView();
    }

    private void onBack(){
        mBackView = findViewById(R.id.likes_back_button);

        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikeActivity.super.onBackPressed();
            }
        });
    }

    private void setupNavigationView(){
        mBottomNavigationView = findViewById(R.id.likes_bottom_view);

        BottomNavigationViewHelper.SetupNavigation(this, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NO);
        menuItem.setChecked(true);
    }
}
