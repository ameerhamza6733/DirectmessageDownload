package com.ameerhamza6733.directmessagesaveandrepost

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.artjimlop.altex.AltexImageDownloader
import com.daimajia.numberprogressbar.NumberProgressBar
import com.daimajia.numberprogressbar.OnProgressBarListener
import com.golshadi.majid.core.DownloadManagerPro
import com.golshadi.majid.report.ReportStructure
import com.golshadi.majid.report.listener.DownloadManagerListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.kingfisher.easy_sharedpreference_library.SharedPreferencesManager

import com.squareup.picasso.Picasso
import lolodev.permissionswrapper.callback.OnRequestPermissionsCallBack
import lolodev.permissionswrapper.wrapper.PermissionWrapper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.FirebaseException
import com.google.firebase.crash.FirebaseCrash

/**
 * Created by AmeerHamza on 10/6/2017.
 */

class downloadingFragment : Fragment(), DownloadManagerListener, OnProgressBarListener {
    override fun onProgressChange(current: Int, max: Int) {

    }

    private var TAG = "MainActivityTAG"
    override fun OnDownloadStarted(taskId: Long) {
        this@downloadingFragment.activity.runOnUiThread({ mNumberBar.progress = 0 })

        Log.d(TAG, "OnDownloadStarted")
    }

    override fun OnDownloadPaused(taskId: Long) {
        Log.d(TAG, "OnDownloadPaused")
    }

    override fun onDownloadProcess(taskId: Long, percent: Double, downloadedLength: Long) {

        this@downloadingFragment.activity.runOnUiThread({ mNumberBar.incrementProgressBy(1) })
        Log.d(TAG, "onDownloadProcess" + percent)
    }

    override fun OnDownloadFinished(taskId: Long) {
        Log.d(TAG, "OnDownloadFinished")
    }


    override fun OnDownloadRebuildStart(taskId: Long) {
        Log.d(TAG, "OnDownloadRebuildStart") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnDownloadRebuildFinished(taskId: Long) {
        Log.d(TAG, "OnDownloadRebuildFinished") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnDownloadCompleted(taskId: Long) {
        activity.runOnUiThread {
            mNumberBar.progress = 100
            mFabRepostButton.visibility = 1
            val repor: ReportStructure = dm.singleDownloadStatus(taskToken);
            mPost.pathToStorage =repor.saveAddress
            saveToPraf(mPost)
        }
        Log.d(TAG, "OnDownloadCompleted") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connectionLost(taskId: Long) {
        Log.d(TAG, "connectionLost") //To change body of created functions use File | Settings | File Templates.
    }

    private fun shareIntent(repost : Boolean) {
        try {
            if (mNumberBar.progress == 100) {
                if (mPost.medium.equals("image"))
                    shareImageIntentToInstagram(repost)
                else
                    shareVideoIntentToInstagram(repost)
            } else {
                Toast.makeText(activity, "downloading sill in progress ", Toast.LENGTH_SHORT).show()
            }

        } catch (ex: Exception) {
            FirebaseCrash.report( Exception(" private fun shareIntent Error code 3 Error : "+ex.message));
            Toast.makeText(activity, "some thing working while sharing Error: code 3  " + ex.message, Toast.LENGTH_LONG).show()
        }


    }

    private fun shareVideoIntentToInstagram(repost: Boolean) {
        val repor: ReportStructure = dm.singleDownloadStatus(taskToken);
        val int: InstaIntent = InstaIntent()
        int.createVideoInstagramIntent("video/*", repor.saveAddress, activity,repost)
    }

    private fun shareImageIntentToInstagram(repost : Boolean) {
        try {
            val bitmapPath: String = MediaStore.Images.Media.insertImage(context.getContentResolver(), mBitMapImageToShare, "title", null);
            val bitmapUri: Uri = Uri.parse(bitmapPath)
            val intent = Intent(Intent.ACTION_SEND)
            Log.d(TAG, "uri=" + bitmapUri.toString())
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            if(repost){
                intent.setPackage("com.instagram.android")
                startActivity(intent)
            }
            else
                startActivity(Intent.createChooser(intent, "Share to"));

        } catch (e: Exception) {
            FirebaseCrash.report( Exception("private fun shareImageIntentToInstagram Error code 4 Error : "+e.message))
            Toast.makeText(activity,"Some thing wrong Error code 4 Error message : "+e.message,Toast.LENGTH_LONG).show()
        }

    }


    private lateinit var mEditTextInputURl: EditText
    private lateinit var mCheckAndSaveButton: Button
    private lateinit var mImage: ImageView
    protected lateinit var mHashTagTextView: TextView
    private lateinit var mCopyHashTagButton: Button
    private lateinit var mDescription: TextView
    private lateinit var mNumberBar: NumberProgressBar
    private lateinit var mFabRepostButton: FloatingActionButton
    private lateinit var mFabShareButton : FloatingActionButton
    private lateinit var mCardView: CardView
    private lateinit var mProgressBar: ProgressBar

    private lateinit var dm: DownloadManagerPro
    private var taskToken: Int = -1
    private lateinit var mBitMapImageToShare: Bitmap
    val mPost = post()
    lateinit var postKeyFromShardPraf : String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_download, container, false)

        Log.d(TAG, "onCreate")

        staupUI(view)
        return view
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        copyDataFromClipBrod()
        setUpListerners()
        dm = DownloadManagerPro(activity)
        dm.init("DMinstaDownload/", 12, this)
        mFabRepostButton.setOnClickListener({ shareIntent(true) })
        mFabShareButton.setOnClickListener ({ shareIntent(false) })
        mCopyHashTagButton.setOnClickListener({ copyHashTagToClipBord() })
    }


    private fun copyHashTagToClipBord() {
        if (!mHashTagTextView.text.isEmpty()) {
            val clipbordHelper = ClipBrodHelper()
            clipbordHelper.WriteToClipBord(activity, mHashTagTextView.text.toString())
        } else {
            Toast.makeText(activity, "No Hash tag not find in this post", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setUpListerners() {
        mCheckAndSaveButton.setOnClickListener({ checkBuildNO() })
        mNumberBar.setOnProgressBarListener(this);
    }

    private fun copyDataFromClipBrod() {
        if (!ClipBrodHelper(activity).clipBrodText.isNullOrEmpty()) {
            mEditTextInputURl.text.clear()
            mEditTextInputURl.setText(ClipBrodHelper(activity).clipBrodText)
            hideKeybord()
            checkBuildNO()
        } else {
            mEditTextInputURl.text.clear()
            Toast.makeText(activity, "URL not valid", Toast.LENGTH_SHORT).show()
            mCardView.visibility = View.INVISIBLE
            mProgressBar.visibility = View.INVISIBLE
        }
    }


    fun staupUI(view: View) {
        mEditTextInputURl = view.findViewById<EditText>(R.id.URL_Input_edit_text) as EditText
        mCheckAndSaveButton = view.findViewById<Button>(R.id.chack_and_save_post) as Button
        mImage = view.findViewById<ImageView>(R.id.imageView) as ImageView
        mHashTagTextView = view.findViewById<TextView>(R.id.hash_tag_text_view) as TextView
        mDescription = view.findViewById<TextView>(R.id.textView_description) as TextView
        mCopyHashTagButton = view.findViewById<Button>(R.id.copy_hash_tag_button) as Button
        mNumberBar = view.findViewById<NumberProgressBar>(R.id.number_progress_bar) as NumberProgressBar
        mFabRepostButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButtonRepost) as com.github.clans.fab.FloatingActionButton
        mFabShareButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButtonShare) as FloatingActionButton
        mCardView = view.findViewById<CardView>(R.id.cardView) as CardView
        mProgressBar = view.findViewById<ProgressBar>(R.id.progressBar) as ProgressBar

    }

    fun checkBuildNO() {

        if (Build.VERSION.SDK_INT > 22) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                askPermistion()
            else {
                val msg: String = mEditTextInputURl.text.toString()

                if (!msg.equals(""))
                    if(!checkIFPosAllreadyDownloaded(mEditTextInputURl.text.toString()))
                        grabData(mEditTextInputURl.text.toString()).execute()
                    else{
                        mProgressBar.visibility =View.INVISIBLE
                    }
            }

        } else {
            val msg: String = mEditTextInputURl.text.toString()

            if (!msg.equals(""))
                if(!checkIFPosAllreadyDownloaded(mEditTextInputURl.text.toString()))
                    grabData(mEditTextInputURl.text.toString()).execute()
                else{
                    mProgressBar.visibility =View.INVISIBLE
                }
        }
    }

    private fun askPermistion() {
        PermissionWrapper.Builder(activity)
                .addPermissions(arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                //enable rationale message with a custom message
                .addRequestPermissionsCallBack(object : OnRequestPermissionsCallBack {
                    override fun onGrant() {
                        val msg: String = mEditTextInputURl.text.toString()

                        if (!msg.equals(""))
                            if(!checkIFPosAllreadyDownloaded(mEditTextInputURl.text.toString()))
                            grabData(mEditTextInputURl.text.toString()).execute()
                            else{
                                mProgressBar.visibility =View.INVISIBLE
                            }
                        //  grabData(mEditTextInputURl.text.toString()).execute()
                    }

                    override fun onDenied(permission: String) {

                    }
                }).build().request()
    }

    private  fun checkIFPosAllreadyDownloaded(toString: String): Boolean {

        try {
            var tempPostID = toString.replace("https://www.instagram.com/p/", "")
            tempPostID =tempPostID.replace("/", "")

            val allEntries = SharedPreferencesManager.getInstance().allKeys
            for (entry in allEntries.entries) {
                if (entry == null || entry.value == null) continue
                Log.e("SharedPreferenceManager", entry.key + ": " + entry.value.toString())

                if(tempPostID.equals(entry.key)){
                    Toast.makeText(activity,"post already downloaded",Toast.LENGTH_SHORT).show()
                    postKeyFromShardPraf=entry.value.toString()

                    return true
                }

            }
        }catch (ex :Exception){

            FirebaseCrash.report( Exception("  private  fun checkIFPosAllreadyDownloaded Error code 5 Error : "+ex.message))
            Toast.makeText(activity,"Some thing wrong Error code 5 Error message : "+ex.message,Toast.LENGTH_LONG).show()

            return false
        }
        return false
    }
    inner class grabData(val ConnURL: String) : AsyncTask<Void, Void, String>() {
        val mHashTags = StringBuilder()
        var isSomeThingWrong = false


        override fun onPreExecute() {
            super.onPreExecute()
            Log.d(TAG, "url =" + ConnURL)
             mProgressBar.progress = 100
                mProgressBar.visibility = View.VISIBLE
                mCardView.visibility = View.INVISIBLE
                hideKeybord()
            FirebaseCrash.log("onPreExecute"+ConnURL);

        }



        override fun doInBackground(vararg p0: Void?): String {
            try {
                val document: Document = Jsoup.connect(ConnURL).timeout(6000).ignoreContentType(true).parser(Parser.htmlParser()).get()
                for (meta in document.select("meta")) {

                    if ((meta.attr("property").equals("instapp:hashtags") || meta.attr("property").equals("video:tag")))
                        mPost.hashTags = mHashTags.append("#").append(meta.attr("content")).append(" ")
                    if (meta.attr("property").equals("og:image"))
                        mPost.imageURL = meta.attr("content")
                    if (meta.attr("property").equals("og:description"))
                        mPost.content = meta.attr("content")
                    if (meta.attr("property").equals("og:video"))
                        mPost.videoURL = meta.attr("content")
                    if (meta.attr("name").equals("medium"))
                        mPost.medium = meta.attr("content")
                    if (meta.attr("property").equals("og:url")) {
                        mPost.postID = meta.attr("content").replace("https://www.instagram.com/p/", "")
                        mPost.postID = mPost.postID.replace("/", "")
                    }


                }


            } catch (ex: Exception) {
                ex.printStackTrace()
                activity.runOnUiThread({
                    Toast.makeText(activity, "Please try again later and check your intent connection  Error code 111  ", Toast.LENGTH_SHORT).show()
                   isSomeThingWrong=true

                })

            }
            return "";
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.d("dec", mPost.content + "mPost.imageURL= " + mPost.imageURL + "mPost.medium " + mPost.medium + " mPost.postDownloadingName  " + mPost.postID)

            if (!isSomeThingWrong) {
                UpdateUI()
                intiDownloader()

            }else
                mProgressBar.visibility = View.INVISIBLE


        }

        private fun UpdateUI() {
            mProgressBar.visibility = View.INVISIBLE

            mCardView.visibility = 1
            mHashTagTextView.setText(mPost.hashTags)
            try {
                if (!mPost.content.isNullOrEmpty() && isContainsColan()){
                    mPost.content = mPost.content.substring(mPost.content.indexOf(":"), mPost.content.length)
                    mDescription.text = mPost.content
                }

                Picasso.with(activity).load(mPost.imageURL).into(mImage)
            } catch (Ex: Exception) {
                FirebaseCrash.report( Exception("  private fun UpdateUI() Error code 6 Error : "+Ex.message))
                Toast.makeText(activity,"Some thing wrong Error code 6 Error message : "+Ex.message,Toast.LENGTH_LONG).show()

                Ex.printStackTrace()
            }


        }

        private fun isContainsColan(): Boolean = mPost.content.contains(":")

        private fun intiDownloader() {

            mFabRepostButton.visibility = 0
            try {

                Toast.makeText(activity, "Downloading start", Toast.LENGTH_SHORT).show()
                if (mPost.medium != "image"){
                    taskToken = dm.addTask(mPost.postID, mPost.videoURL, true, false)
                    dm.startDownload(taskToken)
                }

                else
                    downloadImage()
            } catch (ex: Exception) {
                FirebaseCrash.report( Exception(" private fun intiDownloader()  Error code 7 Error : "+ex.message))
                Toast.makeText(activity,"Some thing wrong Error code 7 Error message : "+ex.message,Toast.LENGTH_LONG).show()

            }
        }

        private fun downloadImage() {
            Log.d(TAG, " downloadImage")

            mNumberBar.progress = 0
            val download: AltexImageDownloader = AltexImageDownloader(object : AltexImageDownloader.OnImageLoaderListener {
                override fun onError(error: AltexImageDownloader.ImageError) {
                    Toast.makeText(activity, "Error " + error.toString(), Toast.LENGTH_SHORT).show()
                }

                override fun onProgressChange(percent: Int) {
                    mNumberBar.incrementProgressBy(1)
                    Log.d(TAG, " onProgressChange" + percent)
                }

                override fun onComplete(result: Bitmap) {
                    mFabRepostButton.visibility = View.VISIBLE
                    mNumberBar.progress = 100
                    mBitMapImageToShare = result
                    val bitmapPath: String = MediaStore.Images.Media.insertImage(context.getContentResolver(), mBitMapImageToShare, "title", null);
                    val bitmapUri: Uri = Uri.parse(bitmapPath)
                    Log.d(TAG, "onImageComplate " + getRealPathFromURI(bitmapUri))
                    mPost.pathToStorage = getRealPathFromURI(bitmapUri)
                    saveToPraf(mPost)
                }

            })
            download.download(mPost.imageURL, true)


        }
    }

    fun getRealPathFromURI(uri: Uri): String {
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
        cursor!!.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return cursor.getString(idx)
    }
    private fun saveToPraf(mPost: post) {
        try {
            SharedPreferencesManager.getInstance().putValue(mPost.postID, mPost);
        } catch (ex: Exception) {
            FirebaseCrash.report( Exception("  private fun saveToPraf Error code 8 Error : "+ex.message))
            Toast.makeText(activity,"Some thing wrong Error code 8 Error message : "+ex.message,Toast.LENGTH_LONG).show()

        }
    }

    fun hideKeybord() {
        val view = activity.getCurrentFocus()
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }
    }
}

