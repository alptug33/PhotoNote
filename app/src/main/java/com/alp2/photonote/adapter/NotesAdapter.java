package com.alp2.photonote.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.alp2.photonote.databinding.ItemNoteBinding;
import com.alp2.photonote.model.PhotoNote;
import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<PhotoNote> notes;
    private final OnNoteClickListener clickListener;
    private final OnNoteDeleteListener deleteListener;

    public interface OnNoteClickListener {
        void onNoteClick(PhotoNote note, int position);
    }

    public interface OnNoteDeleteListener {
        void onNoteDelete(PhotoNote note, int position);
    }

    public NotesAdapter(List<PhotoNote> notes, OnNoteClickListener clickListener, OnNoteDeleteListener deleteListener) {
        this.notes = new ArrayList<>(notes);
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    public void updateNotes(List<PhotoNote> newNotes) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return notes.size();
            }

            @Override
            public int getNewListSize() {
                return newNotes.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return notes.get(oldItemPosition).getText().equals(newNotes.get(newItemPosition).getText());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                PhotoNote oldNote = notes.get(oldItemPosition);
                PhotoNote newNote = newNotes.get(newItemPosition);
                return oldNote.getText().equals(newNote.getText()) &&
                       oldNote.getX() == newNote.getX() &&
                       oldNote.getY() == newNote.getY();
            }
        });
        
        notes = new ArrayList<>(newNotes);
        diffResult.dispatchUpdatesTo(this);
    }

    public void removeNote(int position) {
        if (position >= 0 && position < notes.size()) {
            notes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notes.size());
        }
    }

    public void addNote(PhotoNote note) {
        notes.add(note);
        notifyItemInserted(notes.size() - 1);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoteBinding binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        PhotoNote note = notes.get(position);
        holder.binding.noteText.setText(note.getText());
        
        holder.binding.deleteButton.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
            builder.setTitle("Notu Sil")
                   .setMessage("Bu notu silmek istediğinizden emin misiniz?")
                   .setPositiveButton("Evet", (dialog, which) -> {
                       if (deleteListener != null) {
                           deleteListener.onNoteDelete(note, holder.getAdapterPosition());
                       }
                   })
                   .setNegativeButton("Hayır", (dialog, which) -> dialog.dismiss())
                   .show();
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNoteClick(note, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        final ItemNoteBinding binding;

        NoteViewHolder(ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 