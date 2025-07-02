package com.proj.quest.Group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserInviteAdapter extends RecyclerView.Adapter<UserInviteAdapter.UserViewHolder> {
    private Context context;
    private List<User> allUsers;
    private List<User> filteredUsers;
    private OnUserSelectedListener listener;
    private List<Integer> teamMemberIds;

    public interface OnUserSelectedListener {
        void onUserSelected(User user);
    }

    public UserInviteAdapter(Context context, List<User> users, List<Integer> teamMemberIds, OnUserSelectedListener listener) {
        this.context = context;
        this.allUsers = users;
        this.filteredUsers = new ArrayList<>(users);
        this.teamMemberIds = teamMemberIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_invite, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredUsers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    public void filterUsers(String query) {
        filteredUsers.clear();
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            for (User user : allUsers) {
                if (user.getLogin().toLowerCase().contains(query.toLowerCase())) {
                    filteredUsers.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName, tvUserScore, tvUserStatus;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserScore = itemView.findViewById(R.id.tvUserScore);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
        }

        void bind(User user) {
            tvUserName.setText(user.getLogin());
            tvUserScore.setText("Очки: " + user.getScore());

            // Загружаем аватар
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                String avatarUrl = user.getAvatarUrl();
                if (avatarUrl.startsWith("/avatars/")) {
                    avatarUrl = ApiClient.BASE_URL + avatarUrl.substring(1);
                }
                Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(ivUserAvatar);
            } else {
                ivUserAvatar.setImageResource(R.drawable.profile);
            }

            // Проверяем статус пользователя
            if (teamMemberIds.contains(user.getId())) {
                tvUserStatus.setText("В команде");
                tvUserStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                itemView.setEnabled(false);
                itemView.setAlpha(0.5f);
            } else {
                tvUserStatus.setText("");
                itemView.setEnabled(true);
                itemView.setAlpha(1.0f);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null && !teamMemberIds.contains(user.getId())) {
                    listener.onUserSelected(user);
                }
            });
        }
    }
} 