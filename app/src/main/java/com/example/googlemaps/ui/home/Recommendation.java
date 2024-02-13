package com.example.googlemaps.ui.home;

import com.google.android.gms.maps.model.LatLng;

public class Recommendation {
    private String title,reiew,newRating;
    private  LatLng latLng;
    private String imageUrl;

    public Recommendation(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public Recommendation(String place, String review, String photo, String newRating, LatLng latLng) {
        this.title=place;
        this.reiew=review;
        this.imageUrl=photo;
        this.newRating=newRating;
        this.latLng=latLng;
    }

    public String getTitle() {
        return title;
    }
    public String getNewRating() {
        return newRating;
    }
    public String getReiew() {
        return reiew;
    }
    public LatLng getLatLng() {
        return latLng;
    }
    public String getImageUrl() {
        return imageUrl;
    }
}
