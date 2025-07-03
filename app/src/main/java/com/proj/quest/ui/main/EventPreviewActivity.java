package com.proj.quest.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.proj.quest.R;
import com.proj.quest.Theme.BaseActivity;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.CreateEventRequest;
import com.proj.quest.models.Event;
import com.proj.quest.models.RiddleRequest;
import com.proj.quest.models.ThemeUploadResponse;
import com.proj.quest.utils.FileUtils;
import com.proj.quest.utils.SharedPrefs;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventPreviewActivity extends BaseActivity {

    private CreateEventRequest eventRequest;
    private TextView tvEventName, tvEventDescription, tvEventStartPlace, tvEventDateTime, tvMaxParticipants;
    private LinearLayout riddlesContainer;
    private Button btnConfirmAndCreate, btnSelectThemeImage, btnFullScreenPreview;
    private ImageView ivThemePreview;

    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_preview);

        eventRequest = (CreateEventRequest) getIntent().getSerializableExtra("event_request");

        if (eventRequest == null) {
            Toast.makeText(this, "Ошибка: не удалось получить данные мероприятия.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        populateData();
        setupImagePicker();
        setupButtons();

        // Если пришёл флаг fullscreen, делаем Activity полноэкранной
        if (getIntent().getBooleanExtra("fullscreen", false)) {
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            if (getSupportActionBar() != null) getSupportActionBar().hide();
        }
    }

    private void initViews() {
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventStartPlace = findViewById(R.id.tvEventStartPlace);
        tvEventDateTime = findViewById(R.id.tvEventDateTime);
        tvMaxParticipants = findViewById(R.id.tvMaxParticipants);
        riddlesContainer = findViewById(R.id.riddlesContainer);
        btnConfirmAndCreate = findViewById(R.id.btnConfirmAndCreate);
        btnSelectThemeImage = findViewById(R.id.btnSelectThemeImage);
        ivThemePreview = findViewById(R.id.ivThemePreview);
        btnFullScreenPreview = findViewById(R.id.btnFullScreenPreview);
    }

    private void populateData() {
        tvEventName.setText("Название: " + eventRequest.getName());
        tvEventDescription.setText("Описание: " + eventRequest.getDescription());
        tvEventStartPlace.setText("Место старта: " + eventRequest.getStart_place());
        tvEventDateTime.setText("Дата и время: " + eventRequest.getEvent_time());
        tvMaxParticipants.setText("Макс. участников: " + eventRequest.getMax_participants());

        riddlesContainer.removeAllViews();
        for (int i = 0; i < eventRequest.getRiddles().size(); i++) {
            RiddleRequest riddle = eventRequest.getRiddles().get(i);
            TextView riddleView = new TextView(this);
            riddleView.setText(String.format("%d. %s (%f, %f)", i + 1, riddle.getRiddle_text(), riddle.getLatitude(), riddle.getLongitude()));
            riddleView.setPadding(0, 8, 0, 8);
            riddlesContainer.addView(riddleView);
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(ivThemePreview);
                    }
                }
        );
    }

    private void setupButtons() {
        btnSelectThemeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnConfirmAndCreate.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Пожалуйста, выберите фон для мероприятия.", Toast.LENGTH_SHORT).show();
                return;
            }
            createEvent();
        });

        btnFullScreenPreview.setOnClickListener(v -> {
            if (eventRequest.getRiddles() != null && !eventRequest.getRiddles().isEmpty()) {
                Intent intent = new Intent(this, com.proj.quest.ui.main.RiddlePreviewActivity.class);
                intent.putExtra("RIDDLE_TEXT", eventRequest.getRiddles().get(0).getRiddle_text());
                if (selectedImageUri != null) {
                    intent.putExtra("EVENT_THEME_URI", selectedImageUri.toString());
                } else if (eventRequest.getTheme_url() != null) {
                    intent.putExtra("EVENT_THEME_URL", eventRequest.getTheme_url());
                }
                startActivity(intent);
            } else {
                Toast.makeText(this, "Нет загадок для предпросмотра", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MultipartBody.FORM, descriptionString);
    }

    private void createEvent() {
        String token = new SharedPrefs(this).getToken();
        if (token.isEmpty()) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Пожалуйста, выберите фон для мероприятия.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 1. Загружаем файл на сервер
        File file = FileUtils.getFileFromUri(this, selectedImageUri);
        if (file == null) {
            Toast.makeText(this, "Ошибка выбора файла.", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("theme", file.getName(), requestFile);
        ApiService apiService = ApiClient.getApiService();
        apiService.uploadEventTheme("Bearer " + token, body)
            .enqueue(new Callback<ThemeUploadResponse>() {
                @Override
                public void onResponse(Call<ThemeUploadResponse> call, Response<ThemeUploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String themeUrl = response.body().getTheme_url();
                        eventRequest.setTheme_url(themeUrl);
                        createEventWithThemeUrl(eventRequest, token);
                    } else {
                        Toast.makeText(EventPreviewActivity.this, "Ошибка загрузки фона.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ThemeUploadResponse> call, Throwable t) {
                    Toast.makeText(EventPreviewActivity.this, "Ошибка сети при загрузке фона.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void createEventWithThemeUrl(CreateEventRequest eventRequest, String token) {
        ApiService apiService = ApiClient.getApiService();
        apiService.createEvent("Bearer " + token, eventRequest)
            .enqueue(new Callback<Event>() {
                @Override
                public void onResponse(Call<Event> call, Response<Event> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(EventPreviewActivity.this, "Мероприятие успешно создано!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EventPreviewActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("EventPreview", "Ошибка при создании мероприятия: " + errorBody);
                            Toast.makeText(EventPreviewActivity.this, "Ошибка: " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            Log.e("EventPreview", "Ошибка при чтении ответа об ошибке", e);
                            Toast.makeText(EventPreviewActivity.this, "Неизвестная ошибка на сервере", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<Event> call, Throwable t) {
                    Log.e("EventPreview", "Сетевая ошибка", t);
                    Toast.makeText(EventPreviewActivity.this, "Сетевая ошибка: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
} 