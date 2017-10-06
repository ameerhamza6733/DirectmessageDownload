package com.ameerhamza6733.directmessagesaveandrepost;

/**
 * Created by AmeerHamza on 10/7/2017.
 */



import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
private static final String TAG = "CustomAdapter";

private List<post> mDataSet;

// BEGIN_INCLUDE(recyclerViewSampleViewHolder)
/**
 * Provide a reference to the type of views that you are using (custom ViewHolder)
 */
public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;
    private final ImageView imageView;
    private final FloatingActionButton shareButton;
    private final Button mCopyHashTag;
    private final TextView mDir;

    public ViewHolder(View v) {
        super(v);
        // Define click listener for the ViewHolder's View.
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
            }
        });
        textView = (TextView) v.findViewById(R.id.hash_tag_text_view);
        imageView = v.findViewById(R.id.imageView);
        shareButton = v.findViewById(R.id.floatingActionButtonShare);
        mCopyHashTag = v.findViewById(R.id.copy_hash_tag_button);
        mDir = v.findViewById(R.id.textView_description);
    }

    public TextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public FloatingActionButton getShareButton() {
        return shareButton;
    }

    public TextView getmDir() {
        return mDir;
    }

    public Button getmCopyHashTag() {
        return mCopyHashTag;
    }
}
    // END_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public CustomAdapter(List<post> dataSet) {
        mDataSet = dataSet;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.each_post, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set."+mDataSet.get(position).getHashTags());

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getTextView().setText(mDataSet.get(position).getHashTags());
        viewHolder.getmDir().setText(mDataSet.get(position).getContent());
        Picasso.with(viewHolder.getTextView().getContext()).load(mDataSet.get(position).getImageURL()).into(viewHolder.getImageView());
        viewHolder.getmCopyHashTag().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mDataSet.get(position).getHashTags().length()>0){
                    new ClipBrodHelper().WriteToClipBord(view.getContext(),mDataSet.get(position).getHashTags().toString());
                }
            }
        });

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}