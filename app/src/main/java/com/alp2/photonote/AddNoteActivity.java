package com.alp2.photonote;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alp2.photonote.adapter.NotesAdapter;
import com.alp2.photonote.databinding.ActivityAddNoteBinding;
import com.alp2.photonote.model.PhotoNote;
import com.alp2.photonote.view.NoteMarkerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Objects;

public class AddNoteActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener, NotesAdapter.OnNoteDeleteListener {
    private ActivityAddNoteBinding binding;
    private NotesAdapter notesAdapter;
    private ArrayList<PhotoNote> notes;
    private float lastTouchX;
    private float lastTouchY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Notes");

        // Setup back press handling
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToPhotoDetail();
            }
        });

        String photoUriString = getIntent().getStringExtra("photo_uri");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notes = getIntent().getParcelableArrayListExtra("notes", PhotoNote.class);
        } else {
            notes = getIntent().getParcelableArrayListExtra("notes");
        }
        if (notes == null) {
            notes = new ArrayList<>();
        }

        if (photoUriString != null) {
            Uri photoUri = Uri.parse(photoUriString);
            setupUI(photoUri);
        } else {
            Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupUI(Uri photoUri) {
        binding.photoImageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        
        RequestOptions options = new RequestOptions()
            .fitCenter()
            .error(android.R.drawable.ic_menu_gallery);

        Glide.with(this)
                .load(photoUri)
                .apply(options)
                .into(binding.photoImageView);

        binding.notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter(notes, this, this);
        binding.notesRecyclerView.setAdapter(notesAdapter);

        binding.markerView.clearMarkers();
        for (PhotoNote note : notes) {
            binding.markerView.addMarker(note.getX(), note.getY());
        }

        binding.markerView.setOnMarkerTouchListener((x, y) -> {
            // Get the image and view dimensions
            int viewWidth = binding.markerView.getWidth();
            int viewHeight = binding.markerView.getHeight();
            int imageWidth = binding.photoImageView.getDrawable().getIntrinsicWidth();
            int imageHeight = binding.photoImageView.getDrawable().getIntrinsicHeight();

            // Calculate scaling factors
            float scaleX = (float) viewWidth / imageWidth;
            float scaleY = (float) viewHeight / imageHeight;
            float scale = Math.min(scaleX, scaleY);

            // Calculate image bounds within the view
            float scaledImageWidth = imageWidth * scale;
            float scaledImageHeight = imageHeight * scale;
            float leftOffset = (viewWidth - scaledImageWidth) / 2;
            float topOffset = (viewHeight - scaledImageHeight) / 2;

            // Adjust touch coordinates relative to the image
            float touchX = (x * viewWidth - leftOffset) / scaledImageWidth;
            float touchY = (y * viewHeight - topOffset) / scaledImageHeight;

            // Ensure coordinates are within bounds
            if (touchX >= 0 && touchX <= 1 && touchY >= 0 && touchY <= 1) {
                lastTouchX = touchX;
                lastTouchY = touchY;
                showNoteInput();
            }
        });

        binding.saveFab.setOnClickListener(v -> saveNote());
    }

    private void showNoteInput() {
        binding.addNoteInstructions.setVisibility(View.GONE);
        binding.noteInputLayout.setVisibility(View.VISIBLE);
        binding.saveFab.setVisibility(View.VISIBLE);
        binding.noteEditText.requestFocus();
    }

    private void saveNote() {
        String noteText = Objects.requireNonNull(binding.noteEditText.getText()).toString().trim();
        if (!noteText.isEmpty()) {
            PhotoNote note = new PhotoNote(noteText, lastTouchX, lastTouchY);
            notes.add(note);
            binding.markerView.addMarker(lastTouchX, lastTouchY);
            notesAdapter.addNote(note);
            
            binding.noteEditText.setText("");
            binding.noteInputLayout.setVisibility(View.GONE);
            binding.saveFab.setVisibility(View.GONE);
            binding.addNoteInstructions.setVisibility(View.VISIBLE);
            
            // Notları PhotoDetailActivity'ye gönder ama aktiviteyi kapatma
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("notes", new ArrayList<>(notes));
            setResult(RESULT_OK, resultIntent);
        }
    }

    @Override
    public void onBackPressed() {
        returnToPhotoDetail();
        super.onBackPressed();
    }

    private void returnToPhotoDetail() {
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("notes", new ArrayList<>(notes));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            returnToPhotoDetail();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoteClick(PhotoNote note, int position) {
        binding.markerView.highlightMarker(position);
    }

    @Override
    public void onNoteDelete(PhotoNote note, int position) {
        notes.remove(position);
        binding.markerView.removeMarker(position);
        notesAdapter.removeNote(position);
        returnToPhotoDetail();
    }
} 