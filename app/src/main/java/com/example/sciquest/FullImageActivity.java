package com.example.sciquest;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class FullImageActivity extends AppCompatActivity {

    private ImageView close, fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        close = findViewById(R.id.close);
        fullImageView = findViewById(R.id.fullImageView);
        // Get the image URL passed from ForumDiscussionActivity
        String imageUrl = getIntent().getStringExtra("imageUrl");


        // Use Glide to load the full image
        Glide.with(this)
                .load(imageUrl)
                .into(fullImageView);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}