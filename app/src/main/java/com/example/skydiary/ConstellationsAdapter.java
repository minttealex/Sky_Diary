package com.example.skydiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ConstellationsAdapter extends RecyclerView.Adapter<ConstellationsAdapter.ConstellationViewHolder> {

    public interface OnConstellationInteractionListener {
        void onConstellationSeenChanged(Constellation constellation, boolean isSeen);
        void onConstellationFavoriteChanged(Constellation constellation, boolean isFavorite);
    }

    private final List<Constellation> constellations;
    private final OnConstellationInteractionListener listener;

    public ConstellationsAdapter(List<Constellation> constellations, OnConstellationInteractionListener listener) {
        this.constellations = constellations != null ? new ArrayList<>(constellations) : new ArrayList<>();
        this.listener = listener;
    }

    public void updateConstellations(List<Constellation> newConstellations) {
        this.constellations.clear();
        if (newConstellations != null) {
            this.constellations.addAll(newConstellations);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConstellationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_constellation, parent, false);
        return new ConstellationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConstellationViewHolder holder, int position) {
        Constellation constellation = constellations.get(position);
        holder.bind(constellation, listener);
    }

    @Override
    public int getItemCount() {
        return constellations.size();
    }

    static class ConstellationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivConstellation;
        private final TextView tvName;
        private final TextView tvStarCount;
        private final CheckBox cbSeen;
        private final ImageButton btnFavorite;
        private final View itemView;

        public ConstellationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivConstellation = itemView.findViewById(R.id.iv_constellation);
            tvName = itemView.findViewById(R.id.tv_constellation_name);
            tvStarCount = itemView.findViewById(R.id.tv_star_count);
            cbSeen = itemView.findViewById(R.id.cb_seen);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }

        public void bind(Constellation constellation, OnConstellationInteractionListener listener) {
            tvName.setText(constellation.getName(itemView.getContext()));
            String starCountText = itemView.getContext().getString(R.string.star_count_format, constellation.getStarCount());
            tvStarCount.setText(starCountText);

            if (constellation.isSeen()) {
                itemView.setAlpha(0.6f);
                tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
                tvStarCount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            } else {
                itemView.setAlpha(1.0f);
                tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                tvStarCount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
            }

            try {
                if (constellation.getImageResId() != 0) {
                    ivConstellation.setImageResource(constellation.getImageResId());

                }
                ivConstellation.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (constellation.isSeen()) {
                    ivConstellation.setAlpha(0.6f);
                } else {
                    ivConstellation.setAlpha(1.0f);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ivConstellation.setImageResource(android.R.drawable.ic_menu_gallery);
                ivConstellation.setBackgroundColor(0xFF334477);
            }

            cbSeen.setOnCheckedChangeListener(null);
            btnFavorite.setOnClickListener(null);

            btnFavorite.setSelected(constellation.isFavorite());

            cbSeen.setChecked(constellation.isSeen());

            if (constellation.isSeen()) {
                btnFavorite.setAlpha(0.7f);
            } else {
                btnFavorite.setAlpha(1.0f);
            }

            cbSeen.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onConstellationSeenChanged(constellation, isChecked);
                }
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    boolean newFavoriteState = !constellation.isFavorite();
                    btnFavorite.setSelected(newFavoriteState);

                    if (constellation.isSeen()) {
                        btnFavorite.setAlpha(0.7f);
                    } else {
                        btnFavorite.setAlpha(1.0f);
                    }

                    listener.onConstellationFavoriteChanged(constellation, newFavoriteState);
                }
            });
        }
    }
}