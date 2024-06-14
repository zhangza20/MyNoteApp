package com.example.mynoteapp;

import com.bumptech.glide.Glide;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextNickname, editTextSignature, editTextNewPassword;
    private Button buttonSaveProfile, buttonChangePassword, buttonBack, buttonChooseImage;
    private ImageView imageViewProfile;
    private Uri imageUri;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        editTextNickname = findViewById(R.id.editTextNickname);
        editTextSignature = findViewById(R.id.editTextSignature);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonBack = findViewById(R.id.buttonBack);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        imageViewProfile = findViewById(R.id.imageViewProfile);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        storageRef = FirebaseStorage.getInstance().getReference();

        buttonSaveProfile.setOnClickListener(v -> saveUserProfile());
        buttonChangePassword.setOnClickListener(v -> changePassword());
        buttonBack.setOnClickListener(v -> finish());
        buttonChooseImage.setOnClickListener(v -> chooseImage());

        loadUserProfile();
    }

    private void saveUserProfile() {
        String nickname = editTextNickname.getText().toString().trim();
        String signature = editTextSignature.getText().toString().trim();

        if (nickname.isEmpty() || signature.isEmpty()) {
            Toast.makeText(this, "Nickname and signature cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("nickname").setValue(nickname);
        userRef.child("signature").setValue(signature);

        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("profile_images/" + auth.getCurrentUser().getUid() + ".jpg");
            imageRef.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userRef.child("profileImageUrl").setValue(uri.toString());
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "New password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        user.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewProfile.setImageURI(imageUri);
        }
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nickname = dataSnapshot.child("nickname").getValue(String.class);
                        String signature = dataSnapshot.child("signature").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        editTextNickname.setText(nickname);
                        editTextSignature.setText(signature);

                        if (profileImageUrl != null) {
                            Glide.with(UserActivity.this).load(profileImageUrl).into(imageViewProfile);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UserActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
