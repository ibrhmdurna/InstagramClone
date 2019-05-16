package com.example.ibrhm.instagramclone.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.example.ibrhm.instagramclone.Home.HomeActivity;
import com.example.ibrhm.instagramclone.News.NewsActivity;
import com.example.ibrhm.instagramclone.Profile.ProfileActivity;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Search.SearchActivity;
import com.example.ibrhm.instagramclone.Share.ShareActivity;
import com.hoanganhtuan95ptit.autoplayvideorecyclerview.AutoPlayVideoRecyclerView;

public class BottomNavigationViewHelper {
    public static void SetupNavigation(final Activity context, BottomNavigationView bottomNavigationView){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_item:
                        Intent homeIntent = new Intent(context, HomeActivity.class);
                        context.startActivity(homeIntent);
                        break;
                    case R.id.search_item:
                        Intent searchIntent = new Intent(context, SearchActivity.class);
                        context.startActivity(searchIntent);
                        break;
                    case R.id.share_item:
                        Intent shareIntent = new Intent(context, ShareActivity.class);
                        context.startActivity(shareIntent);
                        break;
                    case R.id.news_item:
                        Intent newsIntent = new Intent(context, NewsActivity.class);
                        newsIntent.putExtra("action","false");
                        context.startActivity(newsIntent);
                        break;
                    case R.id.profile_item:
                        Intent profileIntent = new Intent(context, ProfileActivity.class);
                        context.startActivity(profileIntent);
                        break;
                }
                return true;
            }
        });


        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.home_item:
                        Intent homeIntent = new Intent(context, HomeActivity.class);
                        if(menuItem.getActionView() == context.getCurrentFocus()){
                            context.startActivity(homeIntent);
                        }
                        else{
                            AutoPlayVideoRecyclerView recyclerView = context.findViewById(R.id.posts_container);
                            if(recyclerView != null)
                                recyclerView.smoothScrollToPosition(0);
                        }
                        break;
                    case R.id.search_item:
                        Intent searchIntent = new Intent(context, SearchActivity.class);
                        if(menuItem.getActionView() == context.getCurrentFocus()){
                            //context.startActivity(searchIntent);
                        }
                        break;
                    case R.id.share_item:
                        Intent shareIntent = new Intent(context, ShareActivity.class);
                        if(menuItem.getActionView() == context.getCurrentFocus()){
                            //context.startActivity(shareIntent);
                        }
                        break;
                    case R.id.news_item:
                        Intent newsIntent = new Intent(context, NewsActivity.class);
                        newsIntent.putExtra("action","false");
                        if(menuItem.getActionView() == context.getCurrentFocus()){
                            //context.startActivity(newsIntent);
                        }
                        break;
                    case R.id.profile_item:
                        Intent profileIntent = new Intent(context, ProfileActivity.class);
                        if(menuItem.getActionView() != context.getCurrentFocus()){
                            context.startActivity(profileIntent);
                        }
                        else{
                            NestedScrollView scrollView = context.findViewById(R.id.profile_scroll_view);
                            if(scrollView != null)
                                scrollView.smoothScrollTo(0, 0);
                        }
                        break;
                }
            }
        });
    }
}
