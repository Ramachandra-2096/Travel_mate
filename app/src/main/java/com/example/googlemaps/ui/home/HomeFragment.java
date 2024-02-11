package com.example.googlemaps.ui.home;


import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.googlemaps.LocationUtils_calculate;
import com.example.googlemaps.R;
import com.example.googlemaps.databinding.FragmentHomeBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<PlaceDescription> dummyData;
    private Handler handler = new Handler(Looper.getMainLooper());
    private PlaceDescriptionAdapter adapter;
    private TextView txt;
    private View root;
    private  RecyclerView recyclerView2;
    private List<Recommendation> recomen;
    private RecommendationAdapter recadaptor;
    private MutableLiveData<List<PlaceDescription>> liveData = new MutableLiveData<>();
    private int currentPosition = 0;
    private static final int AUTO_SCROLL_DELAY = 3000; // Adjust the delay as needed
    private Handler handler2;
    private Runnable autoScrollRunnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        txt = binding.increament;

        recyclerView2 = root.findViewById(R.id.recyclerView2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView2);
        recomen = getYourPlaceDescriptionData();
        recadaptor = new RecommendationAdapter(getActivity(), recomen);
        recyclerView2.setAdapter(recadaptor);


        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        SearchView searchView = binding.searchView2;
        List<String> city = new ArrayList<>();
        ListView searchResult = binding.ListviewSearchHome;
        ListAdapter nameAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, city);
        searchResult.setAdapter(nameAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Address address = GetGeoInfo(query);
                if (address != null) {
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    getDataInBackground(latLng);
                }
                searchResult.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    searchResult.setVisibility(View.VISIBLE);
                    // Perform geocoding operation in a separate thread
                    new Thread(() -> {
                        try {
                            List<Address> addresses = getGeoInfo(newText);
                            // Update UI on the main thread
                            requireActivity().runOnUiThread(() -> updateListView(addresses));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
                return false;
            }
        });

        liveData.observe(getViewLifecycleOwner(), placeDescriptions -> {
            if (placeDescriptions != null) {
                // Update UI with the new data
                adapter = new PlaceDescriptionAdapter(placeDescriptions);
                txt.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(adapter);
                adapter.setOnItemCheckedChangeListener(checkedItemCount -> {
                    txt.setText("Selected: " + checkedItemCount);
                });
            }
        });
        handler2 = new Handler();
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (recadaptor.getItemCount() > 0) {
                    currentPosition = (currentPosition + 1) % recadaptor.getItemCount();
                    recyclerView2.smoothScrollToPosition(currentPosition);
                    handler.postDelayed(this, AUTO_SCROLL_DELAY);
                }
            }
        };

        // Start auto-scrolling when the activity is created
        startAutoScroll();

        // Stop auto-scrolling when the user interacts with the RecyclerView
        recyclerView2.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                stopAutoScroll();
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        return root;
    }

    private void startAutoScroll() {
        handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    private void stopAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable);
    }

    private void getDataInBackground(LatLng latLng) {
        new Thread(() -> {
            List<PlaceDescription> data = getData(latLng);
            handler.post(() -> liveData.setValue(data));
        }).start();
    }
    private List<Recommendation> getYourPlaceDescriptionData() {
        List<Recommendation> placeList = new ArrayList<>();
        placeList.add(new Recommendation("Title1","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title2","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title3","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title4","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title5","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title6","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title7","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title8","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title9","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        placeList.add(new Recommendation("Title10","https://www.bing.com/th?id=OIP.QLkEeaC1VM6Jme-LBaCpeQHaE3&w=163&h=100&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        return placeList;
    }

    private List<PlaceDescription> getData(LatLng latLng) {
        List<PlaceDescription> dummyData1 = new ArrayList<>();
        Geocoder geo = new Geocoder(getContext(), Locale.getDefault());
        String state = "";
        try {
            List<Address> address = geo.getFromLocation(latLng.latitude, latLng.longitude, 2);
            assert address != null;
            for (Address a : address) {
                state = a.getAdminArea();
            }
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tourist_place").child(state);
            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    String place = dataSnapshot.child("Place").getValue().toString();
                    String review = dataSnapshot.child("Description").getValue().toString();
                    String photo = dataSnapshot.child("Photo").getValue().toString();
                    String rating = dataSnapshot.child("Rating").getValue().toString();
                    String new_rating="";
                    if (place.isEmpty()) place = " ";
                    if (review.isEmpty()) review = " ";
                    if (photo.isEmpty()) photo = " ";
                    if (rating.isEmpty()){
                        rating = "3";
                        new_rating="★★☆☆☆";
                    }else{
                        for(int i=1;i<=5;i++)
                        {
                            if(i<Math.floor(Float.parseFloat(rating)))
                            {
                                new_rating=new_rating + "★";
                            }else {
                                new_rating=new_rating + "☆";
                            }

                        }
                    }

                    if(LocationUtils_calculate.calculateDistance(new LatLng(Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString()), Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString())),new LatLng(address.get(0).getLatitude(),address.get(0).getLongitude()))<=20)
                    {
                        dummyData1.add(new PlaceDescription(place, review, photo,new_rating,new LatLng(Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString()),Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString()))));
                        // Use handler to post UI updates to the main thread
                        handler.post(() -> liveData.setValue(dummyData1));
                    }
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    // Handle changes to existing data if needed
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    // Handle removal of data if needed
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    // Handle movement of data if needed
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle potential errors
                    Log.e("ChildCount", "Error retrieving data: " + databaseError.getMessage());
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dummyData1;
    }

    public List<Address> getGeoInfo(String name) throws IOException {
        List<Address> address;
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        address = geocoder.getFromLocationName(name, 8);
        return address;
    }

    public Address GetGeoInfo(String location) {
        List<Address> addressList;
        Address address = null;
        if (location != null && !location.isEmpty()) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                addressList = geocoder.getFromLocationName(location, 6);
                if (addressList != null && !addressList.isEmpty()) {
                    address = addressList.get(0);
                } else {
                    // No address found
                    Toast.makeText(getContext(), "No address found for the given location", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                // Handle geocoding exceptions
                Toast.makeText(getContext(), "Geocoding error: Check spelling and try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace(); // Log the exception for further debugging if needed
            }
        } else {
            // Empty or null location
            Toast.makeText(getContext(), "Please enter a valid location", Toast.LENGTH_SHORT).show();
        }
        return address;
    }

    private void updateListView(List<Address> addresses) {
        List<String> cityNames = new ArrayList<>();
        for (Address address : addresses) {
            String cityName = address.getLocality();
            if (cityName != null) {
                cityNames.add(cityName);
            }
        }

        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, cityNames);

        // Use handler to post UI updates to the main thread
        handler.post(() -> {
            // Clear the existing data in the adapter
            binding.ListviewSearchHome.setAdapter(null);
            // Set the new adapter with updated data
            binding.ListviewSearchHome.setAdapter(nameAdapter);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
