package com.proj.quest.Group;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.models.Event;
import com.proj.quest.models.KickRequest;
import com.proj.quest.models.InviteRequest;
import com.proj.quest.models.InviteResponse;
import com.proj.quest.models.Team;
import com.proj.quest.models.User;
import com.proj.quest.ui.main.MainActivity;
import com.proj.quest.ui.main.ProfileActivity;
import com.proj.quest.utils.SharedPrefs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroupActivity extends AppCompatActivity {
    private Button btn_invent, btn_leaderboardTeams, btnDeleteGroup;
    private ListView listView, invitesListView, eventsListView;
    private TextView tvTotalPoints, tvGroupName;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private Team currentTeam;
    private int userId;
    private List<InviteResponse> invites = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private List<Event> teamEvents = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        btn_invent = findViewById(R.id.btn_invent);
        btn_leaderboardTeams = findViewById(R.id.leaderboardTeams);
        btnDeleteGroup = findViewById(R.id.kick_group);
        listView = findViewById(R.id.users);
        tvTotalPoints = findViewById(R.id.total_points);
        tvGroupName = findViewById(R.id.group_name);
        invitesListView = findViewById(R.id.invites_list);
        eventsListView = findViewById(R.id.events_list);
        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(this);
        userId = sharedPrefs.getUserId();
        
        System.out.println("DEBUG: GroupActivity onCreate - userId: " + userId);
        
        loadTeam();
        loadInvites();
        loadAllUsers();

        btn_invent.setOnClickListener(v -> showInviteDialog());

        btn_leaderboardTeams.setOnClickListener(v -> showLeaderboardTeams());

        btnDeleteGroup.setOnClickListener(v -> {
            if (currentTeam != null && currentTeam.getCaptainId() == userId) {
                confirmDeleteGroup();
            } else {
                confirmLeaveGroup();
            }
        });

        setupBottomNavigation();
    }

    private void loadTeam() {
        String token = sharedPrefs.getToken();
        apiService.getMyTeam("Bearer " + token).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    currentTeam = response.body();
                    System.out.println("DEBUG: Team loaded successfully - " + currentTeam.getName() + ", Captain: " + currentTeam.getCaptainId() + ", User: " + userId);
                    showMembers(currentTeam.getMembers());
                    updateGroupInfo();
                    loadTeamEvents();
                    if (currentTeam.getCaptainId() == userId) {
                        btnDeleteGroup.setText("Удалить группу");
                    } else {
                        btnDeleteGroup.setText("Покинуть группу");
                    }
                } else if (response.code() == 404) {
                    System.out.println("DEBUG: No team found for user " + userId);
                    showCreateGroupUI();
                } else {
                    System.out.println("DEBUG: Error loading team - " + response.code());
                    Toast.makeText(GroupActivity.this, "Ошибка загрузки команды", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                System.out.println("DEBUG: Network error loading team - " + t.getMessage());
                Toast.makeText(GroupActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGroupInfo() {
        if (currentTeam != null) {
            tvGroupName.setText(currentTeam.getName());
        }
    }

    private void loadTeamEvents() {
        if (currentTeam == null) return;
        
        String token = sharedPrefs.getToken();
        apiService.getTeamEvents("Bearer " + token, currentTeam.getId()).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    teamEvents = response.body();
                    showTeamEvents();
                }
            }
            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                Toast.makeText(GroupActivity.this, "Ошибка загрузки мероприятий", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTeamEvents() {
        EventGroupAdapter adapter = new EventGroupAdapter(this, teamEvents, currentTeam.getEventId());
        eventsListView.setAdapter(adapter);
    }

    private void loadAllUsers() {
        String token = sharedPrefs.getToken();
        apiService.getAllUsers("Bearer " + token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                Toast.makeText(GroupActivity.this, "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMembers(List<User> members) {
        GroupAdapter adapter = new GroupAdapter(this, members, currentTeam.getCaptainId(), userId, this::kickMember);
        listView.setAdapter(adapter);
        int total = 0;
        for (User u : members) total += u.getScore();
        tvTotalPoints.setText("Общее кол-во очков: " + total);
    }

    private void kickMember(User user) {
        if (currentTeam == null) return;
        String token = sharedPrefs.getToken();
        apiService.kickUser("Bearer " + token, currentTeam.getId(), new KickRequest(user.getId())).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful()) {
                    Toast.makeText(GroupActivity.this, "Участник исключён", Toast.LENGTH_SHORT).show();
                    loadTeam();
                } else {
                    Toast.makeText(GroupActivity.this, "Ошибка исключения", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                Toast.makeText(GroupActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteGroup() {
        if (currentTeam == null) return;
        if (currentTeam.getCaptainId() != userId) {
            Toast.makeText(this, "Только капитан может удалить группу", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Удалить группу?")
            .setMessage("Вы уверены, что хотите удалить группу? Это действие необратимо.")
            .setPositiveButton("Удалить", (d, w) -> deleteGroup())
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void deleteGroup() {
        String token = sharedPrefs.getToken();
        apiService.deleteTeam("Bearer " + token, currentTeam.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful()) {
                    // Очищаем данные команды перед закрытием активности
                    currentTeam = null;
                    
                    // Показываем сообщение об успехе
                    Toast.makeText(GroupActivity.this, "Группа удалена", Toast.LENGTH_SHORT).show();
                    
                    // Закрываем активность и возвращаемся к MainActivity
                    Intent intent = new Intent(GroupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Обрабатываем ошибку от сервера
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.contains("error")) {
                            int startIndex = errorBody.indexOf("\"error\":\"") + 9;
                            int endIndex = errorBody.lastIndexOf("\"");
                            if (startIndex > 8 && endIndex > startIndex) {
                                String errorMessage = errorBody.substring(startIndex, endIndex);
                                showErrorDialog("Ошибка удаления группы", errorMessage);
                            } else {
                                showErrorDialog("Ошибка", "Ошибка удаления группы");
                            }
                        } else {
                            showErrorDialog("Ошибка", "Ошибка удаления группы");
                        }
                    } catch (Exception e) {
                        showErrorDialog("Ошибка", "Ошибка удаления группы");
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                showErrorDialog("Ошибка сети", "Не удалось удалить группу. Проверьте подключение к интернету.");
            }
        });
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showInviteDialog() {
        if (currentTeam == null) return;
        
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_invite_users);

        TextInputEditText etSearchUser = dialog.findViewById(R.id.etSearchUser);
        RecyclerView rvUsers = dialog.findViewById(R.id.rvUsers);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnInvite = dialog.findViewById(R.id.btnInvite);

        // Получаем ID участников команды
        List<Integer> teamMemberIds = currentTeam.getMembers().stream()
            .map(User::getId)
            .collect(Collectors.toList());

        UserInviteAdapter adapter = new UserInviteAdapter(this, allUsers, teamMemberIds, user -> {
            btnInvite.setEnabled(true);
            btnInvite.setTag(user);
        });

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        // Поиск пользователей
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnInvite.setOnClickListener(v -> {
            User selectedUser = (User) btnInvite.getTag();
            if (selectedUser != null) {
                inviteUser(selectedUser.getLogin());
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showLeaderboardTeams(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_team_leaders);

        dialog.setCancelable(true);

        ListView tableTeams = findViewById(R.id.listLeaderboardTeams);

        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void loadInvites() {
        String token = sharedPrefs.getToken();
        apiService.getInvites("Bearer " + token).enqueue(new Callback<List<InviteResponse>>() {
            @Override
            public void onResponse(Call<List<InviteResponse>> call, Response<List<InviteResponse>> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    invites = response.body();
                    showInvites();
                }
            }
            @Override
            public void onFailure(Call<List<InviteResponse>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
            }
        });
    }

    private void showInvites() {
        InviteAdapter adapter = new InviteAdapter(this, invites, this::acceptInvite, this::declineInvite);
        invitesListView.setAdapter(adapter);
    }

    private void acceptInvite(InviteResponse invite) {
        String token = sharedPrefs.getToken();
        apiService.acceptInvite("Bearer " + token, invite.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful()) {
                    Toast.makeText(GroupActivity.this, "Приглашение принято", Toast.LENGTH_SHORT).show();
                    loadInvites();
                    loadTeam();
                } else {
                    // Обрабатываем ошибку от сервера
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.contains("error")) {
                            int startIndex = errorBody.indexOf("\"error\":\"") + 9;
                            int endIndex = errorBody.lastIndexOf("\"");
                            if (startIndex > 8 && endIndex > startIndex) {
                                String errorMessage = errorBody.substring(startIndex, endIndex);
                                showErrorDialog("Ошибка принятия приглашения", errorMessage);
                            } else {
                                showErrorDialog("Ошибка", "Ошибка принятия приглашения");
                            }
                        } else {
                            showErrorDialog("Ошибка", "Ошибка принятия приглашения");
                        }
                    } catch (Exception e) {
                        showErrorDialog("Ошибка", "Ошибка принятия приглашения");
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                showErrorDialog("Ошибка сети", "Не удалось принять приглашение. Проверьте подключение к интернету.");
            }
        });
    }

    private void declineInvite(InviteResponse invite) {
        String token = sharedPrefs.getToken();
        apiService.declineInvite("Bearer " + token, invite.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful()) {
                    Toast.makeText(GroupActivity.this, "Приглашение отклонено", Toast.LENGTH_SHORT).show();
                    loadInvites();
                } else {
                    showErrorDialog("Ошибка", "Ошибка отклонения приглашения");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                showErrorDialog("Ошибка сети", "Не удалось отклонить приглашение. Проверьте подключение к интернету.");
            }
        });
    }

    private void inviteUser(String login) {
        String token = sharedPrefs.getToken();
        apiService.findUserByLogin("Bearer " + token, login).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    int userId = response.body().getId();
                    apiService.inviteUser("Bearer " + token, new InviteRequest(currentTeam.getId(), userId)).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (isFinishing() || isDestroyed()) {
                                return; // Активность уничтожена
                            }
                            
                            if (response.isSuccessful()) {
                                Toast.makeText(GroupActivity.this, "Приглашение отправлено", Toast.LENGTH_SHORT).show();
                                loadInvites();
                            } else {
                                // Обрабатываем ошибку от сервера
                                try {
                                    String errorBody = response.errorBody().string();
                                    // Парсим JSON для получения сообщения об ошибке
                                    if (errorBody.contains("error")) {
                                        // Простое извлечение сообщения об ошибке
                                        int startIndex = errorBody.indexOf("\"error\":\"") + 9;
                                        int endIndex = errorBody.lastIndexOf("\"");
                                        if (startIndex > 8 && endIndex > startIndex) {
                                            String errorMessage = errorBody.substring(startIndex, endIndex);
                                            showErrorDialog("Ошибка приглашения", errorMessage);
                                        } else {
                                            showErrorDialog("Ошибка", "Ошибка отправки приглашения");
                                        }
                                    } else {
                                        showErrorDialog("Ошибка", "Ошибка отправки приглашения");
                                    }
                                } catch (Exception e) {
                                    showErrorDialog("Ошибка", "Ошибка отправки приглашения");
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (isFinishing() || isDestroyed()) {
                                return; // Активность уничтожена
                            }
                            showErrorDialog("Ошибка сети", "Не удалось отправить приглашение. Проверьте подключение к интернету.");
                        }
                    });
                } else {
                    showErrorDialog("Пользователь не найден", "Пользователь с таким логином не найден в системе.");
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                showErrorDialog("Ошибка поиска", "Не удалось найти пользователя. Проверьте подключение к интернету.");
            }
        });
    }

    private void confirmLeaveGroup() {
        if (currentTeam == null) return;
        new AlertDialog.Builder(this)
            .setTitle("Покинуть группу?")
            .setMessage("Вы уверены, что хотите покинуть группу?")
            .setPositiveButton("Покинуть", (d, w) -> leaveGroupInternal())
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void leaveGroupInternal() {
        if (currentTeam == null) return;
        String token = sharedPrefs.getToken();
        apiService.leaveTeam("Bearer " + token, currentTeam.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                if (response.isSuccessful()) {
                    // Очищаем данные команды перед закрытием активности
                    currentTeam = null;
                    
                    // Показываем сообщение об успехе
                    Toast.makeText(GroupActivity.this, "Вы покинули группу", Toast.LENGTH_SHORT).show();
                    
                    // Закрываем активность и возвращаемся к MainActivity
                    Intent intent = new Intent(GroupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Обрабатываем ошибку от сервера
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.contains("error")) {
                            int startIndex = errorBody.indexOf("\"error\":\"") + 9;
                            int endIndex = errorBody.lastIndexOf("\"");
                            if (startIndex > 8 && endIndex > startIndex) {
                                String errorMessage = errorBody.substring(startIndex, endIndex);
                                showErrorDialog("Ошибка выхода из группы", errorMessage);
                            } else {
                                showErrorDialog("Ошибка", "Ошибка выхода из группы");
                            }
                        } else {
                            showErrorDialog("Ошибка", "Ошибка выхода из группы");
                        }
                    } catch (Exception e) {
                        showErrorDialog("Ошибка", "Ошибка выхода из группы");
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return; // Активность уничтожена
                }
                
                showErrorDialog("Ошибка сети", "Не удалось покинуть группу. Проверьте подключение к интернету.");
            }
        });
    }

    private void showCreateGroupUI() {
        startActivity(new Intent(this, CreateGroupActivity.class));
        finish();
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
                // Переходим к GroupsFragment в MainActivity
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
