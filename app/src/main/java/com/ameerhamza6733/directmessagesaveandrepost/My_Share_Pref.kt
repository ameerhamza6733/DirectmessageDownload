package com.ameerhamza6733.directmessagesaveandrepost

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * Created by apple on 10/14/18.
 */
class My_Share_Pref {
    companion object {
        val APP_DATA_FILE = "APP_DATA_FILE"
        val POST_FILE="POST_FILE"
        val IS_FIRST_TIME = "IS_FIRST_TIME"
        val ATO_SAVE="ATO_SAVE"
        val RateMe = "RateMe"
        val gson=Gson()


        fun saveIsFirstTime(context: Context, fistTime: Boolean) {
            val sharePref = context.applicationContext.getSharedPreferences(APP_DATA_FILE, 0)
            var edit = sharePref.edit().putBoolean(IS_FIRST_TIME, fistTime).apply()
        }


        fun saveRateMe(context: Context, fistTime: Boolean) {
            val sharePref = context.applicationContext.getSharedPreferences(APP_DATA_FILE, 0)
            var edit = sharePref.edit().putBoolean(RateMe, fistTime).apply()
        }

        fun getISFirsTime(context: Context): Boolean {
            val sharePref = context.applicationContext.getSharedPreferences(APP_DATA_FILE, 0)
            return sharePref.getBoolean(IS_FIRST_TIME, true);
        }

        fun getRateMe(context: Context):Boolean {
            val sharePref = context.applicationContext.getSharedPreferences(APP_DATA_FILE, 0)
            return sharePref.getBoolean(RateMe,true)

        }

        fun getAtoSave(context: Context):Boolean {
            val sharePref = context.applicationContext.getSharedPreferences(APP_DATA_FILE, 0)
            return sharePref.getBoolean(ATO_SAVE,true)

        }

        fun SaveAtoStart(context: Context,boolean: Boolean):Boolean {
            val sharePref = context.applicationContext.getSharedPreferences(APP_DATA_FILE, 0)
            return sharePref.edit().putBoolean(IS_FIRST_TIME,boolean).commit()

        }


        fun savePost(context: Context,key:String,post: Post):Boolean{
            val sharePref = context.applicationContext.getSharedPreferences(POST_FILE, 0)
            val edit= sharePref.edit();

           return edit.putString(key,gson.toJson(post)).commit()
        }

        fun removePost(context: Context,key: String){
            val sharePref = context.applicationContext.getSharedPreferences(POST_FILE, 0)
            val edit= sharePref.edit();
            edit.remove(key);
        }

        @Synchronized
        fun getAllPost(context: Context): ArrayList<Post> {
            val list = java.util.ArrayList<Post>()
            val gson = Gson()
            val sp = context.applicationContext.getSharedPreferences(POST_FILE, 0)
            val allEntries = sp.all
            for ((_, value) in allEntries) {
               var post= gson.fromJson<Post>(value.toString(), Post::class.java!!)
                if (!File(post.pathToStorage).exists()) continue
                list.add(post)
            }
            return list
        }

    }
}