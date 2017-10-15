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
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.kekstudio.dachshundtablayout.DachshundTabLayout
import com.kingfisher.easy_sharedpreference_library.SharedPreferencesManager
import com.webianks.easy_feedback.EasyFeedback
import wei.mark.standout.StandOutWindow


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private final lateinit var clipboard: ClipboardManager
    private final val isFirstTime = "isFirstTime"

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        val mViewPager = findViewById(R.id.view_pager) as ViewPager
        val mTabLayout = findViewById(R.id.tab_layout) as DachshundTabLayout

        mViewPager.adapter = pagerAdupter(supportFragmentManager)
        mTabLayout.setupWithViewPager(mViewPager);
        SharedPreferencesManager.init(this, true)
        registerClipBord()
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        try {
            prefs = application.getSharedPreferences(isFirstTime, Context.MODE_PRIVATE)
            val first = prefs.getBoolean(isFirstTime, true)
            if (first) {
                prefs.edit().putBoolean(isFirstTime, false).apply()
                checkOverlayPermissionPermission()
            }
        } catch (Ex: Exception) {

        }
        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
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

    private fun registerClipBord() {
        clipboard = this@MainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener(object : ClipboardManager.OnPrimaryClipChangedListener {
            override fun onPrimaryClipChanged() {
                try {
                    if (!ClipBrodHelper(this@MainActivity).clipBrodText.isNullOrEmpty()) {
                        object : CountDownTimer(4000, 1000) {

                            override fun onTick(p0: Long) {

                                if (p0.toInt() == 4000) {
                                    //Log.d(TAG,"showing stand out")
                                    StandOutWindow.show(this@MainActivity, myStandout::class.java, StandOutWindow.DEFAULT_ID)
                                }
                            }

                            override fun onFinish() {
                                if (myStandout.isRunning)
                                    StandOutWindow.close(this@MainActivity, myStandout::class.java, StandOutWindow.DEFAULT_ID)

                            }

                        }.start()
                    }
                    // StandOutWindow.show(this@MainActivity, myStandout::class.java, StandOutWindow.DEFAULT_ID)
                } catch (e: Exception) {
                    e.stackTrace
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
            Toast.makeText(this, "i am working on it, if you have any suggestion please send me feedback ", Toast.LENGTH_SHORT).show()
            true
        } else super.onOptionsItemSelected(item)

    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        when (id) {
            R.id.nav_feedback -> feedBack()
            R.id.nav_rate -> openMarket(this.packageName)
            R.id.nav_facebook_stalker -> openMarket("com.ameerhamza6733.fbprofilescaner")
            R.id.nav_share_this_app -> shareThisApp();
        }


        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
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
