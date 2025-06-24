package com.proj.quest.api;

import com.proj.quest.models.Event;
import com.proj.quest.models.LoginRequest;
import com.proj.quest.models.LoginResponse;
import com.proj.quest.models.RegistrationRequest;
import com.proj.quest.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/register")
    Call<User> register(@Body RegistrationRequest request);

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/profile")
    Call<User> getProfile(@Header("Authorization") String token);

    @GET("api/events")
    Call<List<Event>> getEvents(@Header("Authorization") String token);
}