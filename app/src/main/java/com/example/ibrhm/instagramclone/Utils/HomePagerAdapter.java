package com.example.ibrhm.instagramclone.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

public class HomePagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> mFragmentList;

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
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
