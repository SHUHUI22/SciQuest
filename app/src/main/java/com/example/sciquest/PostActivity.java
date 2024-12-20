package com.example.sciquest;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private ImageView uploadIcon, selectedImage, close;
    private EditText description;
    private Button submitButton;

    private NavController navController; // Added NavController as a member variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Initialize Firebase services
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize UI components
        uploadIcon = findViewById(R.id.uploadIcon);
        selectedImage = findViewById(R.id.sampleImage);
        description = findViewById(R.id.description);
        submitButton = findViewById(R.id.submitButton);
        close = findViewById(R.id.close);

        // Upload icon listener
        uploadIcon.setOnClickListener(v -> openGallery());

        // Submit button listener
        submitButton.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToStorage();
            } else {
                Toast.makeText(PostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Close button listener
        close.setOnClickListener(v -> {
            onBackPressed(); // Navigate back to Forum
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImage.setImageURI(imageUri); // Display the selected image
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImageToStorage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting...");
        progressDialog.show();

        if (imageUri != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

            String postId = reference.push().getKey();
            StorageReference fileReference = storageReference.child(postId + "." + getFileExtension(imageUri));

            //upload file to firebase storage
            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    String userId = user.getUid();

                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

                    HashMap<String, Object> postMap = new HashMap<>();
                    postMap.put("postId", postId);
                    postMap.put("imageUrl", imageUrl);
                    postMap.put("description", description.getText().toString());
                    postMap.put("publisher", userId);
                    postMap.put("timestamp", System.currentTimeMillis());  // Server timestamp

                    FirebaseFirestore.getInstance().collection("Posts").document(postId)
                            .set(postMap)
                            .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(PostActivity.this, "Post uploaded successfully to Firestore", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(PostActivity.this, "Failed to upload post to Firestore", Toast.LENGTH_SHORT).show();
                                }
                            });

                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });


        } else {
            progressDialog.dismiss();
            Toast.makeText(PostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}