/*
package com.example.myapplication;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class server {

    private Retrofit retrofit;
    Gson gson = new GsonBuilder().setLenient().create();
    private retrofit = new Retrofit.Builder()
                .baseUrl("http://10.255.81.72:10024/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

    comm_data service = retrofit,create(comm_data.class)

    Call<String> call = null;
    call = service.post(user.data);

    call.enqueue(new Callback<String>(){
        @Override
        public void onResponse(Call<String> call, Response<String> response){
            Log.e("test", response.body().toString));
        }
        @Override
        public void onFailure(Call<String> call, Throwable t){

        }

    }





}
*/
