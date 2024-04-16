package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class postdata {
    BluetoothAdapter ap1;
    @Expose
//    @SerializedName("user") private String user;
    @SerializedName("sensor") private String sensor;
    @SerializedName("receiver") private String receiver;
    @SerializedName("time") private String time;
    @SerializedName("otp") private String otp;
    @SerializedName("data") private String data;




    public void set_data(String sensor, String receiver, String time, String otp, String data){
        this.sensor = sensor;
        this.receiver = receiver;
        this.time= time;
        this.otp = otp;
        this.data = data;
    }

    Gson gson = new GsonBuilder().setLenient().create();
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://203.255.81.72:10021/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    public void dataSend(){
        comm_data service = retrofit.create(comm_data.class);
        ///////////////////
        ble ble = new ble();
        ble.BLEscan(ap1);
        /////////////////// data 가져오는 부분

//        //Log.e("testtest", "call");
//        Call<String> call = null;
//        call = service.getMember("6jo, haemi, ", data);

    }

    public void data_show(){
//        Log.e("test", user+data);
    }
    public String get_data() {return data;}

}
