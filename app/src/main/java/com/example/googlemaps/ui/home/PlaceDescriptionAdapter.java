package com.example.googlemaps.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlemaps.R;
import com.example.googlemaps.View_Activity;
import com.example.googlemaps.databinding.ItemPlaceDescriptionBinding;
import com.bumptech.glide.Glide;

import java.util.List;

public class PlaceDescriptionAdapter extends RecyclerView.Adapter<PlaceDescriptionAdapter.ViewHolder> {

    private List<PlaceDescription> data;

    public interface OnItemCheckedChangeListener {
        void onItemCheckedChange(int checkedItemCount);
    }

    private OnItemCheckedChangeListener listener;

    public void setOnItemCheckedChangeListener(OnItemCheckedChangeListener listener) {
        this.listener = listener;
    }

    public PlaceDescriptionAdapter(List<PlaceDescription> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlaceDescriptionBinding binding = ItemPlaceDescriptionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, data, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
        holder.itemView.setOnClickListener(v -> {
            // Access the corresponding PlaceDescription object from the data list
            PlaceDescription clickedPlace = data.get(position);
            Intent intent = new Intent(v.getContext(), View_Activity.class);
            intent.putExtra("PLACE_DESCRIPTION", clickedPlace.getDescription());
            intent.putExtra("PLACE_IMAGE", clickedPlace.getImageUrl());
            intent.putExtra("PLACE_TITLE", clickedPlace.getTitle());
            v.getContext().startActivity(intent);
            Toast.makeText(v.getContext(), "Clicked item ID: "+clickedPlace.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemPlaceDescriptionBinding binding;
        private final List<PlaceDescription> data;
        private final OnItemCheckedChangeListener listener;

        public ViewHolder(ItemPlaceDescriptionBinding binding, List<PlaceDescription> data,
                          OnItemCheckedChangeListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.data = data;
            this.listener = listener;

            // Add CheckBox click listener
            binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Handle checkbox click here
                // You can use getAdapterPosition() to get the position of the clicked item
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PlaceDescription clickedPlace = data.get(position);
                    clickedPlace.setChecked(isChecked);

                    // Notify the listener about the change
                    if (listener != null) {
                        int checkedItemCount = getCheckedItemCount();
                        listener.onItemCheckedChange(checkedItemCount);
                    }
                }
            });
        }

        public void bind(PlaceDescription place) {
            binding.titleTextView.setText(place.getTitle());
            binding.descriptionTextView.setText(place.getDescription());
            Glide.with(binding.getRoot())
                    .load(place.getImageUrl())
                    .placeholder(R.drawable.bg)
                    .error(R.drawable.bg)
                    .into(binding.imageView);
            // Set the initial state of the checkbox based on the PlaceDescription object
            binding.checkBox.setChecked(place.isChecked());
        }

        private int getCheckedItemCount() {
            int count = 0;
            for (PlaceDescription placeDescription : data) {
                if (placeDescription.isChecked()) {
                    count++;
                }
            }
            return count;
        }
    }
    public void selectAll() {
        for (PlaceDescription place : data) {
            place.setChecked(true);
        }
        notifyDataSetChanged(); // Notify adapter that the dataset has changed
        if (listener != null) {
            listener.onItemCheckedChange(getItemCount());
        }
    }
    public void deselectAll() {
        for (PlaceDescription place : data) {
            place.setChecked(false);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onItemCheckedChange(0); // No items are selected
        }
    }
}
