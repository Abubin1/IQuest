package com.proj.quest.leaderboard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.TeamLeaderboardEntry;
import com.proj.quest.models.Team;
import com.proj.quest.models.User;
import com.proj.quest.utils.SharedPrefs;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamLeaderboardActivity extends AppCompatActivity {
    private ListView listView;
    private TeamLeaderboardAdapter adapter;
    private List<TeamLeaderboardEntry> teamList = new ArrayList<>();
    private Button awardPointsButton;
    private boolean pointsAwarded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_leaderboard);
        listView = findViewById(R.id.teamLeaderboardListView);
        adapter = new TeamLeaderboardAdapter(this, teamList);
        listView.setAdapter(adapter);
        int eventId = getIntent().getIntExtra("eventId", -1);
        loadTeamLeaderboard(eventId);
        awardPointsButton = findViewById(R.id.awardPointsButton);
        awardPointsButton.setVisibility(Button.GONE);
        fetchUserProfileAndSetAwardButton();
        awardPointsButton.setOnClickListener(v -> awardPoints());
    }

    private void loadTeamLeaderboard(int eventId) {
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.getTeams("Bearer " + token).enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(Call<List<Team>> call, Response<List<Team>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teamList.clear();
                    List<Team> allTeams = response.body();
                    for (Team team : allTeams) {
                        if (team.getEventId() != null && team.getEventId() == eventId) {
                            int score = 0;
                            List<User> members = team.getMembers();
                            if (members != null) {
                                for (User u : members) score += u.getScore();
                            }
                            TeamLeaderboardEntry entry = new TeamLeaderboardEntry();
                            entry.setId(team.getId());
                            entry.setName(team.getName());
                            entry.setCaptainId(team.getCaptainId());
                            entry.setScore(score);
                            teamList.add(entry);
                        }
                    }
                    // Сортировка по убыванию баллов
                    teamList.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(TeamLeaderboardActivity.this, "Ошибка загрузки команд", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Team>> call, Throwable t) {
                Toast.makeText(TeamLeaderboardActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserProfileAndSetAwardButton() {
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.getProfile("Bearer " + token).enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    if (user.isOrganizer()) {
                        awardPointsButton.setVisibility(Button.VISIBLE);
                    } else {
                        awardPointsButton.setVisibility(Button.GONE);
                    }
                } else {
                    awardPointsButton.setVisibility(Button.GONE);
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                awardPointsButton.setVisibility(Button.GONE);
            }
        });
    }

    private void awardPoints() {
        if (pointsAwarded) {
            Toast.makeText(this, "Баллы уже начислены", Toast.LENGTH_SHORT).show();
            return;
        }
        int eventId = getIntent().getIntExtra("EVENT_ID", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Некорректный eventId", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.awardPointsForEvent("Bearer " + token, eventId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeamLeaderboardActivity.this, "Баллы начислены!", Toast.LENGTH_SHORT).show();
                    pointsAwarded = true;
                } else {
                    Toast.makeText(TeamLeaderboardActivity.this, "Ошибка начисления баллов", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Toast.makeText(TeamLeaderboardActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 