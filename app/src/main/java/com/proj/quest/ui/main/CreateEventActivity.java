package com.proj.quest.ui.main;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.proj.quest.R;
import com.proj.quest.Theme.BaseActivity;

import java.util.Calendar;
import java.util.Locale;

public class CreateEventActivity extends BaseActivity {

    private TextInputEditText etEventName, etEventDescription, etEventStartPlace, etMaxTeamMembers, etRiddleCount, etMaxTeams;
    private Button btnSelectDate, btnSelectTime, btnNext;
    private TextView tvSelectedDate, tvSelectedTime;

    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        etEventName = findViewById(R.id.etEventName);
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventStartPlace = findViewById(R.id.etEventStartPlace);
        etMaxTeamMembers = findViewById(R.id.etMaxTeamMembers);
        etRiddleCount = findViewById(R.id.etRiddleCount);
        etMaxTeams = findViewById(R.id.etMaxTeams);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnNext = findViewById(R.id.btnNext);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        btnSelectTime.setOnClickListener(v -> showTimePickerDialog());
        btnNext.setOnClickListener(v -> validateAndProceed());
    }

    private void showDatePickerDialog() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateLabel();
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));

        // Устанавливаем минимальную дату: сегодня
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDate.set(Calendar.MINUTE, minute);
                    updateTimeLabel();
                },
                selectedDate.get(Calendar.HOUR_OF_DAY),
                selectedDate.get(Calendar.MINUTE),
                true); // 24-часовой формат
        timePickerDialog.show();
    }

    private void updateDateLabel() {
        String format = "dd/MM/yyyy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, Locale.getDefault());
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void updateTimeLabel() {
        String format = "HH:mm";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format, Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Moscow"));
        tvSelectedTime.setText(sdf.format(selectedDate.getTime()));
    }

    private void validateAndProceed() {
        String name = etEventName.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();
        String startPlace = etEventStartPlace.getText().toString().trim();
        String maxMembersStr = etMaxTeamMembers.getText().toString().trim();
        String maxTeamsStr = etMaxTeams.getText().toString().trim();
        String riddleCountStr = etRiddleCount.getText().toString().trim();
        String dateStr = tvSelectedDate.getText().toString();
        String timeStr = tvSelectedTime.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(startPlace) ||
            TextUtils.isEmpty(maxMembersStr) || TextUtils.isEmpty(riddleCountStr) || TextUtils.isEmpty(maxTeamsStr) ||
            dateStr.equals("Дата не выбрана") || timeStr.equals("Время не выбрано")) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxMembers = Integer.parseInt(maxMembersStr);
        int riddleCount = Integer.parseInt(riddleCountStr);
        int maxTeams = Integer.parseInt(maxTeamsStr);

        if (maxMembers <= 0) {
            etMaxTeamMembers.setError("Количество участников должно быть больше нуля");
            return;
        }

        if (riddleCount <= 0) {
            etRiddleCount.setError("Количество загадок должно быть больше нуля");
            return;
        }

        if (maxTeams <= 0) {
            etMaxTeams.setError("Количество команд должно быть больше нуля");
            return;
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Moscow"));
        String formattedDateTime = sdf.format(selectedDate.getTime());

        // Переход на CreateRiddlesActivity
        Intent intent = new Intent(this, CreateRiddlesActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("description", description);
        intent.putExtra("start_place", startPlace);
        intent.putExtra("event_time", formattedDateTime);
        intent.putExtra("max_participants", maxMembers);
        intent.putExtra("number_of_riddles", riddleCount);
        intent.putExtra("max_teams", maxTeams);
        startActivity(intent);
    }
} 