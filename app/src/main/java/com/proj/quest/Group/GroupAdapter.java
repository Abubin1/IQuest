package com.proj.quest.Group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.proj.quest.R;

import java.util.List;

public class GroupAdapter extends ArrayAdapter<GroupEntry> {

    public GroupAdapter(@NonNull Context context, @NonNull List<GroupEntry> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GroupEntry groupEntry = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_participant, parent, false);
        }
        TextView posView = convertView.findViewById(R.id.tvPosition);
        TextView nameView = convertView.findViewById(R.id.tvName);
        TextView scoreView = convertView.findViewById(R.id.tvPoints);

        if(groupEntry != null){
            posView.setText(String.valueOf(position + 1));
            nameView.setText(groupEntry.getName());
            scoreView.setText(String.valueOf(groupEntry.getScore()));
        }

        return convertView;
    }
}
