package com.ameerhamza6733.directmessagesaveandrepost

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.ameerhamza6733.directmessagesaveandrepost.Settings.ATO_START_DOWNLOADING
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.artjimlop.altex.AltexImageDownloader
import com.crashlytics.android.Crashlytics
import com.daimajia.numberprogressbar.NumberProgressBar
import com.daimajia.numberprogressbar.OnProgressBarListener
import com.github.clans.fab.FloatingActionButton
import com.golshadi.majid.core.DownloadManagerPro
import com.golshadi.majid.report.ReportStructure
import com.golshadi.majid.report.listener.DownloadManagerListener
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.kingfisher.easy_sharedpreference_library.SharedPreferencesManager
import com.squareup.picasso.Picasso
import lolodev.permissionswrapper.callback.OnRequestPermissionsCallBack
import lolodev.permissionswrapper.wrapper.PermissionWrapper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import java.net.MalformedURLException
import java.net.URL

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

class DownloadingFragment : Fragment(), DownloadManagerListener, OnProgressBarListener {
    companion object {
        private val ARG_CAUGHT = "myFragment_caught"

        fun newInstance(temp: Int): DownloadingFragment {

            return DownloadingFragment()
        }
    }

    override fun onRebuildError(errorMessage: String?) {
        try {
            activity!!.runOnUiThread {
                Toast.makeText(context, "onRebuildError ", Toast.LENGTH_LONG).show()
                mNumberBar.progress = 0
            }
        } catch (Ex: Exception) {

        }
    }

    override fun onProgressChange(current: Int, max: Int) {

    }

    private var TAG = "MainActivityTAG"
    override fun OnDownloadStarted(taskId: Long) {
        try {
            activity!!.runOnUiThread {
                if (this@DownloadingFragment.activity != null) {
                    Toast.makeText(activity, "Downloading start", Toast.LENGTH_SHORT).show()
                    this@DownloadingFragment.activity!!.runOnUiThread({ mNumberBar.progress = 0 })
                    Crashlytics.log("OnDownloadStarted")
                }
            }
        } catch (Ex: Exception) {

        }
    }

    override fun OnDownloadPaused(taskId: Long) {
        Log.d(TAG, "OnDownloadPaused")
        Crashlytics.log("OnDownloadPaused")
    }

    override fun onDownloadProcess(taskId: Long, percent: Double, downloadedLength: Long) {
        try {
            activity!!.runOnUiThread {
                if (this@DownloadingFragment.activity != null)
                    this@DownloadingFragment.activity!!.runOnUiThread({ mNumberBar.incrementProgressBy(1) })
            }
        } catch (Ex: Exception) {

        }
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
        activity!!.runOnUiThread {
            Toast.makeText(activity, "Downloading complete", Toast.LENGTH_SHORT).show()
            mNumberBar.progress = 100
            mFabRepostButton.visibility = View.VISIBLE
            mFabShareButton.visibility = View.VISIBLE
            val repor: ReportStructure = dm.singleDownloadStatus(taskToken);
            mPost.pathToStorage = repor.saveAddress
            Log.d(TAG, "content: " + mPost.content);
            saveToPraf(mPost)
            Crashlytics.log("OnDownloadCompleted")
        }
        Log.d(TAG, "OnDownloadCompleted") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connectionLost(taskId: Long) {
        activity!!.runOnUiThread {
            Snackbar.make(mCardView, "Connection Lost try again", Snackbar.LENGTH_INDEFINITE).setAction("try again", object : View.OnClickListener {
                override fun onClick(v: View?) = try {
                    if (taskToken != null)
                        dm.startDownload(taskToken)
                    else {
                        Toast.makeText(activity, "error while trying to resume your download", Toast.LENGTH_SHORT).show()
                    }

                } catch (Ex: Exception) {

                }
            }).show()
        }
        Log.d(TAG, "connectionLost") //To change body of created functions use File | Settings | File Templates.
    }

    private fun shareIntent(repost: Boolean) {

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
            Crashlytics.logException(ex);
           // FirebaseCrash.report(Exception(" private fun shareIntent Error code 3 Error : " + ex.message));
            Toast.makeText(activity, "some thing working while sharing Error: code 3  " + ex.message, Toast.LENGTH_LONG).show()
        }


    }

    private fun shareVideoIntentToInstagram(repost: Boolean) {
        val repor: ReportStructure = dm.singleDownloadStatus(taskToken);
        val int: InstaIntent = InstaIntent()
        int.createVideoInstagramIntent("video/*", repor.saveAddress, activity, repost)
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
    private lateinit var mNumberBar: NumberProgressBar
    private lateinit var mFabRepostButton: FloatingActionButton
    private lateinit var mFabShareButton: FloatingActionButton
    private lateinit var mCardView: CardView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var rootView: View;


    private lateinit var dm: DownloadManagerPro
    private var taskToken: Int = -1
    private lateinit var mBitMapImageToShare: Bitmap
    val mPost = Post()
    lateinit var postKeyFromShardPraf: String

    private var manualyDownload = false
    private var atoSave = true;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_download, container, false)

        staupUI(view)
        rootView = view
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        SharedPreferencesManager.init(activity, true)
      var rateMe =  SharedPreferencesManager.getInstance().getValue("RateMe", Boolean::class.java,true)
        var firstTIme = SharedPreferencesManager.getInstance().getValue("isFirstTime", Boolean::class.java, true)
        if (firstTIme) {
            val intent = Intent(activity, MyInstructionActivity::class.java)
            activity?.startActivity(intent)
        }
        if (!firstTIme && rateMe){
            showRateMe()
        }
        atoSave = SharedPreferencesManager.getInstance().getValue(ATO_START_DOWNLOADING, Boolean::class.java, true)


        setUpListerners()
        copyDataFromClipBrod()
        checkForConsent();
    }

    private fun copyHashTagToClipBord() {
        if (mRewardedVideoAd != null && mRewardedVideoAd?.isLoaded!!) {
            mRewardedVideoAd?.show()
        } else if (mInterstitialAd != null && mInterstitialAd?.isLoaded!!) {
            mInterstitialAd?.show()
        }
        if (!mHashTagTextView.text.isEmpty()) {
            val clipbordHelper = ClipBrodHelper()
            clipbordHelper.WriteToClipBord(activity, mHashTagTextView.text.toString())
        } else {
            Toast.makeText(activity, "No Hash tag not find in this Post", Toast.LENGTH_SHORT).show()
        }

    }

    private fun CopyBoth() {
        if (mRewardedVideoAd != null && mRewardedVideoAd?.isLoaded!!) {
            mRewardedVideoAd?.show()
        } else if (mInterstitialAd != null && mInterstitialAd?.isLoaded!!) {
            mInterstitialAd?.show()
        }
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
        mNumberBar.setOnProgressBarListener(this);
        mFabRepostButton.setOnClickListener({ shareIntent(true) })
        mFabShareButton.setOnClickListener({ shareIntent(false) })
        mCopyHashTagButton.setOnClickListener({ copyHashTagToClipBord() })
        mCopyCaptionButton.setOnClickListener({ copyCaptionToClipBord() })
        mCopyBothButton.setOnClickListener({ CopyBoth() })
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
        mNumberBar = view.findViewById<NumberProgressBar>(R.id.number_progress_bar) as NumberProgressBar
        mFabRepostButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButtonRepost) as com.github.clans.fab.FloatingActionButton
        mFabShareButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButtonShare) as FloatingActionButton
        mCardView = view.findViewById<CardView>(R.id.cardView) as CardView
        mProgressBar = view.findViewById<ProgressBar>(R.id.progressBar) as ProgressBar

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
        if (atoSave || manualyDownload) {
            if (!mEditTextInputURl.text.toString().isEmpty()) {
                grabData(mEditTextInputURl.text.toString()).execute()
                downloadCaption(mEditTextInputURl.text.toString())
            }

        } else {
            mProgressBar.visibility = View.INVISIBLE
        }
    }

    private var mContext: Context?=null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
        Crashlytics.log("onAttach");
    }

    override fun onDetach() {
        super.onDetach()
        mContext=null
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

        try {
            var tempPostID = toString.replace("https://www.instagram.com/p/", "")
            tempPostID = tempPostID.replace("/", "")

            val allEntries = SharedPreferencesManager.getInstance().allKeys
            for (entry in allEntries.entries) {
                if (entry == null || entry.value == null) continue
                //   Log.e("SharedPreferenceManager", entry.key + ": " + entry.value.toString())

                if (tempPostID.equals(entry.key)) {
                    mProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(activity, "Post already downloaded check History tab ", Toast.LENGTH_SHORT).show()
                    postKeyFromShardPraf = entry.value.toString()


                    return true
                }

            }
        } catch (ex: Exception) {
            Crashlytics.logException(ex);
         //   FirebaseCrash.report(Exception("  private  fun checkIFPosAllreadyDownloaded Error code 5 Error : " + ex.message))
            Toast.makeText(activity, "Some thing wrong Error code 5 Error message : " + ex.message, Toast.LENGTH_LONG).show()

            return false
        }
        return false
    }


    @SuppressLint("StaticFieldLeak")
    inner class grabData(val ConnURL: String) : AsyncTask<Void, Void, String>() {
        val mHashTags = StringBuilder()
        private var isSomeThingWrong = false
        var document: Document? = null;

        override fun onPreExecute() {
            super.onPreExecute()
            Log.d(TAG, "url =" + ConnURL)

            mPost.url = ConnURL;
            mProgressBar.progress = 100
            mProgressBar.visibility = View.VISIBLE
            mCardView.visibility = View.INVISIBLE
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
                intiDownloader()
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


        private fun UpdateUI() {
            mProgressBar.visibility = View.INVISIBLE

            mCardView.visibility = View.VISIBLE
            mHashTagTextView.setText(mPost.hashTags)
            try {
                if (!mPost.content.isNullOrEmpty() && isContainsColan()) {
                    mPost.content = mPost.content.substring(mPost.content.indexOf(":"), mPost.content.length)

                }
                Picasso.with(activity).load(mPost.imageURL).into(mImage)
            } catch (Ex: Exception) {
                Toast.makeText(activity, "Some thing wrong Error code 6 Error message : " + Ex.message, Toast.LENGTH_LONG).show()
                Ex.printStackTrace()
            }


        }

        private fun isContainsColan(): Boolean = mPost.content.contains(":")

        private fun intiDownloader() {

            mFabRepostButton.visibility = View.GONE
            mFabShareButton.visibility = View.GONE
            try {


                if (mPost.medium != "image") {
                    dm = DownloadManagerPro(activity)
                    dm.init("DMinstaDownload/", 12, this@DownloadingFragment)
                    taskToken = dm.addTask(mPost.postID, mPost.videoURL, true, false)
                    dm.startDownload(taskToken)
                } else
                    downloadImage()
            } catch (ex: Exception) {
                Toast.makeText(activity, "Some thing wrong Error code 7 Error message : " + ex.message, Toast.LENGTH_LONG).show()

            }
        }

        private fun downloadImage() {
            mNumberBar.progress = 0
            val download = AltexImageDownloader(object : AltexImageDownloader.OnImageLoaderListener {
                override fun onError(error: AltexImageDownloader.ImageError) {
                    Toast.makeText(activity, "Error " + error.toString(), Toast.LENGTH_SHORT).show()

                }

                override fun onProgressChange(percent: Int) {
                    mNumberBar.incrementProgressBy(1)

                }

                override fun onComplete(result: Bitmap) {

                    mFabRepostButton.visibility = View.VISIBLE
                    mFabShareButton.visibility = View.VISIBLE
                    mNumberBar.progress = 100
                    mBitMapImageToShare = result
                    val bitmapPath: String = MediaStore.Images.Media.insertImage(context!!.getContentResolver(), mBitMapImageToShare, "title", null);
                    val bitmapUri: Uri = Uri.parse(bitmapPath)
                    //      Log.d(TAG, "onImageComplate " + getRealPathFromURI(bitmapUri))
                    mPost.pathToStorage = getRealPathFromURI(bitmapUri)
                    saveToPraf(mPost)
                }
            })
            download.download(mPost.imageURL, true)
        }

    }

    @SuppressLint("Recycle")
    fun getRealPathFromURI(uri: Uri): String {
        val cursor = activity!!.contentResolver.query(uri, null, null, null, null)
        cursor!!.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return cursor.getString(idx)
    }

    private fun saveToPraf(mPost: Post) {
        try {
            if (atoSave)
                SharedPreferencesManager.getInstance().putValue(mPost.postID, mPost);
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

    private fun checkForConsent() {
        if (mContext==null)
            return
        val consentInformation = ConsentInformation.getInstance(mContext)
        val publisherIds = arrayOf("pub-5168564707064012")
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                // User's consent status successfully updated.
                when (consentStatus) {
                    ConsentStatus.PERSONALIZED -> {
                        Log.d(TAG, "Showing Personalized ads")
                        showPersonalizedAds()
                    }
                    ConsentStatus.NON_PERSONALIZED -> {
                        Log.d(TAG, "Showing Non-Personalized ads")
                        showNonPersonalizedAds()
                    }
                    ConsentStatus.UNKNOWN -> {
                        Log.d(TAG, "Requesting Consent")
                        if (ConsentInformation.getInstance(activity?.baseContext)
                                        .isRequestLocationInEeaOrUnknown) {
                            requestConsent()
                        } else {
                            showPersonalizedAds()
                        }
                    }
                    else -> {
                    }
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                // User's consent status failed to update.
            }
        })
    }

    private var form: ConsentForm? = null

    private fun requestConsent() {
        var privacyUrl: URL? = null
        try {
            // TODO: Replace with your app's privacy policy URL.
            /*
            watch this video how to create privacy policy in mint
            https://www.youtube.com/watch?v=lSWSxyzwV-g&t=140s
            */
            privacyUrl = URL("http://alphapk6733.blogspot.com/2018/07/privacy-policy-for-copy-caption-and-tag.html")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            // Handle error.
        }
if (mContext==null)
    return
        form = ConsentForm.Builder(mContext?.applicationContext, privacyUrl)
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        Log.d(TAG, "Requesting Consent: onConsentFormLoaded")
                        showForm()
                    }

                    override fun onConsentFormOpened() {
                        // Consent form was displayed.
                        Log.d(TAG, "Requesting Consent: onConsentFormOpened")
                    }

                    override fun onConsentFormClosed(
                            consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?) {
                        Log.d(TAG, "Requesting Consent: onConsentFormClosed")
                        if (userPrefersAdFree!!) {
                            // Buy or Subscribe
                            Log.d(TAG, "Requesting Consent: User prefers AdFree")
                        } else {
                            Log.d(TAG, "Requesting Consent: Requesting consent again")
                            when (consentStatus) {
                                ConsentStatus.PERSONALIZED -> showPersonalizedAds()
                                ConsentStatus.NON_PERSONALIZED -> showNonPersonalizedAds()
                                ConsentStatus.UNKNOWN -> showNonPersonalizedAds()
                            }

                        }
                        // Consent form was closed.
                    }

                    override fun onConsentFormError(errorDescription: String?) {
                        Log.d(TAG, "Requesting Consent: onConsentFormError. Error - " + errorDescription!!)
                        // Consent form error.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build()
        form?.load()
    }

    private fun showPersonalizedAds() {
        if(mContext==null)
            return
        ConsentInformation.getInstance(mContext?.applicationContext).consentStatus = ConsentStatus.PERSONALIZED
        MobileAds.initialize(mContext?.applicationContext, "ca-app-pub-5168564707064012~5058501866");

        mInterstitialAd = InterstitialAd(mContext?.applicationContext)
        mInterstitialAd?.adUnitId = "ca-app-pub-5168564707064012/6509811189"
        val mAdView: AdView = rootView.findViewById(R.id.adView);
        val adRequest = AdRequest.Builder()
                .addTestDevice("B94C1B8999D3B59117198A259685D4F8")
                .build()
        mAdView.loadAd(adRequest)
        mInterstitialAd?.loadAd(adRequest)
        mInterstitialAd?.adListener = object : AdListener() {
            override fun onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd?.loadAd(adRequest)
            }
        }


        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mContext?.applicationContext)
        mRewardedVideoAd?.loadAd("ca-app-pub-5168564707064012/5568743535", adRequest)


    }

    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedVideoAd: RewardedVideoAd? = null

    private fun showNonPersonalizedAds() {
        if (mContext==null)
            return
        ConsentInformation.getInstance(mContext?.applicationContext).consentStatus = ConsentStatus.NON_PERSONALIZED

        MobileAds.initialize(mContext?.applicationContext, "ca-app-pub-5168564707064012~5058501866");
        val mAdView: AdView = rootView.findViewById(R.id.adView);
        val adRequest = AdRequest.Builder()
                .addTestDevice("B94C1B8999D3B59117198A259685D4F8")
                .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                .build()
        mAdView.loadAd(adRequest)

        mInterstitialAd = InterstitialAd(mContext?.applicationContext)
        mInterstitialAd?.adUnitId = "ca-app-pub-5168564707064012/6509811189"

        mInterstitialAd?.loadAd(adRequest)
        mInterstitialAd?.adListener = object : AdListener() {
            override fun onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd?.loadAd(adRequest)
            }
        }


        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(mContext?.applicationContext)
        mRewardedVideoAd?.loadAd("ca-app-pub-5168564707064012/5568743535", adRequest)
    }

    fun getNonPersonalizedAdsBundle(): Bundle {
        val extras = Bundle()
        extras.putString("npa", "1")

        return extras
    }


    private fun showForm() {
        if (form == null) {
            Log.d(TAG, "Consent form is null")
        }
        if (form != null) {
            Log.d(TAG, "Showing consent form")
            form?.show()
        } else {
            Log.d(TAG, "Not Showing consent form")
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
                .setNegativeButton("never"){dialog, which ->
                    SharedPreferencesManager.getInstance().putValue("RateMe", false)

                }
                .setIcon(android.R.drawable.star_big_on)
                .show()
    }

}