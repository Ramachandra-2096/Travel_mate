package com.example.googlemaps.ui.dashboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.LocationUtils_calculate;
import com.example.googlemaps.MapsActivity;
import com.example.googlemaps.databinding.FragmentDashboardBinding;
import com.example.googlemaps.ui.home.PlaceDescription;
import com.example.googlemaps.ui.home.PlaceDescriptionAdapter;
import com.google.android.gms.internal.location.zzbb;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    String state= null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        // Get SharedPreferences instance
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        // Get SharedPreferences editor to edit preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();

        assert user != null;
        String storedUserName = sharedPreferences.getString("userName", "");
        if (storedUserName.isEmpty()) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String data = dataSnapshot.child("name").getValue(String.class);
                        TextView txt = binding.NamePerson;
                        txt.setText(data);
                        editor.putString("userName", data);
                        editor.apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Network error try again", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            TextView txt = binding.NamePerson;
            txt.setText(storedUserName);
        }
        //updatehistory(user.getUid());
        View root = binding.getRoot();
        final Button buton = binding.button8;
        buton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
        RecyclerView recyclerView=binding.history;
//        List<PlaceDescription> placeDescription=getHistory(user.getUid());
//        PlaceDescriptionAdapter adapter=new PlaceDescriptionAdapter(placeDescription);
//        recyclerView.setAdapter(adapter);
        return root;
    }

    private List<PlaceDescription> getHistory(String uid) {
        List<PlaceDescription> place =new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child("History").child(state);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                String place = dataSnapshot.child("Place").getValue().toString();
                String review = dataSnapshot.child("Description").getValue().toString();
                String photo = dataSnapshot.child("Photo").getValue().toString();
                String rating = dataSnapshot.child("Rating").getValue().toString();
                String new_rating = "";
                if (place.isEmpty()) place = " ";
                if (review.isEmpty() || review.startsWith(" "))
                    review = place + "is one of the best place which you can visit.";
                if (photo.isEmpty()) photo = " ";
                if (rating.isEmpty()) {
                    rating = "3";
                    new_rating = "★★☆☆☆";
                } else {
                    for (int i = 1; i <= 5; i++) {
                        if (i < Math.floor(Float.parseFloat(rating))) {
                            new_rating = new_rating + "★";
                        } else {
                            new_rating = new_rating + "☆";
                        }

                    }
                }
//                dummyData1.add(new PlaceDescription(place, review, photo, new_rating, new LatLng(Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString()), Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString()))));
//                handler.post(() -> liveData.setValue(dummyData1));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        return place;
    }

    private void updatehistory(String uid) {
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("users").child(uid);
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        FusedLocationProviderClient fusedLocationProviderClient = null;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int a=10;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener((Executor) this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Get the last known location of the device
                            Location lastLocation = task.getResult();
                            double latitude = lastLocation.getLatitude();
                            double longitude = lastLocation.getLongitude();
                            try {
                                state = geocoder.getFromLocation(latitude, longitude, 1).get(0).getAdminArea();
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tourist_place").child(state);
                                String finalState = state;
                                databaseReference.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                                        if (LocationUtils_calculate.calculateDistance(new LatLng(Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString()), Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString())), new LatLng(latitude,longitude)) <= 1) {
                                            String key = databaseReference2.child("History").child(finalState).push().getKey();
                                            if (key != null) {
                                                databaseReference.child(key).setValue(dataSnapshot);
                                            }
                                        }
                                    }
                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    }
                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                } );
                            }catch(IOException e){
                                throw new RuntimeException(e);
                            }
                        } else {
                            Toast.makeText(getContext(), "PLease turn on location", Toast.LENGTH_SHORT).show();
                        }
                        }
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}