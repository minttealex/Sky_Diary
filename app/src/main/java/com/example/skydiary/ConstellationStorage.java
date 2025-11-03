package com.example.skydiary;

import static android.provider.Settings.System.getString;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConstellationStorage {
    private static final String PREFS_NAME = "constellations_prefs";
    private static final String CONSTELLATIONS_KEY = "constellations";
    private static final String MIGRATION_KEY = "migration_v5_done";

    private static ConstellationStorage instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    private List<Constellation> constellations;
    private final Context context;

    public List<Constellation> getConstellations() {
        return sortConstellationsByName(new ArrayList<>(constellations));
    }

    public List<Constellation> getFavoriteConstellations() {
        List<Constellation> favorites = new ArrayList<>();
        for (Constellation constellation : constellations) {
            if (constellation.isFavorite()) {
                favorites.add(constellation);
            }
        }
        return sortConstellationsByName(favorites);
    }

    public List<Constellation> searchConstellations(String query) {
        List<Constellation> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getConstellations(); // This will now return sorted list
        }

        String searchQuery = query.toLowerCase().trim();
        for (Constellation constellation : constellations) {
            if (constellation.getName().toLowerCase().contains(searchQuery)) {
                results.add(constellation);
            }
        }
        return sortConstellationsByName(results);
    }

    private List<Constellation> sortConstellationsByName(List<Constellation> constellationsToSort) {
        Collections.sort(constellationsToSort, new Comparator<Constellation>() {
            @Override
            public int compare(Constellation c1, Constellation c2) {
                return c1.getName().compareToIgnoreCase(c2.getName());
            }
        });
        return constellationsToSort;
    }

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
            constellations.clear();
            initializeDefaultConstellations();
            prefs.edit().putBoolean(MIGRATION_KEY, true).apply();
        } else {
            fixConstellationNames();
        }
    }

    private void fixConstellationNames() {
        boolean needsFix = false;
        for (Constellation constellation : constellations) {
            if (constellation.getName() == null || constellation.getName().isEmpty()) {
                needsFix = true;
                break;
            }
        }

        if (needsFix) {
            constellations.clear();
            initializeDefaultConstellations();
            saveConstellations();
        }
    }

    private void initializeDefaultConstellations() {
        constellations.clear();

        constellations.add(new Constellation(getString(R.string.constellation_orion), "The Hunter", 7, ConstellationResources.getConstellationImageResource("Orion")));
        constellations.add(new Constellation(getString(R.string.constellation_ursa_major), "The Great Bear", 7, ConstellationResources.getConstellationImageResource("Ursa Major")));
        constellations.add(new Constellation(getString(R.string.constellation_cassiopeia), "The Seated Queen", 5, ConstellationResources.getConstellationImageResource("Cassiopeia")));
        constellations.add(new Constellation(getString(R.string.constellation_leo), "The Lion", 9, ConstellationResources.getConstellationImageResource("Leo")));
        constellations.add(new Constellation(getString(R.string.constellation_scorpius), "The Scorpion", 18, ConstellationResources.getConstellationImageResource("Scorpius")));
        constellations.add(new Constellation(getString(R.string.constellation_cygnus), "The Swan", 9, ConstellationResources.getConstellationImageResource("Cygnus")));
        constellations.add(new Constellation(getString(R.string.constellation_lyra), "The Lyre", 5, ConstellationResources.getConstellationImageResource("Lyra")));
        constellations.add(new Constellation(getString(R.string.constellation_andromeda), "The Chained Maiden", 16, ConstellationResources.getConstellationImageResource("Andromeda")));
        constellations.add(new Constellation(getString(R.string.constellation_ursa_minor), "The Little Bear", 7, ConstellationResources.getConstellationImageResource("Ursa Minor")));
        constellations.add(new Constellation(getString(R.string.constellation_draco), "The Dragon", 14, ConstellationResources.getConstellationImageResource("Draco")));
        constellations.add(new Constellation(getString(R.string.constellation_cepheus), "The King", 7, ConstellationResources.getConstellationImageResource("Cepheus")));
        constellations.add(new Constellation(getString(R.string.constellation_perseus), "The Hero", 19, ConstellationResources.getConstellationImageResource("Perseus")));
        constellations.add(new Constellation(getString(R.string.constellation_auriga), "The Charioteer", 8, ConstellationResources.getConstellationImageResource("Auriga")));
        constellations.add(new Constellation(getString(R.string.constellation_bootes), "The Herdsman", 13, ConstellationResources.getConstellationImageResource("Bootes")));
        constellations.add(new Constellation(getString(R.string.constellation_corona_borealis), "The Northern Crown", 8, ConstellationResources.getConstellationImageResource("Corona Borealis")));
        constellations.add(new Constellation(getString(R.string.constellation_hercules), "The Hero", 23, ConstellationResources.getConstellationImageResource("Hercules")));
        constellations.add(new Constellation(getString(R.string.constellation_sagitta), "The Arrow", 4, ConstellationResources.getConstellationImageResource("Sagitta")));
        constellations.add(new Constellation(getString(R.string.constellation_aquila), "The Eagle", 10, ConstellationResources.getConstellationImageResource("Aquila")));
        constellations.add(new Constellation(getString(R.string.constellation_delphinus), "The Dolphin", 5, ConstellationResources.getConstellationImageResource("Delphinus")));
        constellations.add(new Constellation(getString(R.string.constellation_pegasus), "The Winged Horse", 17, ConstellationResources.getConstellationImageResource("Pegasus")));
        constellations.add(new Constellation(getString(R.string.constellation_pisces), "The Fishes", 18, ConstellationResources.getConstellationImageResource("Pisces")));
        constellations.add(new Constellation(getString(R.string.constellation_aries), "The Ram", 6, ConstellationResources.getConstellationImageResource("Aries")));
        constellations.add(new Constellation(getString(R.string.constellation_taurus), "The Bull", 19, ConstellationResources.getConstellationImageResource("Taurus")));
        constellations.add(new Constellation(getString(R.string.constellation_gemini), "The Twins", 8, ConstellationResources.getConstellationImageResource("Gemini")));
        constellations.add(new Constellation(getString(R.string.constellation_cancer), "The Crab", 5, ConstellationResources.getConstellationImageResource("Cancer")));
        constellations.add(new Constellation(getString(R.string.constellation_virgo), "The Maiden", 15, ConstellationResources.getConstellationImageResource("Virgo")));
        constellations.add(new Constellation(getString(R.string.constellation_libra), "The Scales", 8, ConstellationResources.getConstellationImageResource("Libra")));

        saveConstellations();
    }

    private String getString(int resId) {
        return context.getString(resId);
    }

    private void saveConstellations() {
        String json = gson.toJson(constellations);
        prefs.edit().putString(CONSTELLATIONS_KEY, json).apply();
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
}