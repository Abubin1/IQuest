package com.proj.quest.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.proj.quest.Group.CreateGroupActivity;
import com.proj.quest.Group.GroupActivity;
import com.proj.quest.Group.InviteAdapter;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.api.ApiService;
import com.proj.quest.models.InviteResponse;
import com.proj.quest.models.Team;
import com.proj.quest.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsFragment extends Fragment {
    private ListView invitesListView;
    private TextView tvNoInvites;
    private Button btnCreateGroup;
    private View noInvitesContainer;
    private ApiService apiService;
    private SharedPrefs sharedPrefs;
    private List<InviteResponse> invites = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        invitesListView = view.findViewById(R.id.invites_list);
        tvNoInvites = view.findViewById(R.id.tv_no_invites);
        btnCreateGroup = view.findViewById(R.id.btn_create_group);
        noInvitesContainer = view.findViewById(R.id.no_invites_container);
        progressBar = view.findViewById(R.id.progressBar);

        apiService = ApiClient.getApiService();
        sharedPrefs = new SharedPrefs(requireContext());

        btnCreateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateGroupActivity.class);
            startActivity(intent);
        });

        loadData();

        return view;
    }

    public void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        checkTeamAndLoadInvites();
    }

    private void checkTeamAndLoadInvites() {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            showNoTeamUI();
            return;
        }

        // Сначала проверяем, есть ли у пользователя команда
        apiService.getMyTeam("Bearer " + token).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded() || getContext() == null) {
                    return; // Фрагмент отключен от активности
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    // У пользователя есть команда, переходим в GroupActivity
                    Intent intent = new Intent(getContext(), GroupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (response.code() == 404) {
                    // У пользователя нет команды, загружаем приглашения
                    loadInvites();
                } else {
                    showNoTeamUI();
                }
            }

            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded() || getContext() == null) {
                    return; // Фрагмент отключен от активности
                }
                showNoTeamUI();
            }
        });
    }

    private void loadInvites() {
        String token = sharedPrefs.getToken();
        if (token == null || token.isEmpty()) {
            showNoInvitesUI();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        apiService.getInvites("Bearer " + token).enqueue(new Callback<List<InviteResponse>>() {
            @Override
            public void onResponse(Call<List<InviteResponse>> call, Response<List<InviteResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded() || getContext() == null) {
                    return; // Фрагмент отключен от активности
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    invites = response.body();
                    System.out.println("DEBUG: Loaded " + invites.size() + " invites");
                    for (InviteResponse invite : invites) {
                        System.out.println("DEBUG: Invite - ID: " + invite.getId() + 
                                         ", Team: " + invite.getTeamName() + 
                                         ", Status: " + invite.getStatus() + 
                                         ", Captain: " + invite.getCaptainLogin());
                    }
                    if (invites.isEmpty()) {
                        showNoInvitesUI();
                    } else {
                        showInvitesUI();
                    }
                } else {
                    System.out.println("DEBUG: Failed to load invites - " + response.code());
                    showNoInvitesUI();
                }
            }

            @Override
            public void onFailure(Call<List<InviteResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded() || getContext() == null) {
                    return; // Фрагмент отключен от активности
                }
                System.out.println("DEBUG: Network error loading invites - " + t.getMessage());
                showNoInvitesUI();
            }
        });
    }

    private void showInvitesUI() {
        if (!isAdded() || getContext() == null) {
            return; // Фрагмент отключен от активности
        }
        
        invitesListView.setVisibility(View.VISIBLE);
        noInvitesContainer.setVisibility(View.GONE);

        InviteAdapter adapter = new InviteAdapter(requireContext(), invites, this::acceptInvite, this::declineInvite);
        invitesListView.setAdapter(adapter);
    }

    private void showNoInvitesUI() {
        if (!isAdded() || getContext() == null) {
            return; // Фрагмент отключен от активности
        }
        
        invitesListView.setVisibility(View.GONE);
        noInvitesContainer.setVisibility(View.VISIBLE);
        tvNoInvites.setText("У вас пока нет приглашений в группу");
        btnCreateGroup.setVisibility(View.VISIBLE);
    }

    private void showNoTeamUI() {
        if (!isAdded() || getContext() == null) {
            return; // Фрагмент отключен от активности
        }
        
        invitesListView.setVisibility(View.GONE);
        noInvitesContainer.setVisibility(View.VISIBLE);
        tvNoInvites.setText("У вас пока нет команды");
        btnCreateGroup.setVisibility(View.VISIBLE);
    }

    private void acceptInvite(InviteResponse invite) {
        if (!isAdded() || getContext() == null) {
            return; // Фрагмент отключен от активности
        }
        
        String token = sharedPrefs.getToken();
        apiService.acceptInvite("Bearer " + token, invite.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded() || getContext() == null) {
                    return; // Фрагмент отключен от активности
                }
                
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Приглашение принято", Toast.LENGTH_SHORT).show();
                    // Переходим в GroupActivity после принятия приглашения
                    Intent intent = new Intent(getContext(), GroupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    // Обрабатываем ошибку от сервера
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.contains("error")) {
                            int startIndex = errorBody.indexOf("\"error\":\"") + 9;
                            int endIndex = errorBody.lastIndexOf("\"");
                            if (startIndex > 8 && endIndex > startIndex) {
                                String errorMessage = errorBody.substring(startIndex, endIndex);
                                showErrorDialog("Ошибка принятия приглашения", errorMessage);
                            } else {
                                showErrorDialog("Ошибка", "Ошибка принятия приглашения");
                            }
                        } else {
                            showErrorDialog("Ошибка", "Ошибка принятия приглашения");
                        }
                    } catch (Exception e) {
                        showErrorDialog("Ошибка", "Ошибка принятия приглашения");
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded() || getContext() == null) {
                    return; // Фрагмент отключен от активности
                }
                showErrorDialog("Ошибка сети", "Не удалось принять приглашение. Проверьте подключение к интернету.");
            }
        });
    }

    private void declineInvite(InviteResponse invite) {
        if (!isAdded() || getContext() == null) {
            return; // Фрагмент отключен от активности
        }
        
        String token = sharedPrefs.getToken();
        apiService.declineInvite("Bearer " + token, invite.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded() || getContext() == null) {
                    return; // Фрагмент отключен от активности
                }
                
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Приглашение отклонено", Toast.LENGTH_SHORT).show();
                    loadInvites(); // Перезагружаем список приглашений
                } else {
                    showErrorDialog("Ошибка", "Ошибка отклонения приглашения");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded() || getContext() == null) {
                    return; // Фрагмент отключен от активности
                }
                showErrorDialog("Ошибка сети", "Не удалось отклонить приглашение. Проверьте подключение к интернету.");
            }
        });
    }

    private void showErrorDialog(String title, String message) {
        if (!isAdded() || getContext() == null) {
            return; // Фрагмент отключен от активности
        }
        
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
} 