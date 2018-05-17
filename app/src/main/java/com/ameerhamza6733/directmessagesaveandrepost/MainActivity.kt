package com.ameerhamza6733.directmessagesaveandrepost

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.ameerhamza6733.directmessagesaveandrepost.InstructionActivity.IS_FIRST_TIME
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.firebase.crash.FirebaseCrash
import com.kekstudio.dachshundtablayout.DachshundTabLayout
import com.webianks.easy_feedback.EasyFeedback
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import wei.mark.standout.StandOutWindow
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, RewardedVideoAdListener {
    override fun onRewardedVideoAdClosed() {

    }

    override fun onRewardedVideoAdLeftApplication() {
    }

    override fun onRewardedVideoAdLoaded() {

    }

    override fun onRewardedVideoAdOpened() {
    }

    override fun onRewarded(p0: RewardItem?) {
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {

    }


    private lateinit var clipboard: ClipboardManager
    private lateinit var prefs: SharedPreferences
    private lateinit var mRewardedVideoAd: RewardedVideoAd


    lateinit var mTimer: Timer

    companion object {
        lateinit var mAdView: AdView
        private lateinit var mInterstitialAd: InterstitialAd
        private var couter = 1
    }


    private val interstitialTestAdd = "ca-app-pub-3940256099942544/1033173712"
    private val AdmobAppID = "ca-app-pub-5168564707064012~5058501866"
    private val interstitialRealAdd = "ca-app-pub-5168564707064012/3666631165"


    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val mViewPager = findViewById<ViewPager>(R.id.view_pager)
        val mTabLayout = findViewById<DachshundTabLayout>(R.id.tab_layout)
        mViewPager.adapter = PagerAdupter(supportFragmentManager)
        // MobileAds.initialize(this, AdmobAppID)
        // intiRewardedVideoAd()

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab!!.position == 1)
                    try {
                        if (mInterstitialAd.isLoaded)
                            mInterstitialAd.show()
                    } catch (Ex: Exception) {
                    }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        });
        //SharedPreferencesManager.init(this, true)
        registerClipBordBroadCastReciver()
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle);
        toggle.syncState()
        Observable.fromCallable {
            try {
                prefs = application.getSharedPreferences(InstructionActivity.IS_FIRST_TIME, Context.MODE_PRIVATE)
                val first = prefs.getBoolean(IS_FIRST_TIME, true)
                if (first) {
                    this@MainActivity.startActivity(Intent(this@MainActivity, InstructionActivity::class.java))
                    prefs.edit().putBoolean(IS_FIRST_TIME, false).apply()

                }
            } catch (Ex: Exception) {
                FirebaseCrash.report(Exception("  override fun onCreate in mainActivity Error code 12 Error : " + Ex.message))
                Toast.makeText(this@MainActivity, "Some thing wrong Error code 12 Error : " + Ex.message, Toast.LENGTH_LONG).show()

            }
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe({ checkOverlayPermissionPermission() })

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        MobileAds.initialize(this@MainActivity, AdmobAppID);
        loadBannerAd();
        //loadIntiAdd()

    }

    private fun intiRewardedVideoAd() {
        Log.d("MiainActivty,", "trying to load rewaded video ads");
        MobileAds.initialize(this, "ca-app-pub-5168564707064012~5058501866")
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()

    }


    private fun loadIntiAdd() {
        try {
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = interstitialRealAdd
            mInterstitialAd.loadAd(AdRequest.Builder().addTestDevice("B94C1B8999D3B59117198A259685D4F8").build())
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    mInterstitialAd.loadAd(AdRequest.Builder().addTestDevice("B94C1B8999D3B59117198A259685D4F8").build())
                }
            }
        } catch (E: Exception) {
        }
    }

    private fun loadBannerAd() {
        try {
            mAdView = findViewById(R.id.adView)
            val adRequest = AdRequest.Builder().addTestDevice("B94C1B8999D3B59117198A259685D4F8").build()
            mAdView.loadAd(adRequest)

        } catch (E: Exception) {
        }
    }


    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-5168564707064012/5568743535",
                AdRequest.Builder().build())
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun checkOverlayPermissionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + packageName))
                startActivityForResult(intent, 111)
            }
        }

    }

    private fun registerClipBordBroadCastReciver() {
        clipboard = this@MainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener(object : ClipboardManager.OnPrimaryClipChangedListener {
            override fun onPrimaryClipChanged() {
                try {
                    if (!ClipBrodHelper(this@MainActivity).clipBrodText.isNullOrEmpty()) {
                        object : CountDownTimer(4000, 1000) {

                            override fun onTick(p0: Long) {

                                if (p0.toInt() == 4000) {
                                    //Log.d(TAG,"showing stand out")
                                    StandOutWindow.show(this@MainActivity, MyStandout::class.java, StandOutWindow.DEFAULT_ID)
                                }
                            }

                            override fun onFinish() {
                                if (MyStandout.isRunning)
                                    StandOutWindow.close(this@MainActivity, MyStandout::class.java, StandOutWindow.DEFAULT_ID)

                            }

                        }.start()
                    }
                    // StandOutWindow.show(this@MainActivity, MyStandout::class.java, StandOutWindow.DEFAULT_ID)
                } catch (e: Exception) {
                    e.stackTrace
                    FirebaseCrash.report(Exception(" private fun registerClipBordBroadCastReciver() Error code 11 Error : " + e.message))
                    Toast.makeText(this@MainActivity, "Some thing wrong Error code 11 Error : " + e.message, Toast.LENGTH_LONG).show()
                }

            }

        })
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


    override fun onResume() {
        super.onResume()
        try {
            couter++
            if (couter > 1 && mInterstitialAd != null && mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            }
        } catch (E: Exception) {
            E.printStackTrace()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        when (id) {
            R.id.nav_feedback -> feedBack()
            R.id.nav_rate -> openMarket(this.packageName)
            R.id.nav_facebook_stalker -> openMarket("com.ameerhamza6733.fbprofilescaner")
            R.id.nav_share_this_app -> shareThisApp();
            R.id.nav_action_settings -> {
                startActivity(Intent(this@MainActivity, com.ameerhamza6733.directmessagesaveandrepost.Settings::class.java))
            }
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareThisApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.ameerhamza6733.directmessagesaveandrepost&hl=en")
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun feedBack() {
        EasyFeedback.Builder(this)
                .withEmail("ameerhamza6733@gmail.com")
                .withSystemInfo()
                .build()
                .start()
    }


    private fun openMarket(PackageName: String) {

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PackageName)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
