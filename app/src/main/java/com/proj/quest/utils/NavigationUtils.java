package com.proj.quest.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.proj.quest.Riddle.RiddleActivity;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.Event;
import com.proj.quest.models.Team;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationUtils {
    /**
     * Универсальный переход на вкладку "Загадки" из любой Activity
     */
    public static void goToRiddles(Activity activity) {
        SharedPrefs sharedPrefs = new SharedPrefs(activity);
        String token = sharedPrefs.getToken();
        ApiService apiService = ApiClient.getApiService();
        if (token == null || token.isEmpty()) {
            Toast.makeText(activity, "Вы не авторизованы", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.getMyTeam("Bearer " + token).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Team team = response.body();
                    if (team.getId() <= 0) {
                        Toast.makeText(activity, "Сейчас вы не записаны на мероприятия, запишитесь группой", Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Получаем все мероприятия для команды
                    apiService.getTeamEvents("Bearer " + token, team.getId()).enqueue(new Callback<java.util.List<Event>>() {
                        @Override
                        public void onResponse(Call<java.util.List<Event>> call, Response<java.util.List<Event>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                java.util.List<Event> registeredEvents = response.body();
                                if (registeredEvents.isEmpty()) {
                                    Toast.makeText(activity, "Сейчас вы не записаны на мероприятия, запишитесь группой", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                // Ищем ближайшее (сейчас идет или ближайшее будущее), исключая завершенные
                                Event bestEvent = null;
                                long now = System.currentTimeMillis();
                                long minDiff = Long.MAX_VALUE;
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                                sdf.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Moscow"));
                                for (Event event : registeredEvents) {
                                    // Пропускаем завершенные мероприятия
                                    if (event.getFinished() != null && event.getFinished()) {
                                        continue;
                                    }
                                    String eventDateTime = event.getStartDate() + "T" + event.getStartTime() + ".000Z";
                                    try {
                                        java.util.Date eventDate = sdf.parse(eventDateTime);
                                        long eventStart = eventDate.getTime();
                                        long eventEnd = eventStart + 3 * 60 * 60 * 1000;
                                        if (now >= eventStart && now < eventEnd) {
                                            // Если мероприятие идет — оно приоритетно
                                            bestEvent = event;
                                            break;
                                        } else if (eventStart > now) {
                                            long diff = eventStart - now;
                                            if (diff < minDiff) {
                                                minDiff = diff;
                                                bestEvent = event;
                                            }
                                        }
                                    } catch (Exception ignore) {}
                                }
                                if (bestEvent != null) {
                                    String eventDateTime = bestEvent.getStartDate() + "T" + bestEvent.getStartTime() + ".000Z";
                                    Intent intent = new Intent(activity, RiddleActivity.class);
                                    intent.putExtra("EVENT_ID", bestEvent.getId());
                                    intent.putExtra("EVENT_TIME", eventDateTime);
                                    intent.putExtra("IS_REGISTERED", true);
                                    intent.putExtra("EVENT_FINISHED", bestEvent.getFinished() != null ? bestEvent.getFinished() : false);
                                    if (bestEvent.getThemeUrl() != null) {
                                        intent.putExtra("EVENT_THEME_URL", bestEvent.getThemeUrl());
                                    }
                                    activity.startActivity(intent);
                                } else {
                                    // Если нет активных мероприятий, показываем сообщение в текущей активности
                                    Toast.makeText(activity, "Сейчас вы не записаны на мероприятия, запишитесь группой", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                activity.runOnUiThread(() -> Toast.makeText(activity, "Ошибка загрузки мероприятий", Toast.LENGTH_SHORT).show());
                            }
                        }
                        @Override
                        public void onFailure(Call<java.util.List<Event>> call, Throwable t) {
                            activity.runOnUiThread(() -> Toast.makeText(activity, "Ошибка сети", Toast.LENGTH_SHORT).show());
                        }
                    });
                } else {
                    Toast.makeText(activity, "Сейчас вы не записаны на мероприятия, запишитесь группой", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Ошибка сети", Toast.LENGTH_SHORT).show());
            }
        });
    }
} 