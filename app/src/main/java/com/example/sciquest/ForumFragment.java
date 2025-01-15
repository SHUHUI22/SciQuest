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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.firebase.firestore.FieldPath;

public class ForumFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FloatingActionButton BtnAddPost;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private ScreenManager screenManager;

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

        // Step 1: Fetch all posts
        db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        List<Post> tempPostList = new ArrayList<>();
                        Set<String> userIds = new HashSet<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String postId = document.getString("postId");
                            String description = document.getString("description");
                            String publisher = document.getString("publisher");
                            String postImageUrl = document.getString("imageUrl");

                            long likeCount = document.contains("likeCount") ? document.getLong("likeCount") : 0;
                            long commentCount = document.contains("commentCount") ? document.getLong("commentCount") : 0;
                            long timeStamp = document.getLong("timestamp") != null ? document.getLong("timestamp") : 0;

                            // Add publisher to the set for user data fetching
                            userIds.add(publisher);

                            // Create a Post object
                            Post post = new Post(postId, description, publisher, postImageUrl);
                            post.setLikeCount(likeCount);
                            post.setCommentCount(commentCount);
                            post.setTimestamp(timeStamp);

                            tempPostList.add(post);
                        }

                        // Step 2: Fetch user data in bulk
                        db.collection("Users")
                                .whereIn(FieldPath.documentId(), new ArrayList<>(userIds))
                                .get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        Map<String, Map<String, String>> userMap = new HashMap<>();

                                        for (DocumentSnapshot userDoc : userTask.getResult()) {
                                            String userId = userDoc.getId();
                                            String username = userDoc.getString("username");
                                            String profilePictureUrl = userDoc.getString("profilePictureUrl");

                                            Map<String, String> userData = new HashMap<>();
                                            userData.put("username", username);
                                            userData.put("profilePictureUrl", profilePictureUrl);

                                            userMap.put(userId, userData);
                                        }

                                        // Step 3: Merge user data with posts
                                        for (Post post : tempPostList) {
                                            Map<String, String> userData = userMap.get(post.getPublisher());
                                            if (userData != null) {
                                                post.setUsername(userData.get("username"));
                                                post.setProfilePictureUrl(userData.get("profilePictureUrl"));
                                            }
                                        }

                                        // Step 4: Update the adapter
                                        postList.addAll(tempPostList);
                                        postAdapter.setPosts(postList);
                                    } else {
                                        Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Error fetching posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}