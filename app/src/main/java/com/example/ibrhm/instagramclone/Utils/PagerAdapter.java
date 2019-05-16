package com.example.ibrhm.instagramclone.Utils;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

public class PagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> mFragmentList;
    private ArrayList<String> mTabList;

    public PagerAdapter(FragmentManager fm, ArrayList<String> tabList) {
        super(fm);
        mFragmentList = new ArrayList<>();
        mTabList = tabList;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabList.get(position);
    }

    public void addFragment(Fragment fragment){
        mFragmentList.add(fragment);
    }

    public void selectRemoveFragment(ViewGroup viewGroup, int position){

        Object removeFragment = this.instantiateItem(viewGroup, position);
        this.destroyItem(viewGroup, position, removeFragment);

    }

    public void selectAddFragment(ViewGroup viewGroup, int position){
        this.instantiateItem(viewGroup, position);
    }
}
