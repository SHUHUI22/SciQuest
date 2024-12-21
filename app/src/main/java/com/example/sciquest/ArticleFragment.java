package com.example.sciquest;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sciquest.Adapter.TopicAdapter;
import com.example.sciquest.Model.Topic;

import java.util.ArrayList;
import java.util.List;

public class ArticleFragment extends Fragment {

    private RecyclerView recyclerView;
    private TopicAdapter adapter;
    private List<Topic> articleList;

    public ArticleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_article, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the action bar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("SciQuest");
        }

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewArticle); // Connect RecyclerView from XML
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Linear layout for vertical list

        // Initialize quiz topics data
        articleList = getArticleList();

        // Set up RecyclerView with adapter and click listener
        adapter = new TopicAdapter(articleList, topic -> {
            // Handle click events here
            // For example: show a message, start an activity, or navigate to quiz details
            System.out.println("Clicked topic: " + topic.getTopic());
        });
        recyclerView.setAdapter(adapter); // Attach the adapter to RecyclerView
    }

    private List<Topic> getArticleList() {
        List<Topic> list = new ArrayList<>(); // Create an empty list

        // Add quiz topics with titles and images
        list.add(new Topic("Disease And Disorder", R.drawable.disease_and_disorder_topic));
        list.add(new Topic("Photosynthesis", R.drawable.photosynthesis_topic));
        list.add(new Topic("Organic Chemistry", R.drawable.organic_chemistry_topic));
        list.add(new Topic("Respiratory System",R.drawable.respiratory_system_topic));
        list.add(new Topic("Motion",R.drawable.motion_topic));
        list.add(new Topic("Atom", R.drawable.atom_topic));

        return list;
    }
}