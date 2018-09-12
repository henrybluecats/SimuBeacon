package com.bluecats.app.simubeacon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTouch;

public class MainActivity extends BaseActivity {


    private static final String TAG = "MainActivity";

    @BindView(R.id.tv_lattitude)
    TextView tv_latitude;
    @BindView(R.id.tv_longitude)
    TextView tv_longitude;
    @BindView(R.id.tv_horizontal)
    TextView tv_horizontal;
    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.et_latitude)
    EditText et_latitude;
    @BindView(R.id.et_longitude)
    EditText et_longitude;
    @BindView(R.id.et_horizontal)
    EditText et_horizontal;
    @BindView(R.id.et_time)
    EditText et_time;

    @BindView(R.id.cb_lattitude)
    CheckBox cb_latitude;
    @BindView(R.id.cb_longitude)
    CheckBox cb_longitude;
    @BindView(R.id.cb_hori)
    CheckBox cb_hori;
    @BindView(R.id.cb_time)
    CheckBox cb_time;

    @BindView(R.id.tv_status)
    TextView tv_status;

    BluetoothAdapter mBluetoothAdapter;

    BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    LocationManager mLocationManager;

    Location mLastKnownLocation;

    @OnTouch(R.id.btn_push)
    public boolean onTouch(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            startBroadcast();
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            stopBroadcast();
        }
        return true;
    }

    @OnCheckedChanged({R.id.cb_lattitude, R.id.cb_longitude, R.id.cb_hori, R.id.cb_time})
    public void onCheckChanged(CompoundButton v, boolean checked) {
        switch (v.getId()) {
            case R.id.cb_lattitude: et_latitude.setEnabled(!checked); break;
            case R.id.cb_longitude: et_longitude.setEnabled(!checked); break;
            case R.id.cb_hori: et_horizontal.setEnabled(!checked); break;
            case R.id.cb_time: et_time.setEnabled(!checked); break;
        }
    }
//    @OnClick(R.id.tb_running)
//    void onClick() {
//        if (tb_running.isChecked()) {
//            startBroadcast();
//        } else {
//            stopBroadcast();
//        }
//        updateUI();
//    }

    AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "onStartSuccess " + settingsInEffect.toString());
        }
    };

    private void stopBroadcast() {
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    private void startBroadcast() {
        if (mBluetoothAdapter.isEnabled() == false) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
            return;
        }

        if (!permissionGranted()) {
            requestPermission();
            return;
        }
        startBle();
    }

    private void startBle() {
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build();

        try {
            AdvertiseData data = new AdvertiseData.Builder()
                    .addManufacturerData(0x104, gpsDataBuilder())
//                .addServiceUuid(ParcelUuid.fromString("4CEF"))
                    .build();
            mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);

        } catch (Exception e) {

        }


    }

    public byte[] gpsDataBuilder() {
        byte[] gpsdata = new byte[22];

        int pos = 0;
        gpsdata[0] = 0x5A; //bc ad type
        gpsdata[1] = 0x00; //team id
        gpsdata[2] = 0x67; //mpow
        gpsdata[3] = 0x60; //batt
        gpsdata[4] = 0x10; //count: 16 bytes
        gpsdata[5] = 0x09; //gps type

        int lat = 0x00000000;
        int lng = 0x00000000;
        int hor = 0xffffffff;
        int time = 0x00000000;

        if (et_latitude.getText().toString().trim().length() > 0) {
            try {
                lat = (int) (Double.parseDouble(et_latitude.getText().toString().trim()) * 10000000.0);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (et_longitude.getText().toString().trim().length() > 0) {
            try {
                lng = (int) (Double.parseDouble(et_longitude.getText().toString().trim()) * 10000000.0);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (et_horizontal.getText().toString().trim().length() > 0) {
            try {
                hor = (int) (Double.parseDouble(et_horizontal.getText().toString().trim()) * 1000.0);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (et_time.getText().toString().trim().length() > 0) {
            try {
                time = (int) (Long.parseLong(et_time.getText().toString().trim()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        pos = 6;
        fill(gpsdata, pos, lat);
        pos += 4;
        fill(gpsdata, pos, lng);
        pos += 4;
        fill(gpsdata, pos, hor);
        pos += 4;
        fill(gpsdata, pos, time);

        return gpsdata;
    }

    private void fill(byte[] data, int offset, int value) {

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(0, value);
        byte[] arr = buffer.array();
        System.arraycopy(arr, 0, data, offset, 4);
    }

    private static String HEX = "0123456789ABCDEF";

    private static byte[] str2bytes(byte[] prefix, String str, int major, int minor) {
        int prefix_len = 0;
        if (prefix != null) {
            prefix_len = prefix.length;
        }
        byte[] data = new byte[21 + prefix_len];
        int pos = 0;
        if (prefix_len > 0) {
            System.arraycopy(prefix, 0, data, 0, prefix_len);
            pos += prefix_len;
        }

        if (TextUtils.isEmpty(str)) {
            return data;
        }
        str = str.replace("-", "").replace(" ", "").replace(".", "");
        int offset = 0;
        for (; offset < str.length(); ) {
            char one = str.charAt(offset);
            offset++;
            if (offset >= str.length()) {
                break;
            }
            char two = str.charAt(offset);
            offset++;
            data[pos] = (byte) ((HEX.indexOf(one) << 4) | HEX.indexOf(two));
            pos++;
        }
        if (pos != 16) {
            pos = 16;
        }
        int h1 = major / 255;
        int h2 = major % 255;
        if (h1 > 255) {
            h1 = h1 % 255;
        }
        data[pos++] = (byte) h1;
        data[pos++] = (byte) h2;

        h1 = minor / 255;
        h2 = minor % 255;
        if (h1 > 255) {
            h1 = h1 % 255;
        }

        data[pos++] = (byte) h1;
        data[pos++] = (byte) h2;

        data[pos++] = (byte) 0x67;
        return data;
    }

    private String byte2str(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        for (byte one : arr) {
            sb.append(String.format("%02X", one));
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        updateUI();
    }

    private void updateUI() {
    }


    @Override
    public void onstart() {
//        startBle();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0.1f, mLocationListener);
    }

    @Override
    public void onstop() {
        try {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        }catch (Exception e) {
            //
        }
        mLocationManager.removeUpdates(mLocationListener);
    }

    private void updateLocation() {
        if (mLastKnownLocation == null) {
            return;
        }
        Location location = new Location(mLastKnownLocation);

        if (location == null) return;
        StringBuilder sb = new StringBuilder();

        sb.append("Latitude: ").append(location.getLatitude()).append('\n')
                .append("Longitude: ").append(location.getLongitude()).append('\n')
                .append("Accuracy: ").append(location.getAccuracy()).append('\n')
                .append("Fix time: ").append(location.getTime()/1000);
        tv_status.setText(sb.toString());

        if (cb_latitude.isChecked()) {et_latitude.setText(""+location.getLatitude());}
        if (cb_longitude.isChecked()) {et_longitude.setText(""+location.getLongitude());}
        if (cb_hori.isChecked()) {et_horizontal.setText(""+location.getAccuracy());}
        if (cb_time.isChecked()) {et_time.setText(""+location.getTime()/1000);}
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            mLastKnownLocation = new Location(location);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateLocation();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}
