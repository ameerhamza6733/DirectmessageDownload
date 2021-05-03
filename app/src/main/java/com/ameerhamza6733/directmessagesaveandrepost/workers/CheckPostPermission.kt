package com.ameerhamza6733.directmessagesaveandrepost.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL

class CheckPostPermission(context: Context, workerParameters: WorkerParameters) :Worker(context, workerParameters) {
    override fun doWork(): Result {
         val crashlytics= FirebaseCrashlytics.getInstance()
        val url = inputData.getString("url");
        val TAG="CheckPostPermission";
        val outPutData= Data.Builder()
        var con: HttpURLConnection? = null

       try {

          con = URL(url).openConnection() as HttpURLConnection
           con.setInstanceFollowRedirects(true)

           con.connect()
           con.getInputStream()

           Log.d(TAG, "respose code ${con.responseCode}")
           val redirectUrl: String = con.url.toString()
           Log.d(TAG, "respose url ${con.url}")

           if (con.responseCode == HttpURLConnection.HTTP_OK ) {
               val userName=redirectUrl.substring(redirectUrl.indexOf("com/")+6)
               Log.d(TAG,"user name of private post is ${userName}")
               if (redirectUrl!=url && (!userName.contains(" ") && url?.contains(userName)!=true)){

                   outPutData.putString("errorMessage", "Private post login required ");
                   outPutData.putString("userName", userName)
                   outPutData.putInt("errorCode", 404)
                   return Result.success(outPutData.build())

               }

           }else if (con.responseCode == HttpURLConnection.HTTP_NOT_FOUND){
               outPutData .putString("errorMessage","Sorry, this post isn't available.\n" +
                       "The link you followed may be broken, or the post may have been removed" )
               outPutData.putInt("errorCode", con.responseCode)
           }else{
               outPutData .putString("errorMessage",con.responseMessage )
               outPutData.putInt("errorCode", con.responseCode)
           }



       }catch (fileNotFound : FileNotFoundException){
           outPutData .putString("errorMessage","Sorry, this post isn't available.\n" +
                   "The link you followed may be broken, or the post may have been removed" )
           outPutData.putInt("errorCode", HttpURLConnection.HTTP_NOT_FOUND)
           crashlytics.recordException(fileNotFound)
       }
       catch (E: Exception){
           outPutData .putString("errorMessage",E.localizedMessage )
           outPutData.putInt("errorCode",-1)
           E.printStackTrace()
           crashlytics.recordException(E)

       }finally {
           con?.disconnect()
       }

        return  Result.failure(outPutData.build())
    }


}