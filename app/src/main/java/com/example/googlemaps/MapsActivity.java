package com.example.googlemaps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker selected_marker;
    private List<Marker> markerList = new ArrayList<>();
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private long lastSpeechEndTime = 0;
    private Marker m1;
    static Polyline poly;
    BitmapDescriptor customMarker;
    private ProgressDialog PD;
    private SearchView Map_Search;
    private ListView listView;
    public boolean is_map_searched = false;
    private ArrayAdapter<String> searchAdapter;
    private List<String> searchResults;
    private Marker usermarker;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    LatLng usr;
    boolean location_added = false;
    private ProgressDialog progressDialog;
    static public boolean isJourney_Started = false;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        PD = new ProgressDialog(MapsActivity.this);
        listView = findViewById(R.id.listView);
        searchResults = new ArrayList<>();
        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchResults);
        listView.setAdapter(searchAdapter);
        progressDialog = new ProgressDialog(MapsActivity.this);
        LocationUtils.checkLocationStatus(this, new LocationUtils.OnLocationEnabledListener() {
            @Override
            public void onLocationEnabled() {
            }

            @Override
            public void onLocationDisabled() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setMessage("Location services are disabled. Please turn on location.")
                        .setCancelable(false)
                        .setPositiveButton("Turn On Location", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                finish();
                startActivity(getIntent());
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Map_Search = findViewById(R.id.mapsearch);
        Map_Search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                is_map_searched = true;
                String location = Map_Search.getQuery().toString();
                Address address = Get_geo_info(location);
                if (address != null) {
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    if (m1 != null) {
                        m1.remove();
                    }
                    mMap.clear();
                    listView.setVisibility(View.GONE);
                    SphericalUtil PolyUtil = null;
                    List<LatLng> polylinePoints = Arrays.asList(usr, latLng);
                    float distance = (float) PolyUtil.computeLength(polylinePoints);
                    isJourney_Started=false;
                    double distanceInKm = distance / 1000.0;
                    double roundedDistance = Math.round(distanceInKm * 100.0) / 100.0; // Round to 2 decimal places
                    DecimalFormat df = new DecimalFormat("#.##");
                    String formattedDistance = df.format(roundedDistance);

                    m1 = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(address.getFeatureName())
                            .snippet(address.getAddressLine(0) + "\nDistance: " + formattedDistance + "Km from Your Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18), 5000, null);
                    Button b4 = findViewById(R.id.button4);
                    b4.setVisibility(View.VISIBLE);
                    b4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            b4.setVisibility(View.GONE);
                            progressDialog.setMessage("Updating Map...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            get_loc_tour_for_search(latLng);
                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (s == null || s.trim().isEmpty()) {

                    listView.setVisibility(View.GONE);
                } else {
                    new GeocodeTask().execute(s);
                    listView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        // Check for runtime location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        Button optionsButton = findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(this::showOptionsPopup);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedResult = searchResults.get(position);
            Address s_add = Get_geo_info(selectedResult);
            if (s_add != null) {
                LatLng latLng = new LatLng(s_add.getLatitude(), s_add.getLongitude());
                if (m1 != null) {
                    m1.remove();
                }
                mMap.clear();
                listView.setVisibility(View.GONE);
                SphericalUtil PolyUtil = null;
                List<LatLng> polylinePoints = Arrays.asList(usr, latLng);
                is_map_searched = true;
                float distance = (float) PolyUtil.computeLength(polylinePoints);
                isJourney_Started=false;
                double distanceInKm = distance / 1000.0;
                double roundedDistance = Math.round(distanceInKm * 100.0) / 100.0; // Round to 2 decimal places
                DecimalFormat df = new DecimalFormat("#.##");
                String formattedDistance = df.format(roundedDistance);
                m1 = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(s_add.getFeatureName())
                        .snippet(s_add.getAddressLine(0) + "\nDistance: " + formattedDistance+ "Km from Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18), 5000, null);
                Button b4 = findViewById(R.id.button4);
                b4.setVisibility(View.VISIBLE);
                b4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        b4.setVisibility(View.GONE);
                        progressDialog.setMessage("Updating Map...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        get_loc_tour_for_search(latLng);
                    }
                });

                listView.setVisibility(View.GONE);
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                lastSpeechEndTime = System.currentTimeMillis();
            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

    }

    // Function for setting up geocoder
    public Address Get_geo_info(String location) {
        List<Address> addressList;
        Address address = null;
        if (location != null && !location.isEmpty()) {
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            try {
                addressList = geocoder.getFromLocationName(location, 1);
                if (addressList != null && !addressList.isEmpty()) {
                    address = addressList.get(0);
                } else {
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
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, location -> {
            String lat = null;
            String lng = null;
            LatLng userLocation;
                    if (location != null) {
                        lat = String.valueOf(location.getLatitude());
                        lng = String.valueOf(location.getLongitude());
                    }
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
                        userLocation = new LatLng(latitude, longitude);
                        usr = userLocation;
                        mMap.setTrafficEnabled(true);
                        mMap.setBuildingsEnabled(true);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18), 4000, null); // Adjust the zoom level as desired
                    } else {
                        startLocationUpdates();
                    }
                });
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        TextView t = findViewById(R.id.textView);
                        Button b = findViewById(R.id.button3);
                        if (is_map_searched) {
                            Button b5 = findViewById(R.id.button4);
                            b5.setVisibility(View.VISIBLE);
                            t.setVisibility(View.GONE);
                            b.setVisibility(View.GONE);
                        }
                        Button b1 = findViewById(R.id.optionsButton);
                        Button b2 = findViewById(R.id.optionsButton1);
                        Button b3 = findViewById(R.id.button2);
                        Button b4 = findViewById(R.id.button);
                        t.setVisibility(View.GONE);
                        b.setVisibility(View.GONE);
                        b1.setVisibility(View.VISIBLE);
                        b2.setVisibility(View.VISIBLE);
                        b3.setVisibility(View.VISIBLE);
                        b4.setVisibility(View.VISIBLE);
                    }
                });
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng latLng) {
                        location_added = false;
                        is_map_searched=false;
                        mMap.clear();

                    }
                });
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        TextView t = findViewById(R.id.textView);
                        Button b = findViewById(R.id.button3);
                        if (!marker.getTitle().toString().equals("Your Location")) {
                            Button b1 = findViewById(R.id.optionsButton);
                            Button b2 = findViewById(R.id.optionsButton1);
                            Button b3 = findViewById(R.id.button2);
                            Button b4 = findViewById(R.id.button);
                            Button b5 = findViewById(R.id.button4);
                            if(!isJourney_Started)
                            {
                                t.setVisibility(View.VISIBLE);
                                b.setVisibility(View.VISIBLE);
                            }else{
                                b.setVisibility(View.GONE);
                                t.setVisibility(View.VISIBLE);
                            }
                            b5.setVisibility(View.GONE);
                            b1.setVisibility(View.GONE);
                            b2.setVisibility(View.GONE);
                            b3.setVisibility(View.GONE);
                            b4.setVisibility(View.GONE);
                            selected_marker = marker;
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            SpannableString content = new SpannableString(marker.getTitle() + "\n" + marker.getSnippet());
                            content.setSpan(new RelativeSizeSpan(1.5f), 0, marker.getTitle().toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            content.setSpan(new RelativeSizeSpan(0.8f), marker.getTitle().toString().length(), marker.getSnippet().toString().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                            t.setText(content);
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mMap.clear();
                                    PD.setMessage("Selecting Best Rout For you, please wait...");
                                    PD.show();
                                    List<LatLng> waypoints = new ArrayList<>();
                                    waypoints.add(usr);
                                    waypoints.add(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
                                    RoutingTask rtsk = new RoutingTask(mMap, poly);
                                    rtsk.execute(waypoints);
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(marker.getPosition())
                                            .title(marker.getTitle().toString())
                                            .snippet(marker.getSnippet().toString())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                                    mMap.addMarker(markerOptions);
                                    startLocationUpdates();
                                    t.setVisibility(View.GONE);
                                    b.setVisibility(View.GONE);
                                    PD.dismiss();
                                    b1.setVisibility(View.VISIBLE);
                                    b2.setVisibility(View.VISIBLE);
                                    b3.setVisibility(View.VISIBLE);
                                    b4.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        return true;
                    }
                });

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
                    usr =userLocation;
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
        }else{
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .icon(customMarker);
            usermarker = mMap.addMarker(markerOptions);
        }

    }

    public void showOptionsPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            // Handle the selected option
            String selectedOption = Objects.requireNonNull(item.getTitle()).toString();
            setupMap(selectedOption);
            return true;
        });

        popupMenu.show();
    }

    public void showHospitals(View view) {
        addMarkers();
    }
//Markers
@SuppressLint("StaticFieldLeak")
private void addMarkers() {
    new AsyncTask<Void, Void, List<Address>>() {

        @SuppressLint("StaticFieldLeak")
        @Override
        protected List<Address> doInBackground(Void... voids) {
            try {
                Geocoder ge = new Geocoder(MapsActivity.this);
                final List<Address>[] a = new List[]{null};
                if(usr==null)
                {
                    LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            // Handle location updates here
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            try {
                                a[0] = ge.getFromLocation(latitude, longitude, 1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    return a[0];
                }
                return ge.getFromLocation(usr.latitude, usr.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if (addresses != null && addresses.size() > 0) {
                String adminArea = addresses.get(0).getAdminArea();
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

                mDatabase.child("Hospital").child(adminArea).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            if (dataSnapshot != null) {
                                int i = 0;
                                for (DataSnapshot hospitalSnapshot : dataSnapshot.getChildren()) {
                                    String hospitalName = hospitalSnapshot.getKey();
                                    double latitude = hospitalSnapshot.child("Lat ").getValue(Double.class);
                                    double longitude = hospitalSnapshot.child("Long ").getValue(Double.class);
                                    LatLng location = new LatLng(latitude, longitude);
                                    if (!markerExists(location)) {
                                        double distance = LocationUtils_calculate.calculateDistance(new LatLng(usr.latitude,usr.longitude), location);
                                        if (distance <= 50) {
                                            addMarker(location, "Hospital " + (++i), hospitalName);
                                        }
                                        if(i<4)
                                        {
                                            if (distance <= 100) {
                                                addMarker(location, "Hospital " + (++i), hospitalName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }.execute();
}

    private boolean markerExists(LatLng location) {
        for (Marker marker : markerList) {
            if (marker.getPosition().equals(location)) {
                return true;
            }
        }
        return false;
    }


    // Function to add a single marker
    private void addMarker(LatLng location, String title, String snippet) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(title)
                .snippet(snippet);
        Marker marker = mMap.addMarker(markerOptions);
        markerList.add(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12), 2000, null);
    }

    public void showLocation(View view) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                if (usermarker != null) {
                    usermarker.remove();
                }
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                usr=userLocation;
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
                usermarker = mMap.addMarker(markerOptions);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20), 4000, null);
            } else {
                showLocation(view);
            }
        });
    }
    public  void  openmic(View view)
    {
        convertSpeechToText();
    }
    private class GeocodeTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            List<String> resultCities = new ArrayList<>();
            List<Address>add;
            String query = params[0];
            if(query.length()>=3) {
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                try {
                    add = geocoder.getFromLocationName(query, 8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (add != null && !add.isEmpty()) {
                    for (Address a : add) {
                        if (a != null) {
                            resultCities.add(a.getAddressLine(0));
                        }
                    }
                }
            }
            return resultCities;
        }
        @Override
        protected void onPostExecute(List<String> resultCities) {
            // Update the list view with the new city names
            searchResults.clear();
            searchAdapter.addAll(resultCities);
            searchAdapter.notifyDataSetChanged();
        }
    }
    public void get_loc_tour(View view) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        progressDialog.setMessage("Updating Map please wait...");
        progressDialog.show();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Geocoder geo = new Geocoder(MapsActivity.this);
                try {
                    List<Address> addresses = geo.getFromLocation(usr.latitude, usr.longitude, 1);
                    // Use addresses if needed

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("Tour").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                DataSnapshot dataSnapshot = task.getResult();
                                if (dataSnapshot != null) {
                                    for (DataSnapshot tourSnapshot : dataSnapshot.getChildren()) {
                                        String placeName = tourSnapshot.getKey();
                                        double latitude = tourSnapshot.child("Lat").getValue(Double.class);
                                        double longitude = tourSnapshot.child("Long").getValue(Double.class);
                                        LatLng location = new LatLng(latitude, longitude);
                                            double distance = LocationUtils_calculate.calculateDistance(new LatLng(usr.latitude, usr.longitude), location);
                                            if (distance <= 100) {
                                                updateMapForTour(tourSnapshot,usr);
                                                location_added =true;
                                            }
                                    }
                                    if(!location_added)
                                    {
                                        Toast.makeText(MapsActivity.this, "Data not found for your location", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });progressDialog.dismiss();

                } catch (IOException e) {
                    progressDialog.dismiss();
                    throw new RuntimeException(e);

                }
            }
        });
    }


    public void get_loc_tour_for_search(LatLng lction) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Geocoder geo = new Geocoder(MapsActivity.this);
                try {
                    List<Address> addresses = geo.getFromLocation(lction.latitude, lction.longitude, 1);
                    // Use addresses if needed

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("Tour").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                DataSnapshot dataSnapshot = task.getResult();
                                if (dataSnapshot != null) {
                                    for (DataSnapshot tourSnapshot : dataSnapshot.getChildren()) {
                                        String placeName = tourSnapshot.getKey();
                                        double latitude = tourSnapshot.child("Lat").getValue(Double.class);
                                        double longitude = tourSnapshot.child("Long").getValue(Double.class);
                                        LatLng location = new LatLng(latitude, longitude);
                                            double distance = LocationUtils_calculate.calculateDistance(new LatLng(lction.latitude, lction.longitude), location);
                                            if (distance <= 100) {
                                                updateMapForTour(tourSnapshot,lction);
                                                location_added =true;
                                            }
                                    }
                                    if(!location_added)
                                    {
                                        Toast.makeText(MapsActivity.this, "Data not found for This location", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            }
                        }
                    });
                    progressDialog.dismiss();
                } catch (IOException e) {
                    progressDialog.dismiss();
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void updateMapForTour(DataSnapshot tourSnapshot,LatLng usr) {
        for (DataSnapshot place : tourSnapshot.getChildren()) {
            if (place != null && !(place.getKey().equals("Lat") || place.getKey().equals("Long"))) {
                double place_lat = place.child("Lat ").getValue(Double.class);
                double place_long = place.child("Long ").getValue(Double.class);
                LatLng lov = new LatLng(place_lat, place_long);

                double place_distance = LocationUtils_calculate.calculateDistance(new LatLng(usr.latitude, usr.longitude), lov);
                int markerColor = getMarkerColor(place_distance);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(lov)
                        .title(place.getKey())
                        .snippet(place.child("Description").getValue().toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

                Marker marker = mMap.addMarker(markerOptions);
                markerList.add(marker);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lov, 12), 2000, null);
            }
        }
    }
    private int getMarkerColor(double place_distance) {
        if (place_distance <= 20) {
            return (int) BitmapDescriptorFactory.HUE_GREEN;
        } else if (place_distance <= 40) {
            return (int) BitmapDescriptorFactory.HUE_ORANGE;
        } else {
            return (int) BitmapDescriptorFactory.HUE_ROSE;
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
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
    private void convertSpeechToText() {
        try {
            startActivityForResult(speechRecognizerIntent, 1);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(MapsActivity.this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String text = matches.get(0);
                Map_Search.setQuery(text,true);
            }
        }

        // Check for a pause between speeches
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpeechEndTime > 4000) { // Adjust the pause duration as needed (e.g., 2000 milliseconds)
            stopSpeechRecognition();
        }
    }

    private void stopSpeechRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
        }
    }
}