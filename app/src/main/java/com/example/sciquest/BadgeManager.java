package com.example.sciquest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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
        System.out.println(last7Days);

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
                    System.out.println(isConsecutive);

                    if (isConsecutive) {
                        // If the user has achieved a consecutive 7-day streak
                        updateBadgeCount(userId);
                    }

                    listener.onStreakCalculated(isConsecutive);
                });
    }

    private void updateBadgeCount(String userId) {
        DocumentReference badgeRef = db.collection("badge").document(userId);

        badgeRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Fetch last earned badge date
                String lastBadgeEarnedDate = documentSnapshot.getString("lastBadgeEarnedDate");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String today = dateFormat.format(Calendar.getInstance().getTime());

                // If there is no last earned date or if 7 days have passed since the last badge was earned
                if (lastBadgeEarnedDate != null && !lastBadgeEarnedDate.equals(today)) {
                    // Check if exactly 7 days have passed since last earned badge
                    try {
                        long diffInMillis = dateFormat.parse(today).getTime() - dateFormat.parse(lastBadgeEarnedDate).getTime();
                        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);  // Convert milliseconds to days

                        // Only award the badge if 7 days have passed
                        if (diffInDays >= 7) {
                            // Update the badge earned date and increment streak count
                            badgeRef.update("lastBadgeEarnedDate", today);

                            // Optionally, increment streak count
                            long currentStreakCount = documentSnapshot.getLong("streak.streakCount") != null
                                    ? documentSnapshot.getLong("streak.streakCount")
                                    : 0;

                            badgeRef.update("streak.streakCount", currentStreakCount + 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // If no badge document exists, create one and set first badge date
                badgeRef.set(new HashMap<String, Object>() {{
                    put("lastBadgeEarnedDate", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                    put("streak", new HashMap<String, Object>() {{
                        put("streakCount", 1); // First streak
                    }});
                }});
            }
        });
    }

    // Method to fetch badges based on streak count and quiz completion
    public void fetchBadges(String userId, OnBadgesFetchedListener listener) {
        db.collection("badge")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    long streakCount = documentSnapshot.getLong("streak.streakCount") != null
                            ? documentSnapshot.getLong("streak.streakCount")
                            : 0;
//                    long quizCompletedCount = documentSnapshot.getLong("quiz.quizCompletedCount") != null
//                            ? documentSnapshot.getLong("quiz.quizCompletedCount")
//                            : 0;

                    List<String> badges = new ArrayList<>();

                    // Add streak badges
                    for (int i = 1; i <= streakCount; i++) {
                        badges.add("SevenDaysStreak");
                    }

//                    // Add quiz badges based on quiz completed count
//                    for (int i = 1; i <= quizCompletedCount; i++) {
//                        badges.add("QuizCompleted");
//                    }

                    listener.onBadgesFetched(badges);
                })
                .addOnFailureListener(e -> listener.onBadgesFetched(new ArrayList<>()));
    }
}
