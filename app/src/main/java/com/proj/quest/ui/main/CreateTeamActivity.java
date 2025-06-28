package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.proj.quest.R;
import com.proj.quest.models.Team;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.utils.SharedPrefs;
import com.proj.quest.ui.main.TeamsActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTeamActivity extends AppCompatActivity {
    private EditText etTeamName;
    private Button btnCreateTeam;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(this);

        eventId = getIntent().getIntExtra("eventId", -1);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTeamName = findViewById(R.id.etTeamName);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);
    }

    private void setupListeners() {
        btnCreateTeam.setOnClickListener(v -> createTeam());
    }

    private void createTeam() {
        String teamName = etTeamName.getText().toString().trim();
        
        if (teamName.isEmpty()) {
            Toast.makeText(this, "Введите название команды", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sharedPrefs.getToken();
        if (token == null) {
            Toast.makeText(this, "Необходима авторизация", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем объект Team для отправки на сервер
        Team team = new Team();
        team.setName(teamName);
        if (eventId > 0) {
            team.setEventId(eventId);
        }
        
        apiService.createTeam("Bearer " + token, team).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateTeamActivity.this, "Команда создана!", Toast.LENGTH_SHORT).show();
                    
                    // Возвращаемся к списку команд
                    Intent intent = new Intent(CreateTeamActivity.this, TeamsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Ошибка создания команды";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("уже в команде") || errorBody.contains("already in team")) {
                                errorMsg = "Вы уже состоите в команде";
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(CreateTeamActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                Toast.makeText(CreateTeamActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 