package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.Group.GroupActivity;
import com.proj.quest.R;
import com.proj.quest.Riddle.RiddleActivity;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.models.User;
import com.proj.quest.models.Team;
import com.proj.quest.models.Event;
import com.proj.quest.ui.auth.LoginActivity;
import com.proj.quest.ui.settings.ProfileSettingsActivity;
import com.proj.quest.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private Button logoutBtn;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private TextView tvLogin, tvEmail, tvRegDate, tvPoints;
    private Button btnProfileSettings;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileImage);
        logoutBtn = findViewById(R.id.logoutBtn);
        tvLogin = findViewById(R.id.tvLogin);
        tvEmail = findViewById(R.id.tvEmail);
        tvRegDate = findViewById(R.id.tvRegDate);
        tvPoints = findViewById(R.id.tvPoints);
        btnProfileSettings = findViewById(R.id.btnProfileSettings);

        sharedPrefs = new SharedPrefs(this);
        apiService = ApiClient.getApiService();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_riddles) {
                SharedPrefs sharedPrefs = new SharedPrefs(this);
                String token = sharedPrefs.getToken();
                ApiService apiService = ApiClient.getApiService();
                if (token == null || token.isEmpty()) {
                    Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
                    return true;
                }
                apiService.getMyTeam("Bearer " + token).enqueue(new retrofit2.Callback<Team>() {
                    @Override
                    public void onResponse(retrofit2.Call<Team> call, retrofit2.Response<Team> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Team team = response.body();
                            if (team.getEventId() == null || team.getEventId() == 0) {
                                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Группа еще не зарегистрировалась на мероприятие", Toast.LENGTH_SHORT).show());
                                return;
                            }
                            apiService.getEvents("Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<Event>>() {
                                @Override
                                public void onResponse(retrofit2.Call<java.util.List<Event>> call, retrofit2.Response<java.util.List<Event>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        java.util.List<Event> events = response.body();
                                        long now = System.currentTimeMillis();
                                        Event nearest = null;
                                        for (Event event : events) {
                                            if (event.getId() == team.getEventId()) {
                                                try {
                                                    java.text.SimpleDateFormat parser = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                                                    parser.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                                                    java.util.Date eventDate = parser.parse(event.getStartDate());
                                                    if (eventDate != null && eventDate.getTime() > now) {
                                                        nearest = event;
                                                        break;
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                        if (nearest != null) {
                                            Intent intent = new Intent(ProfileActivity.this, RiddleActivity.class);
                                            intent.putExtra("EVENT_ID", nearest.getId());
                                            intent.putExtra("EVENT_TIME", nearest.getStartDate());
                                            intent.putExtra("IS_REGISTERED", true);
                                            startActivity(intent);
                                        } else {
                                            runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Нет ближайших мероприятий для вашей группы", Toast.LENGTH_SHORT).show());
                                        }
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Ошибка загрузки мероприятий", Toast.LENGTH_SHORT).show());
                                    }
                                }
                                @Override
                                public void onFailure(retrofit2.Call<java.util.List<Event>> call, Throwable t) {
                                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show());
                                }
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Группа еще не зарегистрировалась на мероприятие", Toast.LENGTH_SHORT).show());
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<Team> call, Throwable t) {
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show());
                    }
                });
                return true;
            } else if (itemId == R.id.nav_leaders) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                overridePendingTransition(0,0);
                finish();
            } else if (itemId == R.id.nav_events) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "events"));
                overridePendingTransition(0,0);
                finish();
            } else if (itemId == R.id.nav_groups) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "groups"));
                overridePendingTransition(0,0);
                finish();
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return true;
        });

        loadProfile();

        logoutBtn.setOnClickListener(v -> {
            sharedPrefs.clear();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnProfileSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ProfileSettingsActivity.class);
            intent.putExtra("login", currentUser.getLogin());
            intent.putExtra("email", currentUser.getEmail());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            tvLogin.setText("Офлайн режим");
            tvEmail.setText("Нет данных");
            tvRegDate.setText("");
            tvPoints.setText("");
            profileImage.setImageResource(R.drawable.profile);
            return;
        }
        apiService.getProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    currentUser = user;
                    tvLogin.setText(user.getLogin());
                    tvEmail.setText(user.getEmail());
                    tvPoints.setText(String.valueOf(user.getScore()));
                    String regDate = user.getRegistrationDate();
                    if (regDate != null && !regDate.isEmpty()) {
                        tvRegDate.setText(regDate.replace('T', ' ').replace(".000Z", ""));
                    } else {
                        tvRegDate.setText("Нет данных");
                    }
                    String avatarUrl = sharedPrefs.getAvatarUrl();
                    if ((avatarUrl != null && !avatarUrl.isEmpty())) {
                        Glide.with(ProfileActivity.this)
                            .load(avatarUrl.startsWith("http") ? avatarUrl : "http://5.175.92.194:3000" + avatarUrl)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(profileImage);
                    } else if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        Glide.with(ProfileActivity.this)
                            .load(user.getAvatarUrl().startsWith("http") ? user.getAvatarUrl() : "http://5.175.92.194:3000" + user.getAvatarUrl())
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.profile);
                    }
                    btnProfileSettings.setOnClickListener(v -> {
                        Intent intent = new Intent(ProfileActivity.this, ProfileSettingsActivity.class);
                        intent.putExtra("login", user.getLogin());
                        intent.putExtra("email", user.getEmail());
                        startActivity(intent);
                    });
                } else if (response.code() == 403) {
                    sharedPrefs.clear();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ProfileActivity.this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 