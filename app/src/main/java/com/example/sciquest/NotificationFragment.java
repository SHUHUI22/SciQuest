package com.example.sciquest;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sciquest.Adapter.NotificationAdapter;
import com.example.sciquest.Model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private TextView TVMarkAllAsRead;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewNotification);
        TVMarkAllAsRead = view.findViewById(R.id.TVMarkAllAsRead);

        // Get NavController
        NavController navController = NavHostFragment.findNavController(this);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList,this::markAllNotificationsAsReadInFirestore, navController);
        recyclerView.setAdapter(adapter);

        loadNotifications();

        TVMarkAllAsRead.setOnClickListener(v -> {
            adapter.markAllAsRead();
            markAllNotificationsAsReadInFirestore();
        });


        // Set the action bar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("SciQuest");
        }
    }

    private void markAllNotificationsAsReadInFirestore() {
        String userId = user.getUid();
        CollectionReference notificationRef = db.collection("Notification").document(userId).collection("UserNotifications");

        // Update all notifications' "isRead" field to true
        notificationRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // For each notification, set "isRead" to true
                    notificationRef.document(document.getId())
                            .update("isRead", true)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("NotificationFragment", "Notification marked as read.");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("NotificationFragment", "Error marking notification as read", e);
                            });
                }
            } else {
                Log.e("NotificationFragment", "Error fetching notifications", task.getException());
            }
        });
    }

    private void loadNotifications() {
        String userId = user.getUid();
        CollectionReference notificationRef = db.collection("Notification").document(userId).collection("UserNotifications");
        notificationRef.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                notificationList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Notification notification = document.toObject(Notification.class);
                    notification.setNotificationId(document.getId());
                    notification.setRead(document.getBoolean("isRead"));
                    notificationList.add(notification);
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("NotificationFragment", "Error fetching notifications", task.getException());
            }
        });
    }
}