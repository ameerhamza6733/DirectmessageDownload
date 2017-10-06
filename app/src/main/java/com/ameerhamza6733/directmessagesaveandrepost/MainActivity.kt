package com.ameerhamza6733.directmessagesaveandrepost

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer

import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity

import com.kekstudio.dachshundtablayout.DachshundTabLayout
import com.kingfisher.easy_sharedpreference_library.SharedPreferencesManager

import wei.mark.standout.StandOutWindow
import java.util.concurrent.TimeoutException


class MainActivity : AppCompatActivity() {
    private final lateinit var clipboard: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       var mViewPager = findViewById(R.id.view_pager) as ViewPager
        var mTabLayout = findViewById(R.id.tab_layout) as DachshundTabLayout
        mViewPager.adapter =  pagerAdupter(supportFragmentManager)
        mTabLayout.setupWithViewPager(mViewPager);
        SharedPreferencesManager.init(this, true)
        registerClipBord()
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

}

