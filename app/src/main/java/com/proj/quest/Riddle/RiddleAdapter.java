package com.proj.quest.Riddle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proj.quest.R;
import com.proj.quest.models.RiddleRequest; // Предполагая, что модель RiddleRequest подходит

import java.util.List;

public class RiddleAdapter extends RecyclerView.Adapter<RiddleAdapter.RiddleViewHolder> {

    private final Context context;
    private final List<RiddleRequest> riddles;

    public RiddleAdapter(Context context, List<RiddleRequest> riddles) {
        this.context = context;
        this.riddles = riddles;
    }

    @NonNull
    @Override
    public RiddleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_riddle, parent, false);
        return new RiddleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiddleViewHolder holder, int position) {
        RiddleRequest riddle = riddles.get(position);
        holder.riddleNumberTextView.setText("Загадка №" + (position + 1));
        holder.riddleTextTextView.setText(riddle.getRiddle_text());

        holder.showOnMapButton.setOnClickListener(v -> {
            // TODO: Реализовать открытие карты с меткой
            Toast.makeText(context, "Открываем карту для загадки №" + (position + 1), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return riddles.size();
    }

    public static class RiddleViewHolder extends RecyclerView.ViewHolder {
        TextView riddleNumberTextView;
        TextView riddleTextTextView;
        Button showOnMapButton;

        public RiddleViewHolder(@NonNull View itemView) {
            super(itemView);
            riddleNumberTextView = itemView.findViewById(R.id.riddleNumberTextView);
            riddleTextTextView = itemView.findViewById(R.id.riddleTextTextView);
            showOnMapButton = itemView.findViewById(R.id.showOnMapButton);
        }
    }
} 