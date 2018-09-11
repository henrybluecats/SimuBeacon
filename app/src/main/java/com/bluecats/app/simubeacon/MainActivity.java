package com.bluecats.app.simubeacon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    private static final String TAG = "MainActivity";
    @BindView(R.id.et_name)
    EditText et_name;

    @BindView(R.id.cb_name)
    CheckBox cb_name;

    @BindView(R.id.cb_bluecats)
    CheckBox cb_bluecats;

    @BindView(R.id.tb_running)
    ToggleButton tb_running;

    @BindView(R.id.tv_status)
    TextView tv_status;

    @BindView(R.id.pb)
            View pb;

    @BindView(R.id.et_uuid) EditText et_uuid;
    @BindView(R.id.et_major) EditText et_major;
    @BindView(R.id.et_minor) EditText et_minor;

    BluetoothAdapter mBluetoothAdapter;

    BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    @OnClick(R.id.tb_running)
    void onClick() {
        if (tb_running.isChecked()) {
            startBroadcast();
        } else {
            stopBroadcast();
        }
        updateUI();
    }

    AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "onStartSuccess "+settingsInEffect.toString());
        }
    };

    private void stopBroadcast() {
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    private void startBroadcast() {
        tv_status.setVisibility(View.GONE);
        if (mBluetoothAdapter == null) {
            tv_status.setText("BluetoothAdapter is null");
            tv_status.setVisibility(View.VISIBLE);
            return;
        }
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
                    .addManufacturerData(0x104, GpsDataBuilder(0x01020304, 0x01020304, 0x01020304, 0x01020304))
//                .addServiceUuid(ParcelUuid.fromString("4CEF"))
                    .build();
            mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);

        } catch (Exception e) {

        }


    }

    public byte[] GpsDataBuilder(int lat, int lng, int hor, int time){
        byte[] gpsdata = new byte[22];

        int pos = 0;
        gpsdata[0] = 0x5A; //bc ad type
        gpsdata[1] = 0x00; //team id
        gpsdata[2] = 0x67; //mpow
        gpsdata[3] = 0x60; //batt
        gpsdata[4] = 0x10; //count: 16 bytes
        gpsdata[5] = 0x09; //gps type

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
        str = str.replace("-","").replace(" ","").replace(".", "");
        int offset = 0;
        for (; offset < str.length();) {
            char one = str.charAt(offset);
            offset++;
            if (offset >= str.length()) {
                break;
            }
            char two = str.charAt(offset);
            offset++;
            data[pos] = (byte)((HEX.indexOf(one) << 4) | HEX.indexOf(two));
            pos++;
        }
        if (pos != 16) {
            pos = 16;
        }
        int h1 = major/255;
        int h2 = major%255;
        if (h1 > 255) {
            h1 = h1 % 255;
        }
        data[pos++] = (byte)h1;
        data[pos++] = (byte)h2;

        h1 = minor/255;
        h2 = minor%255;
        if (h1 > 255) {
            h1 = h1 % 255;
        }

        data[pos++] = (byte)h1;
        data[pos++] = (byte)h2;

        data[pos++] = (byte)0x67;
        return data;
    }

    private String byte2str(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        for(byte one : arr) {
            sb.append(String.format("%02X", one));
        }
        return sb.toString();
    }

    private String getUUID() {
        return et_uuid.getText().toString();
    }

    private int getMajor(){
        return Integer.valueOf(et_major.getText().toString());
    }

    private int getMinor() {
        return Integer.valueOf(et_minor.getText().toString());
    }


    @OnClick(R.id.cb_name)
    void click_cb_name() {
        et_name.setEnabled(cb_name.isChecked());
    }

    @OnClick(R.id.cb_bluecats)
    void click_cb_bluecats() {
        Log.d(TAG, "cb_bluecats "+ cb_bluecats.isChecked());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        if (TextUtils.isEmpty(et_uuid.getText().toString())) {
            et_uuid.setText("61687109-905F-4436-91F8-E602F514C96D");
        }
        updateUI();
    }

    private void updateUI() {
        if (tb_running.isChecked()) {
            cb_name.setEnabled(false);
            cb_bluecats.setEnabled(false);
            et_name.setEnabled(false);
            pb.setVisibility(View.VISIBLE);
        } else {
            cb_name.setEnabled(true);
            cb_bluecats.setEnabled(true);
            if (cb_name.isChecked()) {
                et_name.setEnabled(true);
            } else {
                et_name.setEnabled(false);
            }
            pb.setVisibility(View.GONE);
        }
    }


    @Override
    public void onstart() {
        startBle();
    }

    @Override
    public void onstop() {
        try {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        }catch (Exception e) {
            //
        }
    }

}
