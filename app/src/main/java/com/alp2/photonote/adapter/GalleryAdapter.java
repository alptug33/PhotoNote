package com.alp2.photonote.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alp2.photonote.databinding.ItemPhotoBinding;
import com.alp2.photonote.model.PhotoItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder> {
    private final List<PhotoItem> photos;
    private final OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoItem photo);
    }

    public GalleryAdapter(List<PhotoItem> photos, OnPhotoClickListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPhotoBinding binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new PhotoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoItem photo = photos.get(position);
        
        RequestOptions options = new RequestOptions()
            .centerCrop()
            .error(android.R.drawable.ic_menu_gallery);

        Glide.with(holder.itemView.getContext())
                .load(photo.getPhotoUri())
                .apply(options)
                .into(holder.binding.imageView);

        holder.binding.noteCount.setText(String.valueOf(photo.getNoteCount()));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(photo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        final ItemPhotoBinding binding;

        PhotoViewHolder(ItemPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
} 