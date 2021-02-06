package com.ameerhamza6733.directmessagesaveandrepost;

/**
 * Created by AmeerHamza on 10/7/2017.
 */


import android.content.Intent;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String TAG = "HistoryAdapter";

    private List<Post> mDataSet;

// BEGIN_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public HistoryAdapter(List<Post> dataSet) {
        mDataSet = dataSet;
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.each_post, viewGroup, false);

        return new ViewHolder(v);
    }

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set." + mDataSet.get(position).getHashTags());

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getTextView().setText(mDataSet.get(viewHolder.getAdapterPosition()).getHashTags());
        viewHolder.getmDir().setText(mDataSet.get(viewHolder.getAdapterPosition()).getContent());
        Picasso.get().load(mDataSet.get(viewHolder.getAdapterPosition()).getImageURL()).into(viewHolder.getImageView());
        viewHolder.getmCopyHashTag().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // if(mDataSet.get(viewHolder.getAdapterPosition()).getHashTags().toString()!=null && !mDataSet.get(viewHolder.getAdapterPosition()).getHashTags().toString().isEmpty()){
                try {
                    new ClipBrodHelper().WriteToClipBord(view.getContext(), mDataSet.get(viewHolder.getAdapterPosition()).getHashTags().toString());

                } catch (NullPointerException n) {
                    Toast.makeText(view.getContext(), "NO Hash tag found for this Post", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(view.getContext(), "Some thing wrong unable to copy hash tag to clipbord", Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewHolder.getShareButton().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mDataSet.get(viewHolder.getAdapterPosition()).getMedium().equalsIgnoreCase("video")) {
                    new InstaIntent().createVideoInstagramIntent("video/*", mDataSet.get(viewHolder.getAdapterPosition()).getPathToStorage(), view.getContext(), false);
                } else {
                    new InstaIntent().createVideoInstagramIntent("image/*", mDataSet.get(viewHolder.getAdapterPosition()).getPathToStorage(), view.getContext(), false);

                }
            }
        });
        viewHolder.getRepostButton().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mDataSet.get(viewHolder.getAdapterPosition()).getMedium().equalsIgnoreCase("video")) {
                    new InstaIntent().createVideoInstagramIntent("video/*", mDataSet.get(viewHolder.getAdapterPosition()).getPathToStorage(), view.getContext(), true);
                } else {
                    new InstaIntent().createVideoInstagramIntent("image/*", mDataSet.get(viewHolder.getAdapterPosition()).getPathToStorage(), view.getContext(), true);

                }
            }
        });
        viewHolder.getFabDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = new File(mDataSet.get(viewHolder.getAdapterPosition()).getPathToStorage());
                if (file.exists()) {
                    file.delete();
                }
               My_Share_Pref.Companion.removePost(viewHolder.getFabDelete().getContext(),mDataSet.get(viewHolder.getAdapterPosition()).getUrl());
                mDataSet.remove(viewHolder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });

        viewHolder.getAppCompatImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent internt = new Intent(v.getContext(),PlayerActivity.class);
                internt.putExtra(PlayerActivity.EXTRA_VIDEO_PATH,mDataSet.get(viewHolder.getAdapterPosition()).getPathToStorage());
                v.getContext().startActivity(internt);
            }
        });

        if (mDataSet.get(viewHolder.getAdapterPosition()).getMedium().equals("image"))
            viewHolder.getAppCompatImageView().setVisibility(View.INVISIBLE);


    }




    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;
        private final FloatingActionButton shareButton;
        private final Button mCopyHashTag;
        private final TextView mDir;
        private final FloatingActionButton repostButton;
        private final FloatingActionButton fabDelete;
        private final AppCompatImageView appCompatImageView;

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
            repostButton = v.findViewById(R.id.floatingActionButtonRepost);
            appCompatImageView=v.findViewById(R.id.btPlay);
            fabDelete = v.findViewById(R.id.floatingActionButtonDelete);
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

        public FloatingActionButton getRepostButton() {
            return repostButton;
        }

        public FloatingActionButton getFabDelete() {
            return fabDelete;
        }

        public AppCompatImageView getAppCompatImageView() {
            return appCompatImageView;
        }
    }
}