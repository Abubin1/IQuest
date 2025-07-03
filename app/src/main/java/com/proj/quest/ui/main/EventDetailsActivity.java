package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.proj.quest.R;
import com.proj.quest.Theme.BaseActivity;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.Event;
import com.proj.quest.models.Team;
import com.proj.quest.models.TeamRegistrationRequest;
import com.proj.quest.models.TeamRegistrationResponse;
import com.proj.quest.Group.CreateGroupActivity;
import com.proj.quest.utils.SharedPrefs;
import com.proj.quest.ui.adapters.TeamMemberSelectionAdapter;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends BaseActivity {
    private Event event;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private Button btnRegister;
    private Button btnUnregister;

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
        loadRegisteredTeams();

        Button btnTeamLeaderboard = findViewById(R.id.btnTeamLeaderboard);
        btnTeamLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.proj.quest.leaderboard.TeamLeaderboardActivity.class);
            intent.putExtra("eventId", event.getId());
            startActivity(intent);
        });

        btnUnregister = findViewById(R.id.btnUnregister);
        btnUnregister.setOnClickListener(v -> attemptUnregister());

        // --- ДОБАВЛЕНО: Проверка завершённости мероприятия и регистрации команды ---
        checkRegistrationAndEventStatus();
        // --- КОНЕЦ ДОБАВЛЕНИЯ ---
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
        Date eventDate = parseStartDateTime(event.getStartDate(), event.getStartTime());
        if (eventDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            tvEventDate.setText(dateFormat.format(eventDate));
        } else {
            tvEventDate.setText("Не указана");
        }

        tvStartTime.setText(event.getStartTime() != null ? event.getStartTime() : "Не указано");
        tvStartLocation.setText(event.getStartLocation() != null ? event.getStartLocation() : "Не указано");
        if (event.getMaxTeamLimit() > 0) {
            tvTeamCount.setText("Зарегистрировано: " + event.getCurrentTeamCount() + " / " + event.getMaxTeamLimit() + " команд");
        } else {
            tvTeamCount.setText("Зарегистрировано: " + event.getCurrentTeamCount() + " команд");
        }
        tvMaxMembers.setText(String.valueOf(event.getMaxTeamMembers()));
        tvRiddleCount.setText(String.valueOf(event.getRiddleCount()));
        tvDescription.setText(event.getDescription() != null ? event.getDescription() : "Описание отсутствует");

        btnRegister.setOnClickListener(v -> checkTeamAndRegister());
    }

    private Date parseStartDateTime(String date, String time) {
        if (date == null || time == null) return null;
        String dateTime = date + "T" + time + ".000Z";
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        parser.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        try {
            return parser.parse(dateTime);
        } catch (ParseException e) {
            return null;
        }
    }

    private void loadEventDetails() {
        // Здесь можно загрузить дополнительные детали мероприятия, если нужно
        // Пока используем данные, переданные из Intent
    }

    private void loadRegisteredTeams() {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            return;
        }

        apiService.getTeams("Bearer " + token).enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(Call<List<Team>> call, Response<List<Team>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Team> allTeams = response.body();
                    long registeredCount = 0;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        registeredCount = allTeams.stream()
                                .filter(team -> team.getEventId() != null && team.getEventId().equals(event.getId()))
                                .count();
                    } else {
                        for (Team team : allTeams) {
                            if (team.getEventId() != null && team.getEventId().equals(event.getId())) {
                                registeredCount++;
                            }
                        }
                    }

                    TextView tvTeamCount = findViewById(R.id.tvTeamCount);
                    if (event.getMaxTeamLimit() > 0) {
                        tvTeamCount.setText("Зарегистрировано: " + registeredCount + " / " + event.getMaxTeamLimit() + " команд");
                    } else {
                        tvTeamCount.setText("Зарегистрировано: " + registeredCount + " команд");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Team>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    private void checkTeamAndRegister() {
        // Проверяем, не завершено ли мероприятие
        if (event.getFinished() != null && event.getFinished()) {
            Toast.makeText(this, "Нельзя зарегистрироваться на завершённое мероприятие", Toast.LENGTH_LONG).show();
            return;
        }
        
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, getString(R.string.need_login), Toast.LENGTH_SHORT).show();
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
                            getString(R.string.only_captain_can_register), 
                            Toast.LENGTH_LONG).show();
                    }
                } else if (response.code() == 404) {
                    System.out.println("DEBUG: No team found for user");
                    // У пользователя нет команды, предлагаем создать
                    showCreateTeamDialog();
                } else {
                    System.out.println("DEBUG: Error checking team - " + response.code());
                    Toast.makeText(EventDetailsActivity.this, 
                        getString(R.string.error_check_team), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                System.out.println("DEBUG: Network error checking team - " + t.getMessage());
                Toast.makeText(EventDetailsActivity.this, 
                    getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateTeamDialog() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.create_team_dialog_title))
            .setMessage(getString(R.string.create_team_dialog_message))
            .setPositiveButton(getString(R.string.create_team_button), (dialog, which) -> {
                Intent intent = new Intent(this, CreateGroupActivity.class);
                startActivity(intent);
            })
            .setNegativeButton(getString(R.string.cancel_button), null)
            .show();
    }

    private void showTeamMemberSelectionDialog(Team team) {
        // Создаем диалог для выбора участников
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_team_member_selection, null);
        builder.setView(dialogView);

        // Настраиваем элементы диалога
        TextView tvMemberLimit = dialogView.findViewById(R.id.tvMemberLimit);
        TextView tvSelectedCount = dialogView.findViewById(R.id.tvSelectedCount);
        ListView listViewMembers = dialogView.findViewById(R.id.listViewMembers);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // Устанавливаем лимит участников
        tvMemberLimit.setText(getString(R.string.max_members_limit, event.getMaxTeamMembers()));

        // Создаем адаптер для списка участников
        TeamMemberSelectionAdapter adapter = new TeamMemberSelectionAdapter(
            this, 
            team.getMembers(), 
            event.getMaxTeamMembers(), 
            sharedPrefs.getUserId()
        );
        
        // Устанавливаем слушатель изменений выбора
        adapter.setOnSelectionChangedListener(selectedCount -> {
            updateSelectedCount(tvSelectedCount, selectedCount);
        });
        
        listViewMembers.setAdapter(adapter);

        // Обновляем счетчик выбранных участников
        updateSelectedCount(tvSelectedCount, adapter.getSelectedCount());

        // Создаем диалог
        AlertDialog dialog = builder.create();

        // Настраиваем кнопки
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            Set<Integer> selectedIds = adapter.getSelectedMemberIds();
            if (selectedIds.size() < 1) {
                Toast.makeText(this, getString(R.string.select_at_least_one), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Регистрируем команду с выбранными участниками
            List<Integer> selectedList = new ArrayList<>(selectedIds);
            registerTeamForEvent(team.getId(), selectedList);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateSelectedCount(TextView tvSelectedCount, TeamMemberSelectionAdapter adapter) {
        updateSelectedCount(tvSelectedCount, adapter.getSelectedCount());
    }

    private void updateSelectedCount(TextView tvSelectedCount, int selectedCount) {
        tvSelectedCount.setText(getString(R.string.selected_count, selectedCount));
        
        // Обновляем цвет в зависимости от лимита
        if (selectedCount >= event.getMaxTeamMembers()) {
            tvSelectedCount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvSelectedCount.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        }
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void registerTeamForEvent(int teamId) {
        // Старый метод для обратной совместимости
        registerTeamForEvent(teamId, null);
    }

    private void registerTeamForEvent(int teamId, List<Integer> selectedMemberIds) {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, getString(R.string.need_login), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Создаем объект с правильными параметрами для API
        TeamRegistrationRequest request = new TeamRegistrationRequest();
        request.setTeamId(teamId);
        request.setEventId(event.getId());
        if (selectedMemberIds != null) {
            request.setSelectedMemberIds(selectedMemberIds);
        }
        
        System.out.println("DEBUG: Registering team " + teamId + " for event " + event.getId() + 
                          " with " + (selectedMemberIds != null ? selectedMemberIds.size() : 0) + " members");
        
        apiService.registerTeamForEvent("Bearer " + token, request).enqueue(new Callback<TeamRegistrationResponse>() {
            @Override
            public void onResponse(Call<TeamRegistrationResponse> call, Response<TeamRegistrationResponse> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    System.out.println("DEBUG: Team registration successful");
                    Toast.makeText(EventDetailsActivity.this, 
                        getString(R.string.team_registration_success), Toast.LENGTH_LONG).show();
                    btnRegister.setEnabled(false);
                    btnRegister.setText(getString(R.string.registered_button_text));
                } else {
                    System.out.println("DEBUG: Team registration failed - " + response.code());
                    showErrorDialog("Ошибка", getString(R.string.error_team_registration));
                    checkRegistrationAndEventStatus();
                }
            }
            @Override
            public void onFailure(Call<TeamRegistrationResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                System.out.println("DEBUG: Network error during registration - " + t.getMessage());
                showErrorDialog(getString(R.string.error_network), "Не удалось зарегистрировать команду. Проверьте подключение к интернету.");
            }
        });
    }

    // --- ДОБАВЛЕНО: Метод проверки регистрации и завершённости мероприятия ---
    private void checkRegistrationAndEventStatus() {
        // Проверяем, не завершено ли мероприятие
        if (event.getFinished() != null && event.getFinished()) {
            btnRegister.setVisibility(View.GONE);
            btnUnregister.setVisibility(View.GONE);
            return;
        }
        
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) return;
        apiService.getMyTeam("Bearer " + token).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Team team = response.body();
                    boolean isCaptain = team.getCaptainId() == sharedPrefs.getUserId();
                    boolean isRegistered = team.getEventId() != null && team.getEventId().equals(event.getId());
                    if (isCaptain && isRegistered) {
                        btnUnregister.setVisibility(View.VISIBLE);
                        btnRegister.setVisibility(View.GONE);
                    } else {
                        btnUnregister.setVisibility(View.GONE);
                        btnRegister.setVisibility(View.VISIBLE);
                    }
                } else {
                    btnUnregister.setVisibility(View.GONE);
                    btnRegister.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                btnUnregister.setVisibility(View.GONE);
                btnRegister.setVisibility(View.VISIBLE);
            }
        });
    }

    private void attemptUnregister() {
        // Проверяем, не завершено ли мероприятие
        if (event.getFinished() != null && event.getFinished()) {
            Toast.makeText(this, "Нельзя отменить регистрацию на завершённое мероприятие", Toast.LENGTH_LONG).show();
            return;
        }
        
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, getString(R.string.need_login), Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.getMyTeam("Bearer " + token).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Team team = response.body();
                    TeamRegistrationRequest request = new TeamRegistrationRequest(team.getId(), event.getId());
                    apiService.unregisterTeamForEvent("Bearer " + token, request).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(EventDetailsActivity.this, "Регистрация отменена", Toast.LENGTH_SHORT).show();
                                btnUnregister.setVisibility(View.GONE);
                                btnRegister.setVisibility(View.VISIBLE);
                                loadRegisteredTeams();
                            } else {
                                Toast.makeText(EventDetailsActivity.this, "Ошибка отмены регистрации", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(EventDetailsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(EventDetailsActivity.this, "Ошибка получения команды", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                Toast.makeText(EventDetailsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 