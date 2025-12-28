package com.example.mobile.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.mobile.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get NavHostFragment first
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Connect BottomNavigationView with NavController
        if (navController != null) {
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // Hide bottom nav on auth screens
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (shouldShowBottomNav(destination.getId())) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                } else {
                    bottomNavigationView.setVisibility(View.GONE);
                }
            });
        }
    }

    private boolean shouldShowBottomNav(int destinationId) {
        // Show bottom nav only on main app screens
        return destinationId == R.id.driverHistoryFragment ||
                destinationId == R.id.user_profile_fragment;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}