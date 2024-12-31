package com.alp2.photonote.model;

import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class PhotoItem {
    private Uri photoUri;
    private List<PhotoNote> notes;

    public PhotoItem(Uri photoUri) {
        this.photoUri = photoUri;
        this.notes = new ArrayList<>();
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public List<PhotoNote> getNotes() {
        return notes != null ? notes : new ArrayList<>();
    }

    public void setNotes(List<PhotoNote> notes) {
        this.notes = notes != null ? notes : new ArrayList<>();
    }

    public void addNote(PhotoNote note) {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        notes.add(note);
    }

    public void removeNote(int position) {
        if (notes != null && position >= 0 && position < notes.size()) {
            notes.remove(position);
        }
    }

    public int getNoteCount() {
        return notes != null ? notes.size() : 0;
    }
} 