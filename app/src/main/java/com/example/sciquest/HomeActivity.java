package com.example.sciquest;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance() ;
    private FirebaseUser user = mAuth.getCurrentUser();
    private TextView TVHeaderUsername;
    private ImageView IVProfilePic;
    private ScreenManager screenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        screenManager = new ScreenManager(this);

        Toolbar toolbar = findViewById(R.id.TBHome);
        setSupportActionBar(toolbar);

        // Set up the navigation controller
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.NHFMain);
        navController = host.getNavController();

        // Set up the app bar configuration
        drawerLayout = findViewById(R.id.DLMain);
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.DestHome,R.id.DestProfile,R.id.DestForum,R.id.DestQuizList, R.id.DestArticle, R.id.DestScreenRecord, R.id.DestNotifications).setOpenableLayout(drawerLayout).build();
        NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration);

        // Set up the drawer layout and toggle

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupNavMenu(navController);

    }

    // Set up the side navigation menu listener
    private void setupNavMenu(NavController navController) {
        NavigationView sideNav = findViewById(R.id.side_nav);
        NavigationUI.setupWithNavController(sideNav, navController);

        // Set the username in side bar
        TVHeaderUsername = sideNav.getHeaderView(0).findViewById(R.id.TVHeaderUsername);
        IVProfilePic = sideNav.getHeaderView(0).findViewById(R.id.IVProfilePic);
        fetchAndSetUsername();

        // Start screen time tracking when user logs in
        screenManager.startScreenTimeTracking();
        Long startTime = screenManager.getStartTime();

        sideNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selectedId = item.getItemId();

                if (selectedId == R.id.DestHome) {
                    navController.navigate(R.id.DestHome);
                }
                else if (selectedId == R.id.DestProfile) {
                    navController.navigate(R.id.DestProfile);
                }
                else if(selectedId == R.id.DestForum){
                    navController.navigate(R.id.DestForum);
                }
                else if(selectedId == R.id.DestQuizList){
                    navController.navigate(R.id.DestQuizList);
                }
                else if(selectedId == R.id.DestArticle){
                    navController.navigate(R.id.DestArticle);
                }
                else if (selectedId == R.id.DestScreenRecord){
                    // Pass the startTime to the fragment
                    Bundle bundle = new Bundle();
                    bundle.putLong("startTime", startTime);
                    navController.navigate(R.id.DestScreenRecord,bundle);
                }
                else if(selectedId == R.id.DestNotifications){
                    // Pass the startTime to the fragment
                    Bundle bundle = new Bundle();
                    bundle.putLong("startTime", startTime);
                    navController.navigate(R.id.DestNotifications,bundle);
                }
                else if (selectedId == R.id.DestLogout){
                    // Stop tracking screen time
                    String today = screenManager.getCurrentDate();
                    screenManager.stopScreenTimeTracking(today);

                    FirebaseAuth.getInstance().signOut();
                    navController.navigate(R.id.DestLogin);
                    finish();
                }
                else {
                    return false;
                }

                // Close the drawer after item is selected
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });
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
                            String profilePicUrl = documentSnapshot.getString("profilePictureUrl");

                            // Set fetched data
                            TVHeaderUsername.setText("Hi, "+username);

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
            Toast.makeText(HomeActivity.this,"Error fetching username",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}