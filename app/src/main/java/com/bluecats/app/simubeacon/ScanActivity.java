package com.bluecats.app.simubeacon;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;

import butterknife.ButterKnife;

public class ScanActivity extends BaseActivity {

    private static final String TAG = "ScanActivity";

    private PendingIntent mResultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        ButterKnife.bind(this);


        mResultIntent = getResultIntent();

        setTitle("Scanner 1");
        
    }

    private PendingIntent getResultIntent() {

        return PendingIntent.getBroadcast(this,
                0,
                new Intent(this,
                        ScanResultsReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onstart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBluetoothLeScanner.startScan(getScanFilters(), getScanSettings(), mResultIntent);
        } else {
            mBluetoothLeScanner.startScan(getScanFilters(), getScanSettings(), mScanCallback);
        }
    }

    @Override
    public void onstop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBluetoothLeScanner.stopScan(mResultIntent);
        } else {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult:"+result.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public static ScanSettings getScanSettings() {
        ScanSettings.Builder settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                ;
//        settings.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
//        settings.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);

        return settings.build();
    }

    public static List<ScanFilter> getScanFilters() {
        return null;
    }


}
