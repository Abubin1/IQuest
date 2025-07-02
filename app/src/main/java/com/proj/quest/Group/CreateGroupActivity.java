package com.proj.quest.Group;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.models.Team;
import com.proj.quest.ui.main.EventsFragment;
import com.proj.quest.ui.main.MainActivity;
import com.proj.quest.ui.main.ProfileActivity;
import com.proj.quest.utils.SharedPrefs;
import com.proj.quest.utils.NavigationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupActivity extends AppCompatActivity {
    private Button btnCreateGroup;
    private EditText etNameGroup;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        etNameGroup = findViewById(R.id.etNameGroup);
        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(this);

        btnCreateGroup.setOnClickListener(v->{
            createGroup();
        });

        setupBottomNavigation();
    }

    private void createGroup() {
        String groupName = etNameGroup.getText().toString().trim();
        if (groupName.isEmpty()) {
            etNameGroup.setError("Введите название команды");
            return;
        }
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Необходимо войти в систему", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Получаем eventId из Intent, если он передан
        int eventId = getIntent().getIntExtra("eventId", 0);
        
        Team team = new Team();
        team.setName(groupName);
        if (eventId > 0) {
            team.setEventId(eventId);
        }
        
        apiService.createTeam("Bearer " + token, team).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateGroupActivity.this, "Команда создана!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateGroupActivity.this, GroupActivity.class);
                    intent.putExtra("teamId", response.body().getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Ошибка создания команды", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                Toast.makeText(CreateGroupActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_groups); // Подсветка текущего пункта

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_leaders) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_riddles) {
                com.proj.quest.utils.NavigationUtils.goToRiddles(this);
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
