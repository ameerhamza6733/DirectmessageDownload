package com.ameerhamza6733.directmessagesaveandrepost;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by AmeerHamza on 10/6/2017.
 */

public class pagerAdupter extends FragmentStatePagerAdapter {
    private static final String INTA_DOWNLOAD_FEATURE[] = {"Downloader", "History"};

    public pagerAdupter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (i==0)
        return new downloadingFragment();
        else return new historyFragment();
    }

    @Override
    public int getCount() {
        return INTA_DOWNLOAD_FEATURE.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return INTA_DOWNLOAD_FEATURE[position];
    }
}
