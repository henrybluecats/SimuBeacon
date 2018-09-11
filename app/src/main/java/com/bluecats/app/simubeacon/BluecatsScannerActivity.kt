package com.bluecats.app.simubeacon

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bluecats.sdk.*

class BluecatsScannerActivity : BaseActivity() {

    private lateinit var beaconManager: BCBeaconManager

    private var beaconManagerCallback:BCBeaconManagerCallback = object : BCBeaconManagerCallback() {
        override fun didRangeBeacons(beacons: MutableList<BCBeacon>) {
            Log.d(TAG, "got: " + beacons.size)
        }
    }

    override fun onstart() {
        BlueCatsSDK.startPurringWithAppToken(this, "9b3999a0-6313-4d5c-874f-4ed066669115")
        beaconManager = BCBeaconManager()
//        beaconManagerCallback = MyCallback()
        beaconManager.registerCallback(beaconManagerCallback)
    }

    override fun onstop() {
        beaconManager.unregisterCallback(beaconManagerCallback)
        BlueCatsSDK.stopPurring()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluecats_scanner)

    }

    class MyCallback: BCBeaconManagerCallback() {
        override fun didRangeBeacons(beacons: MutableList<BCBeacon>) {
            Log.d(TAG, "got :" + beacons.size)
        }
    }


}
