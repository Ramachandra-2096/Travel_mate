package com.example.googlemaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
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
                if(is_first_time)
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(is_first,false);
                    editor.apply();
                    startActivity(intent);
                }
                else{
                    startActivity(new Intent(MainActivity.this, Loginsighnup2.class));
                }

            }
        };
        countDownTimer.start();
        animatorSet.start();
        if (isPermissionGranted()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        }

    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}