package com.example.googlemaps;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.googlemaps.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     binding = ActivityHomeBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
    @Override
    public void onBackPressed() {
        SearchView searchView=findViewById(R.id.searchView2);
        ListView list=findViewById(R.id.Listview_search_home);
        if (searchView.isIconified()) {
            // Search view is not active, ask for exit
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to exit?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                super.onBackPressed(); // Let the system handle the back press
                finishAffinity();
            });
            builder.setNegativeButton("No", (dialog, which) -> {
            });
            builder.show();
        } else {
            searchView.clearFocus();
            list.setVisibility(View.GONE);
            // Search view is active, close it
            searchView.setIconified(true);
        }
    }



}