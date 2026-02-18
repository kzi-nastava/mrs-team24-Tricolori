package com.example.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mobile.R;
import com.example.mobile.dto.profile.ChangeDriverStatusRequest;
import com.example.mobile.dto.profile.ProfileResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.network.service.AuthService;
import com.example.mobile.network.service.DriverDailyLogService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashSet;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    private DriverDailyLogService dailyLogService;
    private AuthService authService;

    // nav header fields
    private TextView navHeaderUserName;
    private TextView navHeaderUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dailyLogService = RetrofitClient.getClient(this).create(DriverDailyLogService.class);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            findViewById(android.R.id.content).post(() -> {
                handleIntent(getIntent());
            });
        }

        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.rideHistoryFragment);
        topLevelDestinations.add(R.id.userProfileFragment);
        topLevelDestinations.add(R.id.changeRequestsReviewFragment);
        topLevelDestinations.add(R.id.adminDriverRegistrationFragment);
        topLevelDestinations.add(R.id.driverHomeFragment);
        topLevelDestinations.add(R.id.passengerHomeFragment);
        topLevelDestinations.add(R.id.userReportsFragment);

        View headerView = navigationView.getHeaderView(0);
        navHeaderUserName = headerView.findViewById(R.id.nav_header_user_name);
        navHeaderUserEmail = headerView.findViewById(R.id.nav_header_user_email);

        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations)
                .setOpenableLayout(drawerLayout)
                .build();

        if (navController != null) {
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

            updateMenuVisibility();
            updateNavigationHeader();


            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.loginFragment || id == R.id.registerFragment ||
                        id == R.id.forgotPasswordFragment || id == R.id.resetPasswordFragment) {
                    toolbar.setVisibility(View.GONE);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    setupDriverStatusSwitch();
                }
            });

            navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_status_switch) return false;

            if (id == R.id.nav_logout) {
                logoutUser();
                return true;
            }

            if (id == R.id.rideHistoryFragment) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String userRole = prefs.getString("user_role", "");

                Bundle args = new Bundle();
                args.putString("role", "ROLE_PASSENGER".equals(userRole) ? "PASSENGER" : "DRIVER");

                drawerLayout.closeDrawer(GravityCompat.START);
                navController.navigate(R.id.rideHistoryFragment, args);
                return true;
            }

            if (id == R.id.homeFragment) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String role = prefs.getString("user_role", "");

                drawerLayout.closeDrawer(GravityCompat.START);

                if ("ROLE_DRIVER".equals(role)) {
                    if (navController.getCurrentDestination() != null && 
                        navController.getCurrentDestination().getId() != R.id.driverHomeFragment) {
                        navController.navigate(R.id.driverHomeFragment);
                    }
                } else if ("ROLE_PASSENGER".equals(role)) {
                    if (navController.getCurrentDestination() != null && 
                        navController.getCurrentDestination().getId() != R.id.passengerHomeFragment) {
                        navController.navigate(R.id.passengerHomeFragment);
                    }
                }
                return true;
            }

            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) drawerLayout.closeDrawer(GravityCompat.START);
            return handled;
        });
        }
    }

    private void setupDriverStatusSwitch() {
        Menu menu = navigationView.getMenu();
        MenuItem statusItem = menu.findItem(R.id.nav_status_switch);

        if (statusItem != null && statusItem.isVisible()) {
            SwitchMaterial statusSwitch = (SwitchMaterial) statusItem.getActionView();
            if (statusSwitch != null) {
                statusSwitch.setOnCheckedChangeListener(null);
                statusSwitch.setChecked(true);
                statusItem.setTitle("Status: Online");
                statusItem.setIcon(R.drawable.ic_online);

                statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    updateStatus(isChecked, statusSwitch, statusItem);
                });
            }
        }
    }

    private void updateStatus(boolean isChecked, SwitchMaterial statusSwitch, MenuItem statusItem) {
        ChangeDriverStatusRequest request = new ChangeDriverStatusRequest(isChecked);

        dailyLogService.changeStatus(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    statusItem.setTitle(isChecked ? "Status: Online" : "Status: Offline");

                    statusItem.setIcon(isChecked ?
                            R.drawable.ic_online :
                            R.drawable.ic_offline);

                    Toast.makeText(MainActivity.this, "Status updated!", Toast.LENGTH_SHORT).show();
                } else {
                    revertSwitch(statusSwitch, isChecked, statusItem);
                    Toast.makeText(MainActivity.this, "Server rejected: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                revertSwitch(statusSwitch, isChecked, statusItem);
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revertSwitch(SwitchMaterial statusSwitch, boolean isChecked, MenuItem statusItem) {
        statusSwitch.setOnCheckedChangeListener(null);
        statusSwitch.setChecked(!isChecked);
        statusSwitch.setOnCheckedChangeListener((bv, checked) -> updateStatus(checked, statusSwitch, statusItem));
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        navHeaderUserName.setText("Guest User");
        navHeaderUserEmail.setText("Not logged in");
        updateMenuVisibility();
        navController.navigate(R.id.homeFragment);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // method that makes the menu dynamic - declares what is visible and what not
    public void updateMenuVisibility() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);
        String role = prefs.getString("user_role", null);

        Menu menu = navigationView.getMenu();

        // Get all menu items
        MenuItem home = menu.findItem(R.id.homeFragment);
        MenuItem history = menu.findItem(R.id.rideHistoryFragment);
        // TODO: UNCOMMENT AFTER ADDING FRAGMENTS (for example pricelist, support...)
//        MenuItem supervise = menu.findItem(R.id.rideSupervisorFragment);
//        MenuItem notifications = menu.findItem(R.id.notificationsFragment);
        MenuItem pricelist = menu.findItem(R.id.pricelistFragment);
//        MenuItem support = menu.findItem(R.id.supportFragment);
        MenuItem profile = menu.findItem(R.id.userProfileFragment);
        MenuItem logout = menu.findItem(R.id.nav_logout);
        MenuItem statusSwitch = menu.findItem(R.id.nav_status_switch);
        MenuItem changeRequests = menu.findItem(R.id.changeRequestsReviewFragment);
        MenuItem driverRegistration = menu.findItem(R.id.adminDriverRegistrationFragment);
        MenuItem userReports = menu.findItem(R.id.userReportsFragment);

        if (token != null && role != null) {
            // Common items for all logged in users
            home.setVisible(true);
            history.setVisible(true);
            // support.setVisible(true);
            profile.setVisible(true);
            logout.setVisible(true);
            userReports.setVisible(true);

            // Role-specific items
            if ("ROLE_ADMIN".equals(role)) {
                // supervise.setVisible(true);
//                notifications.setVisible(true);
                pricelist.setVisible(true);
                statusSwitch.setVisible(false);
                changeRequests.setVisible(true);
                driverRegistration.setVisible(true);

            } else if ("ROLE_DRIVER".equals(role)) {
//                supervise.setVisible(false);
//                notifications.setVisible(false);
                pricelist.setVisible(false);
                statusSwitch.setVisible(true);
                changeRequests.setVisible(false);
                driverRegistration.setVisible(false);

            } else if ("ROLE_PASSENGER".equals(role)) {
//                supervise.setVisible(false);
//                notifications.setVisible(true);
                pricelist.setVisible(false);
                statusSwitch.setVisible(false);
                changeRequests.setVisible(false);
                driverRegistration.setVisible(false);
            }

        } else {
            // Not logged in - hide everything
            home.setVisible(false);
            history.setVisible(false);
//            supervise.setVisible(false);
//            notifications.setVisible(false);
            pricelist.setVisible(false);
//            support.setVisible(false);
            profile.setVisible(false);
            logout.setVisible(false);
            statusSwitch.setVisible(false);
            changeRequests.setVisible(false);
            driverRegistration.setVisible(false);
            userReports.setVisible(false);
        }
    }

    public void updateNavigationHeader() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token != null) {
            // User is logged in - fetch profile data
            RetrofitClient.getProfileService(this)
                    .getUserProfile()
                    .enqueue(new Callback<ProfileResponse>() {
                        @Override
                        public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ProfileResponse profile = response.body();
                                String fullName = profile.getFirstName() + " " + profile.getLastName();
                                navHeaderUserName.setText(fullName);
                                navHeaderUserEmail.setText(profile.getEmail());
                            }
                        }

                        @Override
                        public void onFailure(Call<ProfileResponse> call, Throwable t) {
                            // Failed to load profile - use defaults
                            navHeaderUserName.setText("User");
                            navHeaderUserEmail.setText("Loading...");
                        }
                    });
        } else {
            // Not logged in
            navHeaderUserName.setText("Guest User");
            navHeaderUserEmail.setText("Not logged in");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.getPath() != null) {
            String path = data.getPath();

            if (path.contains("activate")) {
                String token = data.getQueryParameter("token");
                if (token != null) {
                    executeActivation(token);
                }
            }
            else if (path.contains("reset-password")) {
                String token = data.getQueryParameter("token");
                if (token != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("token", token);
                    navController.navigate(R.id.resetPasswordFragment, bundle);
                }
            }
        }
    }

    private void executeActivation(String token) {
        authService.activateAccount(token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Account activated successfully! You can now login.", Toast.LENGTH_LONG).show();
                    if (navController != null) {
                        navController.navigate(R.id.loginFragment);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Activation failed. The link might be expired.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}