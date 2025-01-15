package com.example.sciquest;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BadgeManager {

    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    // Listener for streak calculation
    public interface OnStreakCalculatedListener {
        void onStreakCalculated(boolean isConsecutive);
    }

    public interface OnBadgesFetchedListener {
        void onBadgesFetched(List<String> badges);
    }

    // Method to fetch and update badges for the user
    public void updateBadges(String userId, OnStreakCalculatedListener listener) {
        checkStreak(userId, new OnStreakCalculatedListener() {
            @Override
            public void onStreakCalculated(boolean isConsecutive) {
                if (isConsecutive){
                    updateBadgeCount(userId);
                }
                listener.onStreakCalculated(isConsecutive);
            }
        });
    }

    private void checkStreak(String userId, OnStreakCalculatedListener listener) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        List<String> last7Days = new ArrayList<>();

        // Generate last 7 days
        for (int i = 0; i < 7; i++) {
            last7Days.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        // Fetch user's screen time data for the last 7 days
        db.collection("ScreenRecord")
                .document(userId)
                .collection("screenTime")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> activeDays = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        activeDays.add(document.getId());
                    }

                    // Check if user has used the app for 7 consecutive days
                    boolean isConsecutive = true;
                    for (String date : last7Days) {
                        if (!activeDays.contains(date)) {
                            isConsecutive = false;
                            break;
                        }
                    }
                    listener.onStreakCalculated(isConsecutive);
                });
    }

    private void updateBadgeCount(String userId) {
        DocumentReference badgeRef = db.collection("Badge").document(userId);

        badgeRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Fetch last earned badge date
                String last7streakBadgeEarnedDate = documentSnapshot.getString("last7streakBadgeEarnedDate");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String today = dateFormat.format(Calendar.getInstance().getTime());

                // If there is no last earned date or if 7 days have passed since the last badge was earned
                if (last7streakBadgeEarnedDate != null && !last7streakBadgeEarnedDate.equals(today)) {
                    // Check if exactly 7 days have passed since last earned badge
                    try {
                        long diffInMillis = dateFormat.parse(today).getTime() - dateFormat.parse(last7streakBadgeEarnedDate).getTime();
                        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);  // Convert milliseconds to days

                        // Only award the badge if 7 days have passed
                        if (diffInDays >= 7) {
                            // Update the badge earned date and increment streak count
                            badgeRef.update("last7streakBadgeEarnedDate", today);

                            //increment streak count
                            long current7StreakCount = documentSnapshot.getLong("7streakCount") != null
                                    ? documentSnapshot.getLong("7streakCount")
                                    : 0;

                            badgeRef.update("7streakCount", current7StreakCount + 1);
                            addNotification();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // If badge document exists, no first badge date earned
                if (last7streakBadgeEarnedDate==null)  {
                    badgeRef.set(new HashMap<String, Object>() {{
                        put("last7streakBadgeEarnedDate", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                        put("7streakCount", 1); // First streak
                    }});
                    addNotification();
                }
            } else {
                // If no badge document exists, create one and set first badge date
                badgeRef.set(new HashMap<String, Object>() {{
                    put("last7streakBadgeEarnedDate", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                    put("7streakCount", 1); // First streak
                }});
                addNotification();
            }
        });
    }

    // Method to fetch badges based on streak count and quiz completion
    public void fetchBadges(String userId, OnBadgesFetchedListener listener) {
        db.collection("Badge")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    long streakCount = documentSnapshot.getLong("7streakCount") != null
                            ? documentSnapshot.getLong("7streakCount")
                            : 0;
                    long quizGradeACount = documentSnapshot.getLong("quizGradeACount") != null
                            ? documentSnapshot.getLong("quizGradeACount")
                            : 0;

                    List<String> badges = new ArrayList<>();

                    // Add streak badges
                    for (int i = 1; i <= streakCount; i++) {
                        badges.add("SevenDaysStreak");
                    }

                    // Add quiz badges based on quiz GradeA completed count
                    for (int i = 1; i <= quizGradeACount; i++) {
                        badges.add("QuizGradeA");
                    }

                    listener.onBadgesFetched(badges);
                })
                .addOnFailureListener(e -> listener.onBadgesFetched(new ArrayList<>()));
    }

    private void addNotification() {
        String userId = user.getUid();

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", "Great news, you've achieved streak of 7 days !");
        notificationData.put("message", "You've maintained a 7-day streak, keep it up! A badge of seven-days streak is unlocked.");
        notificationData.put("isRead", false); // Default to unread
        notificationData.put("timestamp", FieldValue.serverTimestamp()); // Set timestamp

        DocumentReference notificationRef = db.collection("Notification").document(userId);

        notificationRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // User's Notification document exists, add a new notification
                    notificationRef.collection("UserNotifications")
                            .add(notificationData)
                            .addOnSuccessListener(docRef -> {
                                Log.d("Notification", "Notification added successfully: " + docRef.getId());
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Notification", "Error adding notification", e);
                            });
                } else {
                    // Create user's Notification document if it doesn't exist, then add notification
                    notificationRef.set(new HashMap<>()) // Initialize empty user document
                            .addOnSuccessListener(aVoid -> {
                                notificationRef.collection("UserNotifications")
                                        .add(notificationData)
                                        .addOnSuccessListener(docRef -> {
                                            Log.d("Notification", "Notification added successfully after initializing: " + docRef.getId());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Notification", "Error adding notification", e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Notification", "Error creating user notification document", e);
                            });
                }
            } else {
                Log.e("Notification", "Failed to fetch user notification document", task.getException());
            }
        });
    }
}
