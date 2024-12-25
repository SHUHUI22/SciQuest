package com.example.sciquest;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizFragment extends Fragment {

    private ImageButton BtnBack;
    private Button BtnOpt1, BtnOpt2, BtnOpt3, BtnOpt4, BtnNext;
    private TextView TVQueNum, TVQuestion, TVTimer;
    private ProgressBar PBTimer;
    private CountDownTimer Timer;
    private CardView cardViewQuiz;
    GradientDrawable border;
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private List<Map<String, Object>> questions;
    private int currentQueIndex = 0, secondsRemaining=25;
    Map<String, Object> currentQuestion;
    String topic, correctAnswer;
    private boolean[] questionPass, isCorrect;
    private String[] userAnswers;
    private int score=0, correctAnswersCount=0, wrongAnswersCount=0, unansweredCount=0;
    private boolean firstTimeAttempt;

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TVQueNum = view.findViewById(R.id.TVQueNum);
        PBTimer = view.findViewById(R.id.PBTimer);
        TVTimer = view.findViewById(R.id.TVTimer);
        TVQuestion = view.findViewById(R.id.TVQuestion);
        BtnOpt1 = view.findViewById(R.id.BtnOpt1);
        BtnOpt2 = view.findViewById(R.id.BtnOpt2);
        BtnOpt3 = view.findViewById(R.id.BtnOpt3);
        BtnOpt4 = view.findViewById(R.id.BtnOpt4);
        BtnNext = view.findViewById(R.id.BtnNext);
        cardViewQuiz = view.findViewById(R.id.cardViewQuiz);
        border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(16);   // Set rounded corners
        
        // Get quiz data from Firestore
        Bundle bundle = getArguments();
        if (bundle!=null){
            topic = bundle.getString("topic_name").replace(" ","");
            // Fetch questions from Firestore
            fetchQuestion(topic);
        }

        // Obtain NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.NHFMain);

        // To next question
        BtnNext.setOnClickListener(v -> {
            if (currentQueIndex < questions.size() - 1) {
                currentQueIndex++;
                updateQuestionView();
            } else {
                // Save the user answer to Firestore
                saveQuizAttempt();

                // award badge when attempt score is >= 80
                if(score>=80){
                    addNotification(topic,score);
                    awardBadge();
                }

                // Navigate to result page
                ResultFragment resultFragment = new ResultFragment();
                Bundle resultBundle = new Bundle();
                resultBundle.putString("topic_name",topic);
                resultBundle.putInt("score", score);
                resultBundle.putInt("correct_answers_count", correctAnswersCount);
                resultBundle.putInt("wrong_answers_count", wrongAnswersCount);
                resultBundle.putInt("unanswered_count", unansweredCount);
                resultFragment.setArguments(resultBundle);

                navController.navigate(R.id.DestResult, resultBundle);
            }
        });

        // Hide the toolbar
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
            }
        }

        // Navigate back
        BtnBack = view.findViewById(R.id.BtnBack);
        BtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack(R.id.DestQuizList, true);
                navController.navigate(R.id.DestQuizList);
            }
        });

        // Handle the back press using OnBackPressedDispatcher
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate directly to DestQuizList when back button is pressed
                navController.popBackStack(R.id.DestQuizList, true);
                navController.navigate(R.id.DestQuizList);
            }
        });
    }

    private void fetchQuestion(String topic) {
        db.collection("QuestionBank")
                .document(topic)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            questions = (List<Map<String, Object>>) documentSnapshot.get("questions");
                            questionPass = new boolean[questions.size()];
                            userAnswers = new String[questions.size()];
                            isCorrect = new boolean[questions.size()];

                            // update view
                            updateQuestionView();
                        }
                        else {
                            Toast.makeText(requireContext(), "Topic does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(requireContext(), "Error fetching quiz data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateQuestionView() {
        if (isAdded()) {
            currentQuestion = questions.get(currentQueIndex);
            String question = (String) currentQuestion.get("question");
            List<String> options = (List<String>) currentQuestion.get("options");
            correctAnswer = (String) currentQuestion.get("correctAnswer");

            // Shuffle the options
            Collections.shuffle(options);

            // Set the question number
            int QueNum = currentQueIndex + 1;
            TVQueNum.setText("Q" + QueNum);

            // Set the question text
            TVQuestion.setText(question);

            // Set the options
            BtnOpt1.setText(options.get(0));
            BtnOpt2.setText(options.get(1));
            BtnOpt3.setText(options.get(2));
            BtnOpt4.setText(options.get(3));

            BtnNext.setVisibility(View.INVISIBLE);
            border.setStroke(8, getResources().getColor(R.color.white));

            // Reset buttons
            resetButtons();

            // Start timer
            startTimer();

            // Option buttons click listeners
            BtnOpt1.setOnClickListener(v -> checkAnswer(BtnOpt1));
            BtnOpt2.setOnClickListener(v -> checkAnswer(BtnOpt2));
            BtnOpt3.setOnClickListener(v -> checkAnswer(BtnOpt3));
            BtnOpt4.setOnClickListener(v -> checkAnswer(BtnOpt4));
        }
    }

    private void startTimer() {
        PBTimer.setMax(25); // Set max value
        PBTimer.setProgress(25); // Initialize progress
        TVTimer.setText(secondsRemaining + "s");

        Timer = new CountDownTimer(26000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secondsRemaining = (int) (millisUntilFinished / 1000);
                PBTimer.setProgress(secondsRemaining);
                TVTimer.setText(secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                PBTimer.setProgress(0);
                TVTimer.setText("0s");
                if (!questionPass[currentQueIndex]) {
                    highlightCorrectAnswer();
                    unansweredCount++;
                    isCorrect[currentQueIndex]=false;
                    BtnNext.setVisibility(View.VISIBLE); // Show "Next" button
                    setCardViewBorderColor(cardViewQuiz, false); // Set border to red
                }
            }
        };
        Timer.start();
    }

    private void highlightCorrectAnswer() {
        if (isAdded()) {
            if (BtnOpt1.getText().toString().equals(correctAnswer)) {
                BtnOpt1.setBackgroundColor(getResources().getColor(R.color.green));
            }
            if (BtnOpt2.getText().toString().equals(correctAnswer)) {
                BtnOpt2.setBackgroundColor(getResources().getColor(R.color.green));
            }
            if (BtnOpt3.getText().toString().equals(correctAnswer)) {
                BtnOpt3.setBackgroundColor(getResources().getColor(R.color.green));
            }
            if (BtnOpt4.getText().toString().equals(correctAnswer)) {
                BtnOpt4.setBackgroundColor(getResources().getColor(R.color.green));
            }
        }
    }

    private void resetButtons() {
        // Reset all options to default color
        BtnOpt1.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        BtnOpt2.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        BtnOpt3.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        BtnOpt4.setBackgroundColor(getResources().getColor(R.color.primaryColor));
    }

    private void checkAnswer(Button selectedButton) {
        if (questionPass[currentQueIndex]){
            return;
        }

         // Get the option text
        String selectedOption = selectedButton.getText().toString();
        boolean correct = selectedOption.equals(correctAnswer);

        // Check is correct or not
        if(correct){
            selectedButton.setBackgroundColor(getResources().getColor(R.color.green)); // Correct answer
            score+=10;
            correctAnswersCount++;
            setCardViewBorderColor(cardViewQuiz, true); // Set border to green
        }
        else {
            selectedButton.setBackgroundColor(getResources().getColor(R.color.red)); // Incorrect answer
            highlightCorrectAnswer();
            wrongAnswersCount++;
            setCardViewBorderColor(cardViewQuiz, false); // Set border to red
        }

        // Mark the question as answered
        questionPass[currentQueIndex] = true;
        userAnswers[currentQueIndex] = selectedOption;
        isCorrect[currentQueIndex] = correct;

        // Stop the timer
        if (Timer != null) {
            Timer.cancel();
            PBTimer.setProgress(secondsRemaining);
        }

        // Make the "Next" button visible
        BtnNext.setVisibility(View.VISIBLE);

    }

    // This method will set the border color dynamically based on answer status
    private void setCardViewBorderColor(CardView cardViewQuiz, boolean isCorrect) {

        if (isAdded()) {  // Check if the fragment is attached
            // Set border color based on the answer
            if (isCorrect) {
                border.setStroke(8, getResources().getColor(R.color.green)); // Green border for correct answer
            } else {
                border.setStroke(8, getResources().getColor(R.color.red)); // Red border for incorrect answer
            }

            cardViewQuiz.setBackground(border);
        }
    }

    private void saveQuizAttempt() {
        String userId = user.getUid();
        DocumentReference attemptRef = db.collection("QuizAttempt")
                .document(userId)
                .collection("Topic")
                .document(topic);

        // Fetch the existing data (if any) for this topic
        attemptRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // If document exists, we retrieve the current attempt count and increment it
                    long currentAttemptCount = document.getLong("attemptCount") != null ? document.getLong("attemptCount") : 0;

                    // Prepare the data to save
                    Map<String, Object> quizAttempt = new HashMap<>();
                    quizAttempt.put("attemptCount", currentAttemptCount + 1);  // Increment attempt count
                    quizAttempt.put("score", score);  // Store score
                    quizAttempt.put("correctAnswersCount",correctAnswersCount);
                    quizAttempt.put("wrongAnswersCount",wrongAnswersCount);
                    quizAttempt.put("unansweredCount", unansweredCount);
                    quizAttempt.put("answers", getAnswerMap());  // Store answers as an array of maps

                    // Save updated data
                    attemptRef.set(quizAttempt, SetOptions.merge())  // Merge to preserve other fields
                            .addOnSuccessListener(aVoid -> {
                                // Successfully saved the quiz results
                                Toast.makeText(requireContext(), "Quiz data saved!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Failed to save data
                                Toast.makeText(requireContext(), "Failed to save quiz data", Toast.LENGTH_SHORT).show();
                            });

                }
                else {
                    // If no previous data exists, create the new document
                    Map<String, Object> newQuizAttempt = new HashMap<>();
                    newQuizAttempt.put("attemptCount", 1);  // First attempt
                    newQuizAttempt.put("score",score);
                    newQuizAttempt.put("correctAnswersCount",correctAnswersCount);
                    newQuizAttempt.put("wrongAnswersCount",wrongAnswersCount);
                    newQuizAttempt.put("unansweredCount", unansweredCount);
                    newQuizAttempt.put("answers", getAnswerMap());

                    // Save the data as a new document
                    attemptRef.set(newQuizAttempt)
                            .addOnSuccessListener(aVoid -> {
                                // Successfully saved the quiz results
                                Toast.makeText(requireContext(), "Quiz data saved!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Failed to save data
                                Toast.makeText(requireContext(), "Failed to save quiz data", Toast.LENGTH_SHORT).show();
                            });
                }
            }
            else {
                // Error occurred while fetching data
                Toast.makeText(requireContext(), "Error fetching quiz data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Map<String, Object>> getAnswerMap() {
        List<Map<String, Object>> answerList = new ArrayList<>();

        // Loop through the answers and store them in a map
        for (int i = 0; i < userAnswers.length; i++) {
            Map<String, Object> answerData = new HashMap<>();
            answerData.put("selectedOption", userAnswers[i]);
            answerData.put("isCorrect", isCorrect[i]);  // Compare to the correct answer
            answerList.add(answerData);
        }

        return answerList;
    }

    private void awardBadge() {
        String userId = user.getUid();
        DocumentReference badgeRef = db.collection("Badge").document(userId);

        badgeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    long currentQuizGradeACount = document.getLong("quizGradeACount")!= null ? document.getLong("quizGradeACount") : 0;
                    // If the badge document exists, increment the quizGradeACountCount
                    badgeRef.update("quizGradeACount", (currentQuizGradeACount+1))
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Quiz Grade A count", "Quiz Grade A count updated");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to update quiz Grade A count.", Toast.LENGTH_SHORT).show();
                                Log.d("Quiz completed count", "Failed to update quiz Grade A count");
                            });
                } else {
                    // If no badge document exists, create one and set quizGradeACount to 1
                    badgeRef.set(new HashMap<String, Object>() {{
                                put("quizGradeACount", 1); // First streak
                            }})
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Badge", "Badge document created with quizGradeACount set to 1");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to create badge document.", Toast.LENGTH_SHORT).show();
                                Log.d("Badge", "Failed to create badge document");
                            });
                }
            } else {
                Log.d("Award Badge", "Failed to retrieve badge document", task.getException());
            }
        });
    }

    private void addNotification(String title, long score) {
        String userId = user.getUid();

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", "Well Done, you passed a quiz with grade A !");
        notificationData.put("message", "Congratulations! You've passed a quiz of the topic \""+ topic +"\" with "+score +" marks. You've unlocked a Genious badge.");
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

    @Override
    public void onStop() {
        super.onStop();

        // Show the toolbar when navigating back
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
            }
        }
    }
}