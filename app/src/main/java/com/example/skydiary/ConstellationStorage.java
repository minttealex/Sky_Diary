package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

    private ConstellationStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        }
    }

    private void initializeDefaultConstellations() {
        // Northern constellations (including zodiac)
        constellations.add(new Constellation(ConstellationKeys.ANDROMEDA, 16));
        constellations.add(new Constellation(ConstellationKeys.AQUILA, 10));
        constellations.add(new Constellation(ConstellationKeys.ARIES, 6));
        constellations.add(new Constellation(ConstellationKeys.AURIGA, 8));
        constellations.add(new Constellation(ConstellationKeys.BOOTES, 13));
        constellations.add(new Constellation(ConstellationKeys.CAMELOPARDALIS, 8));
        constellations.add(new Constellation(ConstellationKeys.CANCER, 5));
        constellations.add(new Constellation(ConstellationKeys.CANES_VENATICI, 5));
        constellations.add(new Constellation(ConstellationKeys.CANIS_MINOR, 2));
        constellations.add(new Constellation(ConstellationKeys.CASSIOPEIA, 5));
        constellations.add(new Constellation(ConstellationKeys.CEPHEUS, 7));
        constellations.add(new Constellation(ConstellationKeys.COMA_BERENICES, 8));
        constellations.add(new Constellation(ConstellationKeys.CORONA_BOREALIS, 8));
        constellations.add(new Constellation(ConstellationKeys.CYGNUS, 9));
        constellations.add(new Constellation(ConstellationKeys.DELPHINUS, 5));
        constellations.add(new Constellation(ConstellationKeys.DRACO, 14));
        constellations.add(new Constellation(ConstellationKeys.EQUULEUS, 4));
        constellations.add(new Constellation(ConstellationKeys.GEMINI, 8));
        constellations.add(new Constellation(ConstellationKeys.HERCULES, 23));
        constellations.add(new Constellation(ConstellationKeys.LACERTA, 5));
        constellations.add(new Constellation(ConstellationKeys.LEO, 9));
        constellations.add(new Constellation(ConstellationKeys.LEO_MINOR, 4));
        constellations.add(new Constellation(ConstellationKeys.LYNX, 6));
        constellations.add(new Constellation(ConstellationKeys.LYRA, 5));
        constellations.add(new Constellation(ConstellationKeys.ORION, 7));
        constellations.add(new Constellation(ConstellationKeys.PEGASUS, 17));
        constellations.add(new Constellation(ConstellationKeys.PERSEUS, 19));
        constellations.add(new Constellation(ConstellationKeys.PISCES, 18));
        constellations.add(new Constellation(ConstellationKeys.SAGITTA, 4));
        constellations.add(new Constellation(ConstellationKeys.SCORPIUS, 18));
        constellations.add(new Constellation(ConstellationKeys.TAURUS, 19));
        constellations.add(new Constellation(ConstellationKeys.TRIANGULUM, 5));
        constellations.add(new Constellation(ConstellationKeys.URSA_MAJOR, 7));
        constellations.add(new Constellation(ConstellationKeys.URSA_MINOR, 7));
        constellations.add(new Constellation(ConstellationKeys.VIRGO, 15));
        constellations.add(new Constellation(ConstellationKeys.VULPECULA, 6));
        constellations.add(new Constellation(ConstellationKeys.LIBRA, 8));

        // Southern constellations (Keyser & de Houtman)
        constellations.add(new Constellation(ConstellationKeys.APUS, 4));
        constellations.add(new Constellation(ConstellationKeys.CHAMAELEON, 5));
        constellations.add(new Constellation(ConstellationKeys.DORADO, 4));
        constellations.add(new Constellation(ConstellationKeys.GRUS, 7));
        constellations.add(new Constellation(ConstellationKeys.HYDRUS, 5));
        constellations.add(new Constellation(ConstellationKeys.INDUS, 6));
        constellations.add(new Constellation(ConstellationKeys.MUSCA, 4));
        constellations.add(new Constellation(ConstellationKeys.PAVO, 7));
        constellations.add(new Constellation(ConstellationKeys.PHOENIX, 8));
        constellations.add(new Constellation(ConstellationKeys.TRIANGULUM_AUSTRALE, 5));
        constellations.add(new Constellation(ConstellationKeys.TUCANA, 5));
        constellations.add(new Constellation(ConstellationKeys.VOLANS, 4));

        // Southern constellations (Lacaille)
        constellations.add(new Constellation(ConstellationKeys.ANTLIA, 4));
        constellations.add(new Constellation(ConstellationKeys.CAELUM, 4));
        constellations.add(new Constellation(ConstellationKeys.CIRCINUS, 4));
        constellations.add(new Constellation(ConstellationKeys.FORNAX, 5));
        constellations.add(new Constellation(ConstellationKeys.HOROLOGIUM, 5));
        constellations.add(new Constellation(ConstellationKeys.MENSA, 4));
        constellations.add(new Constellation(ConstellationKeys.MICROSCOPIUM, 5));
        constellations.add(new Constellation(ConstellationKeys.NORMA, 4));
        constellations.add(new Constellation(ConstellationKeys.OCTANS, 6));
        constellations.add(new Constellation(ConstellationKeys.PICTOR, 4));
        constellations.add(new Constellation(ConstellationKeys.PYXIS, 5));
        constellations.add(new Constellation(ConstellationKeys.RETICULUM, 4));
        constellations.add(new Constellation(ConstellationKeys.SCULPTOR, 5));
        constellations.add(new Constellation(ConstellationKeys.TELESCOPIUM, 5));

        // Other southern / equatorial (remaining IAU constellations)
        constellations.add(new Constellation(ConstellationKeys.AQUARIUS, 10));
        constellations.add(new Constellation(ConstellationKeys.ARA, 7));
        constellations.add(new Constellation(ConstellationKeys.CANIS_MAJOR, 8));
        constellations.add(new Constellation(ConstellationKeys.CAPRICORNUS, 9));
        constellations.add(new Constellation(ConstellationKeys.CARINA, 9));
        constellations.add(new Constellation(ConstellationKeys.CENTAURUS, 11));
        constellations.add(new Constellation(ConstellationKeys.CETUS, 15));
        constellations.add(new Constellation(ConstellationKeys.COLUMBA, 5));
        constellations.add(new Constellation(ConstellationKeys.CORONA_AUSTRALIS, 6));
        constellations.add(new Constellation(ConstellationKeys.CORVUS, 4));
        constellations.add(new Constellation(ConstellationKeys.CRATER, 4));
        constellations.add(new Constellation(ConstellationKeys.CRUX, 4));
        constellations.add(new Constellation(ConstellationKeys.ERIDANUS, 24));
        constellations.add(new Constellation(ConstellationKeys.HYDRA, 17));
        constellations.add(new Constellation(ConstellationKeys.LEPUS, 8));
        constellations.add(new Constellation(ConstellationKeys.LUPUS, 9));
        constellations.add(new Constellation(ConstellationKeys.MONOCEROS, 8));
        constellations.add(new Constellation(ConstellationKeys.OPHIUCHUS, 10));
        constellations.add(new Constellation(ConstellationKeys.PISCIS_AUSTRINUS, 7));
        constellations.add(new Constellation(ConstellationKeys.PUPPIS, 9));
        constellations.add(new Constellation(ConstellationKeys.SAGITTARIUS, 15));
        constellations.add(new Constellation(ConstellationKeys.SCUTUM, 4));
        constellations.add(new Constellation(ConstellationKeys.SERPENS, 11));
        constellations.add(new Constellation(ConstellationKeys.SEXTANS, 4));
        constellations.add(new Constellation(ConstellationKeys.VELA, 8));

        saveConstellations();
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

    public List<Constellation> searchConstellations(String query, Context context) {
        List<Constellation> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getConstellations();
        }

        String searchQuery = query.toLowerCase().trim();
        for (Constellation constellation : constellations) {
            if (constellation.getName(context).toLowerCase().contains(searchQuery)) {
                results.add(constellation);
            }
        }
        return results;
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
                Constellation existing = constellations.get(i);
                existing.setSeen(updatedConstellation.isSeen());
                existing.setFavorite(updatedConstellation.isFavorite());
                saveConstellations();
                return;
            }
        }
    }

    public void forceRefreshConstellations() {
        loadConstellations();
    }

    public void resetToDefault() {
        List<Constellation> constellations = getConstellations();
        for (Constellation constellation : constellations) {
            constellation.setSeen(false);
            constellation.setFavorite(false);
        }
        saveConstellations();
        Log.d("ConstellationStorage", "Reset all constellations to default state");
    }
}