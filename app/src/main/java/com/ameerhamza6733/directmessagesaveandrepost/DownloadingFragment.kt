package com.ameerhamza6733.directmessagesaveandrepost

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.*
import com.ameerhamza6733.directmessagesaveandrepost.utils.Constants
import com.ameerhamza6733.directmessagesaveandrepost.utils.Constants.LOGIN_RESULT_CODE
import com.ameerhamza6733.directmessagesaveandrepost.utils.CookieUtils
import com.ameerhamza6733.directmessagesaveandrepost.utils.CookieUtils.settingsHelper
import com.ameerhamza6733.directmessagesaveandrepost.utils.IntentUtils
import com.ameerhamza6733.directmessagesaveandrepost.utils.NetworkUtils
import com.ameerhamza6733.directmessagesaveandrepost.workers.CheckPostPermission
import com.ameerhamza6733.directmessagesaveandrepost.workers.PostScrapWorker
import com.daimajia.numberprogressbar.NumberProgressBar
import com.github.clans.fab.FloatingActionButton
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.squareup.picasso.Picasso
import lolodev.permissionswrapper.callback.OnRequestPermissionsCallBack
import lolodev.permissionswrapper.wrapper.PermissionWrapper
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File
import java.net.HttpURLConnection
import java.util.*


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


    val Crashlytics=FirebaseCrashlytics.getInstance()
    companion object {
        private val ARG_CAUGHT = "myFragment_caught"
        private val TAG="DownloadingFragmentTAG"

        fun newInstance(temp: Int): DownloadingFragment {

            return DownloadingFragment()
        }
    }


    private val fileDownloadListener = object : FileDownloadListener() {
        override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {}

        override fun started(task: BaseDownloadTask?) {
          if (userCookiesSaved){
              firebaseAnalytics?.logEvent("downloadingStartedLogin", null);
          }else{
              firebaseAnalytics?.logEvent("downloadingStarted", null);
          }
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
           Crashlytics.log("retry")
            ex?.let { Crashlytics?.recordException(it) }
            val bundel=Bundle()
            bundel.putString("retryError", ex?.message)
            firebaseAnalytics?.logEvent("retry", bundel);
        }

        override fun completed(task: BaseDownloadTask) {

            activity?.let {
                it.runOnUiThread {
                   if (userCookiesSaved){
                       firebaseAnalytics?.logEvent("downloadingCompletedLogin", null);
                   }else{
                       firebaseAnalytics?.logEvent("downloadingCompleted", null);
                   }
                    numberProgressBar.progress = 100
                    Toast.makeText(activity, "Downloading complete", Toast.LENGTH_SHORT).show()
                    numberProgressBar.progress = 100


                    mFabRepostButton.visibility = View.VISIBLE
                    mFabShareButton.visibility = View.VISIBLE

                    if(mPost?.medium!="image") btPlayVideo.visibility=View.VISIBLE

                    mPost?.pathToStorage = task.path
                    this@DownloadingFragment.task=task

                    activity?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, FileProvider.getUriForFile(it,
                            BuildConfig.APPLICATION_ID + ".provider",
                            File(task.path))))
                    MediaScannerConnection.scanFile(activity?.applicationContext,
                            arrayOf(task.path), null
                    ) { path, uri ->
                        Log.i("ExternalStorage", "Scanned $path:")
                        Log.i("ExternalStorage", "-> uri=$uri")
                    }
                    mPost?.let { it1 -> saveToPraf(it1) }
                    Crashlytics.log("OnDownloadCompleted")
                }
            }

        }

        override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {}

        override fun error(task: BaseDownloadTask, e: Throwable) {
            Crashlytics.log("error")
           e.printStackTrace()
            Crashlytics?.recordException(e)
           if (userCookiesSaved){
               firebaseAnalytics?.logEvent("downloadingErrorLogin", null);
           }else{
               firebaseAnalytics?.logEvent("downloadingError", null);
           }
            if (activity!=null && isAdded){
                Snackbar.make(rootCardView, "Error: i am working on this error", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry") {
                            userCookiesSaved=true
                            firebaseAnalytics?.logEvent("manually_retry", null)

                        }.show()
            }

        }

        override fun warn(task: BaseDownloadTask) {}
    }

    private fun shareIntent(repost: Boolean) {

        try {
            if (numberProgressBar.progress == 100) {

                if (mPost?.medium.equals("image"))
                    shareImageIntentToInstagram(repost)
                else
                    shareVideoIntentToInstagram(repost)
            } else {
                Toast.makeText(activity, "downloading sill in progress ", Toast.LENGTH_SHORT).show()
            }


        } catch (ex: Exception) {
            Crashlytics.recordException(ex);
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

            InstaIntent().createVideoInstagramIntent("image/*", mPost?.pathToStorage, activity, repost);
        } catch (e: Exception) {
            Crashlytics.recordException(e);
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
    private lateinit var mAdPlaceHolder:FrameLayout
    private lateinit var rootView: View;
    private lateinit var rootCardView: CardView
    private lateinit var btPlayVideo:AppCompatImageView
    private  var mNativeAd:NativeAd?=null
    private lateinit var remoteConfig: FirebaseRemoteConfig
//    private lateinit var nativeAdTempalte: TemplateView
    private var firebaseAnalytics:FirebaseAnalytics?=null
    private var userCookiesSaved=false

    private var task: BaseDownloadTask?=null
    private var observerScrapPost:Observer<WorkInfo>?=null
    private var observerCheckPostPermission:Observer<WorkInfo>?=null
    private lateinit var mBitMapImageToShare: Bitmap
    private  var mPost:Post?=null
    lateinit var postKeyFromShardPraf: String

    private var manualyDownload = false
    private var atoSaveStartDownloading :Boolean=true;


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_download, container, false)
       firebaseAnalytics=FirebaseAnalytics.getInstance(activity)
        remoteConfig = Firebase.remoteConfig
        staupUI(view)
        rootView = view
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FileDownloader.setup(activity)
        var rateMe = My_Share_Pref.getRateMe(activity!!)
        var firstTIme =My_Share_Pref.getISFirsTime(activity!!)
        if (firstTIme) {
            val intent = Intent(activity, MyInstructionActivity::class.java)
            activity?.startActivity(intent)
        }
        if (!firstTIme && rateMe) {

        }
        atoSaveStartDownloading =My_Share_Pref.getAtoSave(activity!!)


        setUpListerners()
        copyDataFromClipBrod()
        //laodNativeAd()
       if (remoteConfig.getBoolean(RemoteConfigConstants.DISPLAY_NATIVE_ADS_DOWNLOADING_SCREEN)){
           refreshAd()
       }

    }

    override fun onDestroy() {
        mNativeAd?.destroy()
//        nativeAdTempalte.nativeAdView.destroy() // This was the part I didn't expect to be needed
//        nativeAdTempalte.removeAllViews()
        super.onDestroy()
    }


    private fun refreshAdIntoNativeAdTempalte(){

    }

    private fun refreshAd() {

        val builder = AdLoader.Builder(activity, getString(R.string.native_ad_real_id))
        builder.forNativeAd(
                OnNativeAdLoadedListener { nativeAd ->

                    if (isAdded && activity != null) {

                        mNativeAd?.destroy()
//                        nativeAdTempalte.nativeAdView.destroy() // This was the part I didn't expect to be needed
//                        nativeAdTempalte.removeAllViews()
                        mNativeAd = nativeAd

//                        val styles = NativeTemplateStyle.Builder().build()
//                        nativeAdTempalte.setStyles(styles)
//                        nativeAdTempalte.setNativeAd(mNativeAd)
                        val adView = layoutInflater.inflate(R.layout.native_ad, null) as NativeAdView
                        populateNativeAdView(nativeAd, adView)
                        mAdPlaceHolder.removeAllViews()
                        mAdPlaceHolder.addView(adView)

                    } else {
                        nativeAd.destroy()
                        return@OnNativeAdLoadedListener
                    }

                })
        val videoOptions = VideoOptions.Builder().build()
        val adOptions: NativeAdOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder
                .withAdListener(
                        object : AdListener() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {

                                val error = String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.domain,
                                        loadAdError.code,
                                        loadAdError.message)

                            }
                        })
                .build()
        val adRequest = if (  ConsentInformation.getInstance(activity!!.applicationContext).consentStatus==ConsentStatus.PERSONALIZED){
           AdRequest.Builder()

                    .build()
        }else{
             AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                    .build()
        }
        adLoader.loadAd(adRequest)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

       if (requestCode== LOGIN_RESULT_CODE && resultCode==Activity.RESULT_OK){
           val cookie = data!!.getStringExtra("cookie")
           CookieUtils.setupCookies(cookie)
           settingsHelper.putString(Constants.COOKIE, cookie)
           firebaseAnalytics?.logEvent("cookieStored", null)
           // No use as the timing of show is unreliable
           // Toast.makeText(getContext(), R.string.login_success_loading_cookies, Toast.LENGTH_SHORT).show();

           // adds cookies to database for quick access
           // No use as the timing of show is unreliable
           // Toast.makeText(getContext(), R.string.login_success_loading_cookies, Toast.LENGTH_SHORT).show();

           // adds cookies to database for quick access
           val uid: Long = CookieUtils.getUserIdFromCookie(cookie)
           checkBuildNO()

       }
    }



    private fun copyHashTagToClipBord() {


        activity?.let { activity->
            if (mPost?.hashTags?.toString()?.isNotEmpty()==true) {
                val clipbordHelper = ClipBrodHelper()
                clipbordHelper.WriteToClipBord(activity, mHashTagTextView.text.toString())
            } else {
                Toast.makeText(activity, "No Hash tag not find in this Post", Toast.LENGTH_SHORT).show()
            }

            if (activity is MainActivity){
                activity.showAds()
            }
        }
    }

    private fun CopyBoth() {



        activity?.let { activity->
            var hashTagAndCaption = mPost?.content;

            val clipbordHelper = ClipBrodHelper()
            clipbordHelper.WriteToClipBord(activity!!.applicationContext, hashTagAndCaption)
            if (activity is MainActivity){
                activity.showAds()
            }
        }
    }

    private fun copyCaptionToClipBord() {


        activity?.let { activity->
            if (!mCaptionTextView.text.isEmpty()) {

                val clipbordHelper = ClipBrodHelper()
                clipbordHelper.WriteToClipBord(activity, mCaptionTextView.text.toString())

            }
            if (activity is MainActivity){
                activity.showAds()
            }
        }

    }



    private fun setUpListerners() {
        mCheckAndSaveButton.setOnClickListener {

           if (!mEditTextInputURl.text.isNullOrBlank() && !IntentUtils.parseUrl(mEditTextInputURl.text.toString())?.text.isNullOrBlank()){
               Crashlytics.log("user past url ${mEditTextInputURl.text.toString()}")
               manualyDownload = true;
               userCookiesSaved = false
               checkBuildNO()
               if (remoteConfig.getBoolean(RemoteConfigConstants.DISPLAY_NATIVE_ADS_DOWNLOADING_SCREEN)) {
                   refreshAd()
               }
           }
        }

        mFabRepostButton.setOnClickListener({ shareIntent(true) })
        mFabShareButton.setOnClickListener({ shareIntent(false) })
        mCopyHashTagButton.setOnClickListener({ copyHashTagToClipBord() })
        mCopyCaptionButton.setOnClickListener({ copyCaptionToClipBord() })
        mCopyBothButton.setOnClickListener({ CopyBoth() })
        btPlayVideo.setOnClickListener { PlayVideo() }
    }

    private fun PlayVideo() {


            var internt = Intent(activity, PlayerActivity::class.java)
            internt.putExtra(PlayerActivity.EXTRA_VIDEO_PATH, mPost?.pathToStorage)
        activity?.startActivity(internt)

    }





    private fun copyDataFromClipBrod() {
        try {
        rootCardView.visibility=View.INVISIBLE
        if (!ClipBrodHelper(activity).clipBrodText.isNullOrEmpty()) {

                        mEditTextInputURl.text.clear()
                        mEditTextInputURl.setText(ClipBrodHelper(activity).clipBrodText)
                        hideKeybord()


        } else {
            mEditTextInputURl.text.clear()
            Toast.makeText(activity, "URL not valid", Toast.LENGTH_SHORT).show()
            mCardView.visibility = View.INVISIBLE
            mProgressBar.visibility = View.INVISIBLE
        }
             } catch (Ex: Exception) {
            }

    }

    fun staupUI(view: View) {
    //    nativeAdTempalte=view.findViewById(R.id.my_navtive_ad_template)
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
        mAdPlaceHolder=view.findViewById(R.id.fl_adplaceholder)
        rootCardView=view.findViewById(R.id.cardView)
        btPlayVideo=view.findViewById(R.id.btPlay)

    }

    fun checkBuildNO() {


        if (Build.VERSION.SDK_INT > 22) {
            if (activity!!.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                askPermistion()
            else {

                if (mEditTextInputURl.text.toString() != "")
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

            mProgressBar.visibility = View.VISIBLE
            mCardView.visibility = View.INVISIBLE
            btPlayVideo.visibility = View.INVISIBLE

            if (NetworkUtils.isValidURL(mEditTextInputURl.text.toString())) {
                if (isInstaPost(mEditTextInputURl.text.toString())){

                    Crashlytics.log("user past url ${mEditTextInputURl.text.toString()}")
                   val  postUrl="https://www.instagram.com/p/"+IntentUtils.parseUrl(mEditTextInputURl.text.toString())?.text
                    val cookie = settingsHelper.getString(Constants.COOKIE)

                    if (cookie.isEmpty()){

                        Crashlytics.log("cookies null")
                        grabData(postUrl).execute()

                    }else{

                        userCookiesSaved=true
                        CookieUtils.setupCookies(cookie)
                            fetchPost(postUrl,cookie)
                            Crashlytics.log("cookiesSetup")
                        }

                }else{
                  Snackbar.make(rootCardView, "Enter valid insta post url", Snackbar.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(activity, "Enter valid url", Toast.LENGTH_LONG).show()
            }

        } else {
            mProgressBar.visibility = View.INVISIBLE
        }
    }

    private fun fetchPost(postUrl: String,cookie:String){

        mPost=Post()
        mPost?.url = postUrl;
        mProgressBar.progress = 100
        mProgressBar.visibility = View.VISIBLE
        mCardView.visibility = View.INVISIBLE
        btPlayVideo.visibility = View.INVISIBLE

     val inputData=   Data.Builder()
                .putString("url", postUrl)
             .putString("cookie",cookie)
                .build()
        val postScrapWorker = OneTimeWorkRequest.Builder(PostScrapWorker::class.java)
                .setInputData(
                        inputData )
                .build()
        val checkPostPermission = OneTimeWorkRequest.Builder(CheckPostPermission::class.java)
                .setInputData(inputData)
                .build()
        if (observerCheckPostPermission==null){
            observerCheckPostPermission= Observer {workinfo->
                mProgressBar.visibility=View.GONE
                if (workinfo?.state==WorkInfo.State.SUCCEEDED){
                    try {
                        trowPrivatePost()
                    }catch (E: Exception){
                        Crashlytics.recordException(E)
                    }
                    postIsPrivate()
                    firebaseAnalytics?.logEvent("privatePost", null)

                    Log.d(TAG,"private post")
                }else if (workinfo?.state==WorkInfo.State.FAILED){

                    val errorCode=workinfo.outputData.getInt("errorCode",-1)
                    val errorMessage=workinfo.outputData.getString("errorMessage")
                    val bundel=Bundle()
                    bundel.putInt("errorCode",errorCode)
                    firebaseAnalytics?.logEvent("event_check_post_permission_worker_fail",bundel)
                    Crashlytics.log("fail check post permission worker")
                    Crashlytics.log("errorCode ${errorCode}")
                    try {
                        throwPostScapFail()
                    }catch (E:Exception){
                        Crashlytics.recordException(E)
                    }
                   showError(errorMessage.toString())

                }
            }
        }
        if (observerScrapPost==null){
            observerScrapPost= Observer {
                 workInfo ->
                    if(workInfo?.state == WorkInfo.State.SUCCEEDED) {

                        Log.d(TAG, "work manger done")
                        Crashlytics.log("postScrapWorker done")
                        val postJson=  workInfo.outputData.getString("post")
                        mPost=Gson().fromJson(postJson, Post::class.java)
                        UpdateUI()
                        Downloader()

                    }else if (workInfo?.state==WorkInfo.State.FAILED){

                        val errorMessage=workInfo.outputData.getString("errorMessage")
                        val errorCode=workInfo.outputData.getInt("errorCode",-1)

                        val bundel=Bundle()
                        bundel.putInt("errorCode",errorCode)
                        firebaseAnalytics?.logEvent("event_post_scaper_worker_fail",bundel)
                        Crashlytics.log("fail post scaper worker")
                        Crashlytics.log("errorCode ${errorCode}")

                       if (!userCookiesSaved && errorCode== HttpURLConnection.HTTP_NOT_FOUND){
                           WorkManager.getInstance(activity!!.applicationContext).beginUniqueWork("checkPostPermission", ExistingWorkPolicy.REPLACE, checkPostPermission).enqueue()
                           WorkManager.getInstance(activity!!.applicationContext).getWorkInfoByIdLiveData(checkPostPermission.id).observe(viewLifecycleOwner,observerCheckPostPermission!!)
                           showError("Page not found, checking if the post is private or not, please wait...")
                       }else if(!userCookiesSaved && errorCode==HttpURLConnection.HTTP_UNAUTHORIZED){
                           logInRequired()
                           mProgressBar.visibility=View.GONE
                       }
                       else {
                           try {
                               throwPostScapFail()
                           }catch (E:Exception){
                               Crashlytics.recordException(E)
                           }
                           mProgressBar.visibility=View.GONE
                           showError(errorMessage!!)
                       }
                    }
                }

        }


        WorkManager.getInstance(activity!!.applicationContext).beginUniqueWork("scapPost", ExistingWorkPolicy.REPLACE, postScrapWorker).enqueue()
        WorkManager.getInstance(activity!!.applicationContext).getWorkInfoByIdLiveData(postScrapWorker.id)
                .observe(viewLifecycleOwner,observerScrapPost!!)
    }

    private var mContext: Context? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
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

                        if (!mEditTextInputURl.text.toString().equals(""))
                            if (!checkIFPosAllreadyDownloaded(mEditTextInputURl.text.toString()))
                                intiDownloader()

                    }

                    override fun onDenied(permission: String) {

                    }
                }).build().request()
    }

    private fun checkIFPosAllreadyDownloaded(toString: String): Boolean {

            return false

    }


    @SuppressLint("StaticFieldLeak")
    inner class grabData(val postUrl: String) : AsyncTask<Void, Void, String>() {
        val mHashTags = StringBuilder()
        private var isSomeThingWrong = false
        private var whatsWrong=""
        var document: Document? = null;
        var response: Connection.Response?=null

        override fun onPreExecute() {
            super.onPreExecute()

            mPost=Post()
            document=null
            response=null
            mHashTags.clear()
            mPost?.url = postUrl;
            mProgressBar.progress = 100
            mProgressBar.visibility = View.VISIBLE
            mCardView.visibility = View.INVISIBLE
            btPlayVideo.visibility = View.INVISIBLE
            isSomeThingWrong=false;
            whatsWrong=""
            hideKeybord()
            Log.d(TAG, "coonection to ${postUrl}")
            Crashlytics.log("connecting to $postUrl")

            val bundle=Bundle()
            bundle.putString("connUrl", mPost?.url)
            firebaseAnalytics?.logEvent("scrap", bundle)


        }

        override fun doInBackground(vararg p0: Void?): String {
            try {
                response = Jsoup
                        .connect(mPost?.url)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                        .referrer("http://www.google.com")
                        .timeout(30000)
                        .execute()
                Log.d(TAG, "jsoup response ${response?.statusCode()}")
                response?.statusCode()?.let { Crashlytics.setCustomKey("Jsoup response code ", it) }
               if (200 == response?.statusCode()){
                   document=response?.parse()
               }

            } catch (Ex: Exception) {
                Ex.printStackTrace()
                response?.statusCode()?.let { Crashlytics.setCustomKey("Jsoup response code ", it) }
                Crashlytics.recordException(Ex)
                whatsWrong=Ex.localizedMessage
                isSomeThingWrong = true
                val bundle=Bundle()
                response?.statusCode()?.let { bundle.putInt("Jsoup response code", it) }

                firebaseAnalytics?.logEvent("Jsoup_error", bundle)
            }
            try {
                if (document != null) {
                    Crashlytics.log("documents ${document?.toString()}")
                    for (meta in document!!.select("meta")) {

                        if ((meta.attr("property").equals("instapp:hashtags") || meta.attr("property").equals("video:tag")))
                            mPost?.hashTags = mHashTags.append("#").append(meta.attr("content")).append(" ")
                        if (meta.attr("property").equals("og:image"))
                            mPost?.imageURL = meta.attr("content")
//                    if (meta.attr("property").equals("og:description"))
//                        mPost.content = meta.attr("content")

                        if (meta.attr("property").equals("og:video"))
                            mPost?.videoURL = meta.attr("content")
                        if (meta.attr("name").equals("medium"))
                            mPost?.medium = meta.attr("content")
                        if (meta.attr("property").equals("og:url")) {
                            mPost?.postID = meta.attr("content").replace("https://www.instagram.com/p/", "")
                            mPost?.postID = mPost?.postID?.replace("/", "")
                        }

                        if (meta.attr("property").equals("og:type")){
                            mPost?.type = meta.attr("content")
                        }
                    }
                    //val script = document!!.getElementsByTag("script")

                        val scriptElements: Elements = document!!.getElementsByTag("script")

                        for (element in scriptElements) {
                            if (element.attr("type").equals("application/ld+json")){
                                val json =JSONObject(element.dataNodes()[0].wholeData)

                               try{
                                   mPost?.caption=json.getString("caption")
                               }catch (json: JSONException){
                                   Crashlytics.log("caption json ex")
                                   Crashlytics.recordException(json
                                   )
                               }
                            }
                        }


                }else{
                    Crashlytics.log("document is null")
                }


            } catch (ex: Exception) {
                ex.printStackTrace()
                Crashlytics.recordException(ex)
                whatsWrong=ex.localizedMessage
                isSomeThingWrong = true

            }
            return "";
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

          if (isAdded && activity!=null){
              val bundle=Bundle()
              bundle.putString("connUrl", mPost?.url)
              Crashlytics.log("post = ${Gson().toJson(mPost)}")
                if (!isSomeThingWrong) {


                   if (mPost?.type.equals("profile")){
                       Crashlytics.setCustomKey("url", mPost?.url.toString())
                       try {
                         trowPrivatePost()
                       }catch (E: Exception){
                           Crashlytics.recordException(E)
                       }
                       postIsPrivate()
                       firebaseAnalytics?.logEvent("privatePost", bundle)
                   }else{
                       UpdateUI()
                       Downloader()
                       firebaseAnalytics?.logEvent("initDownload", bundle)
                   }
                }
                else {
                    mProgressBar.visibility = View.INVISIBLE
                    try{
                        showError(whatsWrong)

                    }catch (E: Exception){

                    }
                }
            }
        }
    }

        private fun UpdateUI() {
            mProgressBar.visibility = View.INVISIBLE

            mCardView.visibility = View.VISIBLE
            mHashTagTextView.setText(mPost?.hashTags)
            mCaptionTextView.text=mPost?.caption
            Picasso.get().load(mPost?.imageURL) .into(mImage)

        }



        private fun Downloader() {

            mFabRepostButton.visibility = View.GONE
            mFabShareButton.visibility = View.GONE

            try {

                var downloadingFileUrl: String? =""
                downloadingFileUrl = if(mPost?.type?.contains("photo")==true){
                    Crashlytics.log("mPost imageURL ${mPost?.imageURL.toString()}")
                    mPost?.imageURL.toString()
                }else if (mPost?.type?.contains("video")==true) {
                    Crashlytics.log("mPost videoUrl ${mPost?.videoURL.toString()}")
                    mPost?.videoURL.toString()
                }else{
                    null
                }

               if (downloadingFileUrl!=null){
                   val filePaht = getRootDirPath() + "/" + (System.currentTimeMillis().toString() + getFileExtenstion(downloadingFileUrl))
                   if (filePaht!=null){
                       FileDownloader.getImpl().pauseAll()
                       FileDownloader.getImpl().create(downloadingFileUrl)
                               .setPath(filePaht)
                               .setListener(fileDownloadListener).start()
                   } else {
                       Toast.makeText(activity, "Unable to access to external storage ", Toast.LENGTH_LONG).show()
                   }
               }else{

                   logInRequired()
               }

            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(activity, "Some thing wrong Error code 7 Error message : " + ex.message, Toast.LENGTH_LONG).show()

            }
        }

    fun getFileExtenstion(url: String): String {
      return if (mPost?.medium=="image"){
            ".jpg"
       }else{
           ".mp4"
       }
    }
    fun getRootDirPath(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
           context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
        }else{
            Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).absolutePath
        }

    }



    private fun saveToPraf(mPost: Post) {
        try {

            activity?.let { My_Share_Pref.savePost(it, mPost.url, mPost) }
        } catch (ex: Exception) {
            Toast.makeText(activity, "Some thing wrong Error code 8 Error message : " + ex.message, Toast.LENGTH_LONG).show()

        }
    }

    fun hideKeybord() {
        val view = activity?.getCurrentFocus()
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

    }

    private fun postIsPrivate(){

        val builder: AlertDialog.Builder

        builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Private post")
                .setMessage("You need to login your account to downland this post, you will login your account on instagram official website we will not obtain your information in any way ")
                .setPositiveButton("Login") { dialog, which ->
                    // continue with delete
                    firebaseAnalytics?.logEvent("clickLogin", null)
                   startActivityForResult(Intent(activity, Login::class.java), LOGIN_RESULT_CODE)
                }


                .show()
    }

    private fun  logInRequired(){

        firebaseAnalytics?.logEvent("event_logInRequired", null);
        val builder: AlertDialog.Builder

        builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Login required")
                .setMessage("You need to login your account to downland this post, you will login your account on instagram official website, we will not obtain your any information in any way ")
                .setPositiveButton("Login") { dialog, which ->
                    // continue with delete
                    firebaseAnalytics?.logEvent("clickLogin", null)
                    startActivityForResult(Intent(activity, Login::class.java), LOGIN_RESULT_CODE)
                }


                .show()
    }

    private fun showError(errorMessage: String){

        if (isAdded && activity!=null){
            AlertDialog.Builder(activity!!)
                    .setTitle("Error")
                    .setMessage(errorMessage)
                    .setPositiveButton("Ok",DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                    })
                   .create()
                    .show()

        }
    }
    private fun isInstaPost(url: String):Boolean{

        return url.contains("/p/") || url.contains("/reel/") || url.contains("/tv/")
    }


    private fun trowPrivatePost(){
        throw CustomException("PrivatePost")
    }

   private fun throwPostScapFail(){
       throw CustomException("PostScapFail")
   }

    public class CustomException : Exception {
        constructor() : super()
        constructor(message: String) : super(message)
        constructor(message: String, cause: Throwable) : super(message, cause)
        constructor(cause: Throwable) : super(cause)
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById<View>(R.id.ad_media) as MediaView)

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline))
        adView.setBodyView(adView.findViewById(R.id.ad_body))
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action))
        adView.setIconView(adView.findViewById(R.id.ad_app_icon))
        adView.setPriceView(adView.findViewById(R.id.ad_price))
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars))
        adView.setStoreView(adView.findViewById(R.id.ad_store))
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser))

        // The headline and mediaContent are guaranteed to be in every NativeAd.
        (adView.headlineView as TextView).setText(nativeAd.getHeadline())
        adView.mediaView.setMediaContent(nativeAd.getMediaContent())

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).setText(nativeAd.getBody())
        }
        if (nativeAd.getCallToAction() == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).setText(nativeAd.getCallToAction())
        }
        if (nativeAd.getIcon() == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                    nativeAd.getIcon().getDrawable())
            adView.iconView.visibility = View.VISIBLE
        }
        if (nativeAd.getPrice() == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).setText(nativeAd.getPrice())
        }
        if (nativeAd.getStore() == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).setText(nativeAd.getStore())
        }
        if (nativeAd.getStarRating() == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
           try {
               (adView.starRatingView as RatingBar).rating = nativeAd.getStarRating().toFloat()
               adView.starRatingView.visibility = View.VISIBLE
           }catch (E: Exception){
               E.printStackTrace()
           }
        }
        if (nativeAd.getAdvertiser() == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).setText(nativeAd.getAdvertiser())
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc: VideoController = nativeAd.getMediaContent().getVideoController()

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {


            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.


                    super.onVideoEnd()
                }
            }
        } else {

        }
    }
    fun getNonPersonalizedAdsBundle(): Bundle {
        val extras = Bundle()
        extras.putString("npa", "1")

        return extras
    }

}