package com.hololo.tutorial.library;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class StepPagerAdapter extends FragmentPagerAdapter {
    private List<Step> stepList;

    public StepPagerAdapter(FragmentManager fm, List<Step> stepList) {
        super(fm);
        this.stepList = stepList;
    }

    @Override
    public Fragment getItem(int position) {
        return StepFragment.createFragment(stepList.get(position));
    }

    @Override
    public int getCount() {
        return stepList.size();
    }
}
