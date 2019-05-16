package com.example.ibrhm.instagramclone.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.algolia.instantsearch.helpers.InstantSearch;
import com.algolia.instantsearch.helpers.Searcher;
import com.algolia.instantsearch.ui.views.Hits;
import com.algolia.instantsearch.utils.ItemClickSupport;
import com.example.ibrhm.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

public class DirectSearchActivity extends AppCompatActivity {

    private static final String ALGOLIA_APP_ID = "97L373GJAO";
    private static final String ALGOLIA_SEARCH_API_KEY = "3b0a7c2bfd12a819b20c24fc636a0746";
    private static final String ALGOLIA_INDEX_NAME = "InstagramClone";

    private ImageView mBackView;
    private Searcher searcher;
    private Hits mSearchView;
    private EditText mSearchInput;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_search);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        setupView();
        onBack();
        search();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void search(){
        searcher = Searcher.create(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY, ALGOLIA_INDEX_NAME);
        final InstantSearch helper = new InstantSearch(this, searcher);
        helper.search();
        helper.enableProgressBar();
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
                    mSearchInput.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_clear_icon,0);
                    helper.search(mSearchInput.getText().toString());
                }else{
                    mSearchInput.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
                    searcher.reset();
                    helper.reset();
                    mSearchView.clear();
                    helper.search();
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
            public void onItemClick(RecyclerView recyclerView, int position, View v) {
                try {
                    String searchUID = mSearchView.get(position).getString("user_id");
                    Intent chatIntent = new Intent(DirectSearchActivity.this, ChatActivity.class);
                    chatIntent.putExtra("user_id", searchUID);
                    startActivity(chatIntent);
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
                DirectSearchActivity.super.onBackPressed();
            }
        });
    }

    private void setupView(){
        mBackView = findViewById(R.id.direct_search_back);
        mSearchView = findViewById(R.id.direct_hits_container);
        mSearchInput = findViewById(R.id.direct_search_input);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searcher.destroy();
    }
}
