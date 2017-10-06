package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kingfisher.easy_sharedpreference_library.SharedPreferencesManager;

import java.util.Map;

/**
 * Created by AmeerHamza on 10/6/2017.
 */

public class historyFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

       Log.d(this.getClass().getSimpleName(),"value"+SharedPreferencesManager.getInstance().getValue("BZ6GheCjMXz",post.class).getContent());
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
