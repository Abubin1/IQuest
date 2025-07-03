package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.proj.quest.R;
import com.proj.quest.Group.GroupActivity;
import com.proj.quest.Theme.BaseActivity;
import com.proj.quest.models.Team;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.utils.SharedPrefs;
import com.proj.quest.ui.main.TeamAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SelectTeamActivity extends BaseActivity {
    private ListView listViewTeams;
    private TextView tvTitle;
    private Button btnCreateNewTeam;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team);

        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(this);

        eventId = getIntent().getIntExtra("eventId", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Ошибка: не указано мероприятие", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserTeams();
    }

    private void initViews() {
        listViewTeams = findViewById(R.id.listViewTeams);
        tvTitle = findViewById(R.id.tvTitle);
        btnCreateNewTeam = findViewById(R.id.btnCreateNewTeam);

        tvTitle.setText("Выберите команду для участия");

        btnCreateNewTeam.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTeamActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });
    }

    private void loadUserTeams() {
        String token = sharedPrefs.getToken();
        if (token == null) return;

        // Загружаем команду пользователя
        apiService.getMyTeam("Bearer " + token).enqueue(new retrofit2.Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Team team = response.body();
                    showTeamSelection(team);
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

    private void showTeamSelection(Team team) {
        List<Team> teams = new ArrayList<>();
        teams.add(team);
        
        TeamAdapter adapter = new TeamAdapter(this, teams, teamData -> {
            // Проверяем, не привязана ли уже команда к другому мероприятию
            if (teamData.getEventId() != null && teamData.getEventId() > 0 && teamData.getEventId() != eventId) {
                Toast.makeText(this, "Эта команда уже участвует в другом мероприятии", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Если команда не привязана к мероприятию, привязываем её
            if (teamData.getEventId() == null || teamData.getEventId() == 0) {
                // Здесь нужно добавить API для привязки команды к мероприятию
                Toast.makeText(this, "Функция привязки команды к мероприятию будет добавлена позже", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Переходим к управлению командой
            Intent intent = new Intent(this, GroupActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
            finish();
        });
        
        listViewTeams.setAdapter(adapter);
    }

    private void showNoTeams() {
        tvTitle.setText("У вас нет команд. Создайте новую команду для участия в мероприятии.");
        listViewTeams.setAdapter(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на экран
        loadUserTeams();
    }
} 