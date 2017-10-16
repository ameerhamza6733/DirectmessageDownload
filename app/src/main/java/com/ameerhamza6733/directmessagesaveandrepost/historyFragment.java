package com.ameerhamza6733.directmessagesaveandrepost;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.kingfisher.easy_sharedpreference_library.SharedPreferencesManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by AmeerHamza on 10/6/2017.
 */

public class historyFragment extends Fragment {

    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected List<post> mDataset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.histroy_fragment, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);


        // END_INCLUDE(initializeRecyclerView)


        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            try {
                initDataset();
            } catch (Exception e) {
                e.printStackTrace();
                FirebaseCrash.report(new Exception(" public void setUserVisibleHint Error code 10 Error : "+e.getMessage()));
                Toast.makeText(getActivity(),"unable to load history if you just update your app then make sure uninstall this app first then reinstall Error code 10",Toast.LENGTH_LONG).show();
            }
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data come
     * from a SharedPreferencesManager.
     */
    private void initDataset() throws Exception {
        mDataset = new ArrayList<>();
        Map<String, ?> allEntries = SharedPreferencesManager.getInstance().getAllKeys();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry == null || entry.getValue() == null) continue;
            Log.e("SharedPreferenceManager", entry.getKey() + ": " + entry.getValue().toString());

            mDataset.add(SharedPreferencesManager.getInstance().getValue(entry.getKey(), post.class));
        }
        removeIFPostNotExsitInDevice();
    }

    private void removeIFPostNotExsitInDevice() throws Exception{
        Iterator itr = mDataset.iterator();
        while (itr.hasNext()) {
            post mPost = (post) itr.next();
            if (mPost.getMedium().equalsIgnoreCase("image") || (mPost.getMedium().equalsIgnoreCase("video") ))
                if(new File(mPost.getPathToStorage()).exists()){

               } else {
                itr.remove();
                mDataset.remove(mPost);
                SharedPreferencesManager.getInstance().remove(mPost.getPostID());
            }
        }
        mAdapter = new CustomAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);
    }
}