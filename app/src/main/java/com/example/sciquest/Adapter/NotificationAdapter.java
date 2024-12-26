package com.example.sciquest.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sciquest.Model.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.sciquest.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;
    private OnMarkAllReadListener markAllReadListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private NavController navController;

    public NotificationAdapter(List<Notification> notificationList, OnMarkAllReadListener markAllReadListener, NavController navController) {
        this.notificationList = notificationList;
        this.markAllReadListener = markAllReadListener;
        this.navController = navController;
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

        // Format the timestamp to a readable date string
        if (notification.getTimestamp() != null) {
            Date date = notification.getTimestamp().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedDate = sdf.format(date);
            holder.TVNotificationDate.setText(formattedDate);
        }

        // Set image icon based on title
        switch (notification.getTitle()){
            case "Take a rest !":
                holder.IVNotificationIcon.setImageResource(R.drawable.take_rest);
                break;
            case "Well Done, you passed a quiz with grade A !":
                holder.IVNotificationIcon.setImageResource(R.drawable.grade_a);
                break;
            case "Great news, you've achieved streak of 7 days !":
                holder.IVNotificationIcon.setImageResource(R.drawable.badge);
                break;

        }

        // Highlight unread notifications
        if (!notification.isRead()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryColor)); // Light yellow for unread
            holder.TVNotificationTitle.setTypeface(null, Typeface.BOLD);
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondaryColor)); // Default background for read
            holder.TVNotificationTitle.setTypeface(null, Typeface.NORMAL);
        }

        // Handle click on individual notification to view and to mark as read
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("title_name", notification.getTitle());
            bundle.putString("message",notification.getMessage());

            int imageResource = 0; 
            switch (notification.getTitle()) {
                case "Take a rest !":
                    imageResource = R.drawable.take_rest;
                    break;
                case "Well Done, you passed a quiz with grade A !":
                    imageResource = R.drawable.grade_a;
                    break;
                case "Great news, you've achieved streak of 7 days !":
                    imageResource = R.drawable.badge;
                    break;
            }
            bundle.putInt("image_resource", imageResource);

            // Navigate to the message fragment
            navController.navigate(R.id.DestViewNotification, bundle);
            if (!notification.isRead()) {
                notification.setRead(true);
                notifyItemChanged(position); // Update the UI

                // Update Firestore for this notification
                updateNotificationAsReadInFirestore(notification.getNotificationId());
            }
        });

        // Handle long press for deleting notification
        holder.itemView.setOnLongClickListener(v -> {
            // Show confirmation dialog before deleting
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete this notification?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = holder.getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {  // Check if the item is still valid
                                // Delete notification from Firestore
                                deleteNotificationFromFirestore(notification.getNotificationId());

                                // Remove notification from the list and notify the adapter
                                notificationList.remove(position);
                                notifyItemRemoved(position);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null) // Dismiss the dialog if "No" is clicked
                    .show();


            return true; // Indicating that the long press is handled
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Method to mark all notifications as read
    public void markAllAsRead() {
        for (Notification notification : notificationList) {
            notification.setRead(true);
        }
        notifyDataSetChanged();
        if (markAllReadListener != null) {
            markAllReadListener.onAllRead();
        }
    }

    // Method to update individual notification as read in Firestore
    private void updateNotificationAsReadInFirestore(String notificationId) {
        String userId = user.getUid();
        db.collection("Notification")
                .document(userId)
                .collection("UserNotifications")
                .document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    // Log the successful update if needed
                    Log.d("NotificationAdapter", "Notification marked as read in Firestore.");
                })
                .addOnFailureListener(e -> {
                    // Log the error if the update fails
                    Log.e("NotificationAdapter", "Error updating notification as read in Firestore.", e);
                });
    }

    private void deleteNotificationFromFirestore(String notificationId) {
        String userId = user.getUid();
        db.collection("Notification")
                .document(userId)
                .collection("UserNotifications")
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Log the successful deletion if needed
                    Log.d("NotificationAdapter", "Notification deleted from Firestore.");
                })
                .addOnFailureListener(e -> {
                    // Log the error if the deletion fails
                    Log.e("NotificationAdapter", "Error deleting notification from Firestore.", e);
                });
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

    // Interface for handling "Mark All as Read" action
    public interface OnMarkAllReadListener {
        void onAllRead();
    }
}
