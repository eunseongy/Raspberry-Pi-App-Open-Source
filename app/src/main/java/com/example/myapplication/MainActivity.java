package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View.OnClickListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import android.bluetooth.le.BluetoothLeAdvertiser;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;



public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter.LeScanCallback scanCallback;
    BluetoothAdapter blead = BluetoothAdapter.getDefaultAdapter();
    postdata myData = new postdata();

    String key = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt_add = findViewById(R.id.bt_add);
        Button bt_delete = findViewById(R.id.bt_delete);
        Button bt_communication = findViewById(R.id.bt_communication);
        Button bt_showdata = findViewById(R.id.bt_datashow);
        EditText ed_text = findViewById(R.id.ed_text);
        TextView tv = findViewById(R.id.tv);

        if (!blead.isEnabled()) blead.enable();

        bt_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                blead.startLeScan(scanCallback_le); //scanCallback_le 직접 함수 만들기
                if(key != null){
                    myData.set_data("5jo, Ye Eun Sung, Yoon Jung Seop, Kang Byung Gyu, Yang Hae Mi, Won Hyuk Joo", key);
                    Log.d("key", "found Data : " + key);
                }
            }
        });
        bt_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                blead.stopLeScan(scanCallback_le);//scan 동작 멈추게 만들어야함.
            }
        });
        bt_showdata.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //데이터 찾고, 보이기 만들기
                myData.data_show();
            }
        });
        bt_communication.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                myData.dataSend();
                // 데이터 전송하는거 만들기
            }
        });
    }

    private BluetoothAdapter.LeScanCallback scanCallback_le = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String MacAdd = device.getAddress(); //여기에서 address를 판별해서 받아야하는 걸 만들어야한다.
            String Data = byteArrayToHex(scanRecord);

            if (MacAdd.equals("B8:27:EB:7F:E7:58")) {
                String pattern = "998899";
                key = byteArrayToHex(scanRecord);
                int index = key.indexOf(pattern);
                Log.d("tt", String.valueOf(index));
                if (index != -1) { // 첫 번째 패턴이 발견된 경우
                    // 첫 번째 패턴 다음의 내용값을 추출합니다.
                    String content1 = key.substring(index + pattern.length()).trim();
                    Log.d("tt", content1);

                    // 두 번째 패턴의 인덱스를 찾습니다.
                    int index2 = content1.indexOf(pattern);
                    Log.d("tt", String.valueOf(index2));
                    if (index2 != -1) { // 두 번째 패턴이 발견된 경우
                        // 두 번째 패턴의 전 내용값을 추출합니다.
                        key = content1.substring(0, index2).trim();
                        Log.d("key", "Select key: " + key);
                    }
                }
            }

            Log.d("BLE", "Scanned Device: " + MacAdd + ", Data: " + Data);
        }

    };
    public String byteArrayToHex(byte[] byteArray) {
        StringBuilder hex = new StringBuilder(); //수정
        for (byte b : byteArray) {
            hex.append(String.format("%02x", b&0xff)); //수정
        }
        return hex.toString();
    }


    public static class postdata {
        Gson gson = new GsonBuilder().setLenient().create();
        private final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://203.255.81.72:10021/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        @Expose
        @SerializedName("user") private String user;
        @SerializedName("data") private String data;

        public void set_data(String user, String data){
            this.user = user;
            this.data = data;
        }

        public void data_show(){
            Log.e("test", user+data);
        }
        public String get_data() {

            return data;}

        public void dataSend(){
            comm_data service = retrofit.create(comm_data.class);
            Call<String> call = null;
            //Data에 암호 작성할 것
            //직접 수정
            call = service.post(user, data); // 위에서 직접 설정해서 보내기 (이거 수정해야함 !! )
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.e("test", response.body().toString());
                    //응답을 처리하는 코드
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    //실패 시 처리하는 코드
                }
            });
        }

    }
}
