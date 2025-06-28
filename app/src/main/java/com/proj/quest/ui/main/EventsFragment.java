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

import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.Event;
import com.proj.quest.models.Team;
import com.proj.quest.ui.adapters.EventAdapter;
import com.proj.quest.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter();
        recyclerView.setAdapter(adapter);

        sharedPrefs = new SharedPrefs(requireContext());
        apiService = ApiClient.getApiService();

        loadEvents();

        return view;
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
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        
        // Добавляем неделю к текущей дате
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        Date weekFromNow = calendar.getTime();

        // Находим ближайшее мероприятие пользователя
        Event nearestUserEvent = null;
        List<Event> weekEvents = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.getEventDate() == null) continue;

            // Проверяем, что мероприятие в будущем
            if (event.getEventDate().after(now)) {
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
                    if (nearestUserEvent == null || event.getEventDate().before(nearestUserEvent.getEventDate())) {
                        nearestUserEvent = event;
                    }
                }
                
                // Добавляем мероприятия в течение недели
                if (event.getEventDate().before(weekFromNow)) {
                    weekEvents.add(event);
                }
            }
        }

        // Сортируем мероприятия по дате
        Collections.sort(weekEvents, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                return e1.getEventDate().compareTo(e2.getEventDate());
            }
        });

        // Добавляем ближайшее мероприятие пользователя в начало списка
        if (nearestUserEvent != null) {
            filteredEvents.add(nearestUserEvent);
            // Удаляем его из списка недельных мероприятий, чтобы избежать дублирования
            weekEvents.remove(nearestUserEvent);
        }

        // Добавляем остальные мероприятия в течение недели
        filteredEvents.addAll(weekEvents);

        adapter.setEvents(filteredEvents);
        adapter.setUserTeams(userTeams);
    }
}
