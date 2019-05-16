package com.example.ibrhm.instagramclone.Search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.algolia.instantsearch.helpers.InstantSearch;
import com.algolia.instantsearch.helpers.Searcher;
import com.algolia.instantsearch.ui.views.Hits;
import com.algolia.instantsearch.utils.ItemClickSupport;
import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.ibrhm.instagramclone.Utils.RecentAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgoliaSearchActivity extends AppCompatActivity {

    private static int ACTIVITY_NO = 1;
    private static String TAG = "AlgoliaSearchActivity";

    private static final String ALGOLIA_APP_ID = "97L373GJAO";
    private static final String ALGOLIA_SEARCH_API_KEY = "3b0a7c2bfd12a819b20c24fc636a0746";
    private static final String ALGOLIA_INDEX_NAME = "InstagramClone";

    private ImageView mBackView;
    private BottomNavigationView mBottomNavigationView;
    private Searcher searcher;
    private Hits mSearchView;
    private EditText mSearchInput;

    private List<String> mRecentList;
    private RecyclerView mRecentView;
    private RecentAdapter mRecentAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_algolia_search);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        setupView();
        onBack();
        setupAlgoliaSearch();
        getRecent();
    }

    private void getRecent(){
        mRecentList = new ArrayList<>();
        mRecentAdapter = new RecentAdapter(this, mRecentList);
        mRecentView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecentView.setHasFixedSize(true);
        mRecentView.setAdapter(mRecentAdapter);

        mRootRef.child("Recent").child(mCurrentUser.getUid()).orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRecentList.clear();
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String uid = ds.getKey();
                        mRecentList.add(uid);
                    }
                    descending();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void descending(){
        Collections.reverse(mRecentList);

        mRecentAdapter.notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupAlgoliaSearch(){
        searcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY, ALGOLIA_INDEX_NAME);

        final InstantSearch helper = new InstantSearch(this, searcher);
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mSearchInput.getText().length() > 0){
                    mRecentView.setVisibility(View.GONE);
                    mSearchView.setVisibility(View.VISIBLE);
                    mSearchInput.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_clear_icon,0);
                    helper.search(mSearchInput.getText().toString());
                }else{
                    mRecentView.setVisibility(View.VISIBLE);
                    mSearchView.setVisibility(View.GONE);
                    mSearchInput.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                    helper.reset();
                    mSearchView.clear();
                }
            }
        });

        mSearchInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP && mSearchInput.getText().length() > 0) {
                    if(motionEvent.getRawX() >= (mSearchInput.getRight() - mSearchInput.getCompoundDrawables()[2].getBounds().width())) {
                        mSearchInput.getText().clear();
                    }
                }
                return false;
            }
        });

        mSearchView.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, int i, View view) {
                try {
                    String searchUID = mSearchView.get(i).getString("user_id");

                    if(searchUID.equals(mCurrentUser.getUid())){
                        Intent profileIntent = new Intent(AlgoliaSearchActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                    }else{
                        Intent userProfileIntent = new Intent(AlgoliaSearchActivity.this, UserProfileActivity.class);
                        userProfileIntent.putExtra("activity_no", 1);
                        userProfileIntent.putExtra("user_id", searchUID);
                        startActivity(userProfileIntent);
                    }

                    Map recentMap = new HashMap();
                    recentMap.put("Recent/"+mCurrentUser.getUid()+"/"+searchUID+"/user_id", searchUID);
                    recentMap.put("Recent/"+mCurrentUser.getUid()+"/"+searchUID+"/time", ServerValue.TIMESTAMP);

                    mRootRef.updateChildren(recentMap);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onBack(){
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlgoliaSearchActivity.super.onBackPressed();
            }
        });
    }

    private void setupNavigationView(){
        BottomNavigationViewHelper.SetupNavigation(this, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NO);
        menuItem.setChecked(true);
    }

    private void setupView(){
        mBottomNavigationView = findViewById(R.id.searchAlgoliaBottomNavigationView);
        mBackView = findViewById(R.id.search_back);
        mSearchView = findViewById(R.id.hits_container);
        mSearchInput = findViewById(R.id.search_input);
        mRecentView = findViewById(R.id.recent_container);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupNavigationView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searcher.destroy();
    }
}
