package com.example.sciquest;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ScreenTimeAndBadgeFragment extends Fragment {

    private CalendarView calendarView;
    private TextView TVDate, TVDailyTime, TVScreenDescription2;
    private ImageView IMCalendar, IVScreen;
    private LinearLayout LinearLayoutBadge;
    private ScreenManager screenManager =  new ScreenManager(getContext());;
    private BadgeManager badgeManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private Long weeklyScreenTimeFetched, startTime, totalDailyScreenTime;
    private boolean update = true;

    public ScreenTimeAndBadgeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_screen_time_and_badge, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        badgeManager = new BadgeManager();

        TVDate = view.findViewById(R.id.TVDate);
        TVDailyTime = view.findViewById(R.id.TVDailyTime);
        TVScreenDescription2 = view.findViewById(R.id.TVScreenDescription2);
        IMCalendar = view.findViewById(R.id.IMCalendar);
        IVScreen = view.findViewById(R.id.IVScreen);
        calendarView = view.findViewById(R.id.calendarView);
        LinearLayoutBadge = view.findViewById(R.id.LinearLayoutBadge);

        calendarView.setVisibility(View.GONE);

        // Retrieve startTime
        Bundle bundle = getArguments();
        startTime = bundle.getLong("startTime");

        // Get the current date in yyyy-MM-dd format
        String currentDate = screenManager.getCurrentDate();
        TVDate.setText(currentDate);

        String userId = user.getUid();

        // Default: Calculate daily screen time
        calculateDailyScreenTime(userId,currentDate,startTime);

        // Fetch weekly screen time
        calculateWeeklyScreenTime(userId, new OnWeeklyScreenTimeListener() {
            @Override
            public void onWeeklyScreenTimeCalculated(long weeklyScreenTime) {
                weeklyScreenTimeFetched = weeklyScreenTime;
            }
        });

        // Toggle calendar icon to show weekly screen time
        IMCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle CalendarView visibility
                if (calendarView.getVisibility() == View.GONE) {
                    IVScreen.setVisibility(View.GONE);
                    calendarView.setVisibility(View.VISIBLE);
                    TVScreenDescription2.setText("this week.");
                    TVDate.setText("Weekly Report");

                    Long currentTime = System.currentTimeMillis();
                    Long currentScreenTimeSession = (currentTime - startTime)/60000;
                    Long totalWeeklyScreenTime = currentScreenTimeSession + weeklyScreenTimeFetched;
                    TVDailyTime.setText( totalWeeklyScreenTime.toString() + " minutes");

                    // Select specific day from calendar
                    calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                        @Override
                        public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                            // Format the selected date as yyyy-MM-dd
                            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                            TVDate.setText(selectedDate); // Update the displayed date
                            TVScreenDescription2.setText("on "+selectedDate);
                            screenManager.getScreenTimeForDate(userId, selectedDate, new ScreenManager.OnScreenTimeFetchedListener() {
                                @Override
                                public void onScreenTimeFetched(Long screenTime) {
                                    if (selectedDate.equals(currentDate)){
                                        calculateDailyScreenTime(userId,currentDate,startTime);
                                        TVScreenDescription2.setText("reading today.");
                                    }
                                    TVDailyTime.setText(screenTime.toString() + " minutes");
                                }
                            });
                        }
                    });

                } else {
                    calendarView.setVisibility(View.GONE);
                    IVScreen.setVisibility(View.VISIBLE);
                    TVDate.setText(currentDate);
                    TVScreenDescription2.setText("reading today.");
                    calculateDailyScreenTime(userId,currentDate,startTime); // Calculate daily screen time
                }
            }
        });

        // Badge
        badgeManager.updateBadges(userId, new BadgeManager.OnStreakCalculatedListener() {
            @Override
            public void onStreakCalculated(boolean isConsecutive) {
                // You can pass additional logic here if necessary, for example:
                if (isConsecutive) {
                    // Call the method to fetch badges
                    badgeManager.fetchBadges(userId, new BadgeManager.OnBadgesFetchedListener() {
                        @Override
                        public void onBadgesFetched(List<String> badges) {
                            displayBadges(badges);
                        }
                    });
                }
            }
        });

        // Set the action bar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("SciQuest");
        }
    }

    private void calculateDailyScreenTime(String userId, String currentDate, long startTime) {
        screenManager.getScreenTimeForDate(userId, currentDate,new ScreenManager.OnScreenTimeFetchedListener() {
            @Override
            public void onScreenTimeFetched(Long screenTime) {
               // Fetch time spend in previous session in the same day and add with time spend in current session
                Long currentTime = System.currentTimeMillis();
                Long currentSessionScreenTime = (currentTime - startTime)/60000;
                totalDailyScreenTime = screenTime + currentSessionScreenTime;
                TVDailyTime.setText(totalDailyScreenTime.toString() + " minutes");
                addNotification(totalDailyScreenTime, update);
                update = false;
            }
        });
    }

    private void calculateWeeklyScreenTime(String userId,OnWeeklyScreenTimeListener listener) {
        List<String> last7Days = getLast7DaysDates(); // Helper method to get last 7 days
        AtomicLong totalScreenTime = new AtomicLong(0);

        // Fetch screen time for each day in the last 7 days
        for (String date : last7Days) {
            screenManager.getScreenTimeForDate(userId, date, new ScreenManager.OnScreenTimeFetchedListener() {
                @Override
                public void onScreenTimeFetched(Long screenTime) {
                    totalScreenTime.addAndGet(screenTime);

                    // Check if all 7 days have been processed
                    if (date.equals(last7Days.get(last7Days.size() - 1))) {
                        listener.onWeeklyScreenTimeCalculated(totalScreenTime.get());
                    }
                }
            });
        }
    }

    private List<String> getLast7DaysDates() {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        return dates;
    }

    public interface OnWeeklyScreenTimeListener {
        void onWeeklyScreenTimeCalculated(long weeklyScreenTime);
    }

    private void displayBadges(List<String> badges) {
        // Clear the previous badges
        LinearLayoutBadge.removeAllViews();

        // Iterate over the list of badges and add them to the layout
        for (String badge : badges) {
            ImageView badgeImage = new ImageView(getContext());

            // Set image resource based on badge type
            switch (badge) {
                case "SevenDaysStreak":
                    badgeImage.setImageResource(R.drawable.seven_days_badge);
                    break;
                case "QuizGradeA":
                    badgeImage.setImageResource(R.drawable.genious_badge);
                    break;
            }
            // Add the badge to the LinearLayout
            LinearLayoutBadge.addView(badgeImage);
        }
    }

    private void addNotification(Long totalDailyScreenTime, boolean update) {
        if (totalDailyScreenTime>=120 && update == true){
            String userId = user.getUid();

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("title", "Take a rest !");
            notificationData.put("message", "You've studied for 2 hours! Remember to drink water. Rest is not a waste of time. It's necessary! ");
            notificationData.put("isRead", false); // Default to unread
            notificationData.put("timestamp", FieldValue.serverTimestamp()); // Set timestamp

            DocumentReference notificationRef = db.collection("Notification").document(userId);

            notificationRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
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
}