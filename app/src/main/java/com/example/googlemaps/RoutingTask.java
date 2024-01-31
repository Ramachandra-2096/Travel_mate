package com.example.googlemaps;

import static com.example.googlemaps.MapsActivity.isJourney_Started;

import android.graphics.Color;
import android.os.AsyncTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RoutingTask extends AsyncTask<List<LatLng>, Void, PolylineOptions> {

    private GoogleMap mMap;
    public Polyline existingPolyline;

    public RoutingTask(GoogleMap map, Polyline existingPolyline) {
        this.mMap = map;
        this.existingPolyline = existingPolyline;
    }

    @Override
    protected PolylineOptions doInBackground(List<LatLng>... waypoints) {
        try {
            // Build the API URL with your API key
            String apiKey = "5b3ce3597851110001cf6248eda74a00c65644c9853b00dfba1f1a35";
            StringBuilder apiUrl = new StringBuilder("https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + apiKey);
            // Add waypoints to the API URL
            apiUrl.append("&start="+waypoints[0].get(0).longitude+","+waypoints[0].get(0).latitude);
            apiUrl.append("&end="+waypoints[0].get(1).longitude+","+waypoints[0].get(1).latitude);
            // Make an HTTP request to the OpenRouteService API
            URL url = new URL(apiUrl.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            StringBuilder result = new StringBuilder();
            int data = reader.read();
            while (data != -1) {
                char current = (char) data;
                result.append(current);
                data = reader.read();
            }

            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray coordinates = jsonObject.getJSONArray("features")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates");

            // Create a PolylineOptions object and add points to it
            PolylineOptions polylineOptions = new PolylineOptions();
            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray coordinate = coordinates.getJSONArray(i);
                LatLng point = new LatLng(coordinate.getDouble(1), coordinate.getDouble(0));
                polylineOptions.add(point);
            }

            // Set polyline properties (color, width, etc.)
            polylineOptions.width(23).color(Color.BLUE);
            isJourney_Started =true ;
            return polylineOptions;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(PolylineOptions polylineOptions) {
        // Remove existing polyline from the map
        if (existingPolyline != null) {
            existingPolyline.remove();
        }

        // Add the new Polyline to the map
        if (polylineOptions != null) {
            Polyline newPolyline = mMap.addPolyline(polylineOptions);

            // Update the existingPolyline reference
            existingPolyline = newPolyline;
        }
    }
}
