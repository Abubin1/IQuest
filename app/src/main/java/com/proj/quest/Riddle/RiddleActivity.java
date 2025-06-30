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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riddle);

        // Установка фонового изображения, если передан themeUrl
        String themeUrl = getIntent().getStringExtra("EVENT_THEME_URL");
        if (themeUrl != null && !themeUrl.isEmpty()) {
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

        Intent intent = getIntent();
        eventId = intent.getIntExtra("EVENT_ID", -1);
        eventTime = intent.getStringExtra("EVENT_TIME");
        boolean isRegistered = intent.getBooleanExtra("IS_REGISTERED", false);

        if (!isRegistered) {
            tvNotRegistered.setVisibility(View.VISIBLE);
            timerTextView.setVisibility(View.GONE);
            riddlesRecyclerView.setVisibility(View.GONE);
            singleRiddleLayout.setVisibility(View.GONE);
            return; // Прерываем дальнейшее выполнение
        }

        if (eventId == -1 || eventTime == null) {
            Toast.makeText(this, "Ошибка: не удалось загрузить данные мероприятия.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupBottomNavigation();
        startTimer();
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

    private void startTimer() {
        try {
            Date eventDate = parseEventDate(eventTime);
            if (eventDate == null) throw new ParseException("Unparseable date", 0);
            long eventMillis = eventDate.getTime();
            long nowMillis = System.currentTimeMillis();
            long diff = eventMillis - nowMillis;

            if (diff <= 0) {
                showRiddles();
            } else {
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
            }
        } catch (ParseException e) {
            Log.e("RiddleActivity", "Ошибка парсинга даты", e);
            Toast.makeText(this, "Неверный формат времени мероприятия.", Toast.LENGTH_SHORT).show();
            timerTextView.setText("Ошибка времени");
        }
    }
    
    private void showRiddles() {
        timerTextView.setVisibility(View.GONE);
        riddlesRecyclerView.setVisibility(View.GONE);
        singleRiddleLayout.setVisibility(View.VISIBLE);
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
        checkBtn.setVisibility(View.VISIBLE);
        checkBtn.setOnClickListener(v -> {
            // TODO: Проверка ответа пользователя (можно добавить EditText для ввода)
            tvResult.setText("Проверка ответа...");
            tvResult.setVisibility(View.VISIBLE);
            // После проверки (или сразу для примера) — переход к следующей загадке
            currentRiddleIndex++;
            showCurrentRiddle();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_riddles); 

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_leaders) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_riddles) {
                return true;
            } else if (itemId == R.id.nav_events) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "events"));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_groups) {
                startActivity(new Intent(this, MainActivity.class).putExtra("fragment", "groups"));
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
