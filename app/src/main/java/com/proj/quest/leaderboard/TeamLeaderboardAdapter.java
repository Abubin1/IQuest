package com.proj.quest.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.proj.quest.R;
import com.proj.quest.models.TeamLeaderboardEntry;
import java.util.List;

public class TeamLeaderboardAdapter extends ArrayAdapter<TeamLeaderboardEntry> {
    public TeamLeaderboardAdapter(Context context, List<TeamLeaderboardEntry> teams) {
        super(context, 0, teams);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_team_leaderboard_row, parent, false);
        }
        TeamLeaderboardEntry team = getItem(position);
        TextView tvPlace = convertView.findViewById(R.id.tvTeamPlace);
        TextView tvName = convertView.findViewById(R.id.tvTeamName);
        TextView tvScore = convertView.findViewById(R.id.tvTeamScore);
        tvPlace.setText(String.valueOf(position + 1));
        tvName.setText(team.getName());
        tvScore.setText(String.valueOf(team.getScore()));
        return convertView;
    }
} 