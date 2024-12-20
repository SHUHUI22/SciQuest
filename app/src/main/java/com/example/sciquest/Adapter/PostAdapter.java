package com.example.sciquest.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sciquest.ForumDiscussionActivity;
import com.example.sciquest.LoginActivity;
import com.example.sciquest.Model.Post;
import com.example.sciquest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPost;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context context, List<Post> postList) {
        this.mContext = context;
        this.mPost = postList;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPost.get(position);

        // Bind post description
        holder.description.setText(post.getDescription());

        // Bind post image using Glide
        Glide.with(mContext)
                .load(post.getPostImage())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.postImage);

        // Bind publisher info
        holder.username.setText(post.getUsername());
        holder.publisher.setText(post.getUsername());

        Glide.with(mContext)
                .load(post.getProfilePictureUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.profilePic);

        // Update like count
        holder.likes.setText(post.getLikeCount() + " likes");

        // Update comment count
        if (post.getCommentCount() == 0) {
            holder.comments.setText("No comments yet");
        } else {
            holder.comments.setText("View " + post.getCommentCount() + " comments");
        }

        Long timestamp = post.getTimestamp();
        // Bind and format timestamp
        if (timestamp >0) {
            holder.timestamp.setText(android.text.format.DateFormat.format("dd-MM-yyyy hh:mm a", timestamp ));
        } else {
            holder.timestamp.setText("N/A");
        }

        // Handle like button functionality
        setLikeState(post.getPostId(), holder.like);

        holder.like.setOnClickListener(v -> {
            if (firebaseUser == null) {
                mContext.startActivity(new Intent(mContext, LoginActivity.class));
                Toast.makeText(mContext, "Please log in to like the post", Toast.LENGTH_SHORT).show();
                return;
            }

            toggleLike(post.getPostId(), holder.like, post); // Pass Post object to update like count
        });

        // Comment part
        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ForumDiscussionActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("publisherId", post.getPublisher());
            mContext.startActivity(intent);
        });
    }

    // Toggle like/unlike state in the Firestore database
    private void toggleLike(String postId, ImageView likeButton, Post post) {
        DocumentReference postRef = FirebaseFirestore.getInstance().collection("Posts").document(postId);

        postRef.collection("Likes").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Unlike post
                        postRef.collection("Likes").document(firebaseUser.getUid()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    likeButton.setImageResource(R.drawable.like);
                                    likeButton.setTag("like");
                                    updateLikeCount(postId, false, post); // Decrement like count
                                });
                    } else {
                        // Like post
                        postRef.collection("Likes").document(firebaseUser.getUid()).set(new HashMap<>())
                                .addOnSuccessListener(aVoid -> {
                                    likeButton.setImageResource(R.drawable.ic_liked);
                                    likeButton.setTag("liked");
                                    updateLikeCount(postId, true, post); // Increment like count
                                });
                    }
                });
    }

    // Update like count in Firestore and local Post model
    private void updateLikeCount(String postId, boolean isLiked, Post post) {
        DocumentReference postRef = FirebaseFirestore.getInstance().collection("Posts").document(postId);

        postRef.update("likeCount", FieldValue.increment(isLiked ? 1 : -1))
                .addOnSuccessListener(aVoid -> {
                    Log.d("PostAdapter", "Like count updated successfully.");
                    fetchUpdatedLikeCount(postId, post); // Fetch the updated like count and update the Post model
                })
                .addOnFailureListener(e -> Log.e("PostAdapter", "Error updating like count: " + e.getMessage()));
    }

    // Fetch updated like count from Firestore and update the UI
    private void fetchUpdatedLikeCount(String postId, Post post) {
        DocumentReference postRef = FirebaseFirestore.getInstance().collection("Posts").document(postId);

        postRef.addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Long likeCount = documentSnapshot.getLong("likeCount");
                if (likeCount != null) {
                    post.setLikeCount(likeCount); // Update the like count in the Post model
                    notifyDataSetChanged(); // Refresh the UI
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profilePic, postImage, like, comment, save;
        public TextView username, description, likes, publisher, comments, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profile_picture);
            postImage = itemView.findViewById(R.id.postImage);
            username = itemView.findViewById(R.id.username);
            description = itemView.findViewById(R.id.description);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.publisher);
            comments = itemView.findViewById(R.id.comments);
            timestamp = itemView.findViewById(R.id.post_timestamp);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    // Set the list of posts and notify the adapter that data has changed
    public void setPosts(List<Post> posts) {
        this.mPost = posts;
        notifyDataSetChanged();
    }

    private void setLikeState(String postId, ImageView likeButton) {
        DocumentReference postRef = FirebaseFirestore.getInstance().collection("Posts").document(postId);
        postRef.collection("Likes").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        likeButton.setImageResource(R.drawable.ic_liked);
                        likeButton.setTag("liked");
                    } else {
                        likeButton.setImageResource(R.drawable.like);
                        likeButton.setTag("like");
                    }
                });
    }
}

