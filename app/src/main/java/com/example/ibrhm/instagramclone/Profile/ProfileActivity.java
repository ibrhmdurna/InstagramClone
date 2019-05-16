package com.example.ibrhm.instagramclone.Profile;

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

public class ProfileActivity extends AppCompatActivity {

    private static int ACTIVITY_NO = 4;
    private static String TAG = "ProfileActivity";

    private BottomNavigationView mBottomNavigationView;
    private FrameLayout mProfileContainer;

    public static boolean editFragment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileContainer = findViewById(R.id.profile_container);
        setupProfileView();
    }

    private void setupNavigationView(){
        mBottomNavigationView = findViewById(R.id.profileBottomNavigationView);

        BottomNavigationViewHelper.SetupNavigation(this, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NO);
        menuItem.setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNavigationView();
    }

    private void setupProfileView(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.profile_container, new ProfileFragment());
        transaction.commit();
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
