package com.bluecats.app.simubeacon;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

/**
 * Created by henrycheng on 11/12/17.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_ENABLE_BLUETOOTH = 0x110;
    protected static final int REQUEST_CODE_LOCATION_PERMISSIONS = 0x111;
    protected static final int REQUEST_CODE_LOCATION_SERVICE = 0x112;


    protected BluetoothManager mBluetoothManager;
    protected BluetoothAdapter mBluetoothAdapter;
    protected BluetoothLeScanner mBluetoothLeScanner;
    protected BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    protected AlertDialog mAlertDialog;

    public abstract void onstart();
    public abstract void onstop();



    protected boolean permissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    protected void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE_LOCATION_PERMISSIONS);

    }

    @Override
    protected void onPause() {
        super.onPause();
        onstop();
    }

    private boolean checkPermission() {
        if (!mBluetoothAdapter.isEnabled()) {

            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BaseActivity.REQUEST_CODE_ENABLE_BLUETOOTH);
            return false;
        }
        if (!isLocationServiceEnabled()) {
            showLocationServicesAlert();
            return false;
        }

        if (!permissionGranted()) {
            requestPermission();
            return false;
        }

        return true;
    }

    private void showLocationServicesAlert() {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setMessage("This app requires Location Services to run. Would you like to enable Location Services now?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, REQUEST_CODE_LOCATION_SERVICE);

                                }
                            });
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    finish();
                                }
                            });
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .create();
        }
        if (!mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    public boolean isLocationServiceEnabled() {
        boolean value = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                    value = false;
                }
            } catch (Exception e) {
                value = false;
            }
        } else {
            try {
                String locationProviders = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (TextUtils.isEmpty(locationProviders)) {
                    value = false;
                }
            } catch (Exception e) {
                value = false;
            }
        }

        return value;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission()) {
            onstart();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (mBluetoothAdapter != null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (checkPermission()) {
                onstart();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSIONS || requestCode == REQUEST_CODE_LOCATION_SERVICE) {
            if (checkPermission()) {
                onstart();
            }
        }
    }


}
