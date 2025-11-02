package com.example.skydiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

        public ConstellationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivConstellation = itemView.findViewById(R.id.iv_constellation);
            tvName = itemView.findViewById(R.id.tv_constellation_name);
            tvStarCount = itemView.findViewById(R.id.tv_star_count);
            cbSeen = itemView.findViewById(R.id.cb_seen);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }

        public void bind(Constellation constellation, OnConstellationInteractionListener listener) {
            tvName.setText(constellation.getName());
            String starCountText = itemView.getContext().getString(R.string.star_count_format, constellation.getStarCount());
            tvStarCount.setText(starCountText);

            try {
                if (constellation.getImageResId() != 0) {
                    ivConstellation.setImageResource(constellation.getImageResId());
                    ivConstellation.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    ivConstellation.setImageResource(android.R.drawable.ic_menu_gallery);
                    ivConstellation.setBackgroundColor(0xFF334477);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ivConstellation.setImageResource(android.R.drawable.ic_menu_gallery);
                ivConstellation.setBackgroundColor(0xFF334477);
            }

            cbSeen.setOnCheckedChangeListener(null);
            btnFavorite.setOnClickListener(null);

            cbSeen.setChecked(constellation.isSeen());

            updateFavoriteButtonAppearance(constellation.isFavorite());

            cbSeen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) {
                        listener.onConstellationSeenChanged(constellation, isChecked);
                    }
                }
            });

            btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        boolean newFavoriteState = !constellation.isFavorite();
                        listener.onConstellationFavoriteChanged(constellation, newFavoriteState);
                        updateFavoriteButtonAppearance(newFavoriteState);
                    }
                }
            });
        }

        private void updateFavoriteButtonAppearance(boolean isFavorite) {
            if (isFavorite) {
                btnFavorite.setColorFilter(itemView.getContext().getColor(android.R.color.holo_orange_light));
            } else {
                btnFavorite.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
            }
        }
    }
}