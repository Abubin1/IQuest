package com.proj.quest.api;

import com.proj.quest.models.Event;
import com.proj.quest.models.LoginRequest;
import com.proj.quest.models.LoginResponse;
import com.proj.quest.models.RegistrationRequest;
import com.proj.quest.models.Team;
import com.proj.quest.models.TeamRegistrationRequest;
import com.proj.quest.models.TeamRegistrationResponse;
import com.proj.quest.models.User;
import com.proj.quest.models.UpdateProfileRequest;
import com.proj.quest.models.AvatarResponse;
import com.proj.quest.models.InviteRequest;
import com.proj.quest.models.InviteResponse;
import com.proj.quest.models.KickRequest;
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
import retrofit2.http.Query;
import retrofit2.http.Part;
import retrofit2.http.DELETE;

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

    @GET("api/my-team")
    Call<Team> getMyTeam(
        @Header("Authorization") String token
    );

    @POST("api/teams")
    Call<Team> createTeam(
        @Header("Authorization") String token,
        @Body Team team
    );

    @POST("api/teams/register")
    Call<TeamRegistrationResponse> registerTeamForEvent(
        @Header("Authorization") String token,
        @Body TeamRegistrationRequest request
    );

    @POST("api/invites")
    Call<Void> inviteUser(
        @Header("Authorization") String token,
        @Body InviteRequest inviteRequest
    );

    @GET("api/invites")
    Call<List<InviteResponse>> getInvites(
        @Header("Authorization") String token
    );

    @POST("api/invites/{id}/accept")
    Call<Void> acceptInvite(
        @Header("Authorization") String token,
        @retrofit2.http.Path("id") int inviteId
    );

    @POST("api/invites/{id}/decline")
    Call<Void> declineInvite(
        @Header("Authorization") String token,
        @retrofit2.http.Path("id") int inviteId
    );

    @POST("api/teams/{teamId}/kick")
    Call<Void> kickUser(
        @Header("Authorization") String token,
        @retrofit2.http.Path("teamId") int teamId,
        @Body KickRequest kickRequest
    );

    @POST("api/teams/{teamId}/leave")
    Call<Void> leaveTeam(
        @Header("Authorization") String token,
        @retrofit2.http.Path("teamId") int teamId
    );

    @DELETE("api/teams/{teamId}")
    Call<Void> deleteTeam(
        @Header("Authorization") String token,
        @retrofit2.http.Path("teamId") int teamId
    );

    @GET("api/users/by-login/{login}")
    Call<User> findUserByLogin(
        @Header("Authorization") String token,
        @retrofit2.http.Path("login") String login
    );

    @GET("api/users")
    Call<List<User>> getAllUsers(
        @Header("Authorization") String token
    );

    @GET("api/teams/{teamId}/events")
    Call<List<Event>> getTeamEvents(
        @Header("Authorization") String token,
        @retrofit2.http.Path("teamId") int teamId
    );
}