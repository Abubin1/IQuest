package com.proj.quest.Riddle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.Group.GroupActivity;
import com.proj.quest.R;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.ui.main.MainActivity;
import com.proj.quest.ui.main.ProfileActivity;

public class RiddleActivity extends AppCompatActivity {
    private TextView tvQuestion;
    private Button checkBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riddle);

        tvQuestion = findViewById(R.id.tvQuestion);
        checkBtn = findViewById(R.id.checkBtn);

        checkBtn.setOnClickListener(v->{
            checkAnswer();
        });

        setupBottomNavigation();
    }

    public void checkAnswer(){

    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_riddles); // Подсветка текущего пункта

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_leaders) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_riddles) {
                return true;
            } else if (itemId == R.id.nav_events) {
                startActivity(new Intent(this, MainActivity.class)
                        .putExtra("fragment", "events"));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_groups) {
                startActivity(new Intent(this, GroupActivity.class));
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
