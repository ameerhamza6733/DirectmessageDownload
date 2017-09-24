package com.ameerhamza6733.directmessagesaveandrepost;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

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

       try{
           Intent share = new Intent(android.content.Intent.ACTION_SEND);

           // Set the MIME type
           share.setType(type);

           // Create the URI from the media
           File media = new File(mediaPath);
           Uri uri = Uri.fromFile(media);

           // Add the URI to the Intent.
           Log.d("InstaIntent","URi"+uri.toString());
           share.putExtra(Intent.EXTRA_STREAM, uri);

           // Broadcast the Intent.
           share.setPackage("com.instagram.android");
           context.startActivity(share);
       }catch (Exception e){
           Toast.makeText(context,"Some thing wrong error : "+e.getMessage(),Toast.LENGTH_SHORT).show();
       }

    }

}
