package com.proj.quest.Group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.proj.quest.R;
import com.proj.quest.models.InviteResponse;
import java.util.List;

public class InviteAdapter extends BaseAdapter {
    private final Context context;
    private final List<InviteResponse> invites;
    private final InviteActionListener acceptListener;
    private final InviteActionListener declineListener;

    public interface InviteActionListener {
        void onAction(InviteResponse invite);
    }

    public InviteAdapter(Context context, List<InviteResponse> invites, InviteActionListener acceptListener, InviteActionListener declineListener) {
        this.context = context;
        this.invites = invites;
        this.acceptListener = acceptListener;
        this.declineListener = declineListener;
    }

    @Override
    public int getCount() { return invites.size(); }
    @Override
    public Object getItem(int i) { return invites.get(i); }
    @Override
    public long getItemId(int i) { return invites.get(i).getId(); }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_invite, parent, false);
        }
        InviteResponse invite = invites.get(i);
        TextView tvTeam = view.findViewById(R.id.tvInviteTeam);
        TextView tvStatus = view.findViewById(R.id.tvInviteStatus);
        TextView tvCaptain = view.findViewById(R.id.tvInviteCaptain);
        Button btnAccept = view.findViewById(R.id.btnAcceptInvite);
        Button btnDecline = view.findViewById(R.id.btnDeclineInvite);
        
        // Отображаем информацию о команде
        String teamName = invite.getTeamName();
        if (teamName != null && !teamName.isEmpty()) {
            tvTeam.setText("Команда: " + teamName);
        } else {
            tvTeam.setText("Команда: Неизвестно");
        }
        
        // Отображаем статус
        String status = invite.getStatus();
        if (status != null && !status.isEmpty()) {
            if ("pending".equals(status)) {
                tvStatus.setText("Статус: Ожидает ответа");
            } else if ("accepted".equals(status)) {
                tvStatus.setText("Статус: Принято");
            } else if ("declined".equals(status)) {
                tvStatus.setText("Статус: Отклонено");
            } else {
                tvStatus.setText("Статус: " + status);
            }
        } else {
            tvStatus.setText("Статус: Неизвестно");
        }
        
        // Отображаем капитана команды
        String captainLogin = invite.getCaptainLogin();
        if (captainLogin != null && !captainLogin.isEmpty()) {
            tvCaptain.setText("Капитан: " + captainLogin);
        } else {
            tvCaptain.setText("Капитан: Неизвестно");
        }
        
        // Показываем кнопки только для pending приглашений
        if ("pending".equals(status)) {
            btnAccept.setVisibility(View.VISIBLE);
            btnDecline.setVisibility(View.VISIBLE);
            btnAccept.setOnClickListener(v -> acceptListener.onAction(invite));
            btnDecline.setOnClickListener(v -> declineListener.onAction(invite));
        } else {
            btnAccept.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
        }
        return view;
    }
} 