package com.ameerhamza6733.directmessagesaveandrepost

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.ameerhamza6733.directmessagesaveandrepost.utils.Constants
import com.ameerhamza6733.directmessagesaveandrepost.utils.CookieUtils
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*
import java.net.MalformedURLException
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var rewardVideoAdCallback=object: FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            mRewardedVideoAd = null
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {

        }

        override fun onAdShowedFullScreenContent() {

            mRewardedVideoAd = null
        }
    }

    private var fragmentManager: FragmentManager? = null
    private var TAG = "MainActivityTAG";
    private var mContext: Context? = null
    private var deviceTestId= listOf("DDFA9BBDCDA7A3335DF814D93BF4E2C8")
    private lateinit var progressBar: ProgressBar
    private lateinit var remoteConfig: FirebaseRemoteConfig
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val admonConfig= RequestConfiguration.Builder().setTestDeviceIds(deviceTestId)
        MobileAds.setRequestConfiguration(admonConfig.build());
        MobileAds.initialize(this, object : OnInitializationCompleteListener {
            override fun onInitializationComplete(p0: InitializationStatus?) {

            }

        })
        MobileAds.setAppVolume(0.5f);

        remoteConfig = Firebase.remoteConfig
        if (BuildConfig.DEBUG){
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            remoteConfig.setConfigSettingsAsync(configSettings)


        }
        remoteConfig.setDefaultsAsync(R.xml.remote_confi_defaults)
        fetchRemoteConfigValues();

        mContext = this

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle);
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        progressBar=findViewById(R.id.progressBar2)
        tvAppVersion.text = "App version : " + BuildConfig.VERSION_NAME;
       // checkForConsentForAdmob()

    }
    private fun fetchRemoteConfigValues() {
       val nonPersonalizedAs = remoteConfig.getBoolean(RemoteConfigConstants.DISPLAY_NON_PERSONALIZED_ADS)

        // [START fetch_config_with_callback]
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                    }
                    Log.d(TAG,"fetchSuccess")
                    checkForConsentForAdmob()
                }
        // [END fetch_config_with_callback]
    }


    private fun loadFragment() {
        fragmentManager = supportFragmentManager
        fragmentManager?.beginTransaction()?.replace(R.id.container, DownloadingFragment())?.commitAllowingStateLoss()

    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            startActivity(Intent(this@MainActivity, com.ameerhamza6733.directmessagesaveandrepost.Settings::class.java))
            // Toast.makeText(this, "i am working on it, if you have any suggestion please send me feedback ", Toast.LENGTH_SHORT).show()
            true
        } else super.onOptionsItemSelected(item)

    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        when (id) {

            R.id.nav_rate -> openMarket(this.packageName)
            R.id.nav_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            R.id.nav_logout -> {
                CookieUtils.setupCookies("LOGOUT")
                CookieUtils.settingsHelper.putString(Constants.COOKIE, "")
                Toast.makeText(this, "Logout", Toast.LENGTH_LONG).show()

            }
            R.id.nav_action_settings -> {
                startActivity(Intent(this@MainActivity, com.ameerhamza6733.directmessagesaveandrepost.Settings::class.java))
            }
            R.id.nav_send_feedback -> {
                try {
                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+"develpore2017@gmail.com"))
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about " + getString(R.string.app_name))
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Write your feedback")
//emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, body); //If you are using HTML in your body text

//emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, body); //If you are using HTML in your body text
                    startActivity(Intent.createChooser(emailIntent, "Email with"))
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }




    public fun checkForConsentForAdmob() {
        val nonPersonalizedAs = remoteConfig.getBoolean(RemoteConfigConstants.DISPLAY_NON_PERSONALIZED_ADS)

       if (nonPersonalizedAs){
           val adRequest = AdRequest.Builder()
                   .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                   .build()
           ConsentInformation.getInstance(applicationContext).consentStatus = ConsentStatus.NON_PERSONALIZED

           loadAds(adRequest)
       }else{
           checkForConsent()
       }
    }

    private var form: ConsentForm? = null
    private fun checkForConsent() {
        if (mContext == null)
            return


        val consentInformation = ConsentInformation.getInstance(mContext)

        val publisherIds = arrayOf("pub-5168564707064012")
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                // User's consent status successfully updated.
                when (consentStatus) {
                    ConsentStatus.PERSONALIZED -> {
                        Log.d(TAG, "Showing Personalized ads")
                        ConsentInformation.getInstance(applicationContext).consentStatus = ConsentStatus.PERSONALIZED

                        val adRequest = AdRequest.Builder()
                                .build()
                        loadAds(adRequest)
                    }
                    ConsentStatus.NON_PERSONALIZED -> {
                        Log.d(TAG, "Showing Non-Personalized ads")
                        val adRequest = AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                                .build()
                        ConsentInformation.getInstance(applicationContext).consentStatus = ConsentStatus.NON_PERSONALIZED

                        loadAds(adRequest)
                    }
                    ConsentStatus.UNKNOWN -> {
                        Log.d(TAG, "Requesting Consent")
                        if (ConsentInformation.getInstance(mContext)
                                        .isRequestLocationInEeaOrUnknown) {
                            requestConsent()
                        } else {
                            ConsentInformation.getInstance(applicationContext).consentStatus = ConsentStatus.PERSONALIZED

                            val adRequest = AdRequest.Builder()

                                    .build()
                            loadAds(adRequest)
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
        if (mContext == null)
            return
        form = ConsentForm.Builder(this@MainActivity, privacyUrl)
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
                                ConsentStatus.PERSONALIZED -> {
                                    ConsentInformation.getInstance(applicationContext).consentStatus = ConsentStatus.PERSONALIZED
                                    val adRequest = AdRequest.Builder()
                                            .build()
                                    loadAds(adRequest)
                                }
                                ConsentStatus.NON_PERSONALIZED -> {
                                    ConsentInformation.getInstance(applicationContext).consentStatus = ConsentStatus.NON_PERSONALIZED

                                    val adRequest = AdRequest.Builder()
                                            .build()
                                    loadAds(adRequest)
                                }
                                ConsentStatus.UNKNOWN -> {
                                    ConsentInformation.getInstance(applicationContext).consentStatus = ConsentStatus.UNKNOWN
                                    val adRequest = AdRequest.Builder()
                                            .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                                            .build()
                                    loadAds(adRequest)
                                }
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



    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedVideoAd: RewardedAd? = null

    private fun loadAds(adRequest: AdRequest) {
        if (mContext == null)
            return

        InterstitialAd.load(this@MainActivity, "ca-app-pub-5168564707064012/6509811189", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d(TAG, "interstitial not ad loaded $p0")
                progressBar.visibility = View.INVISIBLE
                loadFragment()
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                mInterstitialAd = p0
                Log.d(TAG, "interstitial ad loaded")
                progressBar.visibility = View.INVISIBLE
                if (adRequest.isTestDevice(applicationContext)){
                    Log.d(TAG, "ad load from test device")
                }
                loadFragment()
            }

        })

        RewardedAd.load(this, "ca-app-pub-5168564707064012/5568743535", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                progressBar?.visibility = View.GONE

                mRewardedVideoAd = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "video ad load with ad metadata ${rewardedAd.adMetadata}")
                if (!rewardedAd.adMetadata.isEmpty) {
                    mRewardedVideoAd = rewardedAd
                    mRewardedVideoAd?.fullScreenContentCallback = rewardVideoAdCallback
                }
            }
        })

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
            if (!isFinishing)
                form?.show()
        } else {
            Log.d(TAG, "Not Showing consent form")
        }
    }

    public fun showAds() {
        Log.d(TAG, "showing ads");
       try {
           if (mRewardedVideoAd != null) {
               mRewardedVideoAd?.show(this) {
                   mRewardedVideoAd = null
               }

           } else if (mInterstitialAd != null) {
               mInterstitialAd?.show(this)

           }
       }catch (E: Exception){
           E.printStackTrace()
       }
    }

    private fun openMarket(PackageName: String) {

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PackageName)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
