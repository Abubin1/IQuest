package com.proj.quest.Theme;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применяем тему перед setContentView()
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
    }
}
