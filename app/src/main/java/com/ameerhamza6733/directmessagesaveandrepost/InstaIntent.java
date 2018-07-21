package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;



import java.io.File;

/**
 * Created by AmeerHamza on 9/17/2017.
 */

public class InstaIntent {

    public void createVideoInstagramIntent(String type, String mediaPath, Context context, boolean repost) {

        // Create the new Intent using the 'Send' action.

        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(type);

            Uri uri =  FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    new File(mediaPath));
            if (uri != null) {
                Log.d("InstaIntent", "URi" + uri.toString());
            }
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (repost) {
                share.setPackage("com.instagram.android");
                context.startActivity(share);
            } else
                context.startActivity(Intent.createChooser(share, "Share to"));


        } catch (Exception e) {
            Toast.makeText(context, "Some thing wrong error : Error code 2 " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

}
