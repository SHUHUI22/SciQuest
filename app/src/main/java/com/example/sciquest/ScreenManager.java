package com.example.sciquest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ScreenManager {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Context context;
    private long startTime, endTime, screenTimeInMinutes;

    public ScreenManager(Context context){
        this.context = context;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    public Long getStartTime(){
        return this.startTime;
    }

    public void startScreenTimeTracking() {
        startTime = System.currentTimeMillis();
    }

    // Get the current date in yyyy-MM-dd format
    public String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(calendar.getTime());
    }

    // Stop tracking screen time and calculate the total screen time
    public void stopScreenTimeTracking(String date) {
        endTime = System.currentTimeMillis(); // Get end time

        long screenTimeInMillis = (endTime - startTime); // Calculate the time difference (in milliseconds)
        screenTimeInMinutes = (screenTimeInMillis / 60000); // Convert milliseconds to minutes

        // Save the calculated screen time
        saveScreenTime(date, screenTimeInMinutes);

    }

    // Save screen time to Firestore
    public void saveScreenTime(String date, Long screenTimeInMinutes) {
        if (user == null) return;  // User is not logged in

        String userId = user.getUid();

        DocumentReference screenTimeRef = db.collection("ScreenRecord")
                .document(userId)
                .collection("screenTime")
                .document(date);

        // Fetch the existing screen time data (if any)
        screenTimeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // If the document exists, we retrieve the current screen time and update it
                    long currentScreenTime = document.getLong("time") != null ? document.getLong("time") : 0;

                    // Prepare the data to save (increment screen time by the current session's time)
                    Map<String, Object> screenTimeData = new HashMap<>();
                    screenTimeData.put("time", currentScreenTime + screenTimeInMinutes);  // Increment screen time by session time

                    // Save updated data
                    screenTimeRef.set(screenTimeData, SetOptions.merge())  // Merge to preserve other fields
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Status","Screen time saved");// Successfully saved the updated screen time
                            })
                            .addOnFailureListener(e -> {
                                // Failed to save data
                                Toast.makeText(context, "Failed to update screen time", Toast.LENGTH_SHORT).show();
                            });

                } else {
                    // If no previous data exists, create the new document with the initial screen time
                    Map<String, Object> newScreenTime = new HashMap<>();
                    newScreenTime.put("time", screenTimeInMinutes);  // Set screen time for the first time
                    System.out.println("here"+screenTimeInMinutes);
                    System.out.println(date);
                    System.out.println(userId);
                    // Save the new screen time data
                    screenTimeRef.set(newScreenTime)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Status","Screen time saved"); // Successfully saved the screen time
                            })
                            .addOnFailureListener(e -> {
                                // Failed to save data
                                Log.e("Error", "Failed to save screen time: ", e);
                                Toast.makeText(context, "Failed to save screen time", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                // Error occurred while fetching data
                Log.e("Error", "Failed to fetch screen time data", task.getException());
                Toast.makeText(context, "Error fetching screen time data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch the screen time for a given date
    public void getScreenTimeForDate(String userId, String date, OnScreenTimeFetchedListener listener) {

        db.collection("ScreenRecord")
                .document(userId)
                .collection("screenTime")
                .document(date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get screen time from Firestore
                            Long screenTime = document.getLong("time");
                            listener.onScreenTimeFetched(screenTime);  // Call listener with the screen time
                        } else {
                            listener.onScreenTimeFetched(0L);  // Returning 0 if no time is available
                            Log.d("ScreenTime", "No screen time recorded for this date.");
                        }
                    } else {
                        listener.onScreenTimeFetched(0L);  // Returning 0 in case of failure
                        Log.d("ScreenTime", "No data found for the specified date.");
                    }
                });

    }

    // Callback interface for fetching screen time
    public interface OnScreenTimeFetchedListener {
        void onScreenTimeFetched(Long screenTime);
    }

}
