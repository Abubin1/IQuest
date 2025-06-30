package com.proj.quest.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.proj.quest.R;
import com.proj.quest.models.Event;
import com.proj.quest.models.Team;
import com.proj.quest.ui.main.EventDetailsActivity;
import com.proj.quest.Riddle.RiddleActivity;
import com.proj.quest.utils.SharedPrefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
        if (event.getStartDate() != null && !event.getStartDate().isEmpty()) {
            Date eventDate = parseStartDate(event.getStartDate());
            if (eventDate != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                holder.tvEventDate.setText("Дата: " + displayFormat.format(eventDate));
            } else {
                holder.tvEventDate.setText("Дата: неверный формат");
            }
        } else {
            holder.tvEventDate.setText("Дата: Не указана");
        }
        
        // Количество команд
        holder.tvTeamCount.setText("Зарегистрированные команды: " + event.getTeamCount());
        
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

    private boolean isUserParticipant(Event event) {
        if (userTeams == null || userTeams.isEmpty()) {
            return false;
        }
        for (Team team : userTeams) {
            if (team.getEventId() != null && team.getEventId() == event.getId()) {
                return true;
            }
        }
        return false;
    }

    private Date parseStartDate(String dateString) {
        if (dateString == null) return null;
        // Формат, который приходит от сервера: "2025-07-01T21:00:00.000Z"
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return parser.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isNearestUserEvent(Event event) {
        if (!isUserParticipant(event)) return false;
        
        Date eventDate = parseStartDate(event.getStartDate());
        if (eventDate == null) return false;
        
        Date now = new Date();
        if (eventDate.after(now)) {
            // Проверяем, нет ли более близких мероприятий
            boolean isNearest = true;
            for (Event otherEvent : events) {
                Date otherEventDate = parseStartDate(otherEvent.getStartDate());
                if (otherEvent.getId() != event.getId() &&
                    isUserParticipant(otherEvent) &&
                    otherEventDate != null &&
                    otherEventDate.after(now) &&
                    otherEventDate.before(eventDate)) {
                    
                    isNearest = false;
                    break;
                }
            }
            return isNearest;
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