package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.Group.GroupActivity;
import com.proj.quest.R;
import com.proj.quest.Riddle.RiddleActivity;
import com.proj.quest.Theme.BaseActivity;
import com.proj.quest.Theme.ThemeHelper;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.models.User;
import com.proj.quest.models.Team;
import com.proj.quest.models.Event;
import com.proj.quest.ui.auth.LoginActivity;
import com.proj.quest.ui.settings.ProfileSettingsActivity;
import com.proj.quest.utils.SharedPrefs;
import com.proj.quest.utils.NavigationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private int currentThemeIndex = 0;
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

        Button btnChangeTheme = findViewById(R.id.ThemeButton);
        currentThemeIndex = ThemeHelper.getSavedTheme(this);

        // Создаем меню для выбора темы
        PopupMenu themeMenu = new PopupMenu(this, btnChangeTheme);

        // Добавляем пункты меню
        for (int i = 0; i < ThemeHelper.APP_THEMES.length; i++) {
            themeMenu.getMenu().add(0, i, i, "Тема " + (i + 1));
        }

        // Обработчик выбора темы
        themeMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                currentThemeIndex = item.getItemId();
                ThemeHelper.saveTheme(ProfileActivity.this, currentThemeIndex);
                recreate();
                return true;
            }
        });

        // Открываем меню при нажатии на кнопку
        btnChangeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                themeMenu.show();
            }
        });

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
                com.proj.quest.utils.NavigationUtils.goToRiddles(this);
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
                        if (avatarUrl.startsWith("/avatars/")) {
                            avatarUrl = ApiClient.BASE_URL + avatarUrl.substring(1);
                        }
                        Glide.with(ProfileActivity.this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(profileImage);
                    } else if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        String url = user.getAvatarUrl();
                        if (url.startsWith("/avatars/")) {
                            url = ApiClient.BASE_URL + url.substring(1);
                        }
                        Glide.with(ProfileActivity.this)
                            .load(url)
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