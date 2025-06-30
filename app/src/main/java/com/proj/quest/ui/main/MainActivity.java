package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.Group.CreateGroupActivity;
import com.proj.quest.Group.GroupActivity;
import com.proj.quest.R;
import com.proj.quest.Riddle.RiddleActivity;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.Event;
import com.proj.quest.models.Team;
import com.proj.quest.utils.SharedPrefs;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private Button createEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.nav_events);
       // createEvent = findViewById(R.id.create_event);

        // Проверяем, есть ли фрагмент в Intent
        String fragment = getIntent().getStringExtra("fragment");
        if (fragment != null && fragment.equals("groups")) {
            // Если переходим на группы, загружаем GroupsFragment
            loadFragment(new GroupsFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_groups);
            return;
        }

        loadFragment(new EventsFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Обновляем состояние фрагментов при возврате в активность
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof GroupsFragment) {
            // Если текущий фрагмент - GroupsFragment, обновляем его данные
            ((GroupsFragment) currentFragment).loadData();
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_events) {
            return loadFragment(new EventsFragment());
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        }else if (itemId == R.id.nav_leaders) {
            startActivity(new Intent(this, LeaderboardActivity.class));
            overridePendingTransition(0, 0);
            return true;
        } else if (itemId == R.id.nav_groups) {
            return loadFragment(new GroupsFragment());
        }else if (itemId == R.id.nav_riddles) {
            // Получаем токен и ApiService
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
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Группа еще не зарегистрировалась на мероприятие", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        // Получаем все мероприятия
                        apiService.getEvents("Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<Event>>() {
                            @Override
                            public void onResponse(retrofit2.Call<java.util.List<Event>> call, retrofit2.Response<java.util.List<Event>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    java.util.List<Event> events = response.body();
                                    Event registeredEvent = null;
                                    for (Event event : events) {
                                        if (event.getId() == team.getEventId()) {
                                            registeredEvent = event;
                                            break;
                                        }
                                    }
                                    if (registeredEvent != null) {
                                        Intent intent = new Intent(MainActivity.this, RiddleActivity.class);
                                        intent.putExtra("EVENT_ID", registeredEvent.getId());
                                        String eventDateTime = registeredEvent.getStartDate() + "T" + registeredEvent.getStartTime() + ".000Z";
                                        intent.putExtra("EVENT_TIME", eventDateTime);
                                        intent.putExtra("IS_REGISTERED", true);
                                        if (registeredEvent.getThemeUrl() != null) {
                                            intent.putExtra("EVENT_THEME_URL", registeredEvent.getThemeUrl());
                                        }
                                        startActivity(intent);
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Мероприятие, на которое зарегистрирована группа, не найдено", Toast.LENGTH_SHORT).show());
                                    }
                                } else {
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Ошибка загрузки мероприятий", Toast.LENGTH_SHORT).show());
                                }
                            }
                            @Override
                            public void onFailure(retrofit2.Call<java.util.List<Event>> call, Throwable t) {
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show());
                            }
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Группа еще не зарегистрировалась на мероприятие", Toast.LENGTH_SHORT).show());
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<Team> call, Throwable t) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show());
                }
            });
            return true;
        }
        return false;
    }
}