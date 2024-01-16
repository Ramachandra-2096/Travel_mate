package com.example.googlemaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 44;
    String lat = "", lng = "";
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //animation
        ImageView imageView = findViewById(R.id.imageView);
        ImageView imageView_b = findViewById(R.id.imageView1);

        // Create a bounce animation
        ObjectAnimator bounceAnimation = ObjectAnimator.ofFloat(imageView_b, "translationY", 10f, -150f, 0f);
        bounceAnimation.setInterpolator(new BounceInterpolator());
        bounceAnimation.setRepeatCount(ObjectAnimator.INFINITE); // Set to repeat indefinitely
        bounceAnimation.setDuration(3000); // Set the duration of the animation in milliseconds
        // Start the bounce animation when the activity is created
        bounceAnimation.start();
        // Create a fade-in animation for the TextView
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
        logoFadeIn.setDuration(2000); // Set the duration of the animation in milliseconds
        // Create an AnimatorSet to coordinate the start of both animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(bounceAnimation, logoFadeIn); // Play both animations together
        // Start the AnimatorSet when the activity is created
        Intent intent=new Intent(MainActivity.this, MapsActivity.class);
        countDownTimer= new CountDownTimer(3000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                intent.putExtra("latitude",lat);
                intent.putExtra("longitude",lng);
                startActivity(intent);
            }
        };
        countDownTimer.start();
        animatorSet.start();
        if (isPermissionGranted()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            lat = String.valueOf(location.getLatitude());
                            lng = String.valueOf(location.getLongitude());
                            Log.i("message:", "MY location is " + lat + "\t" + lng);
                            if (!lat.isEmpty() && !lng.isEmpty()) {

                            } else {

                            }
                        }
                    }
                });
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