package com.example.sciquest;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseUser user = mAuth.getCurrentUser();
    private ImageButton BtnBack;
    private EditText ETUsername, ETAge;
    private RadioGroup RGGender;
    private RadioButton RBMale, RBFemale;
    private Button BtnSave;
    private String username, age, gender;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ETUsername = view.findViewById(R.id.ETUsername);
        ETAge = view.findViewById(R.id.ETAge);
        RGGender = view.findViewById(R.id.RGGender);
        RBMale = view.findViewById(R.id.RBMale);
        RBFemale = view.findViewById(R.id.RBFemale);
        BtnSave = view.findViewById(R.id.BtnSave);

        // Set profile details on UI
        fetchAndSetProfileDetails();

        // Edit profile details
        BtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputUsername = ETUsername.getText().toString();
                String inputAge = ETAge.getText().toString();
                String inputGender = getSelectedGender();

                if (inputUsername.isEmpty()) {
                    ETUsername.setError(getString(R.string.error_empty_username));
                    return;
                }
                if (inputAge.isEmpty()) {
                    ETAge.setError(getString(R.string.error_empty_age));
                    return;
                }
                if (inputGender == null) {
                    Toast.makeText(requireContext(), getString(R.string.error_empty_gender), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Collect updated fields
                String userID = user.getUid();
                Map<String, Object> updates = new HashMap<>();

                if (!inputUsername.equals(username)) {
                    updates.put("username", inputUsername);
                }
                if (!inputAge.equals(age)) {
                    updates.put("age", Integer.parseInt(inputAge));
                }
                if (!inputGender.equals(gender)) {
                    updates.put("gender", inputGender);
                }

                // Perform updates if there are changes
                if (!updates.isEmpty()) {
                    updateFirestore(userID, updates);
                    Toast.makeText(requireContext(), "Profile update successfully.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(requireContext(), "No changes to update.", Toast.LENGTH_SHORT).show();
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
        BtnBack = view.findViewById(R.id.BtnBack);
        BtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void updateFirestore(String userID, Map<String, Object> updates) {
        db.collection("Users").document(userID)
                .update(updates)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(requireContext(), "Error updating profile.", Toast.LENGTH_SHORT).show();
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
                            username = documentSnapshot.getString("username");
                            String email = documentSnapshot.getString("email");
                            age = documentSnapshot.get("age").toString();
                            gender = documentSnapshot.getString("gender");

                            // Set fetched data
                            ETUsername.setText(username);
                            ETAge.setText(age);
                            if (gender.equals("Male")) {
                                RGGender.check(R.id.RBMale);
                            } else if (gender.equals("Female")) {
                                RGGender.check(R.id.RBFemale);
                            }

                        }
                    });
        }
        else{
            Toast.makeText(requireContext(),"Error fetching profile details",Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedGender() {
        int selectedId = RGGender.getCheckedRadioButtonId();
        if (selectedId == - 1 ) {
            // No gender selected
            return null;
        }
        else {
            RadioButton selectedRadioButton = getView().findViewById(selectedId);
            return selectedRadioButton.getText().toString();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Show the toolbar when navigating back
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.TBHome);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
            }
        }
    }
}