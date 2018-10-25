package com.ameerhamza6733.directmessagesaveandrepost

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.ameerhamza6733.directmessagesaveandrepost.Settings.ATO_START_DOWNLOADING_
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.crashlytics.android.Crashlytics
import com.daimajia.numberprogressbar.NumberProgressBar
import com.daimajia.numberprogressbar.OnProgressBarListener
import com.github.clans.fab.FloatingActionButton
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.squareup.picasso.Picasso
import lolodev.permissionswrapper.callback.OnRequestPermissionsCallBack
import lolodev.permissionswrapper.wrapper.PermissionWrapper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import java.io.File

/**
 * Created by AmeerHamza on 10/6/2017.
 */

/**
 * Decide()
 * commit()
 * act()
 * succeeded()
 * repeat()
 */

class DownloadingFragment : Fragment() {


    companion object {
        private val ARG_CAUGHT = "myFragment_caught"

        fun newInstance(temp: Int): DownloadingFragment {

            return DownloadingFragment()
        }
    }


    private val fileDownloadListener = object : FileDownloadListener() {
        override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {}

        override fun started(task: BaseDownloadTask?) {
            Toast.makeText(numberProgressBar.context, "Please wait", Toast.LENGTH_SHORT).show()
            numberProgressBar.max = 100
            numberProgressBar.progress = 0
        }

        override fun connected(task: BaseDownloadTask?, etag: String?, isContinue: Boolean, soFarBytes: Int, totalBytes: Int) {}

        override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
            numberProgressBar.progress = (soFarBytes * 100 / totalBytes)
        }

        override fun blockComplete(task: BaseDownloadTask?) {}

        override fun retry(task: BaseDownloadTask?, ex: Throwable?, retryingTimes: Int, soFarBytes: Int) {

        }

        override fun completed(task: BaseDownloadTask) {
            numberProgressBar.progress = 100

            activity!!.runOnUiThread {
                Toast.makeText(activity, "Downloading complete", Toast.LENGTH_SHORT).show()
                numberProgressBar.progress = 100


                mFabRepostButton.visibility = View.VISIBLE
                mFabShareButton.visibility = View.VISIBLE

                if(mPost.medium!="image") btPlayVideo.visibility=View.VISIBLE

                mPost.pathToStorage = task.path
                this@DownloadingFragment.task=task

                activity?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, FileProvider.getUriForFile(activity!!,
                        BuildConfig.APPLICATION_ID + ".provider",
                        File(task.path)) ))
                MediaScannerConnection.scanFile(activity?.applicationContext,
                        arrayOf(task.path), null
                ) { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "-> uri=$uri")
                }
                saveToPraf(mPost)
                Crashlytics.log("OnDownloadCompleted")
            }
        }

        override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {}

        override fun error(task: BaseDownloadTask, e: Throwable) {

            Snackbar.make(numberProgressBar, "Error: ", Snackbar.LENGTH_LONG).show()

        }

        override fun warn(task: BaseDownloadTask) {}
    }

    private fun shareIntent(repost: Boolean) {

        try {
            if (numberProgressBar.progress == 100) {

                if (mPost.medium.equals("image"))
                    shareImageIntentToInstagram(repost)
                else
                    shareVideoIntentToInstagram(repost)
            } else {
                Toast.makeText(activity, "downloading sill in progress ", Toast.LENGTH_SHORT).show()
            }


        } catch (ex: Exception) {
            Crashlytics.logException(ex);
            // FirebaseCrash.report(Exception(" private fun shareIntent Error code 3 Error : " + ex.message));
            Toast.makeText(activity, "some thing working while sharing Error: code 3  " + ex.message, Toast.LENGTH_LONG).show()
        }


    }

    private fun shareVideoIntentToInstagram(repost: Boolean) {
        val int: InstaIntent = InstaIntent()
        int.createVideoInstagramIntent("video/*", task?.path, activity, repost)
    }

    private fun shareImageIntentToInstagram(repost: Boolean) {
        try {

            InstaIntent().createVideoInstagramIntent("image/*", mPost.pathToStorage, activity, repost);
        } catch (e: Exception) {
            Crashlytics.logException(e);
            //  FirebaseCrash.report(Exception("private fun shareImageIntentToInstagram Error code 4 Error : " + e.message))
            Toast.makeText(activity, "Some thing wrong Error code 4 Error message : " + e.message, Toast.LENGTH_LONG).show()
        }

    }


    private lateinit var mEditTextInputURl: EditText
    private lateinit var mCheckAndSaveButton: Button
    private lateinit var mImage: ImageView
    protected lateinit var mHashTagTextView: TextView
    private lateinit var mCopyHashTagButton: Button
    private lateinit var mCopyCaptionButton: Button
    private lateinit var mCopyBothButton: Button
    private lateinit var mCaptionTextView: TextView
    private lateinit var numberProgressBar: NumberProgressBar
    private lateinit var mFabRepostButton: FloatingActionButton
    private lateinit var mFabShareButton: FloatingActionButton
    private lateinit var mCardView: CardView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var rootView: View;
    private lateinit var rootCardView:CardView
    private lateinit var btPlayVideo:AppCompatImageView

    private var task: BaseDownloadTask?=null
    private lateinit var mBitMapImageToShare: Bitmap
    val mPost = Post()
    lateinit var postKeyFromShardPraf: String

    private var manualyDownload = false
    private var atoSaveStartDownloading :Boolean=true;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_download, container, false)

        staupUI(view)
        rootView = view
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        FileDownloader.setup(activity)
        var rateMe = My_Share_Pref.getRateMe(activity!!)
        var firstTIme =My_Share_Pref.getISFirsTime(activity!!)
        if (firstTIme) {
            val intent = Intent(activity, MyInstructionActivity::class.java)
            activity?.startActivity(intent)
        }
        if (!firstTIme && rateMe) {
            showRateMe()
        }
        atoSaveStartDownloading =My_Share_Pref.getAtoSave(activity!!)


        setUpListerners()
        copyDataFromClipBrod()

    }

    private fun copyHashTagToClipBord() {
        var mainActivity = activity as MainActivity
        if (mainActivity != null)
            mainActivity.showAds()
        if (!mHashTagTextView.text.isEmpty()) {
            val clipbordHelper = ClipBrodHelper()
            clipbordHelper.WriteToClipBord(activity, mHashTagTextView.text.toString())
        } else {
            Toast.makeText(activity, "No Hash tag not find in this Post", Toast.LENGTH_SHORT).show()
        }

    }

    private fun CopyBoth() {
        var mainActivity = activity as MainActivity
        if (mainActivity != null)
            mainActivity.showAds()
        var hashTagAndCaption = "";
        if (!mHashTagTextView.text.isEmpty()) {
            hashTagAndCaption = mHashTagTextView.text.toString();
        }
        if (!mCaptionTextView.text.isEmpty()) {
            hashTagAndCaption += mCaptionTextView.text.toString()
        }
        val clipbordHelper = ClipBrodHelper()
        clipbordHelper.WriteToClipBord(activity, hashTagAndCaption)
    }

    private fun copyCaptionToClipBord() {
        var mainActivity = activity as MainActivity
        if (mainActivity != null)
            mainActivity.showAds()
        if (!mCaptionTextView.text.isEmpty()) {

            val clipbordHelper = ClipBrodHelper()
            clipbordHelper.WriteToClipBord(activity, mCaptionTextView.text.toString())

        }


    }

    private fun RemoveHashTagFromCaption() {

        if (mPost.content != null && !mPost.content.isEmpty()) {
            if (mPost.hashTags != null && !mPost.hashTags.isEmpty()) {
                mPost.content = mPost.content.replace("#", "")
                var hashTagsArray = mPost.hashTags.split("#")
                hashTagsArray
                        .filter { mPost.content.toLowerCase().contains(it.toLowerCase()) }
                        .forEach { mPost.content = mPost.content.replace(it, "") }
                mCaptionTextView.text = mPost.content
            }
        }


    }

    private fun setUpListerners() {
        mCheckAndSaveButton.setOnClickListener({ manualyDownload = true; checkBuildNO() })

        mFabRepostButton.setOnClickListener({ shareIntent(true) })
        mFabShareButton.setOnClickListener({ shareIntent(false) })
        mCopyHashTagButton.setOnClickListener({ copyHashTagToClipBord() })
        mCopyCaptionButton.setOnClickListener({ copyCaptionToClipBord() })
        mCopyBothButton.setOnClickListener({ CopyBoth() })
        btPlayVideo.setOnClickListener { PlayVideo() }
    }

    private fun PlayVideo() {


            var internt = Intent(activity,PlayerActivity::class.java)
            internt.putExtra(PlayerActivity.EXTRA_VIDEO_PATH,mPost.pathToStorage)
        activity?.startActivity(internt)

    }

    private fun downloadCaption(postUrl: String) {

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, "https://api.instagram.com/oembed/?url=" + postUrl, null,
                Response.Listener { response ->
                    var text = response.getString("title")
                    if (!text.isEmpty()) {
                        mPost.content = response.getString("title")
                        RemoveHashTagFromCaption()
                    }

                },
                Response.ErrorListener { error ->
                    // TODO: Handle error
                    error.printStackTrace()
                    Toast.makeText(activity, "Unable to get full caption", Toast.LENGTH_LONG).show()
                }
        )
        Volley.newRequestQueue(activity).add(jsonObjectRequest);


    }

    override fun onPause() {
        super.onPause()
        if (mEditTextInputURl != null)
            mEditTextInputURl.setText("")
    }

    private fun copyDataFromClipBrod() {
        rootCardView.visibility=View.INVISIBLE
        if (!ClipBrodHelper(activity).clipBrodText.isNullOrEmpty()) {
            try {
                if (mPost != null) {
                    if (ClipBrodHelper(activity).clipBrodText.equals(mPost.url)) {
                        Toast.makeText(activity, "Post Already downloaded ", Toast.LENGTH_SHORT).show()
                    } else {
                        mEditTextInputURl.text.clear()
                        mEditTextInputURl.setText(ClipBrodHelper(activity).clipBrodText)
                        hideKeybord()

                    }
                }
            } catch (Ex: Exception) {
            }
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
        mCaptionTextView = view.findViewById<TextView>(R.id.textView_description) as TextView
        mCopyCaptionButton = view.findViewById<Button>(R.id.copy_caption)
        mCopyBothButton = view.findViewById<Button>(R.id.copy_both)
        mCopyHashTagButton = view.findViewById<Button>(R.id.copy_hash_tag_button) as Button
        numberProgressBar = view.findViewById<NumberProgressBar>(R.id.number_progress_bar) as NumberProgressBar
        mFabRepostButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButtonRepost) as com.github.clans.fab.FloatingActionButton
        mFabShareButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButtonShare) as FloatingActionButton
        mCardView = view.findViewById<CardView>(R.id.cardView) as CardView
        mProgressBar = view.findViewById<ProgressBar>(R.id.progressBar) as ProgressBar
        rootCardView=view.findViewById(R.id.cardView)
        btPlayVideo=view.findViewById(R.id.btPlay)

    }

    fun checkBuildNO() {

        if (Build.VERSION.SDK_INT > 22) {
            if (activity!!.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                askPermistion()
            else {
                val msg: String = mEditTextInputURl.text.toString()

                if (!msg.equals(""))
                    if (!checkIFPosAllreadyDownloaded(mEditTextInputURl.text.toString()))
                        intiDownloader()
            }

        } else {
            val msg: String = mEditTextInputURl.text.toString()

            if (!msg.equals(""))
                if (!checkIFPosAllreadyDownloaded(mEditTextInputURl.text.toString()))
                    intiDownloader()
        }
    }

    private fun intiDownloader() {
        if (atoSaveStartDownloading || manualyDownload) {
            rootCardView.visibility=View.VISIBLE
            if (!mEditTextInputURl.text.toString().isEmpty()) {
                grabData(mEditTextInputURl.text.toString()).execute()
                downloadCaption(mEditTextInputURl.text.toString())
            }

        } else {
            mProgressBar.visibility = View.INVISIBLE
        }
    }

    private var mContext: Context? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
        Crashlytics.log("onAttach");
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
        Crashlytics.log("onDetach");
    }

    private fun askPermistion() {
        PermissionWrapper.Builder(activity)
                .addPermissions(arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                //enable rationale message with a custom message
                .addRequestPermissionsCallBack(object : OnRequestPermissionsCallBack {
                    override fun onGrant() {
                        val msg: String = mEditTextInputURl.text.toString()

                        if (!msg.equals(""))
                            if (!checkIFPosAllreadyDownloaded(mEditTextInputURl.text.toString()))
                                intiDownloader()
                        grabData(mEditTextInputURl.text.toString()).execute()
                    }

                    override fun onDenied(permission: String) {

                    }
                }).build().request()
    }

    private fun checkIFPosAllreadyDownloaded(toString: String): Boolean {

            return false

    }


    @SuppressLint("StaticFieldLeak")
    inner class grabData(val ConnURL: String) : AsyncTask<Void, Void, String>() {
        val mHashTags = StringBuilder()
        private var isSomeThingWrong = false
        var document: Document? = null;

        override fun onPreExecute() {
            super.onPreExecute()

            mPost.url = ConnURL;
            mProgressBar.progress = 100
            mProgressBar.visibility = View.VISIBLE
            mCardView.visibility = View.INVISIBLE
            btPlayVideo.visibility = View.INVISIBLE
            hideKeybord()


        }

        override fun doInBackground(vararg p0: Void?): String {
            try {
                document = Jsoup.connect(ConnURL).timeout(6000).ignoreContentType(true).parser(Parser.htmlParser()).get()
            } catch (Ex: Exception) {
                Ex.printStackTrace()
                isSomeThingWrong = true
            }
            try {
                if (document != null) {
                    for (meta in document!!.select("meta")) {

                        if ((meta.attr("property").equals("instapp:hashtags") || meta.attr("property").equals("video:tag")))
                            mPost.hashTags = mHashTags.append("#").append(meta.attr("content")).append(" ")
                        if (meta.attr("property").equals("og:image"))
                            mPost.imageURL = meta.attr("content")
//                    if (meta.attr("property").equals("og:description"))
//                        mPost.content = meta.attr("content")

                        if (meta.attr("property").equals("og:video"))
                            mPost.videoURL = meta.attr("content")
                        if (meta.attr("name").equals("medium"))
                            mPost.medium = meta.attr("content")
                        if (meta.attr("property").equals("og:url")) {
                            mPost.postID = meta.attr("content").replace("https://www.instagram.com/p/", "")
                            mPost.postID = mPost.postID.replace("/", "")
                        }
                    }
                }


            } catch (ex: Exception) {
                ex.printStackTrace()

                activity?.runOnUiThread({
                    Toast.makeText(activity, "Please try again later and check your intent connection  Error code 111  ", Toast.LENGTH_SHORT).show()
                    isSomeThingWrong = true
                    Crashlytics.logException(ex);
                })

            }
            return "";
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (!isSomeThingWrong) {

                UpdateUI()
                Downloader()
            } else {
                mProgressBar.visibility = View.INVISIBLE
                Snackbar.make(mCardView, "unable to get downloading url", Snackbar.LENGTH_INDEFINITE).setAction("try again") {
                    if (!mEditTextInputURl.text.toString().isEmpty()) {
                        grabData(mEditTextInputURl.text.toString()).execute()
                        downloadCaption(mEditTextInputURl.text.toString())
                    }
                }.show()
            }
        }
    }

        private fun UpdateUI() {
            mProgressBar.visibility = View.INVISIBLE

            mCardView.visibility = View.VISIBLE
            mHashTagTextView.setText(mPost.hashTags)
            try {
                if (!mPost.content.isNullOrEmpty() && isContainsColan()) {
                    mPost.content = mPost.content.substring(mPost.content.indexOf(":"), mPost.content.length)

                }
                mCaptionTextView.text=mPost.content


            } catch (Ex: Exception) {
                Toast.makeText(activity, "Some thing wrong Error code 6 Error message : " + Ex.message, Toast.LENGTH_LONG).show()
                Ex.printStackTrace()
            }
            Picasso.get().load( mPost.imageURL) .into(mImage)

        }

        private fun isContainsColan(): Boolean = mPost.content.contains(":")

        private fun Downloader() {

            mFabRepostButton.visibility = View.GONE
            mFabShareButton.visibility = View.GONE

            try {

                var downloadingFileUrl =""
                downloadingFileUrl = if(mPost.medium=="image"){
                    mPost.imageURL
                }else{
                    mPost.videoURL
                }
                val filePaht = getRootDirPath() + "/" + (System.currentTimeMillis().toString() + getFileExtenstion(downloadingFileUrl))
                if (filePaht!=null){
                    FileDownloader.getImpl().create(downloadingFileUrl)
                            .setPath(filePaht)
                            .setListener(fileDownloadListener).start()
                } else {
                    Toast.makeText(activity, "Unable to access to external storage ", Toast.LENGTH_LONG).show()
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(activity, "Some thing wrong Error code 7 Error message : " + ex.message, Toast.LENGTH_LONG).show()

            }
        }

    fun getFileExtenstion(url: String): String {
        return url.substring(url.lastIndexOf("."))
    }
    fun getRootDirPath(): String? {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).absolutePath
        } else
            null
    }



    private fun saveToPraf(mPost: Post) {
        try {

                My_Share_Pref.savePost(activity!!,mPost.url,mPost)
        } catch (ex: Exception) {
            Toast.makeText(activity, "Some thing wrong Error code 8 Error message : " + ex.message, Toast.LENGTH_LONG).show()

        }
    }

    fun hideKeybord() {
        val view = activity!!.getCurrentFocus()
        if (view != null) {
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

    }


    private fun showRateMe() {
        val builder: AlertDialog.Builder

        builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Rate Me")
                .setMessage("Copy caption and tag need your help please rate us on google play")
                .setPositiveButton("Please Rate Me") { dialog, which ->
                    // continue with delete
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity?.packageName)))
                }
                .setNegativeButton("never") { dialog, which ->
                    My_Share_Pref.saveRateMe(activity!!,false)

                }
                .setIcon(android.R.drawable.star_big_on)
                .show()
    }

}