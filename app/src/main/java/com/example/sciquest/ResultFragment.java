package com.example.sciquest;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ResultFragment extends Fragment {

    private Button BtnReview, BtnReattempt, BtnBackToQuizList;
    private TextView TVScore, TVCorrectAnswersCount, TVWrongAnswersCount, TVUnansweredCount;
    private ProgressBar PBScore;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PBScore = view.findViewById(R.id.PBScore);
        TVScore = view.findViewById(R.id.TVScore);
        TVCorrectAnswersCount = view.findViewById(R.id.TVCorrectAnswersCount);
        TVWrongAnswersCount = view.findViewById(R.id.TVWrongAnswersCount);
        TVUnansweredCount = view.findViewById(R.id.TVUnansweredCount);
        BtnReview = view.findViewById(R.id.BtnReview);
        BtnReattempt = view.findViewById(R.id.BtnReattempt);
        BtnBackToQuizList = view.findViewById(R.id.BtnBackToQuizList);

        Bundle bundle = getArguments();
        if (bundle != null) {
            int score = bundle.getInt("score");
            int correctAnswersCount = bundle.getInt("correct_answers_count");
            int wrongAnswersCount = bundle.getInt("wrong_answers_count");
            int unansweredCount = bundle.getInt("unanswered_count");

            // Display results
            PBScore.setIndeterminate(false);  // Ensures it won't rotate
            PBScore.setMax(100);
            PBScore.setProgress(score);
            TVScore.setText(score + "%");
            TVCorrectAnswersCount.setText("Correct Answers: " + correctAnswersCount);
            TVWrongAnswersCount.setText("Wrong Answers: " + wrongAnswersCount);
            TVUnansweredCount.setText("Unanswered Questions: " + unansweredCount);
        }

        // Obtain NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.NHFMain);// Obtain NavController

        BtnReattempt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = bundle.getString("topic_name");
                Bundle quizBundle = new Bundle();
                quizBundle.putString("topic_name", topic);

                navController.popBackStack(R.id.DestQuiz, true);
                navController.navigate(R.id.DestQuiz, quizBundle);
            }
        });

        BtnBackToQuizList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack(R.id.DestQuizList, true);
                navController.navigate(R.id.DestQuizList);
            }
        });

        BtnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = bundle.getString("topic_name");
                Bundle reviewBundle = new Bundle();
                reviewBundle.putString("topic_name", topic);
                navController.navigate(R.id.DestReview, reviewBundle);
            }
        });

        // Hide the toolbar
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
            }
        }

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