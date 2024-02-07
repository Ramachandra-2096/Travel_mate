package com.example.googlemaps.ui.home;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.databinding.FragmentHomeBinding;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    List<PlaceDescription> dummyData;
    PlaceDescriptionAdapter adapter;
    private TextView txt;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        txt=binding.increament;
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        SearchView searchView=binding.searchView2;
        List<String>city=new ArrayList<>();
        ListView search_result= binding.ListviewSearchHome;
        ListAdapter name_adapter=new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, city);
        search_result.setAdapter(name_adapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Address address = Get_geo_info(query);
                if (address != null) {
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    dummyData =getData(latLng);
                    adapter = new PlaceDescriptionAdapter(dummyData);
                    txt.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemCheckedChangeListener(checkedItemCount -> {
                        txt.setText("Selected: " + checkedItemCount);
                    });

                }
                search_result.setVisibility(View.GONE);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()>2) {
                    search_result.setVisibility(View.VISIBLE);
                        // Perform geocoding operation in a separate thread
                        new Thread(() -> {
                            final List<Address> addresses;
                            try {
                                addresses = get_Geo_info(newText);
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
        return root;
    }

    // Helper method to generate dummy data
    private List<PlaceDescription> getData(LatLng latLng) {
        List<PlaceDescription> dummyData = new ArrayList<>();
        dummyData.add(new PlaceDescription("Sunset Over Ocean", "A breathtaking view of the sun setting over the ocean, casting a warm orange glow across the sky.", "https://images.pexels.com/photos/635279/pexels-photo-635279.jpeg?auto=compress&cs=tinysrgb&w=600"));
        dummyData.add(new PlaceDescription("Snow-Capped Mountains", "A stunning panorama of snow-capped mountains against a clear blue sky.", "https://media.istockphoto.com/id/1288385045/photo/snowcapped-k2-peak.jpg?s=2048x2048&w=is&k=20&c=w2Qcpt4yVuD8nfG5pkrxwo0t_aq-fHewpEQX4gRa870="));
        dummyData.add(new PlaceDescription("Serene Lake", "A peaceful scene of a calm lake surrounded by lush greenery.", "https://media.gettyimages.com/id/1306075353/photo/morning-fog-over-a-beautiful-lake-surrounded-by-pine-forest-stock-photo.jpg?s=612x612&w=0&k=20&c=PTH4TzIT6lpLgdHlw7knXEJL7TwmpQX8H5G4Y5nuLaA="));
        dummyData.add(new PlaceDescription("Blossoming Cherry Trees", "Cherry trees in full bloom, creating a sea of pink blossoms.", "https://images.pexels.com/photos/1440476/pexels-photo-1440476.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"));
        dummyData.add(new PlaceDescription("Starry Night Sky", "A clear night sky filled with twinkling stars and a bright moon.", "https://images.pexels.com/photos/9991619/pexels-photo-9991619.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=279.825&fit=crop&h=453.05"));
        dummyData.add(new PlaceDescription("Rainbow Over Field", "A vibrant rainbow arching over a green field after a rain shower.", "https://th.bing.com/th/id/OIP.NE-uXecZgq46-byNJzAMhwHaEK?rs=1&pid=ImgDetMain"));
        dummyData.add(new PlaceDescription("Autumn Forest", "A forest showcasing the beauty of autumn with leaves in shades of red, orange, and yellow.", "https://media.istockphoto.com/id/1066158612/photo/scenic-mountain-landscape-view-on-black-forest-in-germany-covered-in-fog.jpg?s=2048x2048&w=is&k=20&c=EySlbvA5uOg2Vqt76EVq9PJqPDQJOIXVmWckPjmtquk="));
        dummyData.add(new PlaceDescription("Tropical Beach", "A pristine tropical beach with clear blue water and white sand.", "https://cdn.pixabay.com/photo/2017/01/20/00/30/maldives-1993704_640.jpg"));
        dummyData.add(new PlaceDescription("Desert Dunes", "The vast expanse of a desert with towering sand dunes under the scorching sun.", "https://media.istockphoto.com/id/1414943618/photo/the-wind-raises-the-dust-in-desert.jpg?s=2048x2048&w=is&k=20&c=mt2AH-qoBQO0MiRB8Yrm6jClkK6JIWRqf8914YsMiBk="));

        return dummyData;
    }
    public List<Address> get_Geo_info(String name) throws IOException {
        List<Address>address;
        Geocoder geocoder=new Geocoder(getContext(), Locale.getDefault());
        address = geocoder.getFromLocationName(name, 8);
        return address;
    }
    public Address Get_geo_info(String location) {
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
    // Helper method to update ListView with addresses
    private void updateListView(List<Address> addresses) {
        List<String> cityNames = new ArrayList<>();
        for (Address address : addresses) {
            String cityName = address.getLocality();
            if (cityName != null) {
                cityNames.add(cityName); // Change this according to your needs
            }
        }
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, cityNames);
        // Clear the existing data in the adapter
        binding.ListviewSearchHome.setAdapter(null);
        // Set the new adapter with updated data
        binding.ListviewSearchHome.setAdapter(nameAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}