package com.proj.quest.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.proj.quest.R;
import com.proj.quest.models.Team;

import java.util.List;

public class TeamAdapter extends ArrayAdapter<Team> {
    private Context context;
    private List<Team> teams;
    private OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    public TeamAdapter(Context context, List<Team> teams, OnTeamClickListener listener) {
        super(context, 0, teams);
        this.context = context;
        this.teams = teams;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_team, parent, false);
        }

        Team team = getItem(position);
        if (team != null) {
            TextView tvTeamName = convertView.findViewById(R.id.tv_team_name);
            TextView tvMemberCount = convertView.findViewById(R.id.tv_team_score);

            tvTeamName.setText(team.getName());
            tvMemberCount.setText("Участников: " + team.getMembers().size());

            convertView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamClick(team);
                }
            });
        }

        return convertView;
    }
} 