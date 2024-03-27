package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

public class ble {
    private static final long SCAN_PERIOD = 10000; // 스캔 기간을 설정합니다. 여기서는 10초로 설정했습니다.
    BluetoothAdapter blead = BluetoothAdapter.getDefaultAdapter();
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String macAddress = device.getAddress();
            String data = byteArrayToHex(scanRecord);
            // 여기서 스캔 결과를 처리합니다.
        }
    };
    public ble() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // 장치가 블루투스를 지원하지 않는 경우
            throw new RuntimeException("Device does not support Bluetooth");
        }
        handler = new Handler();
    }

    @SuppressLint("MissingPermission")
    public void startScan() {

        if (!blead.isEnabled()) {
            blead.enable();
        }

        blead.startLeScan(scanCallback);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                blead.stopLeScan(scanCallback);
            }
        }, SCAN_PERIOD);
    }

    public String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

/*
    BluetoothAdapter blead = BluetoothAdapter.getDefaultAdapter();

    if(!blead.isEnabled())
        blead.enable();

    blead.startLeScan(scanCallback_le);
    blead.stopLeScan(scanCallback_le);



/*
private Retrofit retrofit;
Gson gson = new GsonBuilder().setLenient().create();
retrofit = new Retrofit.Builder()
            .base
*/
}


