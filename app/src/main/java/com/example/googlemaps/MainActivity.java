package com.example.googlemaps;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 44;
    CountDownTimer countDownTimer;
    private static final String is_first = "is_first";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //animation
        ImageView imageView = findViewById(R.id.imageView);
        ImageView imageView_b = findViewById(R.id.imageView1);

        // Create a bounce animation
        ObjectAnimator bounceAnimation = ObjectAnimator.ofFloat(imageView_b, "translationY", 0f, -70f, 0f);
        bounceAnimation.setInterpolator(new BounceInterpolator());
        bounceAnimation.setDuration(2000); // Set the duration of the animation in milliseconds
        // Start the bounce animation when the activity is created

        // Create a fade-in animation for the TextView
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
        logoFadeIn.setDuration(2000); // Set the duration of the animation in milliseconds
        // Create an AnimatorSet to coordinate the start of both animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(bounceAnimation, logoFadeIn); // Play both animations together
        // Start the AnimatorSet when the activity is created
        Intent intent=new Intent(MainActivity.this, Loginactivity.class);
        countDownTimer= new CountDownTimer(2000,500) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                boolean is_first_time = sharedPreferences.getBoolean(is_first, true);
                Intent intent = new Intent(MainActivity.this, Loginactivity.class);

                if (is_first_time) {
                    startActivity(intent);
                } else {
                    if (isPermissionGranted()) {
                        if (isLocationEnabled()) {
                            // Permissions granted and location enabled, proceed to the next activity
                            startActivity(new Intent(MainActivity.this, Loginsighnup2.class));
                        } else {
                            // Location is not enabled, show the dialog to prompt the user to enable it
                            checkLocationEnabled();
                        }
                    } else {
                        // Permissions not granted, request permissions
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
                    }
                }
            }

        };
        countDownTimer.start();
        animatorSet.start();
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location is not enabled, show a dialog to prompt the user to enable it
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Location is not enabled. Do you want to enable it?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Open location settings
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    recreate();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   System.exit(0);
                }
            });
            builder.show();
        }
    }

}