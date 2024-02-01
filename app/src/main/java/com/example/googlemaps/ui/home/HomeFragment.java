package com.example.googlemaps.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // New RecyclerView setup
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Dummy data for RecyclerView
        List<PlaceDescription> dummyData = getDummyData();
        PlaceDescriptionAdapter adapter = new PlaceDescriptionAdapter(dummyData);
        recyclerView.setAdapter(adapter);

        return root;
    }

    // Helper method to generate dummy data
    private List<PlaceDescription> getDummyData() {
        List<PlaceDescription> dummyData = new ArrayList<>();
        dummyData.add(new PlaceDescription("Title 1", "Description 1", "https://th.bing.com/th?id=OIP.DpcLyyRCeTWoiiMNdCTXxQHaEK&w=333&h=187&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2"));
        dummyData.add(new PlaceDescription("Title 2", "Description 2", "https://example.com/image2.jpg"));
        dummyData.add(new PlaceDescription("Title 3", "Description 3", "https://example.com/image3.jpg"));
        return dummyData;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}