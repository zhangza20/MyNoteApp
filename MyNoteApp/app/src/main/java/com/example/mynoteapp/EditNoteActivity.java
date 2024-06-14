package com.example.mynoteapp;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditNoteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_AUDIO_REQUEST = 2;
    private static final int CAMERA_REQUEST_CODE = 3;

    private EditText editTextTitle, editTextContent;
    private Button buttonSaveNote, buttonChooseImage, buttonChooseAudio, buttonCaptureImage;
    private ImageView imageViewSelected;
    private Uri imageUri;
    private Uri audioUri;
    private DatabaseReference notesRef;
    private StorageReference storageRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        buttonSaveNote = findViewById(R.id.buttonSaveNote);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonChooseAudio = findViewById(R.id.buttonChooseAudio);
        buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        imageViewSelected = findViewById(R.id.imageViewSelected);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        notesRef = FirebaseDatabase.getInstance().getReference("notes").child(user.getUid());
        storageRef = FirebaseStorage.getInstance().getReference();

        buttonSaveNote.setOnClickListener(v -> saveNote());
        buttonChooseImage.setOnClickListener(v -> chooseImage());
        buttonChooseAudio.setOnClickListener(v -> chooseAudio());
        buttonCaptureImage.setOnClickListener(v -> captureImage());
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference newNoteRef = notesRef.push();
        String noteId = newNoteRef.getKey();
        Note note = new Note(noteId, title, content);

        newNoteRef.setValue(note).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                uploadMediaFiles(noteId);
            } else {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadMediaFiles(String noteId) {
        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("images/" + noteId + ".jpg");
            imageRef.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showToastAndFinish("Note saved successfully");
                } else {
                    showToastAndFinish("Failed to upload image");
                }
            });
        } else if (audioUri != null) {
            StorageReference audioRef = storageRef.child("audios/" + noteId + ".3gp");
            audioRef.putFile(audioUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showToastAndFinish("Note saved successfully");
                } else {
                    showToastAndFinish("Failed to upload audio");
                }
            });
        } else {
            showToastAndFinish("Note saved successfully");
        }
    }

    private void showToastAndFinish(String message) {
        runOnUiThread(() -> {
            Toast.makeText(EditNoteActivity.this, message, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EditNoteActivity.this, NoteActivity.class));
            finish();
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void chooseAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                imageUri = data.getData();
                imageViewSelected.setImageURI(imageUri);
            } else if (requestCode == PICK_AUDIO_REQUEST && data != null) {
                audioUri = data.getData();
                Toast.makeText(this, "Audio selected", Toast.LENGTH_SHORT).show();
            } else if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap photo = (Bitmap) extras.get("data");
                imageUri = getImageUri(photo);
                imageViewSelected.setImageURI(imageUri);
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
}
