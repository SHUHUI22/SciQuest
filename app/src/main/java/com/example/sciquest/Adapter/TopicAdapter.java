package com.example.sciquest.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sciquest.Model.Topic;
import com.example.sciquest.R;

import org.checkerframework.checker.units.qual.N;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {
    private List<Topic> topicList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(Topic topic);
    }

    public TopicAdapter(List<Topic> topicList, OnItemClickListener listener){
        this.topicList = topicList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        // Inflate the layout for each topic in the list
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_item, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position){
        // Bind data to the ViewHolder
        holder.bind(topicList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return topicList.size(); // Total number of items
    }

    // ViewHolder class that represents a single item
    static class TopicViewHolder extends RecyclerView.ViewHolder {
        TextView TVTopic;
        ImageView IVTopic;

        TopicViewHolder(View itemView) {
            super(itemView);
            // Initialize the views
            TVTopic = itemView.findViewById(R.id.TVTopic);
            IVTopic = itemView.findViewById(R.id.IVTopic);
        }

        // Bind data to the views and set the click listener
        void bind(final Topic topic, final OnItemClickListener listener) {
            TVTopic.setText(topic.getTopic());
            IVTopic.setImageResource(topic.getImageResID());
            itemView.setOnClickListener(v -> listener.onItemClick(topic));
        }
    }
}
