package com.example.sciquest;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class ReviewFragment extends Fragment {

    private ImageButton BtnBack;
    private TextView TVQuestion, TVExplanation;
    private Button BtnOpt1, BtnOpt2, BtnOpt3, BtnOpt4, BtnPrev, BtnNext;
    private CardView cardViewReview;
    GradientDrawable border;
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    String topic;
    private List<Map<String, Object>> questions, userAnswers;
    int currentQuestionIndex = 0;
    Map<String, Object> currentQuestion, currentUserAnswer;

    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TVQuestion = view.findViewById(R.id.TVQuestion);
        BtnOpt1 = view.findViewById(R.id.BtnOpt1);
        BtnOpt2 = view.findViewById(R.id.BtnOpt2);
        BtnOpt3 = view.findViewById(R.id.BtnOpt3);
        BtnOpt4 = view.findViewById(R.id.BtnOpt4);
        TVExplanation = view.findViewById(R.id.TVExplanation);
        BtnPrev = view.findViewById(R.id.BtnPrev);
        BtnNext = view.findViewById(R.id.BtnNext);
        BtnBack = view.findViewById(R.id.BtnBack);
        cardViewReview = view.findViewById(R.id.cardViewReview);
        border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(16);   // Set rounded corners

        Bundle bundle = getArguments();
        if (bundle!=null){
            topic = bundle.getString("topic_name").replace(" ","");
            String userId = user.getUid();

            // Fetch quiz attempt data
            db.collection("QuizAttempt")
                    .document(userId)
                    .collection("Topic")
                    .document(topic)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()){
                            // Retrieve attempt data
                            userAnswers = (List<Map<String, Object>>) documentSnapshot.get("answers");

                            // Fetch question bank
                            db.collection("QuestionBank").document(topic)
                                    .get()
                                    .addOnSuccessListener(questionSnapshot ->{
                                        if (questionSnapshot.exists()) {
                                            questions = (List<Map<String, Object>>) questionSnapshot.get("questions");

                                            displayReview();
                                        }
                                    });
                        }
                    });
        }

        // Hide the toolbar
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
            }
        }

        // Obtain NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.NHFMain);

        // Navigate back
        BtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getActivity().getSupportFragmentManager().popBackStack();
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

    private void displayReview() {

        // Display the first question
        updateUI();

        BtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    updateUI();
                }
                else {
                    Toast.makeText(requireContext(), "You're at the last question.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        BtnPrev.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                updateUI();
            }
            else {
                Toast.makeText(requireContext(), "You're at the fist question.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        // From QuestionBank
        currentQuestion = questions.get(currentQuestionIndex);
        String question = (String) currentQuestion.get("question");
        List<String> options = (List<String>) currentQuestion.get("options");
        String correctAnswer = (String) currentQuestion.get("correctAnswer");
        String explanation = (String) currentQuestion.get("explanation");

        // From QuizAttempt
        currentUserAnswer = userAnswers.get(currentQuestionIndex);
        String selectedOption = (String) currentUserAnswer.get("selectedOption");
        boolean isCorrect = (boolean) currentUserAnswer.get("isCorrect");

        // Calculate question number
        int QueNum = currentQuestionIndex + 1;

        // Set question text
        TVQuestion.setText("Q"+QueNum+": "+question);

        // Set the options
        BtnOpt1.setText(options.get(0));
        BtnOpt2.setText(options.get(1));
        BtnOpt3.setText(options.get(2));
        BtnOpt4.setText(options.get(3));

        border.setStroke(8, getResources().getColor(R.color.white));
        cardViewReview.setBackground(border);

        // Reset buttons
        resetButtons();

        // Highlight the selected option
        highlightSelectedOption(selectedOption, correctAnswer,isCorrect);

        // Set the explanation
        TVExplanation.setText("Explanation: "+explanation);

    }

    // Highlight user's selected option
    private void highlightSelectedOption(String selectedOption, String correctAnswer,boolean isCorrect) {
        if (!isCorrect&&selectedOption!=null) {
            border.setStroke(8, getResources().getColor(R.color.red));
            if (BtnOpt1.getText().toString().equals(selectedOption)) {
                BtnOpt1.setBackgroundColor(getResources().getColor(R.color.red));
            }
            if (BtnOpt2.getText().toString().equals(selectedOption)) {
                BtnOpt2.setBackgroundColor(getResources().getColor(R.color.red));
            }
            if (BtnOpt3.getText().toString().equals(selectedOption)) {
                BtnOpt3.setBackgroundColor(getResources().getColor(R.color.red));
            }
            if (BtnOpt4.getText().toString().equals(selectedOption)) {
                BtnOpt4.setBackgroundColor(getResources().getColor(R.color.red));
            }
        }
        else if (selectedOption==null){
            border.setStroke(8, getResources().getColor(R.color.red));
        }
        else {
            border.setStroke(8, getResources().getColor(R.color.green));
        }
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
        cardViewReview.setBackground(border);
    }

    private void resetButtons() {
        // Reset all options to default color
        BtnOpt1.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        BtnOpt2.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        BtnOpt3.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        BtnOpt4.setBackgroundColor(getResources().getColor(R.color.primaryColor));
    }

    @Override
    public void onPause() {
        super.onPause();

        // Show the toolbar when navigating back
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
            }
        }
    }
}