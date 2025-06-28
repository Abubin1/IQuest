package com.proj.quest.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.proj.quest.R;
import com.proj.quest.models.Event;
import com.proj.quest.models.Team;
import com.proj.quest.ui.main.EventDetailsActivity;
import com.proj.quest.utils.SharedPrefs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;
    private Context context;
    private List<Team> userTeams;
    private SharedPrefs sharedPrefs;

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public void setUserTeams(List<Team> userTeams) {
        this.userTeams = userTeams;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        sharedPrefs = new SharedPrefs(context);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        
        // Проверяем, является ли это ближайшим мероприятием пользователя
        boolean isNearestUserEvent = isNearestUserEvent(event);
        
        // Название мероприятия
        holder.tvEventName.setText(event.getName() != null ? event.getName() : "Без названия");
        
        // Организатор
        holder.tvOrganizer.setText("Организатор: " + (event.getOrganizer() != null ? event.getOrganizer() : "Не указан"));
        
        // Дата проведения
        if (event.getEventDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            holder.tvEventDate.setText("Дата: " + dateFormat.format(event.getEventDate()));
        } else {
            holder.tvEventDate.setText("Дата: Не указана");
        }
        
        // Количество команд
        holder.tvTeamCount.setText("Команды: " + event.getTeamCount());
        
        // Максимальное количество участников
        holder.tvMaxMembers.setText("Макс. участников: " + event.getMaxTeamMembers());

        // Применяем специальное оформление для ближайшего мероприятия
        if (isNearestUserEvent) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // Светло-голубой фон
            holder.tvEventName.setTextColor(Color.parseColor("#1976D2")); // Синий текст
            holder.tvNearestEvent.setVisibility(View.VISIBLE);
            holder.tvNearestEvent.setText("⭐ Ваше ближайшее мероприятие");
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.tvEventName.setTextColor(Color.BLACK);
            holder.tvNearestEvent.setVisibility(View.GONE);
        }

        // Обработка клика
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("event", event);
            context.startActivity(intent);
        });
    }

    private boolean isNearestUserEvent(Event event) {
        if (userTeams == null || userTeams.isEmpty()) return false;
        
        // Проверяем, записана ли команда пользователя на это мероприятие
        for (Team team : userTeams) {
            if (team.getEventId() != null && team.getEventId().intValue() == event.getId()) {
                // Проверяем, является ли это ближайшим мероприятием
                Date eventDate = event.getEventDate();
                if (eventDate == null) continue;
                
                Date now = new Date();
                if (eventDate.after(now)) {
                    // Проверяем, нет ли более близких мероприятий
                    boolean isNearest = true;
                    for (Event otherEvent : events) {
                        if (otherEvent.getId() != event.getId() && 
                            otherEvent.getEventDate() != null && 
                            otherEvent.getEventDate().after(now) &&
                            otherEvent.getEventDate().before(eventDate)) {
                            
                            // Проверяем, записана ли команда на это более близкое мероприятие
                            for (Team otherTeam : userTeams) {
                                if (otherTeam.getEventId() != null && 
                                    otherTeam.getEventId().intValue() == otherEvent.getId()) {
                                    isNearest = false;
                                    break;
                                }
                            }
                        }
                    }
                    return isNearest;
                }
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvEventName;
        TextView tvOrganizer;
        TextView tvEventDate;
        TextView tvTeamCount;
        TextView tvMaxMembers;
        TextView tvNearestEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvOrganizer = itemView.findViewById(R.id.tvOrganizer);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvTeamCount = itemView.findViewById(R.id.tvTeamCount);
            tvMaxMembers = itemView.findViewById(R.id.tvMaxMembers);
            tvNearestEvent = itemView.findViewById(R.id.tvNearestEvent);
        }
    }
}