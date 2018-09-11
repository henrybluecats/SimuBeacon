package com.bluecats.app.simubeacon

import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

const val TAG:String = "Scan2Activity";

class Scan2Activity : BaseActivity() {

    private lateinit var mPendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        setTitle("Scanner 2")

        mPendingIntent = getResultIntent()

    }


    override fun onstart() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBluetoothLeScanner.startScan(ScanActivity.getScanFilters(), ScanActivity.getScanSettings(), mPendingIntent)
        } else {
            Log.e(TAG, "startScan: Android Oreo is required!!!")
        }
    }

    private fun getResultIntent(): PendingIntent {

        return PendingIntent.getBroadcast(this,
                0,
                Intent(this,
                        ScanResultsReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onstop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBluetoothLeScanner.stopScan(mPendingIntent)
        } else {
            Log.e(TAG, "stopScan: Android Oreo is required!!!")
        }
    }
}
