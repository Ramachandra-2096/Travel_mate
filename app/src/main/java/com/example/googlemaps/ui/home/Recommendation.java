package com.example.googlemaps.ui.home;

public class Recommendation {
    private String title;
    private String imageUrl;

    public Recommendation(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
