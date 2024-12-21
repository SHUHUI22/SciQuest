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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StartQuizFragment extends Fragment {

    private ImageButton BtnBack;
    private TextView TVStartQuizTittle, TVDescription;
    private ImageView IVTopic;
    private Button BtnStart, BtnReview;
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    public StartQuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_quiz, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TVStartQuizTittle = view.findViewById(R.id.TVStartQuizTittle);
        IVTopic = view.findViewById(R.id.IVTopic);
        TVDescription = view.findViewById(R.id.TVDescription);
        BtnStart = view.findViewById(R.id.BtnStart);
        BtnReview = view.findViewById(R.id.BtnReview);

        //Get data from the argument passed from QuizListFragment
        Bundle bundle = getArguments();
        if(bundle!=null){
            String topic = bundle.getString("topic_name");
            int imageRes = bundle.getInt("image_resource");

            //Set the topic and image
            TVStartQuizTittle.setText(topic);
            IVTopic.setImageResource(imageRes);

            // Check if the user has attempted this topic before
            checkUserAttempt(topic.replace(" ",""));

            // Obtain NavController
            NavController navController = Navigation.findNavController(requireActivity(), R.id.NHFMain);

            BtnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Passing the topic_name to QuizFragment via Bundle
                    Bundle quizBundle = new Bundle();
                    quizBundle.putString("topic_name", topic);
                    navController.navigate(R.id.DestQuiz, quizBundle);
                }
            });

            BtnReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle reviewBundle = new Bundle();
                    reviewBundle.putString("topic_name", topic);
                    navController.navigate(R.id.DestReview, reviewBundle);
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

        // Navigate back
        BtnBack = view.findViewById(R.id.BtnBack);
        BtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void checkUserAttempt(String topic) {
        String userID = user.getUid();

        db.collection("QuizAttempt")
                .document(userID)
                .collection("Topic")
                .document(topic)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()){
                            Long score = documentSnapshot.getLong("score");
                            BtnStart.setText("Attempt again");
                            BtnReview.setVisibility(View.VISIBLE);
                            TVDescription.setText("You gained "+score.toString()+" from previous attempt. Challenge yourself to improve your score and refresh your knowledge!");
                        }
                        else {
                            BtnStart.setText("Start Attempt");
                            BtnReview.setVisibility(View.INVISIBLE);
                            TVDescription.setText("Test your knowledge with the first attempt!");
                        }
                    }
                    else {
                        Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
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