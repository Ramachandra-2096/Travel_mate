package com.example.googlemaps;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class Loginsighnup1 extends AppCompatActivity {
        private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
        private Button registerButton;
        private FirebaseAuth mAuth;
        private DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_loginsighnup1);
            nameEditText = findViewById(R.id.name);
            emailEditText = findViewById(R.id.email);
            passwordEditText = findViewById(R.id.password);
            confirmPasswordEditText = findViewById(R.id.confirmPassword);
            registerButton = findViewById(R.id.button6);
            sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            mAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("users");
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registerUser();
                }
            });

            TextView sighnin =findViewById(R.id.textView3);
            sighnin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(Loginsighnup1.this, Loginsighnup2.class);
                    startActivity(intent);
                }
            });
        }

        private void registerUser() {
            final String name = nameEditText.getText().toString().trim();
            final String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(Loginsighnup1.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(Loginsighnup1.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                String userId = user.getUid();
                                // Store user details in the Realtime Database
                                databaseReference.child(userId).child("name").setValue(name);
                                databaseReference.child(userId).child("email").setValue(email);
                                String is_first = "is_first";
                                boolean is_first_time = sharedPreferences.getBoolean(is_first, true);
                                if(is_first_time)
                                {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(is_first,false);
                                    editor.apply();
                                }
                                Toast.makeText(Loginsighnup1.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(Loginsighnup1.this, MapsActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(Loginsighnup1.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to exit?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            super.onBackPressed(); // Let the system handle the back press
            finishAffinity();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
        });
        builder.show();
    }
    }
