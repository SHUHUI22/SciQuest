package com.example.sciquest.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sciquest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Map<String, Object>> mComments;
    private Context mContext;
    private String postId;

    public CommentAdapter(List<Map<String, Object>> comments, Context context,String postId) {
        this.mComments = comments;
        this.mContext = context;
        this.postId = postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> commentMap = mComments.get(position);
        String commentId = (String) commentMap.get("id"); // Assuming `id` is stored
        String commentText = (String) commentMap.get("comment");
        Long timestamp = (Long) commentMap.get("timestamp");
        String publisher = (String) commentMap.get("publisher");

        holder.commentText.setText(commentText);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(publisher)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && userTask.getResult() != null) {
                        DocumentSnapshot userDoc = userTask.getResult();
                        String username = userDoc.getString("username");
                        holder.commenter.setText(username);
                    }
                });

        if (timestamp != null) {
            holder.timestamp.setText(android.text.format.DateFormat.format("dd-MM-yyyy hh:mm a", timestamp));
        } else {
            holder.timestamp.setText("N/A");
        }

        // Fetch likes using postId
        FirebaseFirestore.getInstance()
                .collection("Posts")
                .document(postId) // Use the postId directly here
                .collection("Comments")
                .document(commentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Boolean> likes = (Map<String, Boolean>) documentSnapshot.get("likes");
                    if (likes != null && !likes.isEmpty()) {
                        int likeCount = likes.size();
                        holder.likeCount.setVisibility(View.VISIBLE); // Show the like count
                        holder.likeCount.setText(String.valueOf(likeCount));
                        if (likes.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            holder.like.setImageResource(R.drawable.ic_liked); // Liked icon
                        } else {
                            holder.like.setImageResource(R.drawable.like); // Default like icon
                        }
                    } else {
                        holder.likeCount.setVisibility(View.GONE); // Hide the like count if no likes
                        holder.like.setImageResource(R.drawable.like); // Reset to default like icon
                    }
                });

        // Handle like click
        holder.like.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference commentRef = FirebaseFirestore.getInstance()
                    .collection("Posts")
                    .document(postId)
                    .collection("Comments")
                    .document(commentId);

            FirebaseFirestore.getInstance().runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(commentRef);
                Map<String, Boolean> likes = (Map<String, Boolean>) snapshot.get("likes");

                if (likes == null) likes = new HashMap<>();

                if (likes.containsKey(userId)) {
                    likes.remove(userId);
                } else {
                    likes.put(userId, true);
                }

                transaction.update(commentRef, "likes", likes);
                return null;
            }).addOnSuccessListener(aVoid -> {
                // Refresh the likes after a successful transaction
                notifyItemChanged(position);
            });
        });
    }


    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView commentText, timestamp,likeCount, commenter;
        public ImageView like;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.Comment);
            timestamp = itemView.findViewById(R.id.comment_timestamp);
            like = itemView.findViewById(R.id.like);
            likeCount = itemView.findViewById(R.id.numLike);
            commenter = itemView.findViewById(R.id.UserName);
        }
    }
}
