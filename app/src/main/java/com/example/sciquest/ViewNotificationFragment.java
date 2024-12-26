package com.example.sciquest;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewNotificationFragment extends Fragment {

    private ImageButton BtnBack;
    private TextView TVNotificationTitle, TVNotificationMessage;
    private ImageView IVMessage;

    public ViewNotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_notification, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BtnBack = view.findViewById(R.id.BtnBack);
        TVNotificationTitle = view.findViewById(R.id.TVNotificationTitle);
        IVMessage = view.findViewById(R.id.IVMessage);
        TVNotificationMessage = view.findViewById(R.id.TVNotificationMessage);

        // Get the arguments (data passed from the adapter)
        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title_name");
            String message = bundle.getString("message");
            int imageResource = bundle.getInt("image_resource");

            // Set the data to the views
            TVNotificationTitle.setText(title);
            TVNotificationMessage.setText(message);
            IVMessage.setImageResource(imageResource);
        }

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