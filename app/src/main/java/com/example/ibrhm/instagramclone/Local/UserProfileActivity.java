package com.example.ibrhm.instagramclone.Local;

import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;

import org.greenrobot.eventbus.EventBus;

public class UserProfileActivity extends AppCompatActivity {

    private static int ACTIVITY_NO = 0;
    private static String TAG = "ProfileActivity";

    private BottomNavigationView mBottomNavigationView;
    private FrameLayout mProfileContainer;

    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ACTIVITY_NO = getIntent().getIntExtra("activity_no", 0);
        user_id = getIntent().getStringExtra("user_id");

        mProfileContainer = findViewById(R.id.user_profile_container);
        setupProfileView();
    }

    private void setupNavigationView(){
        mBottomNavigationView = findViewById(R.id.userProfileBottomNavigationView);

        BottomNavigationViewHelper.SetupNavigation(this, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NO);
        menuItem.setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setupNavigationView();
    }

    private void setupProfileView(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.user_profile_container, new UserProfileFragment());
        transaction.commit();

        EventBusDataEvents.SendUID events = new EventBusDataEvents.SendUID(user_id);
        EventBus.getDefault().postSticky(events);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0){
            super.onBackPressed();
        }else{
            getSupportFragmentManager().popBackStack();
        }
    }
}
