package com.example.sciquest;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseUser user = mAuth.getCurrentUser();
    TextView TVHomeUsername;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the action bar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("SciQuest");
        }

        // Set the username in home
        TVHomeUsername = view.findViewById(R.id.TVHi);
        fetchAndSetUsername();
    }

    private void fetchAndSetUsername() {
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

                            // Set fetched data
                            TVHomeUsername.setText("Hi, "+username);
                        }
                    });
        }
        else{
            Toast.makeText(requireContext(),"Error fetching username",Toast.LENGTH_SHORT).show();
        }
    }
}