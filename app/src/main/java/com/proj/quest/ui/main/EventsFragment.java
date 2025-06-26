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
import com.proj.quest.ui.adapters.EventAdapter;
import com.proj.quest.utils.SharedPrefs;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;

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
            Toast.makeText(getContext(), "Офлайн режим", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getEvents("Bearer " + token).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (isAdded()) { // Проверяем, что фрагмент прикреплен к Activity
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.setEvents(response.body());
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
                if (isAdded()) { // Проверяем, что фрагмент прикреплен к Activity
                    Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
