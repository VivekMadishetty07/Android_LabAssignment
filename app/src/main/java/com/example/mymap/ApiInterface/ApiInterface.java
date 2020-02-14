package com.example.mymap.ApiInterface;

import com.example.mymap.model.DirectionResponse;
import com.example.mymap.model.PlacesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiInterface
{
    @GET
    Call<PlacesResponse> fetchdata(@Url String urlstring);

    @GET
    Call<DirectionResponse> fetchdirection(@Url String urlstring);

//    @POST("forgetPassword")
//    @FormUrlEncoded
//    Call<ResetRec> resetrec(@Field("email") String email);
}
