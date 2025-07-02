package com.proj.quest.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.proj.quest.R;
import com.proj.quest.api.ApiClient;
import com.proj.quest.models.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamMemberSelectionAdapter extends BaseAdapter {
    private final Context context;
    private final List<User> members;
    private final Set<Integer> selectedMemberIds;
    private final int maxMembers;
    private final int currentUserId;
    private OnSelectionChangedListener selectionChangedListener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public TeamMemberSelectionAdapter(Context context, List<User> members, int maxMembers, int currentUserId) {
        this.context = context;
        this.members = members;
        this.maxMembers = maxMembers;
        this.currentUserId = currentUserId;
        this.selectedMemberIds = new HashSet<>();
        
        // Автоматически выбираем капитана (текущего пользователя)
        selectedMemberIds.add(currentUserId);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    @Override
    public int getCount() { return members.size(); }
    @Override
    public Object getItem(int i) { return members.get(i); }
    @Override
    public long getItemId(int i) { return members.get(i).getId(); }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_team_member_selection, parent, false);
        }
        
        User user = members.get(i);
        TextView tvName = view.findViewById(R.id.tvMemberName);
        TextView tvScore = view.findViewById(R.id.tvMemberScore);
        ImageView ivAvatar = view.findViewById(R.id.ivMemberAvatar);
        CheckBox checkBox = view.findViewById(R.id.checkBoxMember);
        
        tvName.setText(user.getLogin());
        tvScore.setText("Очки: " + user.getScore());
        
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
                .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.profile);
        }
        
        // Настраиваем чекбокс
        boolean isSelected = selectedMemberIds.contains(user.getId());
        checkBox.setChecked(isSelected);
        
        // Капитан всегда выбран и не может быть отменен
        if (user.getId() == currentUserId) {
            checkBox.setEnabled(false);
            tvName.setText(user.getLogin() + " (Капитан)");
        } else {
            checkBox.setEnabled(true);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (selectedMemberIds.size() < maxMembers) {
                        selectedMemberIds.add(user.getId());
                        notifySelectionChanged();
                    } else {
                        buttonView.setChecked(false);
                    }
                } else {
                    selectedMemberIds.remove(user.getId());
                    notifySelectionChanged();
                }
            });
        }
        
        return view;
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedMemberIds.size());
        }
    }

    public Set<Integer> getSelectedMemberIds() {
        return new HashSet<>(selectedMemberIds);
    }

    public int getSelectedCount() {
        return selectedMemberIds.size();
    }

    public boolean canSelectMore() {
        return selectedMemberIds.size() < maxMembers;
    }
} 