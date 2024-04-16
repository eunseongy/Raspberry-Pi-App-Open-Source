package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface comm_data {

    //Key-Value 형식의 데이터 전송
    @FormUrlEncoded
    @POST("dustsensor/sensing/")
    Call<String> getMember(
            @Field("sensor") String sensor,
            @Field("mac") String mac,
            @Field("receiver") String receiver,
            @Field("time") String time,
            @Field("otp") String otp,
            @Field("data") String data
    );

    // JSON 형식의 데이터 전송
    @POST("dustsensor/commtest_json/")
    Call<postdata> post_json(
            @Body postdata pd
    );

    @GET("dustsensor/commtest_get/")
    Call<String> get(
            @Query("sensor") String sensor,
            @Query("receiver") String receiver,
            @Query("time") String time,
            @Query("otp") String otp,
            @Query("data") String data
    );


}
