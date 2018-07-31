package com.ameerhamza6733.directmessagesaveandrepost

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.webianks.easy_feedback.EasyFeedback
import java.net.MalformedURLException
import java.net.URL


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var fragmentManager:FragmentManager?=null
    private var TAG ="MainActivityTAG";
    private var mContext : Context?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mContext=this

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle);
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        fragmentManager = supportFragmentManager
        fragmentManager?.beginTransaction()?.replace(R.id.container, DownloadingFragment())?.commit()


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
            R.id.nav_feedback -> feedBack()
            R.id.nav_rate -> openMarket(this.packageName)
            R.id.nav_history -> {
               fragmentManager?.beginTransaction()?.replace(R.id.container,HistoryFragment())?.commit()
            }

            R.id.nav_action_settings -> {
                startActivity(Intent(this@MainActivity, com.ameerhamza6733.directmessagesaveandrepost.Settings::class.java))
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }



    private fun feedBack() {
        EasyFeedback.Builder(this)
                .withEmail("develpore2017@gmail.com")
                .withSystemInfo()
                .build()
                .start()
    }
public fun checkForConsentForAdmob(){
    checkForConsent()
}
    private var form: ConsentForm? = null
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
                        if (ConsentInformation.getInstance(mContext)
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
        val mAdView: AdView = findViewById(R.id.adView);
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
        val mAdView: AdView = findViewById(R.id.adView);
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
    public fun showAds(){
        if (mRewardedVideoAd != null && mRewardedVideoAd?.isLoaded!!) {
            mRewardedVideoAd?.show()
        } else if (mInterstitialAd != null && mInterstitialAd?.isLoaded!!) {
            mInterstitialAd?.show()
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
