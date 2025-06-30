package com.proj.quest.leaderboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.ui.main.MainActivity;
import com.proj.quest.R;
import com.proj.quest.ui.main.ProfileActivity;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable leaderboardUpdater;
    private LeaderboardAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        listView = findViewById(R.id.leaderboardListView);
        adapter = new LeaderboardAdapter(this, new java.util.ArrayList<>());
        listView.setAdapter(adapter);

        ApiService apiService = ApiClient.getApiService();

        leaderboardUpdater = new Runnable() {
            @Override
            public void run() {
                apiService.getLeaderboard().enqueue(new retrofit2.Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<User> users = response.body();
                            java.util.List<LeaderboardEntry> entries = new java.util.ArrayList<>();
                            for (User user : users) {
                                String avatarUrl = user.getAvatarUrl();
                                if (avatarUrl != null && avatarUrl.startsWith("/avatars/")) {
                                    avatarUrl = ApiClient.BASE_URL + avatarUrl.substring(1);
                                }
                                entries.add(new LeaderboardEntry(user.getLogin(), user.getScore(), avatarUrl));
                            }
                            adapter.clear();
                            adapter.addAll(entries);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(LeaderboardActivity.this, "Ошибка загрузки лидеров", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        Toast.makeText(LeaderboardActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                handler.postDelayed(this, 30000); // 30 секунд
            }
        };
        leaderboardUpdater.run();

        // Настройка нижней навигации
        setupBottomNavigation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(leaderboardUpdater);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_leaders); // Подсветка текущего пункта

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_leaders) {
                return true;
            } else if (itemId == R.id.nav_riddles) {
                Toast.makeText(this, "Перейдите на страницу мероприятия, чтобы открыть загадки", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_events) {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("fragment", "events"));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_groups) {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("fragment", "groups"));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}