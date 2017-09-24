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
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.artjimlop.altex.AltexImageDownloader
import com.artjimlop.altex.AltexImageDownloader.ImageError
import com.artjimlop.altex.AltexImageDownloader.OnImageLoaderListener
import com.daimajia.numberprogressbar.NumberProgressBar
import com.daimajia.numberprogressbar.OnProgressBarListener
import com.golshadi.majid.core.DownloadManagerPro
import com.golshadi.majid.report.ReportStructure
import com.golshadi.majid.report.listener.DownloadManagerListener
import com.squareup.picasso.Picasso
import lolodev.permissionswrapper.callback.OnRequestPermissionsCallBack
import lolodev.permissionswrapper.wrapper.PermissionWrapper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser


class MainActivity : AppCompatActivity(), DownloadManagerListener, OnProgressBarListener {
    override fun onProgressChange(current: Int, max: Int) {

    }

    private var TAG = "MainActivityTAG"
    override fun OnDownloadStarted(taskId: Long) {
        runOnUiThread {
            mNumberBar.progress = 0
            mFabShareButton.visibility = 0
        }
        Log.d(TAG, "OnDownloadStarted")
    }

    override fun OnDownloadPaused(taskId: Long) {
        Log.d(TAG, "OnDownloadPaused")
    }

    override fun onDownloadProcess(taskId: Long, percent: Double, downloadedLength: Long) {

        runOnUiThread { mNumberBar.incrementProgressBy(1) }
        Log.d(TAG, "onDownloadProcess" + percent)
    }

    override fun OnDownloadFinished(taskId: Long) {
        Log.d(TAG, "OnDownloadFinished")
    }

    private fun shareIntent() {
        try {
            if(mNumberBar.progress == 100){
                if (mPost.medium.equals("image"))
                    shareImageIntentToInstagram()
                else
                    shareVideoIntentToInstagram()
            }else{
                Toast.makeText(this@MainActivity,"downloading sill in progress ",Toast.LENGTH_SHORT).show()
            }

        } catch (ex: Exception) {
            Toast.makeText(this@MainActivity, "some thing working while sharing Error: " + ex.message, Toast.LENGTH_LONG).show()
        }


    }

    private fun shareVideoIntentToInstagram() {
        val repor: ReportStructure = dm.singleDownloadStatus(taskToken);
        val int: InstaIntent = InstaIntent()
        int.createInstagramIntent("video/*", repor.saveAddress, this@MainActivity)
    }

    private fun shareImageIntentToInstagram() {
        try {
            val bitmapPath: String = MediaStore.Images.Media.insertImage(getContentResolver(), mBitMapImageToShare, "title", null);
            val bitmapUri: Uri = Uri.parse(bitmapPath)
            val intent: Intent = Intent(Intent.ACTION_SEND)
            Log.d(TAG,"uri="+bitmapUri.toString())
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            intent.setPackage("com.instagram.android")
            startActivity(intent)
        }catch (e : Exception){

        }

    }

    override fun OnDownloadRebuildStart(taskId: Long) {
        Log.d(TAG, "OnDownloadRebuildStart") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnDownloadRebuildFinished(taskId: Long) {
        Log.d(TAG, "OnDownloadRebuildFinished") //To change body of created functions use File | Settings | File Templates.
    }

    override fun OnDownloadCompleted(taskId: Long) {
        runOnUiThread {
            mNumberBar.progress = 100
            mFabShareButton.visibility = 1
        }
        Log.d(TAG, "OnDownloadCompleted") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connectionLost(taskId: Long) {
        Log.d(TAG, "connectionLost") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var mEditTextInputURl: EditText
    private lateinit var mCheckAndSaveButton: Button
    private lateinit var mImage: ImageView
    protected lateinit var mHashTagTextView: TextView
    private lateinit var mCopyHashTagButton: Button
    private lateinit var mDescription: TextView
    private lateinit var mNumberBar: NumberProgressBar
    private lateinit var mFabShareButton: FloatingActionButton
    private lateinit var mCardView: CardView
    private lateinit var mProgressBar: ProgressBar

    private lateinit var dm: DownloadManagerPro
    private var taskToken: Int = -1
    private lateinit var mBitMapImageToShare: Bitmap
    val mPost = post()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")
        staupUI()
        copyDataFromClipBrod()
        setUpListerners()
        dm = DownloadManagerPro(this@MainActivity)
        dm.init("DMinstaDownload/", 12, this@MainActivity)
        mFabShareButton.setOnClickListener({ shareIntent() })
        mCopyHashTagButton.setOnClickListener({ copyHashTagToClipBord() })
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onRestart() {
        super.onRestart()
        copyDataFromClipBrod()
    }

    private fun copyHashTagToClipBord() {
        if (!mHashTagTextView.text.isEmpty()) {
            val clipbordHelper: ClipBrodHelper = ClipBrodHelper()
            clipbordHelper.WriteToClipBord(this@MainActivity, mHashTagTextView.text.toString())
        }else{
            Toast.makeText(this@MainActivity,"No Hash tag not find in this post",Toast.LENGTH_SHORT).show()
        }

    }

    private fun setUpListerners() {
        mCheckAndSaveButton.setOnClickListener({ checkBuildNO() })
        mNumberBar.setOnProgressBarListener(this@MainActivity);
    }

    private fun copyDataFromClipBrod() {
        if (!ClipBrodHelper(this@MainActivity).clipBrodText.isNullOrEmpty()) {
            mEditTextInputURl.text.clear()
            mEditTextInputURl.setText(ClipBrodHelper(this@MainActivity).clipBrodText)
            hideKeybord()
            checkBuildNO()
        }
    }


    fun staupUI() {
        mEditTextInputURl = findViewById(R.id.URL_Input_edit_text) as EditText
        mCheckAndSaveButton = findViewById(R.id.chack_and_save_post) as Button
        mImage = findViewById(R.id.imageView) as ImageView
        mHashTagTextView = findViewById(R.id.hash_tag_text_view) as TextView
        mDescription = findViewById(R.id.textView_description) as TextView
        mCopyHashTagButton = findViewById(R.id.copy_hash_tag_button) as Button
        mNumberBar = findViewById(R.id.number_progress_bar) as NumberProgressBar
        mFabShareButton = findViewById(R.id.floatingActionButtonShare) as FloatingActionButton
        mCardView = findViewById(R.id.cardView) as CardView
        mProgressBar = findViewById(R.id.progressBar) as ProgressBar

    }

    fun checkBuildNO() {
        if (Build.VERSION.SDK_INT > 22) {
            if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            askPermistion()
            else{
                val msg: String = mEditTextInputURl.text.toString()

                if (!msg.equals(""))
                    grabData(mEditTextInputURl.text.toString()).execute()

            }

        } else {
            val msg: String = mEditTextInputURl.text.toString()

            if (!msg.equals(""))
                grabData(mEditTextInputURl.text.toString()).execute()
        }
    }

    private fun askPermistion() {
        PermissionWrapper.Builder(this)
                .addPermissions(arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                //enable rationale message with a custom message
                .addRequestPermissionsCallBack(object : OnRequestPermissionsCallBack {
                    override fun onGrant() {
                        val msg: String = mEditTextInputURl.text.toString()

                        if (!msg.equals(""))
                            grabData(mEditTextInputURl.text.toString()).execute()
                        //  grabData(mEditTextInputURl.text.toString()).execute()
                    }

                    override fun onDenied(permission: String) {

                    }
                }).build().request()
    }


    inner class grabData(val ConnURL: String) : AsyncTask<Void, Void, String>() {
        val mHashTags = StringBuilder()

        override fun onPreExecute() {
            super.onPreExecute()
            mProgressBar.progress =100
            mProgressBar.visibility = View.VISIBLE
            mCardView.visibility = View.INVISIBLE
            hideKeybord()
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
                    if (meta.attr("property").equals("og:url"))
                        mPost.postDownloadingName = meta.attr("content").replace("https://www.instagram.com/p/", "")

                }


            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return "";
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.d("dec", mPost.content + "mPost.imageURL= " + mPost.imageURL + "mPost.medium " + mPost.medium)
            UpdateUI()
            intiDownloader()
        }

        private fun UpdateUI() {
            mProgressBar.visibility = View.INVISIBLE

            mCardView.visibility = 1
            mHashTagTextView.setText(mPost.hashTags)
            if (!mPost.content.isNullOrEmpty())
                mDescription.setText(mPost.content.substring(mPost.content.indexOf(":"), mPost.content.length))
            Picasso.with(this@MainActivity).load(mPost.imageURL).into(mImage)
        }

        private fun intiDownloader() {

            mFabShareButton.visibility = 0
            try {
                taskToken = dm.addTask(mPost.postDownloadingName.replace("/", ""), mPost.videoURL, true, false)
                Toast.makeText(this@MainActivity, "Downloading start", Toast.LENGTH_SHORT).show()
                if (mPost.medium != "image")
                    dm.startDownload(taskToken)
                else
                    downloadImage()
            } catch (ex: Exception) {

            }
        }

        private fun downloadImage() {
            Log.d(TAG, " downloadImage")
            // var download :AltexImageDownloader =  AltexImageDownloader.writeToDisk(this@MainActivity, mPost.imageURL, this@MainActivity.packageName)
            mNumberBar.progress = 0
            val download: AltexImageDownloader = AltexImageDownloader(object : OnImageLoaderListener {
                override fun onError(error: ImageError) {
                    Toast.makeText(this@MainActivity, "Error " + error.toString(), Toast.LENGTH_SHORT).show()
                }

                override fun onProgressChange(percent: Int) {
                    mNumberBar.incrementProgressBy(1)
                    Log.d(TAG, " onProgressChange" + percent)
                }

                override fun onComplete(result: Bitmap) {
                    Log.d(TAG, "onImageComplate " + result.toString())
                    mFabShareButton.visibility = View.VISIBLE
                    mNumberBar.progress = 100
                    this@MainActivity.mBitMapImageToShare = result
                }

            })
            download.download(mPost.imageURL, true)

        }
    }

    fun hideKeybord() {
        val view = this@MainActivity.getCurrentFocus()
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }
    }
}
