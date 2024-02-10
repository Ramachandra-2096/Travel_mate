package com.example.googlemaps;// ... (previous imports)

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class View_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Intent intent = getIntent();
        String url = intent.getStringExtra("PLACE_IMAGE");
        String name1 = intent.getStringExtra("PLACE_TITLE");
        String description = intent.getStringExtra("PLACE_DESCRIPTION");
        if (description != null) {
            description += " This is a very nice place to visit";
        }
        double latitude = intent.getDoubleExtra("PLACE_LATITUDE", 0.0);
        double longitude = intent.getDoubleExtra("PLACE_LONGITUDE", 0.0);
        String rating = intent.getStringExtra("PLACE_RATING");

        ImageView img = findViewById(R.id.imageView4);
        Glide.with(this)
                .load(url)
                .error(R.drawable.bg)
                .into(img);

        TextView name = findViewById(R.id.textView4);
        TextView desc = findViewById(R.id.textView6);
        TextView rat = findViewById(R.id.textView7);
        name.setText(name1);
        desc.setText(description);
        rat.setText("Rating : " + rating);

        Button button = findViewById(R.id.button9);
        String[] names = {name1};
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(View_Activity.this, MapsActivity.class);
                intent1.putExtra("Name", names);
                Geocoder geo = new Geocoder(View_Activity.this, Locale.getDefault());
                try {
                        List<Address> add = geo.getFromLocation(latitude, longitude, 1);
                        if (add != null && !add.isEmpty()) {
                            intent1.putExtra("State", add.get(0).getAdminArea().toString());
                            startActivity(intent1);
                        }
                } catch (IOException | NumberFormatException e) {
                    // Handle the exception (IOException for geocoding failure, NumberFormatException for parsing failure)
                    Log.e("View_Activity", "Error in geocoding or parsing: " + e.getMessage());
                }
            }
        });
    }
}
