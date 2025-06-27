package com.proj.quest.api;

import com.proj.quest.models.Event;
import com.proj.quest.models.LoginRequest;
import com.proj.quest.models.LoginResponse;
import com.proj.quest.models.RegistrationRequest;
import com.proj.quest.models.User;
import com.proj.quest.models.UpdateProfileRequest;
import com.proj.quest.models.AvatarResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {
    @POST("api/register")
    Call<User> register(@Body RegistrationRequest request);

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/profile")
    Call<User> getProfile(@Header("Authorization") String token);

    @GET("api/events")
    Call<List<Event>> getEvents(@Header("Authorization") String token);

    @PUT("api/profile")
    Call<User> updateProfile(
        @Header("Authorization") String token,
        @Body UpdateProfileRequest request
    );

    @Multipart
    @POST("api/profile/avatar")
    Call<AvatarResponse> uploadAvatar(
        @Header("Authorization") String token,
        @Part MultipartBody.Part avatar
    );

    @GET("api/leaderboard")
    Call<List<User>> getLeaderboard();
}