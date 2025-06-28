package com.proj.quest.Group;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.R;
import com.proj.quest.leaderboard.LeaderboardActivity;
import com.proj.quest.models.Team;
import com.proj.quest.ui.main.EventsFragment;
import com.proj.quest.ui.main.MainActivity;
import com.proj.quest.ui.main.ProfileActivity;

public class CreateGroupActivity extends AppCompatActivity {
    private Button btnCreateGroup;
    private EditText etNameGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Button test = findViewById(R.id.btnOfflineGroup);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        etNameGroup = findViewById(R.id.etNameGroup);

        test.setOnClickListener(v->{
            Intent intent = new Intent(this, GroupActivity.class);
            startActivity(intent);
        });

        btnCreateGroup.setOnClickListener(v->{
            createGroup();
        });

        setupBottomNavigation();
    }

    private void createGroup(){
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
