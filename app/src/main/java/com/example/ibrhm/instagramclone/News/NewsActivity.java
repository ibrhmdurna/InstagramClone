package com.example.ibrhm.instagramclone.News;

import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.ibrhm.instagramclone.Utils.PagerAdapter;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {

    private static int ACTIVITY_NO = 3;
    private static String TAG = "News Activity";

    private BottomNavigationView mBottomNavigationView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        setupView();
        setupViewPager();

        if(getIntent().getStringExtra("action").equals("request")){
            NewsYouFragment.goToRequests = true;
        }
    }

    private void setupNavigationView(){
        BottomNavigationViewHelper.SetupNavigation(this, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NO);
        menuItem.setChecked(true);
    }

    private void setupView(){
        mBottomNavigationView = findViewById(R.id.newsBottomNavigationView);
        mTabLayout = findViewById(R.id.new_tab_layout);
        mViewPager = findViewById(R.id.news_pager);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().popBackStackImmediate()){
            getSupportFragmentManager().popBackStack();
        }
        else{
            super.onBackPressed();
        }
    }

    private void setupViewPager() {
        ArrayList<String> mTabList = new ArrayList<>();
        mTabList.add(getString(R.string.following));
        mTabList.add(getString(R.string.you));

        final PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mTabList);
        pagerAdapter.addFragment(new NewsFollowingFragment());
        pagerAdapter.addFragment(new NewsYouFragment());

        mViewPager.setAdapter(pagerAdapter);

        if(App.backgroundNews.getNewsLayoutState().equals("You")){
            mViewPager.setCurrentItem(1);
        }
        else{
            mViewPager.setCurrentItem(0);
        }

        if(!App.backgroundNews.getNewsLayoutState().equals("Following")){
            if(!App.background.isThereBackground("News_Following")){
                pagerAdapter.selectRemoveFragment(mViewPager, 0);
            }
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if(i == 0){
                    pagerAdapter.selectAddFragment(mViewPager, 0);
                    App.backgroundNews.setNewsLayoutState("Following");
                }
                if(i == 1){
                    App.backgroundNews.setNewsLayoutState("You");
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNavigationView();
    }
}
