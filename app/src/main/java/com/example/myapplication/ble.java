package com.example.myapplication;


import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


@SuppressLint("MissingPermission")
public class ble{

    private static final int REQUEST_ENABLE_BT = 1;
    //블루투스가 활성화되었는지 여부를 확인하기 위해 사용하는 코드

    private String scannedData;
    //블루투스로 수집한 데이터를 저장하는 변수

    BluetoothAdapter ap1;
    //기본 로컬 불루투스 어댑터에 대한 핸들을 가져온다.
    //항상 기본 어댑터가 반환됨.


    public void BLEscan(BluetoothAdapter ap1)
    {
        this.ap1 = ap1;
        if (!ap1.isEnabled()) {
            ap1.enable();
        }
        // Bluetooth adapter를 사용하지 못하는 상태면 사용 가능하게 설정


        ap1.startLeScan(scanCallback_le); //BLE Scan 시작, scancallback_le 함수를 직접 작성하여 원하는 동작

//        while (!foundDevice) {
//            try {
//                Thread.sleep(10); // 1초마다 체크
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        ap1.stopLeScan(scanCallback_le); //BLE Scan 중지

    }



    Gson gson = new GsonBuilder().setLenient().create();
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://203.255.81.72:10021/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    private final BluetoothAdapter.LeScanCallback scanCallback_le = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            Log.e("bletest","scan");
            String MacAdd = device.getAddress();
            String data = byteArrayToHex(scanRecord);
            //scannedData = data;         //16진수로 바꾼 데이터를 scannedData에 복사
//            Log.e("bletest2", MacAdd);
            if(MacAdd.equals("B8:27:EB:7F:E7:58")){
            comm_data service = retrofit.create(comm_data.class);


            scannedData = makePW(data);
            Call<String> call = null;
            call = service.getMember("5jo, haemi, jungseob, hyuckju, eunsung, byunggyu ", scannedData);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.e("MacAdd", MacAdd);
                    Log.e("comm", response.body().toString());
                    //응답을 처리하는 코드
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("err","1234123");
                    //실패 시 처리하는 코드
                }
            });

             ap1.stopLeScan(scanCallback_le); //BLE Scan 중지
            }

        }
    };

    public String byteArrayToHex(byte[] scanRecord){
        StringBuilder sb= new StringBuilder();
        //Log.e("tag2", Arrays.toString(scanRecord));
        for(final byte b: scanRecord)
            sb.append(String.format("%02x ", b));
        return sb.toString();
    }

    public String makePW(String data){
        StringBuilder sd= new StringBuilder();
        for(int i =0;i<data.length();i++)
            sd.append(data.indexOf(i));

        return sd.toString();
    }

}
