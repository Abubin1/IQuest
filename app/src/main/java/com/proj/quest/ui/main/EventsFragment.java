package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.Event;
import com.proj.quest.models.Team;
import com.proj.quest.models.User;
import com.proj.quest.ui.adapters.EventAdapter;
import com.proj.quest.utils.SharedPrefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private List<Event> allEvents = new ArrayList<>();
    private List<Team> userTeams = new ArrayList<>();
    private MaterialButton createEventButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        createEventButton = view.findViewById(R.id.createEvent);

        adapter = new EventAdapter();
        recyclerView.setAdapter(adapter);

        sharedPrefs = new SharedPrefs(requireContext());
        apiService = ApiClient.getApiService();

        createEventButton.setVisibility(View.GONE); // Скрыть по умолчанию
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateEventActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile(); // Загружаем данные каждый раз, когда фрагмент становится видимым
    }

    private void loadUserProfile() {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            loadEvents(); // Продолжаем загрузку событий даже без токена
            return;
        }

        apiService.getProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isAdded() && getContext() != null && response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    if (user.isOrganizer()) {
                        createEventButton.setVisibility(View.VISIBLE);
                    }
                }
                loadEvents(); // Загружаем события после проверки профиля
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                }
                loadEvents(); // Все равно загружаем события
            }
        });
    }

    private void loadEvents() {
        String token = sharedPrefs.getToken();

        if (token == null || token.isEmpty()) {
            // Не загружать, если нет токена (офлайн режим)
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Офлайн режим", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Загружаем все мероприятия
        apiService.getEvents("Bearer " + token).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (isAdded() && getContext() != null) { // Проверяем, что фрагмент прикреплен к Activity
                    if (response.isSuccessful() && response.body() != null) {
                        allEvents = response.body();
                        loadUserTeams();
                    } else if (response.code() == 403) {
                        // Токен истёк или невалиден
                        sharedPrefs.clear();
                        Intent intent = new Intent(getContext(), com.proj.quest.ui.auth.LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Ошибка загрузки мероприятий", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                if (isAdded() && getContext() != null) { // Проверяем, что фрагмент прикреплен к Activity
                    Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUserTeams() {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            filterAndDisplayEvents();
            return;
        }

        // Загружаем команды пользователя
        apiService.getMyTeam("Bearer " + token).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                if (isAdded() && getContext() != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        userTeams.add(response.body());
                    }
                    filterAndDisplayEvents();
                }
            }

            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    filterAndDisplayEvents();
                }
            }
        });
    }

    private void filterAndDisplayEvents() {
        List<Event> filteredEvents = new ArrayList<>();
        Date now = new Date();

        // Находим ближайшее мероприятие пользователя
        Event nearestUserEvent = null;
        List<Event> futureEvents = new ArrayList<>();

        for (Event event : allEvents) {
            Date eventDate = parseStartDate(event.getStartDate());
            if (eventDate == null) continue;

            // Проверяем, что мероприятие в будущем
            if (eventDate.after(now)) {
                futureEvents.add(event);

                // Проверяем, записана ли команда пользователя на это мероприятие
                boolean isRegistered = false;
                for (Team team : userTeams) {
                    if (team.getEventId() != null && team.getEventId().intValue() == event.getId()) {
                        isRegistered = true;
                        break;
                    }
                }

                if (isRegistered) {
                    // Если это ближайшее мероприятие пользователя
                    Date nearestEventDate = (nearestUserEvent != null) ? parseStartDate(nearestUserEvent.getStartDate()) : null;
                    if (nearestEventDate == null || eventDate.before(nearestEventDate)) {
                        nearestUserEvent = event;
                    }
                }
            }
        }

        // Сортируем будущие мероприятия по дате
        Collections.sort(futureEvents, (e1, e2) -> {
            Date d1 = parseStartDate(e1.getStartDate());
            Date d2 = parseStartDate(e2.getStartDate());
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d1.compareTo(d2);
        });

        // Удаляем ближайшее мероприятие пользователя из общего списка, чтобы избежать дублирования
        if (nearestUserEvent != null) {
            futureEvents.remove(nearestUserEvent);
        }

        // Добавляем ближайшее мероприятие пользователя в начало списка, если оно есть
        if (nearestUserEvent != null) {
            filteredEvents.add(nearestUserEvent);
        }

        // Добавляем остальные будущие мероприятия
        filteredEvents.addAll(futureEvents);


        adapter.setEvents(filteredEvents);
        adapter.setUserTeams(userTeams);
    }

    private Date parseStartDate(String dateString) {
        if (dateString == null) return null;
        // Формат, который приходит от сервера: "2025-07-01T21:00:00.000Z"
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return parser.parse(dateString);
        } catch (ParseException e) {
            // e.printStackTrace(); // Не спамим в лог, если дата просто некорректна
            return null;
        }
    }
}
