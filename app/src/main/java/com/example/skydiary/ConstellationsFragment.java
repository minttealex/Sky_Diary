package com.example.skydiary;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ConstellationsFragment extends Fragment implements ConstellationsAdapter.OnConstellationInteractionListener {

    private ConstellationStorage constellationStorage;
    private ConstellationsAdapter constellationsAdapter;
    private FavoritesAdapter favoritesAdapter;

    private TextView tvSeenCounter;
    private EditText searchBar;
    private RecyclerView recyclerFavorites;
    private RecyclerView recyclerConstellations;
    private TextView tvNoFavorites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_constellations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        constellationStorage = ConstellationStorage.getInstance(requireContext());

        initializeViews(view);

        setupAdapters();

        setupListeners();

        refreshData();

        debugImageLoading();
    }

    private void debugImageLoading() {
        List<Constellation> constellations = constellationStorage.getConstellations();
        for (Constellation constellation : constellations) {
            int resId = constellation.getImageResId();
            String resName;
            try {
                if (resId != 0) {
                    resName = getResources().getResourceName(resId);
                } else {
                    resName = "Resource ID is 0";
                }
            } catch (Exception e) {
                resName = "Error: " + e.getMessage();
            }
            Log.d("ConstellationDebug",
                    "Name: " + constellation.getName(requireContext()) +
                            ", ImageResId: " + resId +
                            ", ResId Name: " + resName);
        }
    }

    private void initializeViews(View view) {
        tvSeenCounter = view.findViewById(R.id.tv_seen_counter);
        searchBar = view.findViewById(R.id.search_bar);
        recyclerFavorites = view.findViewById(R.id.recycler_favorites);
        recyclerConstellations = view.findViewById(R.id.recycler_constellations);
        tvNoFavorites = view.findViewById(R.id.tv_no_favorites);
        ImageButton btnBack = view.findViewById(R.id.button_back);

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupAdapters() {
        favoritesAdapter = new FavoritesAdapter(constellationStorage.getFavoriteConstellations());
        LinearLayoutManager favoritesLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerFavorites.setLayoutManager(favoritesLayoutManager);
        recyclerFavorites.setAdapter(favoritesAdapter);

        constellationsAdapter = new ConstellationsAdapter(constellationStorage.getConstellations(), this);
        GridLayoutManager constellationsLayoutManager = new GridLayoutManager(requireContext(), 1);
        recyclerConstellations.setLayoutManager(constellationsLayoutManager);
        recyclerConstellations.setAdapter(constellationsAdapter);
    }

    private void setupListeners() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchConstellations(s.toString());
            }
        });
    }

    private void refreshData() {
        updateSeenCounter();
        updateFavoritesList();
        updateConstellationsList();
    }

    private void updateSeenCounter() {
        int seen = constellationStorage.getSeenCount();
        int total = constellationStorage.getTotalCount();
        tvSeenCounter.setText(seen + "/" + total);
    }

    private void updateFavoritesList() {
        List<Constellation> favorites = constellationStorage.getFavoriteConstellations();
        favoritesAdapter.updateFavorites(favorites);

        if (favorites.isEmpty()) {
            tvNoFavorites.setVisibility(View.VISIBLE);
            recyclerFavorites.setVisibility(View.GONE);
        } else {
            tvNoFavorites.setVisibility(View.GONE);
            recyclerFavorites.setVisibility(View.VISIBLE);
        }
    }

    private void updateConstellationsList() {
        List<Constellation> constellations = constellationStorage.getConstellations();
        constellationsAdapter.updateConstellations(constellations);
    }

    private void searchConstellations(String query) {
        List<Constellation> results = constellationStorage.searchConstellations(query, requireContext());
        constellationsAdapter.updateConstellations(results);
    }

    @Override
    public void onConstellationSeenChanged(Constellation constellation, boolean isSeen) {
        try {
            constellation.setSeen(isSeen);
            constellationStorage.updateConstellation(constellation);
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConstellationFavoriteChanged(Constellation constellation, boolean isFavorite) {
        try {
            constellation.setFavorite(isFavorite);
            constellationStorage.updateConstellation(constellation);
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        constellationStorage.forceRefreshConstellations();
        refreshData();

        List<Constellation> constellations = constellationStorage.getConstellations();
        for (Constellation constellation : constellations) {
            Log.d("ConstellationFinal", "Final Name: " + constellation.getName(requireContext()));
        }
    }
}