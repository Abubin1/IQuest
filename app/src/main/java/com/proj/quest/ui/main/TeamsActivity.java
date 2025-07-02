package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.Group.GroupActivity;
import com.proj.quest.R;
import com.proj.quest.Theme.BaseActivity;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.models.Team;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.User;
import com.proj.quest.models.InviteResponse;
import com.proj.quest.utils.SharedPrefs;
import com.proj.quest.ui.main.InviteListAdapter;
import com.proj.quest.ui.main.TeamAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import retrofit2.Call;
import retrofit2.Response;

public class TeamsActivity extends BaseActivity {
    private ListView listViewTeams;
    private ListView listViewInvites;
    private TextView tvMyTeams;
    private TextView tvInvites;
    private Button btnCreateTeam;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);

        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(this);

        initViews();
        loadCurrentUser();
        setupBottomNavigation();
    }

    private void initViews() {
        listViewTeams = findViewById(R.id.listViewTeams);
        listViewInvites = findViewById(R.id.listViewInvites);
        tvMyTeams = findViewById(R.id.tvMyTeams);
        tvInvites = findViewById(R.id.tvInvites);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);

        btnCreateTeam.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTeamActivity.class);
            startActivity(intent);
        });
    }

    private void loadCurrentUser() {
        String token = sharedPrefs.getToken();
        if (token == null) return;
        
        apiService.getProfile("Bearer " + token).enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().getId();
                    loadMyTeams();
                    loadInvites();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(TeamsActivity.this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyTeams() {
        String token = sharedPrefs.getToken();
        if (token == null) return;

        // Загружаем команду пользователя
        apiService.getMyTeam("Bearer " + token).enqueue(new retrofit2.Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Team team = response.body();
                    showTeam(team);
                } else {
                    showNoTeams();
                }
            }
            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                showNoTeams();
            }
        });
    }

    private void showTeam(Team team) {
        tvMyTeams.setText("Моя команда");
        
        List<Team> teams = new ArrayList<>();
        teams.add(team);
        
        TeamAdapter adapter = new TeamAdapter(this, teams, teamData -> {
            // Переход к детальному просмотру команды
            Intent intent = new Intent(this, GroupActivity.class);
            intent.putExtra("eventId", teamData.getEventId());
            startActivity(intent);
        });
        
        listViewTeams.setAdapter(adapter);
    }

    private void showNoTeams() {
        tvMyTeams.setText("У вас пока нет команды");
        listViewTeams.setAdapter(null);
    }

    private void loadInvites() {
        String token = sharedPrefs.getToken();
        if (token == null) return;

        // Загружаем все приглашения пользователя
        apiService.getInvites("Bearer " + token).enqueue(new retrofit2.Callback<List<InviteResponse>>() {
            @Override
            public void onResponse(Call<List<InviteResponse>> call, Response<List<InviteResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    showInvites(response.body());
                } else {
                    showNoInvites();
                }
            }
            @Override
            public void onFailure(Call<List<InviteResponse>> call, Throwable t) {
                showNoInvites();
            }
        });
    }

    private void showInvites(List<InviteResponse> invites) {
        // Фильтрация дубликатов по паре teamId+eventId
        Map<String, InviteResponse> uniqueInvites = new LinkedHashMap<>();
        for (InviteResponse invite : invites) {
            String key = invite.getTeamId() + "_" + invite.getEventId();
            uniqueInvites.put(key, invite);
        }
        List<InviteResponse> filteredInvites = new ArrayList<>(uniqueInvites.values());
        tvInvites.setText("Приглашения (" + filteredInvites.size() + ")");
        InviteListAdapter adapter = new InviteListAdapter(this, filteredInvites, new InviteListAdapter.OnInviteActionListener() {
            @Override
            public void onAccept(InviteResponse invite) {
                acceptInvite(invite.getId());
            }
            @Override
            public void onDecline(InviteResponse invite) {
                declineInvite(invite.getId());
            }
        });
        listViewInvites.setAdapter(adapter);
    }

    private void acceptInvite(int inviteId) {
        String token = sharedPrefs.getToken();
        if (token == null) return;
        
        apiService.acceptInvite("Bearer " + token, inviteId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(TeamsActivity.this, "Вы вступили в команду!", Toast.LENGTH_SHORT).show();
                loadMyTeams();
                loadInvites();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TeamsActivity.this, "Ошибка принятия приглашения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void declineInvite(int inviteId) {
        String token = sharedPrefs.getToken();
        if (token == null) return;
        
        apiService.declineInvite("Bearer " + token, inviteId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(TeamsActivity.this, "Приглашение отклонено", Toast.LENGTH_SHORT).show();
                loadInvites();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TeamsActivity.this, "Ошибка отклонения приглашения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoInvites() {
        tvInvites.setText("Приглашений нет");
        listViewInvites.setAdapter(null);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_groups);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_events) {
                    startActivity(new Intent(TeamsActivity.this, MainActivity.class)
                            .putExtra("fragment", "events"));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_groups) {
                    startActivity(new Intent(TeamsActivity.this, MainActivity.class)
                            .putExtra("fragment", "groups"));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_leaders) {
                    startActivity(new Intent(TeamsActivity.this, LeaderboardActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(TeamsActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на экран
        loadMyTeams();
        loadInvites();
    }
} 