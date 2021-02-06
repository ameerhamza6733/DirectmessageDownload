package com.ameerhamza6733.directmessagesaveandrepost;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by AmeerHamza on 10/6/2017.
 */

public class MYPagerAdupter extends FragmentStatePagerAdapter {
    private static final String INTA_DOWNLOAD_FEATURE[] = {"Downloader", "History"};

    public MYPagerAdupter(FragmentManager fm) {
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
