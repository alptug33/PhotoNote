package com.alp2.photonote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alp2.photonote.adapter.GalleryAdapter;
import com.alp2.photonote.databinding.ActivityMainBinding;
import com.alp2.photonote.model.PhotoItem;
import com.alp2.photonote.model.PhotoNote;
import com.alp2.photonote.storage.PhotoStorage;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GalleryAdapter.OnPhotoClickListener {
    private ActivityMainBinding binding;
    private ArrayList<PhotoItem> photos;
    private GalleryAdapter galleryAdapter;
    private PhotoStorage photoStorage;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> addNoteLauncher;
    private ActivityResultLauncher<Intent> photoDetailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.galleryRecyclerView.setPadding(
                insets.left, 
                insets.top, 
                insets.right, 
                0
            );
            
            binding.addPhotoFab.setTranslationY(-insets.bottom);
            
            return WindowInsetsCompat.CONSUMED;
        });

        photoStorage = new PhotoStorage(this);
        photos = photoStorage.loadPhotos();

        setupLaunchers();
        setupRecyclerView();
        setupAddPhotoButton();
    }

    private void setupLaunchers() {
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startAddNoteActivity();
                } else {
                    Toast.makeText(this, "Permission needed to access photos", Toast.LENGTH_SHORT).show();
                }
            }
        );

        addNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri == null) {
                        Toast.makeText(this, "Error: Could not load photo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        getContentResolver().takePersistableUriPermission(selectedImageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        boolean exists = false;
                        for (PhotoItem photo : photos) {
                            if (photo.getPhotoUri() != null &&
                                photo.getPhotoUri().toString().equals(selectedImageUri.toString())) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            PhotoItem newPhoto = new PhotoItem(selectedImageUri);
                            photos.add(newPhoto);
                            galleryAdapter.notifyItemInserted(photos.size() - 1);
                            photoStorage.savePhotos(photos);
                        } else {
                            Toast.makeText(MainActivity.this, "This photo already exists in the gallery", Toast.LENGTH_SHORT).show();
                        }
                    } catch (SecurityException e) {
                        Toast.makeText(this, "Error: Could not access photo", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: Could not process photo", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        );

        photoDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    boolean deletePhoto = data.getBooleanExtra("DELETE_PHOTO", false);
                    int position = data.getIntExtra("PHOTO_POSITION", -1);
                    
                    if (deletePhoto && position != -1) {
                        photos.remove(position);
                        photoStorage.savePhotos(photos);
                        galleryAdapter.notifyItemRemoved(position);
                    } else {
                        ArrayList<PhotoNote> updatedNotes;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            updatedNotes = data.getParcelableArrayListExtra("UPDATED_NOTES", PhotoNote.class);
                        } else {
                            updatedNotes = data.getParcelableArrayListExtra("UPDATED_NOTES");
                        }
                        
                        if (updatedNotes != null && position != -1) {
                            PhotoItem photo = photos.get(position);
                            photo.setNotes(new ArrayList<>(updatedNotes));
                            photoStorage.savePhotos(photos);
                            galleryAdapter.notifyItemChanged(position);
                        }
                    }
                }
            }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        photoStorage.savePhotos(photos);
    }

    private void setupRecyclerView() {
        binding.galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        galleryAdapter = new GalleryAdapter(photos, this);
        binding.galleryRecyclerView.setAdapter(galleryAdapter);
    }

    private void setupAddPhotoButton() {
        binding.addPhotoFab.setOnClickListener(v -> checkAndRequestPermission());
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                startAddNoteActivity();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                startAddNoteActivity();
            }
        }
    }

    private void startAddNoteActivity() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        addNoteLauncher.launch(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onPhotoClick(PhotoItem photo) {
        if (photo != null && photo.getPhotoUri() != null) {
            Intent intent = new Intent(this, PhotoDetailActivity.class);
            intent.putExtra("photo_uri", photo.getPhotoUri().toString());
            intent.putExtra("PHOTO_POSITION", photos.indexOf(photo));
            
            ArrayList<PhotoNote> notes = new ArrayList<>(photo.getNotes() != null ? photo.getNotes() : new ArrayList<>());
            intent.putParcelableArrayListExtra("notes", notes);
            
            photoDetailLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
            photos.remove(photo);
            galleryAdapter.notifyDataSetChanged();
        }
    }
}