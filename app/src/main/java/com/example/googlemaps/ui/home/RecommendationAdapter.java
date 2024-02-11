package com.example.googlemaps.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.googlemaps.R;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
    private Context context;
    private List<Recommendation> placeList;

    public RecommendationAdapter(Context context, List<Recommendation> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recomendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Recommendation place = placeList.get(position);
        // Set data to views
        holder.imageView.setImageResource(R.drawable.bg); // Change this to use the actual image resource
        holder.titleTextView.setText(place.getTitle());
        // Load image using Glide or any other image loading library
        Glide.with(context)
                .load(place.getImageUrl())
                .into(holder.imageView);
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
    }
}
