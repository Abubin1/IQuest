package com.proj.quest.Group;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.proj.quest.leaderboard.LeaderboardEntry;

import java.util.List;

public class LeaderboardTeamsAdapter extends ArrayAdapter<LeaderboardTeamsAdapter> {

    public LeaderboardTeamsAdapter(@NonNull Context context, @NonNull List<GroupEntry> objects) {
        super(context, 0);
    }

}
