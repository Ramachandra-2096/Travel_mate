package com.example.googlemaps.ui.home;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.databinding.FragmentHomeBinding;
import com.google.android.gms.maps.model.LatLng;
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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<PlaceDescription> dummyData;
    private Handler handler = new Handler(Looper.getMainLooper());
    private PlaceDescriptionAdapter adapter;
    private TextView txt;
    private View root;
    private MutableLiveData<List<PlaceDescription>> liveData = new MutableLiveData<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        txt = binding.increament;

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

        return root;
    }

    private void getDataInBackground(LatLng latLng) {
        new Thread(() -> {
            List<PlaceDescription> data = getData(latLng);
            // Use handler to post UI updates to the main thread
            handler.post(() -> liveData.setValue(data));
        }).start();
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
            databaseReference.limitToFirst(100).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                    String place = dataSnapshot.child("Place").getValue().toString();
                    String review = dataSnapshot.child("Review").getValue().toString();
                    String photo = dataSnapshot.child("Photo").getValue().toString();
                    if (place.isEmpty()) place = " ";
                    if (review.isEmpty()) review = " ";
                    if (photo.isEmpty()) photo = " ";
                    dummyData1.add(new PlaceDescription(place, review, photo));
                    Log.d("TAG", "onChildAdded: " + place + review + photo);

                    // Use handler to post UI updates to the main thread
                    handler.post(() -> liveData.setValue(dummyData1));
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
