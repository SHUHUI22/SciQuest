package com.example.sciquest;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class StartQuizFragment extends Fragment {

    private ImageButton BtnBack;
    private TextView TVStartQuizTittle, TVDescription;
    private ImageView IVTopic;

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

        //Get data from the argument passed from QuizListFragment
        Bundle bundle = getArguments();
        if(bundle!=null){
            String topic = bundle.getString("topic_name");
            int imageRes = bundle.getInt("image_resource");

            //Set the topic and image
            TVStartQuizTittle.setText(topic);
            IVTopic.setImageResource(imageRes);
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