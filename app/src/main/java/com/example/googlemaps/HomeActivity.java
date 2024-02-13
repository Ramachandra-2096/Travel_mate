package com.example.googlemaps;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.googlemaps.Utility.NetworkChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
private ActivityHomeBinding binding;
NetworkChangeListener networkChangeListener=new NetworkChangeListener();

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
        Button b= findViewById(R.id.selectbtn);
        b.setVisibility(View.GONE);
        SearchView searchView=findViewById(R.id.searchView2);
        ListView list=findViewById(R.id.Listview_search_home);
        RecyclerView rec1=findViewById(R.id.recyclerView);
        rec1.setVisibility(View.GONE);
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

    @Override
    protected void onStart() {
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}