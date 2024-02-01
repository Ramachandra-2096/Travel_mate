package com.example.googlemaps.ui.home;

// PlaceDescriptionAdapter.java
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.R;
import com.example.googlemaps.databinding.ItemPlaceDescriptionBinding;
import com.bumptech.glide.Glide;
import java.util.List;

public class PlaceDescriptionAdapter extends RecyclerView.Adapter<PlaceDescriptionAdapter.ViewHolder> {

    private List<PlaceDescription> data;

    public PlaceDescriptionAdapter(List<PlaceDescription> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlaceDescriptionBinding binding = ItemPlaceDescriptionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceDescription place = data.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemPlaceDescriptionBinding binding;

        public ViewHolder(ItemPlaceDescriptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PlaceDescription place) {
            binding.titleTextView.setText(place.getTitle());
            binding.descriptionTextView.setText(place.getDescription());
            Glide.with(binding.getRoot())
                    .load(place.getImageUrl())
                    .placeholder(R.drawable.bg)
                    .error(R.drawable.bg)
                    .into(binding.imageView);
        }
    }
}
