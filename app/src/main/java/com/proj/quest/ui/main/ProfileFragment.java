package com.proj.quest.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.User;
import com.proj.quest.utils.SharedPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView tvLogin, tvEmail, tvPoints, tvRegDate;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvLogin = view.findViewById(R.id.tvLogin);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPoints = view.findViewById(R.id.tvPoints);
        tvRegDate = view.findViewById(R.id.tvRegDate);

        sharedPrefs = new SharedPrefs(requireContext());
        apiService = ApiClient.getApiService();

        loadProfile();

        return view;
    }

    private void loadProfile() {
        String token = sharedPrefs.getToken();

        if (token == null || token.isEmpty()) {
            tvLogin.setText("Офлайн режим");
            tvEmail.setText("Нет данных");
            return;
        }

        apiService.getProfile("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        tvLogin.setText(user.getLogin());
                        tvEmail.setText(user.getEmail());
                        tvPoints.setText(String.valueOf(user.getPoints()));
                        tvRegDate.setText(user.getRegistrationDate().toString());
                    } else {
                        Toast.makeText(getContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}