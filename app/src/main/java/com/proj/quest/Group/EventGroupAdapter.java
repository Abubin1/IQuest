package com.proj.quest.Group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proj.quest.R;
import com.proj.quest.models.Event;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventGroupAdapter extends BaseAdapter {
    private final Context context;
    private final List<Event> events;
    private final Integer currentEventId;

    public EventGroupAdapter(Context context, List<Event> events, Integer currentEventId) {
        this.context = context;
        this.events = events;
        this.currentEventId = currentEventId;
    }

    @Override
    public int getCount() { return events.size(); }
    @Override
    public Object getItem(int i) { return events.get(i); }
    @Override
    public long getItemId(int i) { return events.get(i).getId(); }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_event_group, parent, false);
        }
        
        Event event = events.get(i);
        TextView tvEventName = view.findViewById(R.id.tvEventName);
        TextView tvEventDate = view.findViewById(R.id.tvEventDate);
        TextView tvEventTime = view.findViewById(R.id.tvEventTime);
        TextView tvEventLocation = view.findViewById(R.id.tvEventLocation);
        TextView tvEventStatus = view.findViewById(R.id.tvEventStatus);
        
        tvEventName.setText(event.getName());
        tvEventLocation.setText(event.getStartLocation() != null ? event.getStartLocation() : "Не указано");
        tvEventTime.setText(event.getStartTime() != null ? event.getStartTime() : "Не указано");
        
        // Форматируем дату
        if (event.getEventDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            tvEventDate.setText(dateFormat.format(event.getEventDate()));
        } else {
            tvEventDate.setText("Не указана");
        }
        
        // Определяем статус мероприятия
        if (currentEventId != null && currentEventId.equals(event.getId())) {
            tvEventStatus.setText("Текущее мероприятие");
            tvEventStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            Date now = new Date();
            Date eventDate = event.getEventDate();
            if (eventDate != null) {
                long diffInMillis = eventDate.getTime() - now.getTime();
                long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
                
                if (diffInDays < 0) {
                    tvEventStatus.setText("Завершено");
                    tvEventStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                } else if (diffInDays <= 7) {
                    tvEventStatus.setText("Скоро (" + diffInDays + " дн.)");
                    tvEventStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    tvEventStatus.setText("Запланировано");
                    tvEventStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                }
            } else {
                tvEventStatus.setText("Дата не указана");
                tvEventStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        }
        
        return view;
    }
} 