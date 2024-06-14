package com.example.mynoteapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import java.io.IOException;

public class NoteDetailActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private ImageView imageViewNote;
    private Button buttonBack, buttonSave, buttonPlayAudio;
    private String noteId;
    private DatabaseReference noteRef;
    private StorageReference storageRef;
    private StorageReference audioRef;
    private Uri imageUri;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        imageViewNote = findViewById(R.id.imageViewNote);
        buttonBack = findViewById(R.id.buttonBack);
        buttonSave = findViewById(R.id.buttonSave);
        buttonPlayAudio = findViewById(R.id.buttonPlayAudio);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        noteId = getIntent().getStringExtra("noteId");
        noteRef = FirebaseDatabase.getInstance().getReference("notes").child(user.getUid()).child(noteId);
        audioRef = FirebaseStorage.getInstance().getReference().child("audios").child(noteId + ".3gp");  // 初始化音频存储引用
        storageRef = FirebaseStorage.getInstance().getReference("images").child(noteId + ".jpg");

        displayNoteDetails();

        buttonBack.setOnClickListener(v -> finish());
        buttonPlayAudio.setOnClickListener(v -> playAudio());
        buttonSave.setOnClickListener(v -> saveNoteDetails());
    }

    private void displayNoteDetails() {
        noteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue(String.class);
                String content = dataSnapshot.child("content").getValue(String.class);
                editTextTitle.setText(title);
                editTextContent.setText(content);

                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Picasso.get().load(uri).into(imageViewNote);
                    imageUri = uri;
                }).addOnFailureListener(exception -> {
                    // Handle any errors
                });
                checkAudioExists(); // 检查音频文件是否存在
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle cancel event
            }
        });
    }

    private void saveNoteDetails() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        noteRef.child("title").setValue(title);
        noteRef.child("content").setValue(content).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(NoteDetailActivity.this, "Note updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NoteDetailActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void playAudio() {
        audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(NoteDetailActivity.this, uri);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(NoteDetailActivity.this, "Playing audio", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(NoteDetailActivity.this, "Failed to play audio", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(NoteDetailActivity.this, "Failed to retrieve audio", Toast.LENGTH_SHORT).show();
        });
    }
    private void checkAudioExists() {
        audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // 如果音频存在，显示播放按钮
            buttonPlayAudio.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {
            // 如果音频不存在，隐藏播放按钮
            buttonPlayAudio.setVisibility(View.GONE);
        });
    }

}
