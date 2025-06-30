package com.proj.quest.Group;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proj.quest.R;
import com.proj.quest.models.Event;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
        TextView tvEventLocation = view.findViewById(R.id.tvEventLocation);
        TextView tvEventStatus = view.findViewById(R.id.tvEventStatus);
        
        tvEventName.setText(event.getName());
        tvEventLocation.setText(event.getStartLocation() != null ? event.getStartLocation() : "Не указано");
        
        // Форматируем дату и время
        Date eventDate = parseStartDateTime(event.getStartDate(), event.getStartTime());
        if (eventDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            tvEventDate.setText(dateFormat.format(eventDate));
        } else {
            tvEventDate.setText("Не указана");
        }
        
        // Определяем статус мероприятия
        if (currentEventId != null && currentEventId.equals(event.getId())) {
            tvEventStatus.setText("Ближайшее мероприятие");
            tvEventStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            // Выделяем фон и текст
            view.setBackgroundColor(Color.parseColor("#E3F2FD"));
            tvEventName.setTextColor(Color.parseColor("#1976D2"));
        } else {
            Date now = new Date();
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
            // Сброс выделения
            view.setBackgroundColor(Color.WHITE);
            tvEventName.setTextColor(Color.parseColor("#333333"));
        }
        
        return view;
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
} 