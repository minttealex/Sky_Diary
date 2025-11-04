package com.example.skydiary;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private final List<Constellation> favorites;

    public FavoritesAdapter(List<Constellation> favorites) {
        this.favorites = favorites != null ? new ArrayList<>(favorites) : new ArrayList<>();
    }

    public void updateFavorites(List<Constellation> newFavorites) {
        this.favorites.clear();
        if (newFavorites != null) {
            this.favorites.addAll(newFavorites);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_constellation_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Constellation constellation = favorites.get(position);
        holder.bind(constellation);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivConstellation;
        private final TextView tvName;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivConstellation = itemView.findViewById(R.id.iv_constellation);
            tvName = itemView.findViewById(R.id.tv_constellation_name);
        }

        public void bind(Constellation constellation) {
            tvName.setText(constellation.getName(itemView.getContext()));

            try {
                int resId = constellation.getImageResId();
                if (resId != 0) {
                    ivConstellation.setImageResource(resId);
                    ivConstellation.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    ivConstellation.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            } catch (Resources.NotFoundException e) {
                ivConstellation.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Log.e("FavoritesAdapter", "Image not found for: " + constellation.getName(itemView.getContext()));
            } catch (Exception e) {
                ivConstellation.setImageResource(android.R.drawable.ic_menu_gallery);
                ivConstellation.setBackgroundColor(0xFF334477);
            }
        }
    }
}