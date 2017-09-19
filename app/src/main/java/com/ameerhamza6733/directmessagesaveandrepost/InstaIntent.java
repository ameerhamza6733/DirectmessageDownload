package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.golshadi.majid.core.DownloadManagerPro;
import com.golshadi.majid.report.ReportStructure;

import java.io.File;
import java.util.List;

/**
 * Created by AmeerHamza on 9/17/2017.
 */

public class InstaIntent {

    public  void createInstagramIntent(String type, String mediaPath, Context context) {

        // Create the new Intent using the 'Send' action.

        Intent share = new Intent(Intent.ACTION_SEND);

        // Set the MIME type
        share.setType(type);

        // Create the URI from the media
        File media = new File(mediaPath);
        Uri uri = Uri.fromFile(media);

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);

        // Broadcast the Intent.
        context.startActivity(Intent.createChooser(share, "Share to"));

    }
}
