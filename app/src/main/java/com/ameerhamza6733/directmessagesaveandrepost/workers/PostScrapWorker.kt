package com.ameerhamza6733.directmessagesaveandrepost.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ameerhamza6733.directmessagesaveandrepost.DownloadingFragment
import com.ameerhamza6733.directmessagesaveandrepost.Post
import com.ameerhamza6733.directmessagesaveandrepost.model.IntentModelType
import com.ameerhamza6733.directmessagesaveandrepost.model.MediaItemType
import com.ameerhamza6733.directmessagesaveandrepost.utils.IntentUtils
import com.ameerhamza6733.directmessagesaveandrepost.utils.NetworkUtils
import com.ameerhamza6733.directmessagesaveandrepost.utils.ResponseBodyUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class PostScrapWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val crashlytics=FirebaseCrashlytics.getInstance()
    override fun doWork(): Result {

        val url = inputData.getString("url")
        val cookie=inputData.getString("cookie")
        val model = IntentUtils.parseUrl(url!!)
        val outPutData= Data.Builder()
        if (model == null || model.type != IntentModelType.POST) {
            return Result.failure()
        }
        var conn: HttpURLConnection? = null
        val gson = Gson()
        try {
            val connectionUrl = "https://www.instagram.com/p/" + model.text + "/?__a=1"
            crashlytics.log("connectionUrl  $connectionUrl")
            conn = URL(connectionUrl).openConnection() as HttpURLConnection
            conn?.setRequestProperty("Content-type", "application/json")
            conn?.setRequestProperty("Cookie",cookie)
            conn.useCaches = false
            conn!!.connect()
            Log.d(TAG, "occented to $connectionUrl")

            crashlytics.log("response code ${conn.responseCode} ")
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
              val response=  NetworkUtils.readFromConnection(conn)
                Log.d(TAG,"response ${response}")
                crashlytics.log("response ${response}")
                val mediaJson = JSONObject(response).getJSONObject("graphql")
                        .getJSONObject("shortcode_media")
                val media1 = ResponseBodyUtils.parseGraphQLItem(mediaJson)

                val post = Post()
                post.hashTags = extractHashTag(media1.caption.text)
                post.content =media1.caption.text
                post.caption= removeHashTags(media1.caption.text)
                post.url=url
              if (media1.mediaType==MediaItemType.MEDIA_TYPE_IMAGE){
                 post.imageURL= media1.imageVersions2.candidates[0].url
                  post.medium="image";
                  post.type="photo"
              }else if (media1.mediaType==MediaItemType.MEDIA_TYPE_VIDEO){
                  post.videoURL=media1.videoVersions[0].url
                  post.imageURL= media1.imageVersions2.candidates[0].url
                  post.medium="video";
                  post.type="video"
              }else if (media1.mediaType==MediaItemType.MEDIA_TYPE_SLIDER && media1.videoVersions==null){
                  post.imageURL= media1.imageVersions2.candidates[0].url
                  post.medium="image";
                  post.type="photo"
              }else if (media1.mediaType==MediaItemType.MEDIA_TYPE_SLIDER && media1.videoVersions!=null){
                  post.videoURL=media1.videoVersions[0].url
                  post.imageURL= media1.imageVersions2.candidates[0].url
                  post.medium="video";
                  post.type="video"
              }else{

                  outPutData.putInt("errorCode",1)
                  outPutData.putString("errorMessage","No media type found")
                  Result.failure(outPutData.build())
              }
                   post.postID=model.text
                 outPutData .putString("post", gson.toJson(post))
                return Result.success(outPutData.build())
            }else if (conn.responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                Log.d(TAG,"errorMessage ${conn?.responseMessage}")
                outPutData.putInt("errorCode",conn.responseCode)
                outPutData .putString("errorMessage","Sorry, this post isn't available.\n" +
                        "The link you followed may be broken, or the post may have been removed" )
            }
            else {
                Log.d(TAG,"error code ${conn?.responseCode}")
                outPutData.putInt("errorCode",conn.responseCode)
                outPutData .putString("errorMessage", conn?.responseMessage)
            }
        }catch (jsonEx : JSONException){
            outPutData.putInt("errorCode",HttpURLConnection.HTTP_UNAUTHORIZED)
            outPutData .putString("errorMessage",jsonEx.localizedMessage )
            jsonEx.printStackTrace()
            crashlytics.recordException(jsonEx)
        }
        catch (e: Exception) {
            outPutData.putInt("errorCode",-1)
            outPutData .putString("errorMessage", e.message)
            e.printStackTrace()
            crashlytics.recordException(e)
        } finally {
            conn?.disconnect()
        }
        return Result.failure(outPutData.build())
    }

    private fun extractHashTag(caption: String?): StringBuilder {
        val hashTags = StringBuilder()
        if (caption != null) {
            val hashTagsArray = caption.split("#".toRegex()).toTypedArray().reversedArray()
            for (hashtag in hashTagsArray) {
                val trimHashTag= hashtag.trim()
               if (trimHashTag.contains(" ")){
                   continue
               }else{
                   Log.d(TAG, "hashTAg $trimHashTag")
                   hashTags.append("#")
                           .append(trimHashTag)
                           .append(" ")
               }

            }
        }
        return hashTags
    }

    private fun removeHashTags( caption: String?):String{

        var newCaption = caption
        if (caption != null) {
            val hashTagsArray = caption.split("#".toRegex()).toTypedArray().reversedArray()
            for (hashtag in hashTagsArray) {
                val trimHashTag= hashtag.trim()
                if (trimHashTag.contains(" ")){
                    continue
                }else{
                    Log.d(TAG, "hashTAg $trimHashTag")
                    newCaption=newCaption?.replace("#${trimHashTag}","")

                }

            }
        }
        return newCaption.toString()
    }



    private fun throwReponseFail(){
        throw DownloadingFragment.CustomException("responseFail")
    }

    companion object {
        private const val TAG = "PostScrapWorker"
    }
}