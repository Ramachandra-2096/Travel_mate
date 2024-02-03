package com.example.googlemaps;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Loginsighnup2 extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private Button signInButton;
    private SignInButton googleSignInButton;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPreferences";
    private static final String KEY_LAST_LOGIN = "lastLoginTimestamp";
    private static final String KEY_SAVED_EMAIL = "savedEmail";
    private static final String KEY_SAVED_PASSWORD = "savedPassword";

    private static final int RC_SIGN_IN = 123;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginsighnup2);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String is_first = "is_first";
        boolean is_first_time = sharedPreferences.getBoolean(is_first, true);
        if(is_first_time)
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(is_first,false);
            editor.apply();
        }
        emailTextInputLayout = findViewById(R.id.textInputLayoutEmail);
        passwordTextInputLayout = findViewById(R.id.textInputLayoutPassword);
        signInButton = findViewById(R.id.button7);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the user is already signed in with Google
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null && currentUser.getProviderData().contains(GoogleAuthProvider.PROVIDER_ID)) {
                    // User is already signed in with Google, show a message or handle accordingly
                    Toast.makeText(Loginsighnup2.this, "You are already signed in with Google.", Toast.LENGTH_SHORT).show();
                } else {
                    // User is not signed in with Google, proceed with email and password sign-in
                    String email = emailTextInputLayout.getEditText().getText().toString();
                    String password = passwordTextInputLayout.getEditText().getText().toString();

                    if(email.isEmpty()) {
                        Toast.makeText(Loginsighnup2.this, "Email cannot be Empty", Toast.LENGTH_SHORT).show();
                    } else if (password.isEmpty()) {
                        Toast.makeText(Loginsighnup2.this, "Password cannot be Empty", Toast.LENGTH_SHORT).show();
                    } else {
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(Loginsighnup2.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            Toast.makeText(Loginsighnup2.this, "Authentication successful.", Toast.LENGTH_SHORT).show();

                                            // Link Google credentials with the email/password account
                                            linkGoogleCredentials(user);

                                            saveLastLoginTimestamp();
                                            saveUserCredentials(email, password);
                                            Intent intent = new Intent(Loginsighnup2.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish(); // Optional, to finish the login activity
                                        } else {
                                            Toast.makeText(Loginsighnup2.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        checkAutoLogin();

        TextView t= findViewById(R.id.textView3);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Loginsighnup2.this, Loginsighnup1.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void checkAutoLogin() {
        long lastLoginTimestamp = sharedPreferences.getLong(KEY_LAST_LOGIN, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastLoginTimestamp < (7 * 24 * 60 * 60 * 1000)) {
            String savedEmail = sharedPreferences.getString(KEY_SAVED_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_SAVED_PASSWORD, "");

            if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                mAuth.signInWithEmailAndPassword(savedEmail, savedPassword)
                        .addOnCompleteListener(Loginsighnup2.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(Loginsighnup2.this, "Automatic login successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Loginsighnup2.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Loginsighnup2.this, "Automatic login failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }
    // Function to link Google credentials with the current user
    private void linkGoogleCredentials(FirebaseUser user) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            user.linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Loginsighnup2.this, "Google credentials linked successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Loginsighnup2.this, "Failed to link Google credentials.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void saveLastLoginTimestamp() {
        long currentTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_LOGIN, currentTime);
        editor.apply();
    }

    private void saveUserCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SAVED_EMAIL, email);
        editor.putString(KEY_SAVED_PASSWORD, password);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            Toast.makeText(Loginsighnup2.this, "Google Sign In failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(Loginsighnup2.this, "Google Sign In successful.", Toast.LENGTH_SHORT).show();

                             Intent intent = new Intent(Loginsighnup2.this, HomeActivity.class);
                             startActivity(intent);
                             finish();
                        } else {
                            Toast.makeText(Loginsighnup2.this, "Authentication with Google failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play services connection failed.", Toast.LENGTH_SHORT).show();
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
