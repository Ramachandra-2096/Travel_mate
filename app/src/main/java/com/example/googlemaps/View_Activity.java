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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        isindatabase(name1,latitude,longitude, new DatabaseCallback() {
            @Override
            public void onCallback(boolean isInDatabase) {
                if (!isInDatabase) {
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Geocoder geo =new Geocoder(View_Activity.this,Locale.getDefault());
                            try {
                                List<Address> add = geo.getFromLocation(latitude,longitude,1);
                                Intent intent1 = new Intent(View_Activity.this, MapsActivity.class);
                                String state = add.get(0).getAdminArea();
                                intent1.putExtra("Name", names);
                                intent1.putExtra("Main", "Tourist_place");
                                intent1.putExtra("State", state);
                                Log.d("TAG", "onClick: "+state);
                                startActivity(intent1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } else {
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Geocoder geo =new Geocoder(View_Activity.this,Locale.getDefault());
                            try {
                                Log.d("TAG", "onClick: Hotel");
                            List<Address> add = geo.getFromLocation(latitude,longitude,1);
                            Intent intent1 = new Intent(View_Activity.this, MapsActivity.class);
                            String state = add.get(0).getAdminArea();
                            intent1.putExtra("Name", names);
                            intent1.putExtra("Main", "Hotels");
                            intent1.putExtra("State", state);
                            startActivity(intent1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        });
    }

    public interface DatabaseCallback {
        void onCallback(boolean isInDatabase);
    }

    public void isindatabase(String name1, double latitude,double longitude,DatabaseCallback callback) {
        Geocoder geo =new Geocoder(View_Activity.this,Locale.getDefault());
        String state;
        try {
            List<Address> add = geo.getFromLocation(latitude, longitude, 1);
            state = add.get(0).getAdminArea();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Hotels").child(state);
        databaseReference.child(name1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isInDatabase = snapshot.exists();
                callback.onCallback(isInDatabase);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error if needed
                callback.onCallback(false);
            }
        });
    }

}

