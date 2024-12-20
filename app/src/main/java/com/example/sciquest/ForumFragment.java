package com.example.sciquest;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sciquest.Adapter.PostAdapter;
import com.example.sciquest.Model.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ForumFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FloatingActionButton BtnAddPost;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    public ForumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forum, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the action bar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("SciQuest");
        }
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // If no user is authenticated, navigate to the login page
        if (firebaseUser == null) {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.DestLogin);  // Use NavController to navigate to login activity
        } else {
            // User is authenticated, proceed with data fetching
            fetchPosts();
        }

        // Setup button and RecyclerView
        BtnAddPost = view.findViewById(R.id.BtnAddPost);
        BtnAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PostActivity.class);
            startActivity(intent);
        });

        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);
    }

    private void fetchPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String postId = document.getString("postId");
                            String description = document.getString("description");
                            String publisher = document.getString("publisher");
                            String postImageUrl = document.getString("imageUrl");

                            // Ensure that likeCount and commentCount are fetched and properly converted to long
                            long likeCount = 0;
                            if (document.contains("likeCount")) {
                                Number likeCountNumber = document.getLong("likeCount");
                                if (likeCountNumber != null) {
                                    likeCount = likeCountNumber.longValue();
                                }
                            }

                            long commentCount = 0;
                            if (document.contains("commentCount")) {
                                Number commentCountNumber = document.getLong("commentCount");
                                if (commentCountNumber != null) {
                                    commentCount = commentCountNumber.longValue();
                                }
                            }

                            long timeStamp = document.getLong("timestamp") != null ? document.getLong("timestamp") : 0;


                            // Create a new Post object and set basic post data
                            Post post = new Post(postId, description, publisher, postImageUrl);
                            post.setLikeCount(likeCount);
                            post.setCommentCount(commentCount);
                            post.setTimestamp(timeStamp);

                            // Fetch user data for the publisher
                            db.collection("Users").document(publisher)
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                                            DocumentSnapshot userDoc = userTask.getResult();
                                            String username = userDoc.getString("username");
                                            String profileImageUrl = userDoc.getString("profilePictureUrl");

                                            post.setUsername(username);
                                            post.setProfilePictureUrl(profileImageUrl);
                                            post.setPublisher(username);

                                            // Add the post to the list
                                            postList.add(post);
                                        }

                                        // Update the adapter
                                        postAdapter.setPosts(postList);
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Error fetching posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}