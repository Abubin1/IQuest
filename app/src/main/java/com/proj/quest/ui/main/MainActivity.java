package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proj.quest.Group.CreateGroupActivity;
import com.proj.quest.Group.GroupActivity;
import com.proj.quest.R;
import com.proj.quest.Riddle.RiddleActivity;
import com.proj.quest.leaderboard.LeaderboardActivity;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private Button createEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.nav_events);
       // createEvent = findViewById(R.id.create_event);

        // Проверяем, есть ли фрагмент в Intent
        String fragment = getIntent().getStringExtra("fragment");
        if (fragment != null && fragment.equals("groups")) {
            // Если переходим на группы, загружаем GroupsFragment
            loadFragment(new GroupsFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_groups);
            return;
        }

        loadFragment(new EventsFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Обновляем состояние фрагментов при возврате в активность
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof GroupsFragment) {
            // Если текущий фрагмент - GroupsFragment, обновляем его данные
            ((GroupsFragment) currentFragment).loadData();
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_events) {
            return loadFragment(new EventsFragment());
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            return false;
        }else if (itemId == R.id.nav_leaders) {
            startActivity(new Intent(this, LeaderboardActivity.class));
            overridePendingTransition(0, 0);
            return false;
        } else if (itemId == R.id.nav_groups) {
            return loadFragment(new GroupsFragment());
        }else if (itemId == R.id.nav_riddles) {
            startActivity(new Intent(this, RiddleActivity.class));
            overridePendingTransition(0, 0);
            return false;
        }

        // Добавьте обработку других пунктов меню, если нужно
        return false;
    }
}