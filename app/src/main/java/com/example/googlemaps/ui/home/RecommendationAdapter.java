package com.example.googlemaps.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.googlemaps.R;
import com.example.googlemaps.View_Activity;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
    private Context context;
    private List<Recommendation> placeList;

    public RecommendationAdapter(Context context, List<Recommendation> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recomendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(placeList.get(position));
        holder.itemView.setOnClickListener(v -> {
            Recommendation clickedPlace = placeList.get(position);
            Intent intent = new Intent(v.getContext(), View_Activity.class);
            intent.putExtra("PLACE_DESCRIPTION", clickedPlace.getReiew());
            intent.putExtra("PLACE_IMAGE", clickedPlace.getImageUrl());
            intent.putExtra("PLACE_TITLE", clickedPlace.getTitle());
            intent.putExtra("PLACE_RATING", clickedPlace.getNewRating());
            LatLng lt = clickedPlace.getLatLng();
            intent.putExtra("PLACE_LATITUDE", lt.latitude);
            intent.putExtra("PLACE_LONGITUDE", lt.longitude);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView titleTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView_rec);
            titleTextView = itemView.findViewById(R.id.textview_rec);
        }

        public void bind(Recommendation recommendation) {
            // Set data to views
            imageView.setImageResource(R.drawable.bg); // Change this to use the actual image resource
            titleTextView.setText(recommendation.getTitle());
            // Load image using Glide or any other image loading library
            Glide.with(context)
                    .load(recommendation.getImageUrl())
                    .into(imageView);
        }
    }

    public void clearData() {
        if (placeList != null) {
            placeList.clear();
            notifyDataSetChanged();  // Notify the adapter that the data has changed
        }
    }
}
