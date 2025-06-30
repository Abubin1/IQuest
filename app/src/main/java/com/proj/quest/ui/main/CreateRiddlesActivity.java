package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.CreateEventRequest;
import com.proj.quest.models.RiddleRequest;
import com.proj.quest.models.Event;
import com.proj.quest.utils.SharedPrefs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateRiddlesActivity extends AppCompatActivity {

    private LinearLayout riddlesContainer;
    private Button btnPreviewEvent;
    private List<View> riddleViews = new ArrayList<>();

    private ApiService apiService;
    private SharedPrefs sharedPrefs;

    private String eventName, eventDescription, eventDateTime, startPlace;
    private int maxMembers, riddleCount, maxTeams;

    private EditText currentLatEditText;
    private EditText currentLonEditText;

    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double latitude = result.getData().getDoubleExtra("latitude", 0);
                    double longitude = result.getData().getDoubleExtra("longitude", 0);
                    if (currentLatEditText != null && currentLonEditText != null) {
                        currentLatEditText.setText(String.format(Locale.US, "%.6f", latitude));
                        currentLonEditText.setText(String.format(Locale.US, "%.6f", longitude));
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_riddles);

        riddlesContainer = findViewById(R.id.riddlesContainer);
        btnPreviewEvent = findViewById(R.id.btnPreviewEvent);

        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(this);

        Intent intent = getIntent();
        eventName = intent.getStringExtra("name");
        eventDescription = intent.getStringExtra("description");
        eventDateTime = intent.getStringExtra("event_time");
        startPlace = intent.getStringExtra("start_place");
        maxMembers = intent.getIntExtra("max_participants", 0);
        riddleCount = intent.getIntExtra("number_of_riddles", 0);
        maxTeams = intent.getIntExtra("max_teams", 0);

        if (riddleCount > 0) {
            addRiddleInputViews();
        }

        btnPreviewEvent.setOnClickListener(v -> collectAndPreviewEvent());
    }

    private void addRiddleInputViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < riddleCount; i++) {
            View riddleView = inflater.inflate(R.layout.item_riddle_input, riddlesContainer, false);
            TextView tvRiddleNumber = riddleView.findViewById(R.id.tvRiddleNumber);
            tvRiddleNumber.setText("Загадка №" + (i + 1));

            EditText etLatitude = riddleView.findViewById(R.id.etLatitude);
            EditText etLongitude = riddleView.findViewById(R.id.etLongitude);
            Button btnOpenMap = riddleView.findViewById(R.id.btnOpenMap);

            btnOpenMap.setOnClickListener(v -> {
                currentLatEditText = etLatitude;
                currentLonEditText = etLongitude;
                Intent mapIntent = new Intent(this, MapPickerActivity.class);
                mapLauncher.launch(mapIntent);
            });

            riddlesContainer.addView(riddleView);
            riddleViews.add(riddleView);
        }
    }

    private void collectAndPreviewEvent() {
        List<RiddleRequest> riddles = new ArrayList<>();
        boolean allValid = true;

        for (View riddleView : riddleViews) {
            EditText etRiddleText = riddleView.findViewById(R.id.etRiddleText);
            EditText etLatitude = riddleView.findViewById(R.id.etLatitude);
            EditText etLongitude = riddleView.findViewById(R.id.etLongitude);

            String text = etRiddleText.getText().toString().trim();
            String latStr = etLatitude.getText().toString().trim();
            String lonStr = etLongitude.getText().toString().trim();

            if (TextUtils.isEmpty(text) || TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lonStr)) {
                allValid = false;
                break;
            }

            try {
                double latitude = Double.parseDouble(latStr);
                double longitude = Double.parseDouble(lonStr);
                riddles.add(new RiddleRequest(text, latitude, longitude));
            } catch (NumberFormatException e) {
                allValid = false;
                break;
            }
        }

        if (!allValid || riddles.size() != riddleCount) {
            Toast.makeText(this, "Пожалуйста, заполните все поля для всех загадок корректно", Toast.LENGTH_LONG).show();
            return;
        }

        CreateEventRequest request = new CreateEventRequest(
                eventName, eventDescription, eventDateTime,
                maxMembers, riddleCount, startPlace, maxTeams
        );
        request.setRiddles(riddles);

        Intent intent = new Intent(CreateRiddlesActivity.this, EventPreviewActivity.class);
        intent.putExtra("event_request", request);
        startActivity(intent);
    }
} 