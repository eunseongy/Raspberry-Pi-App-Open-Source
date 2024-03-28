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
    @POST("dustsensor/commtest/")
    Call<String> getMember(
            @Field("user") String user,
            @Field("data") String data);

    // JSON 형식의 데이터 전송
    @POST("dustsensor/commtest_json/")
    Call<postdata> post_json(
            @Body postdata pd
    );

    @GET("dustsensor/commtest_get/")
    Call<String> get(
            @Query("user") String user,
            @Query("data") String data
    );


}
