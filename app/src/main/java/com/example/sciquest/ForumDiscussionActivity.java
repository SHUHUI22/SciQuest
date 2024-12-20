package com.example.sciquest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sciquest.Adapter.CommentAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumDiscussionActivity extends AppCompatActivity {

    private EditText addComment;
    private TextView commenter;
    private ImageView picture, send, close;
    private RecyclerView recyclerView;

    private String postId, publisherId, postPictureUrl;
    private FirebaseUser firebaseUser;

    private List<Map<String, Object>> commentList;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_discussion);
        // Check if the user is logged in
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Intent intent = new Intent(ForumDiscussionActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize UI components
        addComment = findViewById(R.id.addComment);
        picture = findViewById(R.id.picture);
        send = findViewById(R.id.send);
        close = findViewById(R.id.close);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        publisherId = intent.getStringExtra("userId");
        String postPicture = intent.getStringExtra("imageUrl");

        Glide.with(this).load(postPicture).into(picture);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerViewComments); // Replace with actual RecyclerView ID
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList, this, postId); // Pass postId
        recyclerView.setAdapter(commentAdapter);

        // Load comments and post details
        loadPostDetails(postId);
        loadComments(postId);

        // Set listeners
        send.setOnClickListener(v -> {
            if (addComment.getText().toString().trim().isEmpty()) {
                Toast.makeText(ForumDiscussionActivity.this, "You can't send an empty comment!", Toast.LENGTH_SHORT).show();
            } else {
                addComment();
            }
        });

        close.setOnClickListener(v -> {
//            NavController navController = Navigation.findNavController(this, R.id.DestForum);
//            navController.navigate(R.id.DestForum); // Navigate back to Forum
            onBackPressed();
        });

        picture.setOnClickListener(v -> {
            // Pass the URL of the image to the FullImageActivity
            Intent intents = new Intent(ForumDiscussionActivity.this, FullImageActivity.class);
            intents.putExtra("imageUrl", postPictureUrl);
            startActivity(intents);
        });


    }

    private void addComment() {
        CollectionReference commentRef = FirebaseFirestore.getInstance()
                .collection("Posts")
                .document(postId)
                .collection("Comments");

        DocumentReference newCommentRef = commentRef.document(); // Generate the document reference with an ID
        String commentId = newCommentRef.getId(); // Get the generated ID

        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("id", commentId); // Store the comment's ID
        commentMap.put("comment", addComment.getText().toString());
        commentMap.put("publisher", firebaseUser.getUid());
        commentMap.put("timestamp", System.currentTimeMillis());

        newCommentRef.set(commentMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(ForumDiscussionActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
            addComment.setText("");
            updateCommentCount(postId, true);
            loadComments(postId); // Refresh comments
        }).addOnFailureListener(e -> {
            Toast.makeText(ForumDiscussionActivity.this, "Failed to add comment", Toast.LENGTH_SHORT).show();
        });
    }


    private void updateCommentCount(String postId, boolean increment) {
        DocumentReference postRef = FirebaseFirestore.getInstance().collection("Posts").document(postId);

        postRef.update("commentCount", FieldValue.increment(increment ? 1 : -1))
                .addOnSuccessListener(aVoid -> Log.d("ForumDiscussionActivity", "Comment count updated successfully."))
                .addOnFailureListener(e -> Log.e("ForumDiscussionActivity", "Error updating comment count: " + e.getMessage()));
    }

    private void loadComments(String postId) {
        FirebaseFirestore.getInstance()
                .collection("Posts")
                .document(postId)
                .collection("Comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Map<String, Object> comment = doc.getData();
                        if (comment != null) {
                            comment.put("id", doc.getId()); // Include the document ID
                            commentList.add(comment);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                });
    }


    private void loadPostDetails(String postId) {
        DocumentReference postRef = FirebaseFirestore.getInstance()
                .collection("Posts")
                .document(postId);

        postRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                postPictureUrl = documentSnapshot.getString("imageUrl");

                Glide.with(this)
                        .load(postPictureUrl)
                        .placeholder(R.drawable.pic_upload)
                        .into(picture);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load post details", Toast.LENGTH_SHORT).show()
        );
    }
}