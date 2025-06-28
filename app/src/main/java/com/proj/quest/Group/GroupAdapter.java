package com.proj.quest.Group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.proj.quest.R;
import com.proj.quest.models.User;
import java.util.List;

public class GroupAdapter extends BaseAdapter {
    private final Context context;
    private final List<User> members;
    private final int captainId;
    private final int userId;
    private final KickListener kickListener;

    public interface KickListener {
        void onKick(User user);
    }

    public GroupAdapter(Context context, List<User> members, int captainId, int userId, KickListener kickListener) {
        this.context = context;
        this.members = members;
        this.captainId = captainId;
        this.userId = userId;
        this.kickListener = kickListener;
    }

    @Override
    public int getCount() { return members.size(); }
    @Override
    public Object getItem(int i) { return members.get(i); }
    @Override
    public long getItemId(int i) { return members.get(i).getId(); }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_participant, parent, false);
        }
        User user = members.get(i);
        TextView tvName = view.findViewById(R.id.tvParticipantName);
        TextView tvScore = view.findViewById(R.id.tvParticipantScore);
        ImageView ivAvatar = view.findViewById(R.id.ivParticipantAvatar);
        Button btnKick = view.findViewById(R.id.btnKick);
        
        tvName.setText(user.getLogin());
        tvScore.setText("Очки: " + user.getScore());
        
        // Загружаем аватар
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                .load(user.getAvatarUrl().startsWith("http") ? user.getAvatarUrl() : "http://5.175.92.194:3000" + user.getAvatarUrl())
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.profile);
        }
        
        if (captainId == userId && user.getId() != captainId) {
            btnKick.setVisibility(View.VISIBLE);
            btnKick.setOnClickListener(v -> kickListener.onKick(user));
        } else {
            btnKick.setVisibility(View.GONE);
        }
        return view;
    }
}
