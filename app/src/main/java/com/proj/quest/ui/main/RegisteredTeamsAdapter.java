package com.proj.quest.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.proj.quest.R;
import com.proj.quest.models.RegisteredTeamProgress;
import com.proj.quest.models.Event;

import java.util.List;

public class RegisteredTeamsAdapter extends ArrayAdapter<RegisteredTeamProgress> {
    private final Event event;
    public RegisteredTeamsAdapter(Context context, List<RegisteredTeamProgress> teams, Event event) {
        super(context, 0, teams);
        this.event = event;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_team, parent, false);
        }
        RegisteredTeamProgress team = getItem(position);
        TextView tvTeamName = convertView.findViewById(R.id.tv_team_name);
        TextView tvTeamScore = convertView.findViewById(R.id.tv_team_score);
        tvTeamName.setText(team.getName());
        // Определяем статус мероприятия
        boolean eventStarted = false;
        boolean eventFinished = false;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            java.util.Date now = new java.util.Date();
            java.util.Date eventStart = sdf.parse(event.getStartDate() + "T" + event.getStartTime() + ".000Z");
            eventStarted = now.after(eventStart);
            eventFinished = event.getFinished() != null && event.getFinished();
        } catch (Exception e) { eventStarted = false; eventFinished = false; }
        if (!eventStarted) {
            // До начала — только название
            tvTeamScore.setText("");
        } else if (!eventFinished) {
            // Во время мероприятия
            if (team.getCompletionTimeSeconds() != null) {
                int seconds = (int)Math.round(team.getCompletionTimeSeconds());
                int min = seconds / 60;
                int sec = seconds % 60;
                tvTeamScore.setText("Завершили за: " + min + " мин " + sec + " сек");
            } else {
                tvTeamScore.setText("Группа в процессе");
            }
        } else {
            // После завершения мероприятия
            if (team.getCompletionTimeSeconds() != null) {
                int seconds = (int)Math.round(team.getCompletionTimeSeconds());
                int min = seconds / 60;
                int sec = seconds % 60;
                tvTeamScore.setText("Завершили за: " + min + " мин " + sec + " сек");
            } else {
                tvTeamScore.setText("Группа не прошла");
            }
        }
        return convertView;
    }
} 