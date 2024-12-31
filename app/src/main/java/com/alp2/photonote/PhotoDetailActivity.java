package com.alp2.photonote;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.alp2.photonote.adapter.NotesAdapter;
import com.alp2.photonote.databinding.ActivityPhotoDetailBinding;
import com.alp2.photonote.model.PhotoNote;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;
import java.util.Objects;

public class PhotoDetailActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener, NotesAdapter.OnNoteDeleteListener {
    private ActivityPhotoDetailBinding binding;
    private ArrayList<PhotoNote> notes;
    private NotesAdapter notesAdapter;
    private Uri photoUri;
    private int position = -1;
    private ActivityResultLauncher<Intent> addNoteLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge-to-edge görünümü etkinleştir
        androidx.activity.EdgeToEdge.enable(this);
        
        binding = ActivityPhotoDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Sistem barları için inset'leri ayarla
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            
            // Toolbar için üst padding'i ayarla
            binding.toolbar.setPadding(0, insets.top, 0, 0);
            
            // FAB'lar için bottom inset'i ayarla
            binding.addNoteFab.setTranslationY(-insets.bottom);
            binding.deletePhotoFab.setTranslationY(-insets.bottom);
            
            return windowInsets;
        });

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Fotoğraf Detayları");

        setupAddNoteLauncher();
        getIntentData();
        setupUI();
        setupRecyclerView();
        updateNotesVisibility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notes != null) {
            notesAdapter.updateNotes(notes);
            updateNotesVisibility();
            
            binding.markerView.clearMarkers();
            for (PhotoNote note : notes) {
                binding.markerView.addMarker(note.getX(), note.getY());
            }
        }
    }

    private void setupAddNoteLauncher() {
        addNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<PhotoNote> updatedNotes;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        updatedNotes = result.getData().getParcelableArrayListExtra("notes", PhotoNote.class);
                    } else {
                        updatedNotes = result.getData().getParcelableArrayListExtra("notes");
                    }
                    
                    if (updatedNotes != null) {
                        notes.clear();
                        notes.addAll(updatedNotes);
                        notesAdapter.updateNotes(notes);
                        updateNotesVisibility();
                        
                        binding.markerView.clearMarkers();
                        for (PhotoNote note : notes) {
                            binding.markerView.addMarker(note.getX(), note.getY());
                        }
                        
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("PHOTO_POSITION", position);
                        resultIntent.putParcelableArrayListExtra("UPDATED_NOTES", new ArrayList<>(notes));
                        setResult(RESULT_OK, resultIntent);
                    }
                }
            }
        );
    }

    private void getIntentData() {
        String uriString = getIntent().getStringExtra("photo_uri");
        if (uriString != null) {
            photoUri = Uri.parse(uriString);
        }
        
        position = getIntent().getIntExtra("PHOTO_POSITION", -1);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notes = getIntent().getParcelableArrayListExtra("notes", PhotoNote.class);
        } else {
            notes = getIntent().getParcelableArrayListExtra("notes");
        }
        
        if (notes == null) {
            notes = new ArrayList<>();
        }
    }

    private void setupUI() {
        if (photoUri != null) {
            binding.photoImageView.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            binding.photoImageView.setAdjustViewBounds(true);

            RequestOptions options = new RequestOptions()
                .fitCenter()
                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                .error(android.R.drawable.ic_menu_gallery);

            Glide.with(this)
                    .load(photoUri)
                    .apply(options)
                    .into(binding.photoImageView);

            for (PhotoNote note : notes) {
                binding.markerView.addMarker(note.getX(), note.getY());
            }
        } else {
            Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.addNoteFab.setOnClickListener(v -> startAddNoteActivity());
        binding.deletePhotoFab.setOnClickListener(v -> deletePhoto());
    }

    private void setupRecyclerView() {
        binding.notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesAdapter = new NotesAdapter(notes, this, this);
        binding.notesRecyclerView.setAdapter(notesAdapter);
    }

    private void updateNotesVisibility() {
        if (notes.isEmpty()) {
            binding.noNotesText.setVisibility(View.VISIBLE);
            binding.notesRecyclerView.setVisibility(View.GONE);
        } else {
            binding.noNotesText.setVisibility(View.GONE);
            binding.notesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void startAddNoteActivity() {
        Intent intent = new Intent(this, AddNoteActivity.class);
        intent.putExtra("photo_uri", photoUri.toString());
        intent.putParcelableArrayListExtra("notes", new ArrayList<>(notes));
        addNoteLauncher.launch(intent);
    }

    private void deletePhoto() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Fotoğrafı Sil")
               .setMessage("Bu fotoğrafı silmek istediğinizden emin misiniz?")
               .setPositiveButton("Evet", (dialog, which) -> {
                   Intent resultIntent = new Intent();
                   resultIntent.putExtra("DELETE_PHOTO", true);
                   resultIntent.putExtra("PHOTO_POSITION", position);
                   setResult(RESULT_OK, resultIntent);
                   finish();
               })
               .setNegativeButton("Hayır", (dialog, which) -> dialog.dismiss())
               .show();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("PHOTO_POSITION", position);
        resultIntent.putParcelableArrayListExtra("UPDATED_NOTES", new ArrayList<>(notes));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("PHOTO_POSITION", position);
            resultIntent.putParcelableArrayListExtra("UPDATED_NOTES", new ArrayList<>(notes));
            setResult(RESULT_OK, resultIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isFinishing()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("PHOTO_POSITION", position);
            resultIntent.putParcelableArrayListExtra("UPDATED_NOTES", new ArrayList<>(notes));
            setResult(RESULT_OK, resultIntent);
        }
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
        updateNotesVisibility();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("PHOTO_POSITION", this.position);
        resultIntent.putParcelableArrayListExtra("UPDATED_NOTES", new ArrayList<>(notes));
        setResult(RESULT_OK, resultIntent);
    }
} 