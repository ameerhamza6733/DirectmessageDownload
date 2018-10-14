package com.ameerhamza6733.directmessagesaveandrepost

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, HistoryFragment.newInstance(1))
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
}
