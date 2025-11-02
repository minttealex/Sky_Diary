package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConstellationStorage {
    private static final String PREFS_NAME = "constellations_prefs";
    private static final String CONSTELLATIONS_KEY = "constellations";
    private static final String MIGRATION_KEY = "migration_v2_done";

    private static ConstellationStorage instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    private List<Constellation> constellations;
    private final Context context;

    private ConstellationStorage(Context context) {
        this.context = context.getApplicationContext();
        prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder().create();
        loadConstellations();
        checkAndMigrateData();
    }

    public static synchronized ConstellationStorage getInstance(Context context) {
        if (instance == null) {
            instance = new ConstellationStorage(context);
        }
        return instance;
    }

    private void loadConstellations() {
        String json = prefs.getString(CONSTELLATIONS_KEY, "[]");
        Type listType = new TypeToken<List<Constellation>>(){}.getType();
        constellations = gson.fromJson(json, listType);
        if (constellations == null) {
            constellations = new ArrayList<>();
        }
    }

    private void checkAndMigrateData() {
        boolean migrationDone = prefs.getBoolean(MIGRATION_KEY, false);
        if (!migrationDone || constellations.isEmpty()) {
            initializeDefaultConstellations();
            prefs.edit().putBoolean(MIGRATION_KEY, true).apply();
        }
    }

    private void initializeDefaultConstellations() {
        constellations.clear();

        constellations.add(createConstellation("Orion", "The Hunter", 7, "orion"));
        constellations.add(createConstellation("Ursa Major", "The Great Bear", 7, "ursa_major"));
        constellations.add(createConstellation("Cassiopeia", "The Seated Queen", 5, "cassiopeia"));
        constellations.add(createConstellation("Leo", "The Lion", 9, "leo"));
        constellations.add(createConstellation("Scorpius", "The Scorpion", 18, "scorpius"));
        constellations.add(createConstellation("Cygnus", "The Swan", 9, "cygnus"));
        constellations.add(createConstellation("Lyra", "The Lyre", 5, "lyra"));
        constellations.add(createConstellation("Andromeda", "The Chained Maiden", 16, "andromeda"));

        saveConstellations();
    }

    private Constellation createConstellation(String name, String description, int starCount, String imageName) {
        int resId = getResourceId(imageName);
        if (resId == 0) {
            resId = android.R.drawable.ic_menu_gallery;
        }
        return new Constellation(name, description, starCount, resId);
    }

    private int getResourceId(String imageName) {
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }

    private void saveConstellations() {
        String json = gson.toJson(constellations);
        prefs.edit().putString(CONSTELLATIONS_KEY, json).apply();
    }

    public List<Constellation> getConstellations() {
        return new ArrayList<>(constellations);
    }

    public List<Constellation> getFavoriteConstellations() {
        List<Constellation> favorites = new ArrayList<>();
        for (Constellation constellation : constellations) {
            if (constellation.isFavorite()) {
                favorites.add(constellation);
            }
        }
        return favorites;
    }

    public int getSeenCount() {
        int count = 0;
        for (Constellation constellation : constellations) {
            if (constellation.isSeen()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCount() {
        return constellations.size();
    }

    public void updateConstellation(Constellation updatedConstellation) {
        for (int i = 0; i < constellations.size(); i++) {
            if (constellations.get(i).getId().equals(updatedConstellation.getId())) {
                constellations.set(i, updatedConstellation);
                saveConstellations();
                return;
            }
        }
    }

    public List<Constellation> searchConstellations(String query) {
        List<Constellation> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(constellations);
        }

        String searchQuery = query.toLowerCase().trim();
        for (Constellation constellation : constellations) {
            if (constellation.getName().toLowerCase().contains(searchQuery)) {
                results.add(constellation);
            }
        }
        return results;
    }
}