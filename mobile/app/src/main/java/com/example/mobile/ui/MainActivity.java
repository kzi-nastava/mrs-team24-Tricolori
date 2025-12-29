package com.example.mobile.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mobile.R;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicijalizacija View-ova
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // 2. Nav Controller Setup
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // 3. Konfiguracija AppBar-a (Top Level destinacije)
        // Ovde definišemo ekrane koji imaju Hamburger meni (ne strelicu nazad)
        Set<Integer> topLevelDestinations = new HashSet<>();

        // PAŽNJA: Ovi ID-evi moraju postojati u nav_graph.xml
        topLevelDestinations.add(R.id.rideHistoryFragment);
        topLevelDestinations.add(R.id.userProfileFragment);
        // Ako je changePasswordFragment takođe u meniju kao glavna stavka, dodaj i njega:
        // topLevelDestinations.add(R.id.changePasswordFragment);

        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations)
                .setOpenableLayout(drawerLayout)
                .build();

        // 4. Povezivanje komponenti
        if (navController != null) {
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

            // Logika za sakrivanje menija na Login/Register ekranima
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();

                // Sakrij Toolbar na Auth ekranima
                if (id == R.id.loginFragment || id == R.id.registerFragment ||
                        id == R.id.forgotPasswordFragment || id == R.id.resetPasswordFragment) {

                    toolbar.setVisibility(View.GONE);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            });

            // 5. Posebna logika za Log Out
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_logout) {
                    Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                    // Vrati na login i obriši back stack (da back ne vraća u app)
                    navController.navigate(R.id.loginFragment);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                // Za ostale stavke (History, Profile) koristi default navigaciju
                // Ovo radi samo ako je ID u meniju ISTI kao ID u nav_graph!
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return handled;
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}