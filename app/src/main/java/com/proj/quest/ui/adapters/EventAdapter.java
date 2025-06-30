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
import java.util.stream.Collectors;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SECTION_HEADER = 0;
    private static final int VIEW_TYPE_EVENT_ITEM = 1;

    public static class SectionedItem {
        public int type; // 0 - header, 1 - event
        public String headerText;
        public Event event;
        public SectionedItem(int type, String headerText, Event event) {
            this.type = type;
            this.headerText = headerText;
            this.event = event;
        }
    }

    private List<SectionedItem> items = new java.util.ArrayList<>();
    private List<Team> userTeams;
    private Context context;
    private SharedPrefs sharedPrefs;

    public void setEvents(List<Event> allEvents, Event nearestUserEvent, List<Event> otherEvents, List<Event> finishedEvents) {
        items.clear();
        if (nearestUserEvent != null) {
            items.add(new SectionedItem(VIEW_TYPE_SECTION_HEADER, "Ближайшее с группой", null));
            items.add(new SectionedItem(VIEW_TYPE_EVENT_ITEM, null, nearestUserEvent));
        }
        if (!otherEvents.isEmpty()) {
            items.add(new SectionedItem(VIEW_TYPE_SECTION_HEADER, "Остальные мероприятия", null));
            for (Event e : otherEvents) {
                items.add(new SectionedItem(VIEW_TYPE_EVENT_ITEM, null, e));
            }
        }
        if (!finishedEvents.isEmpty()) {
            items.add(new SectionedItem(VIEW_TYPE_SECTION_HEADER, "Завершенные мероприятия", null));
            for (Event e : finishedEvents) {
                items.add(new SectionedItem(VIEW_TYPE_EVENT_ITEM, null, e));
            }
        }
        notifyDataSetChanged();
    }

    public void setUserTeams(List<Team> userTeams) {
        this.userTeams = userTeams;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        sharedPrefs = new SharedPrefs(context);
        if (viewType == VIEW_TYPE_SECTION_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_section_header, parent, false);
            return new SectionHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SectionedItem item = items.get(position);
        if (item.type == VIEW_TYPE_SECTION_HEADER) {
            ((SectionHeaderViewHolder) holder).tvSectionHeader.setText(item.headerText);
        } else if (item.type == VIEW_TYPE_EVENT_ITEM) {
            Event event = item.event;
            EventViewHolder eventHolder = (EventViewHolder) holder;
            boolean isNearestUserEvent = isNearestUserEvent(event);
            eventHolder.tvEventName.setText(event.getName() != null ? event.getName() : "Без названия");
            eventHolder.tvOrganizer.setText("Организатор: " + (event.getOrganizer() != null ? event.getOrganizer() : "Не указан"));
            Date eventDate = parseStartDateTime(event.getStartDate(), event.getStartTime());
            if (eventDate != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                eventHolder.tvEventDate.setText("Дата: " + displayFormat.format(eventDate));
            } else {
                eventHolder.tvEventDate.setText("Дата: неверный формат");
            }
            eventHolder.tvTeamCount.setText("Зарегистрированные команды: " + event.getTeamCount());
            eventHolder.tvMaxMembers.setText("Макс. участников: " + event.getMaxTeamMembers());
            if (isNearestUserEvent) {
                eventHolder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                eventHolder.tvEventName.setTextColor(Color.parseColor("#1976D2"));
                eventHolder.tvNearestEvent.setVisibility(View.VISIBLE);
                eventHolder.tvNearestEvent.setText("⭐ Ваше ближайшее мероприятие");
            } else {
                eventHolder.cardView.setCardBackgroundColor(Color.WHITE);
                eventHolder.tvEventName.setTextColor(Color.BLACK);
                eventHolder.tvNearestEvent.setVisibility(View.GONE);
            }
            eventHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("event", event);
                context.startActivity(intent);
            });
        }
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

    private Date parseStartDateTime(String date, String time) {
        if (date == null || time == null) return null;
        String dateTime = date + "T" + time + ".000Z";
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        parser.setTimeZone(TimeZone.getDefault());
        try {
            return parser.parse(dateTime);
        } catch (ParseException e) {
            return null;
        }
    }

    private boolean isNearestUserEvent(Event event) {
        if (!isUserParticipant(event)) return false;
        
        Date eventDate = parseStartDateTime(event.getStartDate(), event.getStartTime());
        if (eventDate == null) return false;
        
        Date now = new Date();
        if (eventDate.after(now)) {
            // Проверяем, нет ли более близких мероприятий
            boolean isNearest = true;
            for (Event otherEvent : items.stream()
                    .filter(item -> item.type == VIEW_TYPE_EVENT_ITEM)
                    .map(item -> item.event)
                    .collect(Collectors.toList())) {
                Date otherEventDate = parseStartDateTime(otherEvent.getStartDate(), otherEvent.getStartTime());
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
        return items.size();
    }

    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionHeader;
        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionHeader = itemView.findViewById(R.id.tvSectionHeader);
        }
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