package com.example.sciquest.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sciquest.Model.Notification;

import java.util.List;
import com.example.sciquest.R;
import com.google.android.material.imageview.ShapeableImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.TVNotificationTitle.setText(notification.getTitle());
        holder.TVNotificationMessage.setText(notification.getMessage());
        holder.TVNotificationDate.setText(notification.getTimestamp());
        holder.IVNotificationIcon.setImageResource(notification.getImageResID());

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView TVNotificationTitle;
        TextView TVNotificationMessage;
        TextView TVNotificationDate;
        ShapeableImageView IVNotificationIcon;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            TVNotificationTitle = itemView.findViewById(R.id.TVNotificationTitle);
            TVNotificationMessage = itemView.findViewById(R.id.TVNotificationMessage);
            TVNotificationDate = itemView.findViewById(R.id.TVNotificationDate);
            IVNotificationIcon = itemView.findViewById(R.id.IVNotificationIcon);
        }
    }
}
