package com.proj.quest.Theme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.proj.quest.R;
import com.proj.quest.ui.main.MainActivity;


public class ThemeHelper {
    private static final String PREFS_NAME = "app_theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    // Доступные темы
    public static final int[] APP_THEMES = {
            R.style.Theme_MyApp_Theme1,
            R.style.Theme_MyApp_Theme2,
            R.style.Theme_MyApp_Theme3,
            R.style.Theme_MyApp_Theme4,
            R.style.Theme_MyApp_Theme5
    };

    // Сохраняем выбранную тему
    public static void saveTheme(Context context, int themeIndex) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, themeIndex).apply();
    }


    // Получаем сохраненную тему
    public static int getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, 0); // 0 = тема по умолчанию
    }
    public static void restartApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof MainActivity) {
            ((MainActivity) context).finish();
        }
    }

    // Применяем тему ко всему приложению
    public static void applyTheme(Context context) {
        int themeIndex = getSavedTheme(context);
        context.setTheme(APP_THEMES[themeIndex]);
    }
}
