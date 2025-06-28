package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.Event;
import com.proj.quest.models.Team;
import com.proj.quest.models.TeamRegistrationRequest;
import com.proj.quest.models.TeamRegistrationResponse;
import com.proj.quest.Group.CreateGroupActivity;
import com.proj.quest.utils.SharedPrefs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends AppCompatActivity {
    private Event event;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Получаем данные о мероприятии из Intent
        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Ошибка загрузки мероприятия", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(this);

        setupViews();
        loadEventDetails();
    }

    private void setupViews() {
        TextView tvEventTitle = findViewById(R.id.tvEventTitle);
        TextView tvOrganizer = findViewById(R.id.tvOrganizer);
        TextView tvEventDate = findViewById(R.id.tvEventDate);
        TextView tvStartTime = findViewById(R.id.tvStartTime);
        TextView tvStartLocation = findViewById(R.id.tvStartLocation);
        TextView tvTeamCount = findViewById(R.id.tvTeamCount);
        TextView tvMaxMembers = findViewById(R.id.tvMaxMembers);
        TextView tvRiddleCount = findViewById(R.id.tvRiddleCount);
        TextView tvDescription = findViewById(R.id.tvDescription);
        btnRegister = findViewById(R.id.btnRegister);

        // Заполняем данные
        tvEventTitle.setText(event.getName());
        tvOrganizer.setText(event.getOrganizer() != null ? event.getOrganizer() : "Не указан");
        
        // Форматируем дату
        if (event.getEventDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            tvEventDate.setText(dateFormat.format(event.getEventDate()));
        } else {
            tvEventDate.setText("Не указана");
        }

        tvStartTime.setText(event.getStartTime() != null ? event.getStartTime() : "Не указано");
        tvStartLocation.setText(event.getStartLocation() != null ? event.getStartLocation() : "Не указано");
        tvTeamCount.setText(String.valueOf(event.getTeamCount()));
        tvMaxMembers.setText(String.valueOf(event.getMaxTeamMembers()));
        tvRiddleCount.setText(String.valueOf(event.getRiddleCount()));
        tvDescription.setText(event.getDescription() != null ? event.getDescription() : "Описание отсутствует");

        btnRegister.setOnClickListener(v -> checkTeamAndRegister());
    }

    private void loadEventDetails() {
        // Здесь можно загрузить дополнительные детали мероприятия, если нужно
        // Пока используем данные, переданные из Intent
    }

    private void checkTeamAndRegister() {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Необходимо войти в систему", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем, есть ли у пользователя команда для этого мероприятия
        apiService.getMyTeam("Bearer " + token).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    Team team = response.body();
                    System.out.println("DEBUG: Team found - " + team.getName() + ", Captain: " + team.getCaptainId() + ", User: " + sharedPrefs.getUserId());
                    // У пользователя есть команда
                    if (team.getCaptainId() == sharedPrefs.getUserId()) {
                        System.out.println("DEBUG: User is captain, proceeding with registration");
                        // Пользователь - капитан, показываем диалог выбора участников
                        showTeamMemberSelectionDialog(team);
                    } else {
                        System.out.println("DEBUG: User is not captain - Captain: " + team.getCaptainId() + ", User: " + sharedPrefs.getUserId());
                        // Пользователь не капитан
                        Toast.makeText(EventDetailsActivity.this, 
                            "Только капитан команды может регистрировать команду на мероприятие", 
                            Toast.LENGTH_LONG).show();
                    }
                } else if (response.code() == 404) {
                    System.out.println("DEBUG: No team found for user");
                    // У пользователя нет команды, предлагаем создать
                    showCreateTeamDialog();
                } else {
                    System.out.println("DEBUG: Error checking team - " + response.code());
                    Toast.makeText(EventDetailsActivity.this, 
                        "Ошибка проверки команды", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                System.out.println("DEBUG: Network error checking team - " + t.getMessage());
                Toast.makeText(EventDetailsActivity.this, 
                    "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateTeamDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Создание команды")
            .setMessage("У вас нет команды для участия в этом мероприятии. Хотите создать команду?")
            .setPositiveButton("Создать", (dialog, which) -> {
                Intent intent = new Intent(this, CreateGroupActivity.class);
                intent.putExtra("eventId", event.getId());
                startActivity(intent);
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showTeamMemberSelectionDialog(Team team) {
        // Здесь должна быть логика выбора участников команды
        // Пока просто регистрируем команду
        registerTeamForEvent(team.getId());
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void registerTeamForEvent(int teamId) {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Необходимо войти в систему", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Создаем объект с правильными параметрами для API
        TeamRegistrationRequest request = new TeamRegistrationRequest();
        request.setTeamId(teamId);
        request.setEventId(event.getId());
        
        System.out.println("DEBUG: Registering team " + teamId + " for event " + event.getId());
        
        apiService.registerTeamForEvent("Bearer " + token, request).enqueue(new Callback<TeamRegistrationResponse>() {
            @Override
            public void onResponse(Call<TeamRegistrationResponse> call, Response<TeamRegistrationResponse> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    System.out.println("DEBUG: Team registration successful");
                    Toast.makeText(EventDetailsActivity.this, 
                        "Команда успешно зарегистрирована на мероприятие!", Toast.LENGTH_LONG).show();
                    btnRegister.setEnabled(false);
                    btnRegister.setText("Зарегистрированы");
                } else {
                    System.out.println("DEBUG: Team registration failed - " + response.code());
                    // Обрабатываем ошибку от сервера
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.contains("error")) {
                            // Простое извлечение сообщения об ошибке
                            int startIndex = errorBody.indexOf("\"error\":\"") + 9;
                            int endIndex = errorBody.lastIndexOf("\"");
                            if (startIndex > 8 && endIndex > startIndex) {
                                String errorMessage = errorBody.substring(startIndex, endIndex);
                                showErrorDialog("Ошибка регистрации", errorMessage);
                            } else {
                                showErrorDialog("Ошибка", "Ошибка регистрации команды на мероприятие");
                            }
                        } else {
                            showErrorDialog("Ошибка", "Ошибка регистрации команды на мероприятие");
                        }
                    } catch (Exception e) {
                        showErrorDialog("Ошибка", "Ошибка регистрации команды на мероприятие");
                    }
                }
            }
            @Override
            public void onFailure(Call<TeamRegistrationResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                System.out.println("DEBUG: Network error during registration - " + t.getMessage());
                showErrorDialog("Ошибка сети", "Не удалось зарегистрировать команду. Проверьте подключение к интернету.");
            }
        });
    }
} 