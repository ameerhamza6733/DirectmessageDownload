package com.ameerhamza6733.directmessagesaveandrepost

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ameerhamza6733.directmessagesaveandrepost.model.IntentModelType
import com.ameerhamza6733.directmessagesaveandrepost.model.MediaItemType
import com.ameerhamza6733.directmessagesaveandrepost.utils.IntentUtils
import com.ameerhamza6733.directmessagesaveandrepost.utils.NetworkUtils
import com.ameerhamza6733.directmessagesaveandrepost.utils.ResponseBodyUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class PostScrapWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val crashlytics=FirebaseCrashlytics.getInstance()
    override fun doWork(): Result {

        val url = inputData.getString("url")
        val model = IntentUtils.parseUrl(url!!)
        if (model == null || model.type != IntentModelType.POST) {
            return Result.failure()
        }
        var conn: HttpURLConnection? = null
        val gson = Gson()
        try {
            val connectionUrl = "https://www.instagram.com/p/" + model.text + "/?__a=1"
            conn = URL(connectionUrl).openConnection() as HttpURLConnection
            conn.useCaches = false
            conn!!.connect()
            Log.d(TAG, "occented to $connectionUrl")
            crashlytics.log("connected to $connectionUrl")
            crashlytics.log("response code ${conn.responseCode} ")
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {

                val mediaJson = JSONObject(NetworkUtils.readFromConnection(conn)).getJSONObject("graphql")
                        .getJSONObject("shortcode_media")
                val media1 = ResponseBodyUtils.parseGraphQLItem(mediaJson)
                crashlytics.log("mediaJson ${mediaJson}")
                val post = Post()
                post.hashTags = extractHashTag(media1.caption.text)
                post.content = media1.caption.text
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
              }
              post.postID=model.text
               val outPutData= Data.Builder()
                       .putString("post", gson.toJson(post))
                       .build()
                return Result.success(outPutData)
            }else{
               try {
                   throwReponseFail()
               }catch (E:Exception){
                   crashlytics.recordException(E)
               }
                Result.failure()
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
        } finally {
            conn?.disconnect()
        }
        return Result.failure()
    }

    private fun extractHashTag(caption: String?): StringBuilder {
        val hashTags = StringBuilder()
        if (caption != null) {
            val hashTagsArray = caption.split("#".toRegex()).toTypedArray()
            for (hashtag in hashTagsArray) {
                val trimHashTag= hashtag.trim()
               if (trimHashTag.contains(" ")){
                   continue
               }else{
                   Log.d(TAG, "hashTAg $trimHashTag")
                   hashTags.append("#")
                           .append(" ")
                           .append(trimHashTag)
               }

            }
        }
        return hashTags
    }

    private fun throwReponseFail(){
        throw DownloadingFragment.CustomException("responseFail")
    }

    companion object {
        private const val TAG = "PostScrapWorker"
    }
}