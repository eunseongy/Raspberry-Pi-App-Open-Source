package com.example.dust_sensor_connection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface comm_data {

    //Advertising/Connection (dust sensor)
    @FormUrlEncoded
    @POST("dustsensor_v2/sensing/")
    Call<String> sensing(
            @Field("sensor") String sensor, //몇 조의 라즈베리파이인지?
            @Field("mode") String mode,     //데이터 수집 방식 ex. advertising/connection
            @Field("mac") String mac,       //라즈베리파이 mac주소
            @Field("receiver") String receiver, //데이터를 보내는 기기의 고유 ID
            @Field("time") String time,         //adveritising packet에서 얻은 시간 데이터 값
            @Field("otp") String otp,           //adveritising packet에서 얻은 otp 값
            @Field("key") String key,           //location 인증 값
            @Field("data") String data          //adveritising packet에서 얻은 미세먼지 데이터 값 0.1/2.5/10 순서로 전송
    );

    //Advertising/Connection (air quality sensor)

    @FormUrlEncoded
    @POST("airquality/sensing/")
    Call<String> air_string(
            @Field("sensor") String sensor, //몇 조의 라즈베리파이인지?
            @Field("mode") String mode,     //데이터 수집 방식 ex. advertising/connection
            @Field("mac") String mac,       //라즈베리파이 mac주소
            @Field("receiver") String receiver, //데이터를 보내는 기기의 고유 ID
            @Field("time") String time,         //adveritising packet에서 얻은 시간 데이터 값
            @Field("otp") String otp,           //adveritising packet에서 얻은 otp 값
            @Field("key") String key,           //location 인증 값
            @Field("data") String data          //adveritising packet에서 얻은 air quality 값 adv의 경우 2개로 나눠진 16진수 값 합치기
    );

    @FormUrlEncoded
    @POST("localization/locationcheck/")
        //Wifi scan 결과 전송
        //Mac주소!rssi/Mac주소!rssi 형식으로 전송
    Call<String> location(
            @Field("wifidata") String data
    );


}