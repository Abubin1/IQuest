package com.proj.quest.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.proj.quest.R;

import java.util.List;

public class LeaderboardAdapter extends ArrayAdapter<LeaderboardEntry> {
    public LeaderboardAdapter(@NonNull Context context, @NonNull List<LeaderboardEntry> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LeaderboardEntry entry = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_leaderboard_row, parent, false);
        }

        TextView positionView = convertView.findViewById(R.id.positionTextView);
        TextView nameView = convertView.findViewById(R.id.nameTextView);
        TextView scoreView = convertView.findViewById(R.id.scoreTextView);
        ImageView avatarView = convertView.findViewById(R.id.avatarImageView);

        if (entry != null) {
            positionView.setText(String.valueOf(position + 1));
            nameView.setText(entry.getName());
            scoreView.setText(String.valueOf(entry.getScore()));
            if (entry.getAvatarUrl() != null && !entry.getAvatarUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(entry.getAvatarUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(avatarView);
            } else {
                avatarView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        return convertView;
    }
}