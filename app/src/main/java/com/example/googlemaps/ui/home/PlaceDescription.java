package com.example.googlemaps.ui.home;

import com.google.android.gms.maps.model.LatLng;

public class PlaceDescription {

    private String title;
    private String rating;
    private String description;
    private String imageUrl;
    private LatLng lt;

    public PlaceDescription(String title, String description, String imageUrl,String rating,LatLng lt) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating=rating;
        this.lt=lt;
    }

    public String getTitle() {
        return title;
    }
    public String getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }
    public LatLng getLocation() {
        return lt;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    private boolean isChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}

