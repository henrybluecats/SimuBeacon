package com.bluecats.app.simubeacon;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class ScanResultsReceiver extends BroadcastReceiver {

    private String TAG = "ScanResultsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        int bleCallbackType = intent.getIntExtra(BluetoothLeScanner.EXTRA_CALLBACK_TYPE, -1);
        if (bleCallbackType != -1) {
            Log.d(TAG, "Passive background scan callback type: "+bleCallbackType);
            ArrayList<ScanResult> scanResults = intent.getParcelableArrayListExtra(
                    BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT);

            for (ScanResult rst : scanResults) {
                Log.d(TAG, rst.toString());
            }
        }
    }
}
