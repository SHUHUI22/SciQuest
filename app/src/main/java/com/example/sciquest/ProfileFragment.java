package com.example.sciquest;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseUser user = mAuth.getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private TextView TVUsername, TVEmail, TVAge, TVGender, TVHi;
    private ImageView IVProfilePic;
    private ImageButton IMBtnEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the action bar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("SciQuest");
        }

        TVHi = view.findViewById(R.id.TVHi);
        TVUsername = view.findViewById(R.id.TVUsername);
        TVEmail = view.findViewById(R.id.TVEmail);
        TVGender = view.findViewById(R.id.TVGender);
        TVAge = view.findViewById(R.id.TVAge);
        IVProfilePic = getView().findViewById(R.id.IVProfilePic);
        IMBtnEdit = view.findViewById(R.id.IMBtnEdit);

        // Set profile details on UI
        fetchAndSetProfileDetails();

        // Obtain NavController
        NavController navController = Navigation.findNavController(requireActivity(), R.id.NHFMain);

        // Navigate to edit profile picture
        IVProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.DestUploadProfilePic);
            }
        });

        // Navigate to edit profile detail
        IMBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.DestEditProfile);
            }
        });
    }

    private void fetchAndSetProfileDetails() {
        if(user!=null){
            String userID = user.getUid();
            db.collection("Users").document(userID)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            // Handle error
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String email = documentSnapshot.getString("email");
                            String age = documentSnapshot.get("age").toString();
                            String gender = documentSnapshot.getString("gender");
                            String profilePicUrl = documentSnapshot.getString("profilePictureUrl");

                            // Set fetched data
                            TVHi.setText("Hi, "+username);
                            TVUsername.setText(username);
                            TVEmail.setText(email);
                            TVAge.setText(age+" years old");
                            TVGender.setText(gender);

                            // If a profile picture URL exists, load it with Glide
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                Glide.with(this).load(profilePicUrl).into(IVProfilePic);
                            } else {
                                // If no URL exists, set a default image
                                Glide.with(this).load(R.drawable.profile_icon).into(IVProfilePic);
                            }
                        }
                    });
        }
        else{
            Toast.makeText(requireContext(),"Error fetching profile details",Toast.LENGTH_SHORT).show();
        }
    }
}