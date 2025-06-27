package com.proj.quest.leaderboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.ui.main.MainActivity;
import com.proj.quest.R;
import com.proj.quest.ui.main.ProfileActivity;

import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Инициализация ListView
        ListView listView = findViewById(R.id.leaderboardListView);

        // Загрузка данных из файла
        List<LeaderboardEntry> entries = LeaderboardFileManager.loadLeaderboard(this);

        // Проверка на пустые данные
        if (entries == null || entries.isEmpty()) {
            Toast.makeText(this, "Нет данных о рекордах", Toast.LENGTH_SHORT).show();
        } else {
            // Создание и установка адаптера
            LeaderboardAdapter adapter = new LeaderboardAdapter(this, entries);
            listView.setAdapter(adapter);
        }

        // Настройка нижней навигации
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_leaders); // Подсветка текущего пункта

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_leaders) {
                // Уже на экране лидеров
                return true;
            } else if (itemId == R.id.nav_riddles) {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("fragment", "riddles"));
                overridePendingTransition(0, 0);
                finish();
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