package com.proj.quest.leaderboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.Event;
import com.proj.quest.models.RegisteredTeamProgress;
import com.proj.quest.models.User;
import com.proj.quest.ui.main.RegisteredTeamsAdapter;
import com.proj.quest.utils.SharedPrefs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamLeaderboardActivity extends AppCompatActivity {
    private ListView listView;
    private RegisteredTeamsAdapter adapter;
    private List<RegisteredTeamProgress> teamList = new ArrayList<>();
    private Event event;
    private Button awardPointsButton;
    private boolean pointsAwarded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_leaderboard);
        listView = findViewById(R.id.teamLeaderboardListView);
        int eventId = getIntent().getIntExtra("eventId", -1);
        loadEvent(eventId);

        awardPointsButton = findViewById(R.id.awardPointsButton);
        awardPointsButton.setVisibility(Button.GONE);
        fetchUserProfileAndSetAwardButton();
        awardPointsButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Завершить мероприятие?")
                .setMessage("Вы уверены, что хотите завершить мероприятие и начислить баллы? Это действие необратимо.")
                .setPositiveButton("Да", (dialog, which) -> awardPoints())
                .setNegativeButton("Отмена", null)
                .show();
        });

        // Установка фонового изображения мероприятия, если оно есть
        // (themeUrl будет доступен после загрузки event)
    }

    private void loadEvent(int eventId) {
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.getEvents("Bearer " + token).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Event e : response.body()) {
                        if (e.getId() == eventId) {
                            event = e;
                            break;
                        }
                    }
                    if (event != null) {
                        adapter = new RegisteredTeamsAdapter(TeamLeaderboardActivity.this, teamList, event);
                        listView.setAdapter(adapter);
                        TextView header = findViewById(R.id.team_leaderboard_header);
                        if (header != null) header.setText("Список команд");
                        loadRegisteredTeams(eventId);
                        setEventBackground();
                        setAwardButtonState();
                        checkAndAutoFinishEvent();
                    } else {
                        Toast.makeText(TeamLeaderboardActivity.this, "Мероприятие не найдено", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TeamLeaderboardActivity.this, "Ошибка загрузки мероприятия", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(TeamLeaderboardActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRegisteredTeams(int eventId) {
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.getEventTeamsProgress("Bearer " + token, eventId).enqueue(new Callback<List<RegisteredTeamProgress>>() {
            @Override
            public void onResponse(Call<List<RegisteredTeamProgress>> call, Response<List<RegisteredTeamProgress>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teamList.clear();
                    teamList.addAll(response.body());
                    // Сортировка: сначала прошедшие (по времени), потом остальные
                    Collections.sort(teamList, new Comparator<RegisteredTeamProgress>() {
                        @Override
                        public int compare(RegisteredTeamProgress t1, RegisteredTeamProgress t2) {
                            boolean t1Finished = t1.getCompletionTimeSeconds() != null;
                            boolean t2Finished = t2.getCompletionTimeSeconds() != null;
                            if (t1Finished && t2Finished) {
                                return Double.compare(t1.getCompletionTimeSeconds(), t2.getCompletionTimeSeconds());
                            } else if (t1Finished) {
                                return -1;
                            } else if (t2Finished) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(TeamLeaderboardActivity.this, "Ошибка загрузки команд", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<RegisteredTeamProgress>> call, Throwable t) {
                Toast.makeText(TeamLeaderboardActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setEventBackground() {
        // if (event != null && event.getThemeUrl() != null && !event.getThemeUrl().isEmpty()) {
        //     final View rootView = findViewById(android.R.id.content);
        //     Glide.with(this)
        //         .load(event.getThemeUrl())
        //         .placeholder(R.color.white)
        //         .error(R.color.white)
        //         .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
        //             @Override
        //             public void onResourceReady(android.graphics.drawable.Drawable resource, com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
        //                 rootView.setBackground(resource);
        //             }
        //             @Override
        //             public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
        //                 rootView.setBackground(placeholder);
        //             }
        //             @Override
        //             public void onLoadFailed(android.graphics.drawable.Drawable errorDrawable) {
        //                 super.onLoadFailed(errorDrawable);
        //                 rootView.setBackground(errorDrawable);
        //             }
        //         });
        // }
        // Явно устанавливаем фон из windowBackground текущей темы
        final View rootView = findViewById(android.R.id.content);
        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, outValue, true);
        rootView.setBackgroundResource(outValue.resourceId);
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
        int eventId = getIntent().getIntExtra("eventId", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Некорректный eventId", Toast.LENGTH_SHORT).show();
            return;
        }
        // Если нет ни одной команды, просто завершить мероприятие
        if (teamList == null || teamList.isEmpty()) {
            ApiService apiService = ApiClient.getApiService();
            String token = new SharedPrefs(this).getToken();
            apiService.awardPointsForEvent("Bearer " + token, eventId).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        pointsAwarded = true;
                        awardPointsButton.setVisibility(Button.GONE);
                        Toast.makeText(TeamLeaderboardActivity.this, "Мероприятие завершено (без команд)", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(TeamLeaderboardActivity.this, "Ошибка завершения мероприятия", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    Toast.makeText(TeamLeaderboardActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        // Обычный сценарий: есть команды
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.awardPointsForEvent("Bearer " + token, eventId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeamLeaderboardActivity.this, "Мероприятие завершено, баллы начислены!", Toast.LENGTH_LONG).show();
                    pointsAwarded = true;
                    awardPointsButton.setVisibility(Button.GONE);
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

    private void checkEventStatus() {
        int eventId = getIntent().getIntExtra("eventId", -1);
        if (eventId == -1) return;
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.getEvents("Bearer " + token).enqueue(new Callback<List<com.proj.quest.models.Event>>() {
            @Override
            public void onResponse(Call<List<com.proj.quest.models.Event>> call, Response<List<com.proj.quest.models.Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (com.proj.quest.models.Event event : response.body()) {
                        if (event.getId() == eventId) {
                            if (event.getFinished() != null && event.getFinished()) {
                                pointsAwarded = true;
                                awardPointsButton.setEnabled(false);
                                awardPointsButton.setText("Мероприятие завершено");
                            }
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<com.proj.quest.models.Event>> call, Throwable t) {
            }
        });
    }

    private void setAwardButtonState() {
        if (event != null && event.getFinished() != null && event.getFinished()) {
            awardPointsButton.setEnabled(false);
            awardPointsButton.setText("Мероприятие завершено");
        } else {
            awardPointsButton.setEnabled(true);
            awardPointsButton.setText("Завершить мероприятие и начислить баллы");
        }
    }

    private void checkAndAutoFinishEvent() {
        if (event == null || event.getFinished() != null && event.getFinished()) {
            setAwardButtonState();
            return;
        }
        String startDate = event.getStartDate();
        String startTime = event.getStartTime();
        if (startDate == null || startTime == null) return;
        String dateTime = startDate + "T" + startTime + ".000Z";
        java.text.SimpleDateFormat parser = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
        parser.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Moscow"));
        try {
            java.util.Date eventStart = parser.parse(dateTime);
            if (eventStart == null) return;
            long eventEnd = eventStart.getTime() + 3 * 60 * 60 * 1000;
            long now = System.currentTimeMillis();
            if (now > eventEnd) {
                // Автоматически завершаем мероприятие
                awardPoints();
                event.setFinished(true); // Локально помечаем завершённым
                setAwardButtonState();
            }
        } catch (java.text.ParseException ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndAutoFinishEvent();
    }
} 