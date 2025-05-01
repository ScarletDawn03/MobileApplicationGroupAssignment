package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;
    ShapeableImageView imageView;
    TextView name, mail;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private Button btnGoogleSignIn;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                    auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                auth = FirebaseAuth.getInstance();
                                FirebaseUser currentUser = auth.getCurrentUser();
                                String email = currentUser.getEmail();

                                if (email != null && email.endsWith("@1utar.my")) {
                                    Glide.with(SignInActivity.this)
                                            .load(currentUser.getPhotoUrl())
                                            .into(imageView);

                                    name.setText(currentUser.getDisplayName());
                                    mail.setText(currentUser.getEmail());

                                    saveUserToDatabase(currentUser);

                                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                    finish();
                                }
                                else {
                                    // Invalid email: sign out and show error message
                                    FirebaseAuth.getInstance().signOut();
                                    mGoogleSignInClient.signOut();

                                    Toast.makeText(SignInActivity.this,
                                            "Access denied. Please use your @1utar.my email.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(SignInActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        FirebaseApp.initializeApp(this);
        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);


        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(SignInActivity.this, options);

        auth = FirebaseAuth.getInstance();

        SignInButton signInButton = findViewById(R.id.signIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = mGoogleSignInClient.getSignInIntent();
                activityResultLauncher.launch(intent);
            }
        });

        if (auth.getCurrentUser() != null) {
            Glide.with(SignInActivity.this).load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl()).into(imageView);
            name.setText(auth.getCurrentUser().getDisplayName());
            mail.setText(auth.getCurrentUser().getEmail());
        }
    }

    private void saveUserToDatabase(FirebaseUser user) {
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        String uid = user.getUid();

        mDatabase.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    // Data doesn't exist yet, proceed to save
                    String username = user.getDisplayName();
                    String email = user.getEmail();
                    User newUser = new User(username, email);

                    mDatabase.child(uid).setValue(newUser).addOnCompleteListener(setTask -> {
                        if (setTask.isSuccessful()) {
                            Log.d("FirebaseDB", "User data saved successfully.");
                            // Save email to SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_email", email);
                            editor.apply();
                            Log.d("SharedPrefs", "Email saved: " + email);
                        } else {
                            Log.e("FirebaseDB", "Failed to save user data.", setTask.getException());
                        }
                    });
                } else {
                    Log.d("FirebaseDB", "User data already exists. Skipping write.");

                    // You can still store email even if user data already exists
                    String email = user.getEmail();
                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_email", email);
                    editor.apply();
                    Log.d("SharedPrefs", "Email saved: " + email);
                }
            } else {
                Log.e("FirebaseDB", "Failed to check existing user data.", task.getException());
            }
        });
    }



}
