package com.proj.quest.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.proj.quest.R;
import com.proj.quest.models.InviteResponse;

import java.util.List;

public class InviteListAdapter extends ArrayAdapter<InviteResponse> {
    private Context context;
    private List<InviteResponse> invites;
    private OnInviteActionListener listener;

    public interface OnInviteActionListener {
        void onAccept(InviteResponse invite);
        void onDecline(InviteResponse invite);
    }

    public InviteListAdapter(Context context, List<InviteResponse> invites, OnInviteActionListener listener) {
        super(context, 0, invites);
        this.context = context;
        this.invites = invites;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_invite, parent, false);
        }

        InviteResponse invite = getItem(position);
        if (invite != null) {
            TextView tvTeamName = convertView.findViewById(R.id.tvInviteTeam);
            TextView tvCaptainLogin = convertView.findViewById(R.id.tvInviteStatus);
            Button btnAccept = convertView.findViewById(R.id.btnAcceptInvite);
            Button btnDecline = convertView.findViewById(R.id.btnDeclineInvite);

            tvTeamName.setText(invite.getTeamName());
            tvCaptainLogin.setText("От: " + invite.getCaptainLogin());

            btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccept(invite);
                }
            });

            btnDecline.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecline(invite);
                }
            });
        }

        return convertView;
    }
} 