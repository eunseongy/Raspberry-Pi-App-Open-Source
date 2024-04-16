package com.example.myapplication;


import static androidx.core.app.ActivityCompat.startActivityForResult;

import static java.lang.Integer.parseInt;

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
        Log.e("ap", String.valueOf(ap1));
        if (ap1 == null) {
            // Bluetooth를 지원하지 않는 경우 처리
            Log.e("Bluetooth", "Bluetooth not supported on this device");
            return;
        }

        if (!ap1.isEnabled()) {
            Log.e("test", "enabled");
            ap1.enable();
        }
        // Bluetooth adapter를 사용하지 못하는 상태면 사용 가능하게 설정



        ap1.startLeScan(scanCallback_le); //BLE Scan 시작, scancallback_le 함수를 직접 작성하여 원하는 동작

//        Log.e("test", "end");


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
            String MacAdd = device.getAddress();
            String data = byteArrayToHex(scanRecord);
            String timeOTP="";
            String sensingTime="";
            String sensorData="";

            int flag = 0;
            String sensor="";
//            Log.e("test","scan");


            if(MacAdd.equals("D8:3A:DD:42:AC:7F")||MacAdd.equals("D8:3A:DD:42:AC:64")||MacAdd.equals("B8:27:EB:DA:F2:5B")||MacAdd.equals("B8:27:EB:0C:F3:83")) {
                flag= 1;
                sensor="1jo";
            }
            if(MacAdd.equals("D8:3A:DD:79:8F:97")||MacAdd.equals("D8:3A:DD:79:8F:B9")||MacAdd.equals("D8:3A:DD:79:8F:54")||MacAdd.equals("D8:3A:DD:79:8F:80")) {
                flag= 1;
                sensor="2jo";
            }
            if(MacAdd.equals("D8:3A:DD:79:8E:D9")||MacAdd.equals("D8:3A:DD:42:AC:9A")||MacAdd.equals("D8:3A:DD:42:A8:FB")||MacAdd.equals("D8:3A:DD:79:8E:9B")) {
                flag= 1;
                sensor="3jo";
            }
            if(MacAdd.equals("D8:3A:DD:78:A7:1A")||MacAdd.equals("D8:3A:DD:79:8E:BF")||MacAdd.equals("D8:3A:DD:79:8E:92")||MacAdd.equals("D8:3A:DD:79:8F:59")) {
                flag= 1;
                sensor="4jo";
            }
            if(MacAdd.equals("B8:27:EB:D3:40:06")||MacAdd.equals("B8:27:EB:E4:D0:FC")||MacAdd.equals("B8:27:EB:47:8D:50")||MacAdd.equals("B8:27:EB:57:71:7D")) {
                Log.e("test", data);
                flag = 1;
                sensor = "5jo";
            }


            if(flag == 1) {
                String[] hexArray = data.split(" ");
                timeOTP =  findData(hexArray, 1);
                sensingTime = findData(hexArray, 2);
                sensorData = findData(hexArray, 3);

                Log.e("test", "sensor: "+ sensor);
                Log.e("test", "mac: "+ MacAdd);
                Log.e("test", timeOTP);
                Log.e("test", sensingTime);
                Log.e("test", sensorData);



                ap1.stopLeScan(scanCallback_le); //BLE Scan 중지


                comm_data service = retrofit.create(comm_data.class);


                Call<String> call = null;
                call = service.getMember(sensor, MacAdd, "5jo", sensingTime, timeOTP, sensorData);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Log.e("MacAdd", MacAdd);
                        Log.e("comm", response.body().toString());
                        //응답을 처리하는 코드
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("err", "1234123");
                        //실패 시 처리하는 코드
                    }
                });
            }

        }
    };


    private static String findData(String[] hexArray,int n) {
        boolean found = false;
        StringBuilder result = new StringBuilder();

        if(n==1){       //TimeOTP 찾을 때
            for (int i = 0; i < hexArray.length - 2; i++) {
                if (hexArray[i].equals("f0") && hexArray[i + 1].equals("f0")) {
                    // "f0 f0" 다음의 데이터를 추출 (다음 3개의 요소)
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 2],16)));
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 3],16)));
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 4],16)));
                    found = true;
                    break;
                }
            }
        }
        if(n==2){       //센싱데이터 찾을 때
            for (int i = 0; i < hexArray.length - 2; i++) {
                if (hexArray[i].equals("99") && hexArray[i + 1].equals("99")) {
                    // "f0 f0" 다음의 데이터를 추출 (다음 3개의 요소)
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 2],16)));
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 3],16)));
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 4],16)));
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 5],16)));
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 6],16)));
                    found = true;
                    break;
                }
            }
        }
        if(n==3){       //센서 데이터 찾을 때
            for (int i = 0; i < hexArray.length - 2; i++) {
                if (hexArray[i].equals("fd")) {
                    // "f0 f0" 다음의 데이터를 추출 (다음 3개의 요소)
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 1],16)));
                    result.append("/");
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 2],16)));
                    result.append("/");
                    result.append(String.valueOf(Integer.valueOf(hexArray[i + 3],16)));
                    found = true;
                    break;
                }
            }
        }


        if (!found) {
            System.out.println("Target pattern not found.");
            return ""; // 원하는 패턴이 없을 경우 빈 문자열 반환
        }

        return result.toString();
    }



    public String byteArrayToHex(byte[] scanRecord){
        StringBuilder sb= new StringBuilder();
        //Log.e("tag2", Arrays.toString(scanRecord));
        for(final byte b: scanRecord)
            sb.append(String.format("%02x ", b));
        return sb.toString();
    }
}