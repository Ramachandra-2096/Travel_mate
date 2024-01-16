package com.example.googlemaps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker m1,mark;
    BitmapDescriptor customMarker;
    private SearchView Map_Search;
    private Marker usermarker;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Map_Search = findViewById(R.id.mapsearch);
        Map_Search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location = Map_Search.getQuery().toString();
                Address address = Get_geo_info(location);
                if (address != null) {
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    if (m1 != null) {
                        m1.remove();
                    }
                    m1 = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(address.getFeatureName())
                            .snippet(address.getAddressLine(0))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18), 5000, null);
                }
                    return false;
                }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        // Check for runtime location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        Button optionsButton = findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsPopup(v);
            }
        });
    }
    // Function for setting up geocoder
public Address Get_geo_info(String location)
{
    List<Address> addressList = null;
    Address address = null;
    if (location != null && !location.isEmpty()) {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                address = addressList.get(0);
            }else {
            // No address found
            Toast.makeText(MapsActivity.this, "No address found for the given location", Toast.LENGTH_SHORT).show();
        }
        } catch (IOException e) {
            // Handle geocoding exceptions
            Toast.makeText(MapsActivity.this, "Geocoding error: Check spelling and try again", Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // Log the exception for further debugging if needed
        }
    } else {
        // Empty or null location
        Toast.makeText(MapsActivity.this, "Please enter a valid location", Toast.LENGTH_SHORT).show();
    }
    return address;
}
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap("");
        Intent intent = getIntent();
        String lat = intent.getStringExtra("latitude");
        String lng = intent.getStringExtra("longitude");

        if (lat != null && lng != null && !lat.isEmpty() && !lng.isEmpty()) {
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lng);
            // Get the custom marker layout as a Bitmap
            IconGenerator iconGenerator = new IconGenerator(this); // 'this' refers to the context of the current activity
            View markerLayout = LayoutInflater.from(this).inflate(R.layout.custom_marker_layout, null);
            iconGenerator.setContentView(markerLayout);
// Create a BitmapDescriptor from the custom marker layout
            customMarker = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon());
// Add the custom marker to the map
            LatLng userLocation = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .icon(customMarker);

            mark =mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18),4000,null); // Adjust the zoom level as desired
        }else {
            startLocationUpdates();
        }

    }
    private void setupMap(String setting) {
        switch (setting) {
            case "Satellite Mode":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "Hybrid Mode":
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "Normal Mode":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Terrain Mode":
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000); // Update every 5 seconds
        locationRequest.setFastestInterval(500); // Fastest update interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    LatLng userLocation = new LatLng(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude());
                    // Update the marker on the map
                    updateMarker(userLocation);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateMarker(LatLng userLocation) {
        // Get the custom marker layout as a Bitmap
        IconGenerator iconGenerator = new IconGenerator(this);
        View markerLayout = LayoutInflater.from(this).inflate(R.layout.custom_marker_layout, null);
        iconGenerator.setContentView(markerLayout);
        // Create a BitmapDescriptor from the custom marker layout
        customMarker = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon());
        // Remove existing marker and add the updated marker to the map
        if (usermarker != null) {
            usermarker.remove();
            // Clear existing marker
            // Add the updated marker to the map without changing the camera position
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .icon(customMarker);
            usermarker = mMap.addMarker(markerOptions);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m1 != null) {
            m1.remove();
        }
        // Remove existing marker and recycle the customMarker bitmap
        if (usermarker != null) {
            usermarker.remove();
        }
        // Stop location updates when the activity is destroyed
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public void showOptionsPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the selected option
                String selectedOption = item.getTitle().toString();
                setupMap(selectedOption);
                return true;
            }
        });

        popupMenu.show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    public void showHospitals(View view)
    {
        addMarkers();
    }
//Markers
    private void addMarkers() {
        int i=0;
        Map<String, String> hospitalCoordinates = new HashMap<>();
        hospitalCoordinates.put("Lombard Memorial Hospital Drug Store", "13.327770, 74.751940");
        hospitalCoordinates.put("Adarsha Hospital", "13.343730, 74.746640");
        hospitalCoordinates.put("Dr TMA Pai Hospital", "13.334330, 74.748060");
        hospitalCoordinates.put("Gandhi Hospital", "13.335330, 74.756000");
        hospitalCoordinates.put("Hitech Medicare Hospital", "13.322520, 74.739650");
        hospitalCoordinates.put("Karnataka Government Koosamma Shambhu Shetty Memorial Haji Abdullah Mother & Child Hospital(MCH UDUPI)", "13.344780, 74.753260");
        hospitalCoordinates.put("City Hospital And Nursing College", "13.352223, 74.744180");
        hospitalCoordinates.put("Dr. A. V. Baliga Memorial Hospital", "13.363841752532997, 74.76180063683583");
        hospitalCoordinates.put("Mitra Hospital1", "13.324840, 74.768640");
        hospitalCoordinates.put("District Hospital Udupi", "13.334590, 74.743020");
        hospitalCoordinates.put("Manipal Hospital2", "13.304878926591606, 74.7358797693113");
        hospitalCoordinates.put("Government Maternity And Children Hospital", "13.34212907066328, 74.74841104963774");
        hospitalCoordinates.put("New City Hospital", "13.347386991604832, 74.74414691584089");
        hospitalCoordinates.put("Manipal Hospital3", "13.361082612408323, 74.78946551866522");
        hospitalCoordinates.put("Lalith Hospital", "13.346050791877484, 74.74792346607624");
        hospitalCoordinates.put("Ajjarkadu", "13.33736531348114, 74.74414691584087");
        hospitalCoordinates.put("SDM Ayurveda Hospital,Kuthpady,Udupi", "13.3193252457573, 74.73281726513478");
        hospitalCoordinates.put("Dr TMA Pai Hospital Udupi", "13.328679522927885, 74.73968372010818");
        hospitalCoordinates.put("Sunad Hospital", "13.35339979887379, 74.76337298976635");
        // Add markers using LatLng for each location
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Map.Entry<String, String> entry : hospitalCoordinates.entrySet()) {
            String hospitalName = entry.getKey();
            Address address = Get_geo_info(hospitalName+" Udupi");
            LatLng location = new LatLng(address.getLatitude(),address.getLongitude());
            if (!markerExists(location)) {
                addMarker(location, "Hospital " + (++i), hospitalName);
            }
            builder.include(location); // Include the marker's position in the bounding box
        }
        // Move the camera to include all markers in the bounding box
        LatLngBounds bounds = builder.build();
        int padding = 50; // Padding in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);
        // Add more markers as needed
    }
    private boolean markerExists(LatLng location) {

        return false;
    }
    // Function to add a single marker
    private void addMarker(LatLng location, String title, String snippet) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(title)
                .snippet(snippet);
        mMap.addMarker(markerOptions);
    }
    public void showLocation(View view) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    if (mark != null) {
                        mark.remove();
                    }
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    IconGenerator iconGenerator = new IconGenerator(MapsActivity.this); // 'this' refers to the context of the current activity
                    View markerLayout = LayoutInflater.from(MapsActivity.this).inflate(R.layout.custom_marker_layout, null);
                    iconGenerator.setContentView(markerLayout);
// Create a BitmapDescriptor from the custom marker layout
                    customMarker = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon());
// Add the custom marker to the mark
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(userLocation)
                            .title("Your Location")
                            .icon(customMarker);

                    mark = mMap.addMarker(markerOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20), 4000, null);
                } else {
                    showLocation(view);
                }
            }
        });
    }
}