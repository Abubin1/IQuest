package com.proj.quest.Group;

import android.content.Context;
import android.content.res.Resources;

import com.proj.quest.R;
import com.proj.quest.leaderboard.LeaderboardEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupFileManager {
    public static List<GroupEntry> loadGroup(Context context) {
        List<GroupEntry> entries = new ArrayList<>();
        Resources resources = context.getResources();

        try {
            InputStream inputStream = resources.openRawResource(R.raw.group);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    entries.add(new GroupEntry(name, score));
                }
            }
            reader.close();
        } catch (IOException | Resources.NotFoundException e) {
            e.printStackTrace();
        }

        // Сортируем по убыванию очков
        Collections.sort(entries, new Comparator<GroupEntry>() {
            @Override
            public int compare(GroupEntry o1, GroupEntry o2) {
                return Integer.compare(o2.getScore(), o1.getScore());
            }
        });

        return entries;
    }

    public static int totalPoints(List<GroupEntry> entries){
        int total = 0;
        for(GroupEntry obj : entries){
            total += obj.getScore();
        }

        return total;
    }

}
