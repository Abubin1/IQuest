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
import com.proj.quest.models.CreateEventRequest;
import com.proj.quest.models.RiddleRequest;
import com.proj.quest.models.ThemeUploadResponse;
import com.proj.quest.models.EventProgress;
import com.proj.quest.models.RegisteredTeamProgress;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

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
import retrofit2.http.Path;

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

    @POST("api/teams/unregister")
    Call<Void> unregisterTeamForEvent(
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

    @POST("api/events")
    Call<Event> createEvent(
            @Header("Authorization") String token,
            @Body CreateEventRequest request
    );

    @GET("api/events/{id}/riddles")
    Call<List<RiddleRequest>> getEventRiddles(@Header("Authorization") String token, @Path("id") int eventId);

    @PUT("api/events/{id}/theme")
    Call<Event> updateEventTheme(@Header("Authorization") String token, @Path("id") int eventId, @Body RequestBody themeUrl);

    @GET("api/teams")
    Call<List<Team>> getTeams(@Header("Authorization") String token);

    @Multipart
    @POST("api/events/theme")
    Call<ThemeUploadResponse> uploadEventTheme(@Header("Authorization") String token, @Part MultipartBody.Part theme);

    @GET("team-leaderboard")
    Call<List<com.proj.quest.models.TeamLeaderboardEntry>> getTeamLeaderboard(@Header("Authorization") String token, @Query("eventId") int eventId);

    @POST("api/events/{eventId}/award-points")
    Call<Void> awardPointsForEvent(@Header("Authorization") String token, @Path("eventId") int eventId);

    @POST("api/teams/complete-event")
    Call<Void> completeEvent(@Header("Authorization") String token, @Body TeamRegistrationRequest request);

    @GET("api/teams/{teamId}/event/{eventId}/riddle-progress")
    Call<okhttp3.ResponseBody> getTeamRiddleProgress(
        @Header("Authorization") String token,
        @Path("teamId") int teamId,
        @Path("eventId") int eventId
    );

    @POST("api/teams/{teamId}/event/{eventId}/riddle-progress")
    Call<Void> setTeamRiddleProgress(
        @Header("Authorization") String token,
        @Path("teamId") int teamId,
        @Path("eventId") int eventId,
        @Body okhttp3.RequestBody body
    );

    @POST("api/teams/{teamId}/event/{eventId}/finish")
    Call<Void> setTeamFinishTime(
        @Header("Authorization") String token,
        @Path("teamId") int teamId,
        @Path("eventId") int eventId,
        @Query("finishTime") long finishTime
    );

    @GET("api/events/{eventId}/teams-progress")
    Call<List<RegisteredTeamProgress>> getEventTeamsProgress(@Header("Authorization") String token, @Path("eventId") int eventId);
}