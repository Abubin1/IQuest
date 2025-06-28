package com.proj.quest.Group;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.proj.quest.R;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.ui.main.MainActivity;
import com.proj.quest.ui.main.ProfileActivity;

import java.util.List;

public class GroupActivity extends AppCompatActivity {
    private Button btn_invent, btn_leaderboardTeams;
    private ListView listView;
    private TextView tvTotalPoints;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        btn_invent = findViewById(R.id.btn_invent);
        btn_leaderboardTeams = findViewById(R.id.leaderboardTeams);
        listView = findViewById(R.id.users);
        tvTotalPoints = findViewById(R.id.total_points);

        List<GroupEntry> entries = GroupFileManager.loadGroup(this);
        int totalPoints = GroupFileManager.totalPoints(entries);

        tvTotalPoints.setText("Общее кол-во очков: " + Integer.toString(totalPoints));

        if(entries == null || entries.isEmpty()){
            Toast.makeText(this, "Нет участников в группе", Toast.LENGTH_SHORT).show();
        }else {
            GroupAdapter adapter = new GroupAdapter(this, entries);
            listView.setAdapter(adapter);
        }

        btn_invent.setOnClickListener(v->{
            showInviteDialog();
        });

        btn_leaderboardTeams.setOnClickListener(v->{
            showLeaderboardTeams();
        });

        setupBottomNavigation();
    }

    private void showInviteDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_invite);

        dialog.setCancelable(true);

        final TextInputEditText etLogin = dialog.findViewById(R.id.etLogin);
        Button btnInvite = dialog.findViewById(R.id.btnInvite);

        btnInvite.setOnClickListener(v -> {
            String login = etLogin.getText().toString().trim();
            if (!login.isEmpty()) {
                // Здесь обработка введенного логина
                inviteUser(login);
                dialog.dismiss();
            } else {
                etLogin.setError("Введите логин");
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

    private void inviteUser(String login) {
        // Реальная логика приглашения пользователя
        Toast.makeText(this, "Приглашение отправлено пользователю: " + login,
                Toast.LENGTH_SHORT).show();

        // Здесь можно добавить вызов API для приглашения
        // viewModel.inviteUser(login);
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
