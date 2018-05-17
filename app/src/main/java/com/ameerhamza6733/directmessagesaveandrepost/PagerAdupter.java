package com.ameerhamza6733.directmessagesaveandrepost;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by AmeerHamza on 10/6/2017.
 */

public class PagerAdupter extends FragmentStatePagerAdapter {
    private static final String INTA_DOWNLOAD_FEATURE[] = {"Downloader", "History"};

    public PagerAdupter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (i==0)
        return DownloadingFragment.Companion.newInstance(0);
        else return HistoryFragment.newInstance(0);
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
