package com.example.sciquest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class UploadProfilePicFragment extends Fragment {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseUser user = mAuth.getCurrentUser();
    private static final int PICK_IMAGE_REQUEST = 1;  // Request code for image selection
    private Uri imageUri;
    private ImageView IVProfilePic;
    private ImageButton BtnBack;
    private Button BtnChoosePic, BtnUploadPic;

    public UploadProfilePicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload_profile_pic, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IVProfilePic = view.findViewById(R.id.IVProfilePic);
        BtnChoosePic = view.findViewById(R.id.BtnChoosePic);
        BtnUploadPic = view.findViewById(R.id.BtnUploadPic);
        BtnBack = view.findViewById(R.id.BtnBack);

        // Load current profile picture from Firestore (if available)
        loadProfilePicture();

        BtnChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,PICK_IMAGE_REQUEST);
            }
        });

        BtnUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadImage();
                } else {
                    Toast.makeText(getContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Hide the toolbar
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
            }
        }

        // Navigate back
        BtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    // Method to upload the selected image to Firebase Storage
    private void uploadImage() {
        String userID = user.getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures/" + userID + ".jpg");

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveProfilePictureUrlToFirestore(downloadUrl);  // Save the URL to Firestore
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });

    }

    // Method to save the profile picture's URL to Firestore
    private void saveProfilePictureUrlToFirestore(String downloadUrl) {
        String userID = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(userID)
                .update("profilePictureUrl", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile picture uploaded", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save profile picture", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to handle the result of the image selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            IVProfilePic.setImageURI(imageUri); // Preview the selected image
        }
    }

    // Method to load the current profile picture from Firestore
    private void loadProfilePicture() {
        String userID = user.getUid();
        db.collection("Users").document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                    if (profilePictureUrl != null) {
                        Glide.with(this)
                                .load(profilePictureUrl)
                                .placeholder(R.drawable.profile_pic)
                                .into(IVProfilePic);
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Show the toolbar when navigating back
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
            }
        }
    }
}