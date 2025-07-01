package com.proj.quest.Riddle;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.models.RiddleRequest;
import com.proj.quest.ui.main.MainActivity;
import com.proj.quest.ui.main.ProfileActivity;
import com.proj.quest.utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.drawable.Drawable;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import com.proj.quest.models.Team;

public class RiddleActivity extends AppCompatActivity {

    private TextView timerTextView;
    private RecyclerView riddlesRecyclerView;
    private RiddleAdapter riddleAdapter;
    private List<RiddleRequest> riddleList = new ArrayList<>();
    private TextView tvNotRegistered;
    private CountDownTimer countDownTimer;
    private int eventId;
    private String eventTime; // Формат "YYYY-MM-DD HH:mm:ss"
    private LinearLayout singleRiddleLayout;
    private TextView tvRiddleNumber, tvQuestion, tvResult;
    private Button checkBtn;
    private int currentRiddleIndex = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float RIDDLE_RADIUS_METERS = 5.0f;
    private long eventEndMillis = -1;
    private long eventStartMillis = -1;
    private boolean pointsAwarded = false;
    private boolean isCaptain = false;
    private Team myTeam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riddle);

        // Получаем themeUrl из Intent
        String themeUrl = getIntent().getStringExtra("EVENT_THEME_URL");
        eventId = getIntent().getIntExtra("EVENT_ID", -1);
        eventTime = getIntent().getStringExtra("EVENT_TIME");
        boolean isRegistered = getIntent().getBooleanExtra("IS_REGISTERED", false);
        Log.d("RiddleActivity", "eventId=" + eventId + ", eventTime=" + eventTime + ", isRegistered=" + isRegistered);
        if ((themeUrl == null || themeUrl.isEmpty()) && eventId != -1) {
            // Если themeUrl не передан, загружаем Event с сервера
            ApiService apiService = ApiClient.getApiService();
            String token = new SharedPrefs(this).getToken();
            apiService.getEvents("Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<com.proj.quest.models.Event>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.List<com.proj.quest.models.Event>> call, retrofit2.Response<java.util.List<com.proj.quest.models.Event>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (com.proj.quest.models.Event event : response.body()) {
                            if (event.getId() == eventId && event.getThemeUrl() != null && !event.getThemeUrl().isEmpty()) {
                                setThemeBackground(event.getThemeUrl());
                                break;
                            }
                        }
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<java.util.List<com.proj.quest.models.Event>> call, Throwable t) {}
            });
        } else if (themeUrl != null && !themeUrl.isEmpty()) {
            setThemeBackground(themeUrl);
        }

        timerTextView = findViewById(R.id.timerTextView);
        riddlesRecyclerView = findViewById(R.id.riddlesRecyclerView);
        tvNotRegistered = findViewById(R.id.tvNotRegistered);
        singleRiddleLayout = findViewById(R.id.singleRiddleLayout);
        tvRiddleNumber = findViewById(R.id.tvRiddleNumber);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvResult = findViewById(R.id.tvResult);
        checkBtn = findViewById(R.id.checkBtn);

        riddleList = new ArrayList<>();
        riddleAdapter = new RiddleAdapter(this, riddleList);
        riddlesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        riddlesRecyclerView.setAdapter(riddleAdapter);

        if (!isRegistered || eventTime == null) {
            tvNotRegistered.setVisibility(View.VISIBLE);
            timerTextView.setVisibility(View.GONE);
            riddlesRecyclerView.setVisibility(View.GONE);
            singleRiddleLayout.setVisibility(View.GONE);
            return;
        }

        if (eventId == -1 || eventTime == null) {
            Toast.makeText(this, "Ошибка: не удалось загрузить данные мероприятия.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupBottomNavigation();
        loadMyTeamAndContinue();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возврате на экран всегда актуализируем состояние
        parseEventTimes();
        startTimerOrShowRiddles();
        // Если мероприятие завершено — начисляем баллы
        long nowMillis = System.currentTimeMillis();
        if (eventEndMillis > 0 && nowMillis >= eventEndMillis && !pointsAwarded) {
            awardPoints();
        }
    }

    private Date parseEventDate(String dateString) {
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        };
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(dateString);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    private void parseEventTimes() {
        if (eventTime == null) {
            Log.d("RiddleActivity", "eventTime is null, skipping parseEventTimes");
            eventStartMillis = -1;
            eventEndMillis = -1;
            return;
        }
        Date eventDate = parseEventDate(eventTime);
        if (eventDate != null) {
            eventStartMillis = eventDate.getTime();
            eventEndMillis = eventStartMillis + 3 * 60 * 60 * 1000; // 3 часа
            Log.d("RiddleActivity", "eventStartMillis=" + eventStartMillis + ", eventEndMillis=" + eventEndMillis);
        } else {
            Log.d("RiddleActivity", "eventDate is null for eventTime=" + eventTime);
        }
    }

    private void startTimerOrShowRiddles() {
        long nowMillis = System.currentTimeMillis();
        Log.d("RiddleActivity", "nowMillis=" + nowMillis + ", eventStartMillis=" + eventStartMillis + ", eventEndMillis=" + eventEndMillis);
        if (eventStartMillis == -1 || eventEndMillis == -1) {
            timerTextView.setText("Ошибка времени мероприятия");
            return;
        }
        if (nowMillis < eventStartMillis) {
            Log.d("RiddleActivity", "Сейчас до начала мероприятия");
            long diff = eventStartMillis - nowMillis;
            new CountDownTimer(diff, 1000) {
                public void onTick(long millisUntilFinished) {
                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                    timerTextView.setText(String.format(Locale.getDefault(), "До начала: %02d:%02d:%02d", hours, minutes, seconds));
                }
                public void onFinish() {
                    showRiddles();
                }
            }.start();
        } else if (nowMillis >= eventStartMillis && nowMillis < eventEndMillis) {
            showCurrentRiddleUI();
        } else {
            Log.d("RiddleActivity", "Мероприятие завершено");
            timerTextView.setText("Мероприятие завершено");
            riddlesRecyclerView.setVisibility(View.GONE);
            singleRiddleLayout.setVisibility(View.GONE);
        }
    }
    
    private void showRiddles() {
        timerTextView.setVisibility(View.GONE);
        singleRiddleLayout.setVisibility(View.GONE);
        riddlesRecyclerView.setVisibility(View.VISIBLE);
        loadRiddles(eventId);
    }
    
    private void loadRiddles(int eventId) {
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();

        apiService.getEventRiddles("Bearer " + token, eventId).enqueue(new Callback<List<RiddleRequest>>() {
            @Override
            public void onResponse(Call<List<RiddleRequest>> call, Response<List<RiddleRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    riddleList.clear();
                    riddleList.addAll(response.body());
                    riddleAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(RiddleActivity.this, "Failed to load riddles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RiddleRequest>> call, Throwable t) {
                Toast.makeText(RiddleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCurrentRiddleUI() {
        timerTextView.setVisibility(View.GONE);
        riddlesRecyclerView.setVisibility(View.GONE);
        singleRiddleLayout.setVisibility(View.VISIBLE);
        loadRiddlesAndShowFirst();
    }

    private void loadRiddlesAndShowFirst() {
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.getEventRiddles("Bearer " + token, eventId).enqueue(new Callback<List<RiddleRequest>>() {
            @Override
            public void onResponse(Call<List<RiddleRequest>> call, Response<List<RiddleRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    riddleList.clear();
                    riddleList.addAll(response.body());
                    currentRiddleIndex = 0;
                    showCurrentRiddle();
                } else {
                    Toast.makeText(RiddleActivity.this, "Failed to load riddles", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<RiddleRequest>> call, Throwable t) {
                Toast.makeText(RiddleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCurrentRiddle() {
        if (riddleList.isEmpty() || currentRiddleIndex >= riddleList.size()) {
            tvRiddleNumber.setText("Все загадки решены!");
            tvQuestion.setText("");
            checkBtn.setVisibility(View.GONE);
            return;
        }
        RiddleRequest riddle = riddleList.get(currentRiddleIndex);
        tvRiddleNumber.setText("Загадка " + (currentRiddleIndex + 1));
        tvQuestion.setText(riddle.getRiddle_text());
        tvResult.setVisibility(View.INVISIBLE);
        if (isCaptain) {
            checkBtn.setVisibility(View.VISIBLE);
            checkBtn.setEnabled(true);
            checkBtn.setText(R.string.check_button_text);
            checkBtn.setOnClickListener(v -> {
                checkLocationForRiddle(riddle);
            });
        } else {
            checkBtn.setVisibility(View.VISIBLE);
            checkBtn.setEnabled(false);
            checkBtn.setText(R.string.only_captain_can_check);
        }
    }

    private void checkLocationForRiddle(RiddleRequest riddle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            tvResult.setText("Требуется разрешение на геолокацию");
            tvResult.setVisibility(View.VISIBLE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    riddle.getLatitude(), riddle.getLongitude(),
                    results
                );
                if (results[0] <= RIDDLE_RADIUS_METERS) {
                    tvResult.setText("Верно! Вы находитесь в нужной точке.");
                    tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    tvResult.setVisibility(View.VISIBLE);
                    // НЕ переходим к следующей загадке, пользователь остается на этой
                } else {
                    tvResult.setText(String.format("Неверно! Вы находитесь %.1f м от точки.", results[0]));
                    tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    tvResult.setVisibility(View.VISIBLE);
                }
            } else {
                tvResult.setText("Не удалось получить местоположение. Попробуйте еще раз.");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                tvResult.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentRiddle();
            } else {
                Toast.makeText(this, "Разрешение на геолокацию не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_riddles); 

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_leaders) {
                startActivity(new Intent(this, com.proj.quest.leaderboard.LeaderboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_riddles) {
                return true;
            } else if (itemId == R.id.nav_events) {
                startActivity(new Intent(this, com.proj.quest.ui.main.MainActivity.class).putExtra("fragment", "events"));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_groups) {
                startActivity(new Intent(this, com.proj.quest.ui.main.MainActivity.class).putExtra("fragment", "groups"));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, com.proj.quest.ui.main.ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setThemeBackground(String themeUrl) {
        final View rootView = findViewById(android.R.id.content);
        Glide.with(this)
            .load(themeUrl)
            .into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    rootView.setBackground(resource);
                }
                @Override
                public void onLoadCleared(Drawable placeholder) {}
            });
    }

    private void awardPoints() {
        if (pointsAwarded) return;
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.awardPointsForEvent("Bearer " + token, eventId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    pointsAwarded = true;
                } else if (response.code() == 409) { // Баллы уже начислены
                    pointsAwarded = true;
                }
                // Можно добавить уведомление пользователю, если нужно
            }
            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {}
        });
    }

    private void loadMyTeamAndContinue() {
        ApiService apiService = ApiClient.getApiService();
        String token = new SharedPrefs(this).getToken();
        apiService.getMyTeam("Bearer " + token).enqueue(new retrofit2.Callback<com.proj.quest.models.Team>() {
            @Override
            public void onResponse(retrofit2.Call<com.proj.quest.models.Team> call, retrofit2.Response<com.proj.quest.models.Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myTeam = response.body();
                    int userId = new SharedPrefs(RiddleActivity.this).getUserId();
                    isCaptain = (myTeam.getCaptainId() == userId);
                } else {
                    isCaptain = false;
                }
                // После загрузки команды продолжаем обычный flow
                parseEventTimes();
                startTimerOrShowRiddles();
            }
            @Override
            public void onFailure(retrofit2.Call<com.proj.quest.models.Team> call, Throwable t) {
                isCaptain = false;
                parseEventTimes();
                startTimerOrShowRiddles();
            }
        });
    }
}
