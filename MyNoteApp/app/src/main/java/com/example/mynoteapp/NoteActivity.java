package com.example.mynoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NoteActivity extends AppCompatActivity {

    private TextView textViewUserInfo;
    private ImageView imageViewProfile;
    private Button buttonEditNote, buttonUser;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        textViewUserInfo = findViewById(R.id.textViewUserInfo);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonEditNote = findViewById(R.id.buttonEditNote);
        buttonUser = findViewById(R.id.buttonUser);
        auth = FirebaseAuth.getInstance();

        displayUserInfo();
        displayNotes();

        buttonEditNote.setOnClickListener(view -> {
            startActivity(new Intent(NoteActivity.this, EditNoteActivity.class));
        });

        buttonUser.setOnClickListener(view -> {
            startActivity(new Intent(NoteActivity.this, UserActivity.class));
        });
    }

    private void displayUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nickname = dataSnapshot.child("nickname").getValue(String.class);
                        String signature = dataSnapshot.child("signature").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        String userInfo = "Nickname: " + nickname + "\n" +
                                "Signature: " + signature + "\n" +
                                "Email: " + email;

                        textViewUserInfo.setText(userInfo);

                        if (profileImageUrl != null) {
                            Glide.with(NoteActivity.this).load(profileImageUrl).into(imageViewProfile);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(NoteActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayNotes() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference().child("notes").child(userId);
            notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    LinearLayout notesLayout = findViewById(R.id.notesLayout);
                    notesLayout.removeAllViews(); // 清空布局

                    for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                        String noteId = noteSnapshot.getKey();
                        String title = noteSnapshot.child("title").getValue(String.class);

                        Button noteButton = new Button(NoteActivity.this);
                        noteButton.setText(title);
                        noteButton.setOnClickListener(view -> {
                            Intent intent = new Intent(NoteActivity.this, NoteDetailActivity.class);
                            intent.putExtra("noteId", noteId);
                            startActivity(intent);
                        });

                        notesLayout.addView(noteButton);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(NoteActivity.this, "Failed to load notes", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
