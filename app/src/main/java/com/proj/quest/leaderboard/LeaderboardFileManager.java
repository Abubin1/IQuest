package com.proj.quest.leaderboard;

import android.content.Context;
import android.content.res.Resources;

import com.proj.quest.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardFileManager {
    public static List<LeaderboardEntry> loadLeaderboard(Context context) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        Resources resources = context.getResources();

        try {
            InputStream inputStream = resources.openRawResource(R.raw.leaders);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    entries.add(new LeaderboardEntry(name, score));
                }
            }
            reader.close();
        } catch (IOException | Resources.NotFoundException e) {
            e.printStackTrace();
        }

        // Сортируем по убыванию очков
        Collections.sort(entries, new Comparator<LeaderboardEntry>() {
            @Override
            public int compare(LeaderboardEntry o1, LeaderboardEntry o2) {
                return Integer.compare(o2.getScore(), o1.getScore());
            }
        });

        return entries;
    }
}
