package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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
    private TextInputEditText searchEditText;
    private List<Event> filteredEvents = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        createEventButton = view.findViewById(R.id.createEvent);
        searchEditText = view.findViewById(R.id.search_edit_text);

        adapter = new EventAdapter();
        recyclerView.setAdapter(adapter);

        sharedPrefs = new SharedPrefs(requireContext());
        apiService = ApiClient.getApiService();

        createEventButton.setVisibility(View.GONE); // Скрыть по умолчанию
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateEventActivity.class);
            startActivity(intent);
        });

        setupSearch();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchEditText != null) {
            searchEditText.setText(""); // Сброс поиска
        }
        loadUserProfile(); // Загружаем данные каждый раз, когда фрагмент становится видимым
    }

    private void setupSearch(){
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void filterEvents(String query){
        filteredEvents.clear();

        if(query.isEmpty()){
            filteredEvents.addAll(allEvents);
        } else{
          String lowerCaseQuery = query.toLowerCase();
          for(Event event : allEvents){
              if(event.getName().toLowerCase().contains(lowerCaseQuery)){
                  filteredEvents.add(event);
              }
          }
        }
        filterAndDisplayEvents();
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
                        filteredEvents.clear();
                        filteredEvents.addAll(allEvents);
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
        Date now = new Date();
        Event registeredEvent = null;
        List<Event> availableEvents = new ArrayList<>();
        List<Event> finishedEvents = new ArrayList<>();
        // Получаем id мероприятия, на которое команда зарегистрирована (status != completed)
        Integer activeEventId = null;
        for (Team team : userTeams) {
            if (team.getEventId() != null) {
                activeEventId = team.getEventId();
                break;
            }
        }
        for (Event event : filteredEvents) {
            Date eventDate = parseStartDateTime(event.getStartDate(), event.getStartTime());
            if (eventDate == null) continue;
            long eventStart = eventDate.getTime();
            long eventEnd = eventStart + 3 * 60 * 60 * 1000;
            boolean isFinished = (event.getFinished() != null && event.getFinished()) || (event.getCompletionStatus() != null && event.getCompletionStatus().equals("completed")) || now.getTime() > eventEnd;
            if (activeEventId != null && event.getId() == activeEventId && !isFinished) {
                registeredEvent = event;
            } else if (!isFinished && (activeEventId == null)) {
                availableEvents.add(event);
            } else if (isFinished) {
                finishedEvents.add(event);
            }
        }
        // Сортировка
        Comparator<Event> byDate = (e1, e2) -> {
            Date d1 = parseStartDateTime(e1.getStartDate(), e1.getStartTime());
            Date d2 = parseStartDateTime(e2.getStartDate(), e2.getStartTime());
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d1.compareTo(d2);
        };
        availableEvents.sort(byDate);
        finishedEvents.sort((e1, e2) -> {
            Date d1 = parseStartDateTime(e1.getStartDate(), e1.getStartTime());
            Date d2 = parseStartDateTime(e2.getStartDate(), e2.getStartTime());
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return d2.compareTo(d1); // завершённые — новые сверху
        });
        // Передаём в адаптер
        adapter.setEvents(registeredEvent, null, new ArrayList<>(), availableEvents, finishedEvents);
        adapter.setUserTeams(userTeams);
    }

    private Date parseStartDateTime(String date, String time) {
        if (date == null || time == null) return null;
        String dateTime = date + "T" + time + ".000Z";
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        parser.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        try {
            return parser.parse(dateTime);
        } catch (ParseException e) {
            return null;
        }
    }
}
