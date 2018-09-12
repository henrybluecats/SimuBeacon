package com.bluecats.app.simubeacon

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_startup.*

class StartupActivity : AppCompatActivity() {

    private var TAG:String = "StartupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        bt_advertiser.setOnClickListener { view -> onClick(view) }
//        bt_scanner.setOnClickListener{ view -> onClick(view) }
//        bt_scanner2.setOnClickListener{ view -> onClick(view) }
//        bc_scanner.setOnClickListener { view -> onClick(view) }
    }

    fun onClick(view: View) {
        when (view) {
            bt_advertiser -> {
                startActivity(Intent(this, MainActivity::class.java))

            }
//            bt_scanner -> {
//                startActivity(Intent(this, ScanActivity::class.java))
//            }
//            bt_scanner2 -> {
//                startActivity(Intent(this, Scan2Activity::class.java))
//            }
//            bc_scanner -> {
//                startActivity(Intent(this, BluecatsScannerActivity::class.java))
//            }
        }

    }
}
