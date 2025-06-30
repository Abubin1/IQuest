package com.proj.quest.Group;

import android.content.Context;
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
        TextView tvEventTime = view.findViewById(R.id.tvEventTime);
        TextView tvEventLocation = view.findViewById(R.id.tvEventLocation);
        TextView tvEventStatus = view.findViewById(R.id.tvEventStatus);
        TextView tvTeamCount = view.findViewById(R.id.tvTeamCount);
        TextView tvOrganizer = view.findViewById(R.id.tvOrganizer);
        
        tvEventName.setText(event.getName());
        tvEventLocation.setText(event.getStartLocation() != null ? event.getStartLocation() : "Не указано");
        tvEventTime.setText(event.getStartTime() != null ? event.getStartTime() : "Не указано");
        
        // Форматируем дату
        Date eventDate = parseStartDate(event.getStartDate());
        if (eventDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            tvEventDate.setText(dateFormat.format(eventDate));
        } else {
            tvEventDate.setText("Не указана");
        }
        
        // Определяем статус мероприятия
        if (currentEventId != null && currentEventId.equals(event.getId())) {
            tvEventStatus.setText("Текущее мероприятие");
            tvEventStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
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
        }
        
        tvTeamCount.setText(String.valueOf(event.getTeamCount()));
        tvOrganizer.setText(event.getOrganizer() != null ? event.getOrganizer() : "-");
        
        return view;
    }

    private Date parseStartDate(String dateString) {
        if (dateString == null) return null;
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return parser.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
} 