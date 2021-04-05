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
import com.ameerhamza6733.directmessagesaveandrepost.utils.CookieUtils
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.material.navigation.NavigationView

import java.net.MalformedURLException
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var fragmentManager: FragmentManager? = null
    private var TAG = "MainActivityTAG";
    private var mContext: Context? = null
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val admonConfig=RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("84B80A634F2B75467E10A4579885F9C7"))
        MobileAds.setRequestConfiguration(admonConfig.build());
        MobileAds.initialize(this, getString(R.string.admob_app_id))

        mContext = this

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle);
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        progressBar=findViewById(R.id.progressBar2)
        checkForConsentForAdmob()

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
                Toast.makeText(this,"Logout",Toast.LENGTH_LONG).show()

            }
            R.id.nav_action_settings -> {
                startActivity(Intent(this@MainActivity, com.ameerhamza6733.directmessagesaveandrepost.Settings::class.java))
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }




    public fun checkForConsentForAdmob() {
        checkForConsent()
    }

    private var form: ConsentForm? = null
    private fun checkForConsent() {
        if (mContext == null)
            return
        ConsentInformation.getInstance(mContext).addTestDevice("84B80A634F2B75467E10A4579885F9C7")
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
        if (mContext == null)
            return
        ConsentInformation.getInstance(this@MainActivity).consentStatus = ConsentStatus.PERSONALIZED

        mInterstitialAd = InterstitialAd(this@MainActivity)
        mInterstitialAd?.adUnitId = "ca-app-pub-5168564707064012/6509811189"



        val adRequest = AdRequest.Builder()
                .build()

        mInterstitialAd?.loadAd(adRequest)
        mInterstitialAd?.adListener = object : AdListener() {
            override fun onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd?.loadAd(adRequest)
            }

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                progressBar.visibility=View.INVISIBLE
                loadFragment()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                progressBar.visibility=View.INVISIBLE
                loadFragment()
            }
        }


        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this@MainActivity)
        mRewardedVideoAd?.loadAd("ca-app-pub-5168564707064012/5568743535", adRequest)


    }

    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedVideoAd: RewardedVideoAd? = null

    private fun showNonPersonalizedAds() {
        if (mContext == null)
            return
        ConsentInformation.getInstance(this@MainActivity).consentStatus = ConsentStatus.NON_PERSONALIZED


        val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                .build()


        mInterstitialAd = InterstitialAd(this@MainActivity)
        mInterstitialAd?.adUnitId = "ca-app-pub-5168564707064012/6509811189"

        mInterstitialAd?.loadAd(adRequest)
        mInterstitialAd?.adListener = object : AdListener() {
            override fun onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd?.loadAd(adRequest)
            }

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                progressBar.visibility=View.INVISIBLE
                loadFragment()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                progressBar.visibility=View.INVISIBLE
                loadFragment()
            }
        }


        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this@MainActivity)
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
            if (!isFinishing)
                form?.show()
        } else {
            Log.d(TAG, "Not Showing consent form")
        }
    }

    public fun showAds() {
        Log.d(TAG, "showing ads");
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
