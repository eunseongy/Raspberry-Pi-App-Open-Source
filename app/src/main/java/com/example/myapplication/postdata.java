package com.example.myapplication;

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
    public String get_data() {return data;}



}