package com.example.skydiary;

import android.content.Context;

import java.util.Objects;
import java.util.UUID;

public class Constellation {
    private String id;
    private String key;
    private boolean isSeen;
    private boolean isFavorite;
    private int starCount;

    public Constellation() {
        this.id = UUID.randomUUID().toString();
    }

    public Constellation(String key, int starCount) {
        this.id = UUID.randomUUID().toString();
        this.key = key;
        this.starCount = starCount;
        this.isSeen = false;
        this.isFavorite = false;
    }

    public String getName(Context context) {
        if (context == null) return key;
        return getLocalizedName(context);
    }

    public String getDescription(Context context) {
        if (context == null) return "";
        return getLocalizedDescription(context);
    }

    public int getImageResId() {
        return getImageResourceByKey();
    }

    private String getLocalizedName(Context context) {
        switch (key) {
            case ConstellationKeys.ORION: return context.getString(R.string.constellation_orion);
            case ConstellationKeys.URSA_MAJOR: return context.getString(R.string.constellation_ursa_major);
            case ConstellationKeys.CASSIOPEIA: return context.getString(R.string.constellation_cassiopeia);
            case ConstellationKeys.LEO: return context.getString(R.string.constellation_leo);
            case ConstellationKeys.SCORPIUS: return context.getString(R.string.constellation_scorpius);
            case ConstellationKeys.CYGNUS: return context.getString(R.string.constellation_cygnus);
            case ConstellationKeys.LYRA: return context.getString(R.string.constellation_lyra);
            case ConstellationKeys.ANDROMEDA: return context.getString(R.string.constellation_andromeda);
            case ConstellationKeys.URSA_MINOR: return context.getString(R.string.constellation_ursa_minor);
            case ConstellationKeys.DRACO: return context.getString(R.string.constellation_draco);
            case ConstellationKeys.CEPHEUS: return context.getString(R.string.constellation_cepheus);
            case ConstellationKeys.PERSEUS: return context.getString(R.string.constellation_perseus);
            case ConstellationKeys.AURIGA: return context.getString(R.string.constellation_auriga);
            case ConstellationKeys.BOOTES: return context.getString(R.string.constellation_bootes);
            case ConstellationKeys.CORONA_BOREALIS: return context.getString(R.string.constellation_corona_borealis);
            case ConstellationKeys.HERCULES: return context.getString(R.string.constellation_hercules);
            case ConstellationKeys.SAGITTA: return context.getString(R.string.constellation_sagitta);
            case ConstellationKeys.AQUILA: return context.getString(R.string.constellation_aquila);
            case ConstellationKeys.DELPHINUS: return context.getString(R.string.constellation_delphinus);
            case ConstellationKeys.PEGASUS: return context.getString(R.string.constellation_pegasus);
            case ConstellationKeys.PISCES: return context.getString(R.string.constellation_pisces);
            case ConstellationKeys.ARIES: return context.getString(R.string.constellation_aries);
            case ConstellationKeys.TAURUS: return context.getString(R.string.constellation_taurus);
            case ConstellationKeys.GEMINI: return context.getString(R.string.constellation_gemini);
            case ConstellationKeys.CANCER: return context.getString(R.string.constellation_cancer);
            case ConstellationKeys.VIRGO: return context.getString(R.string.constellation_virgo);
            case ConstellationKeys.LIBRA: return context.getString(R.string.constellation_libra);
            default: return key;
        }
    }

    private String getLocalizedDescription(Context context) {
        switch (key) {
            case ConstellationKeys.ORION: return context.getString(R.string.constellation_orion_description);
            case ConstellationKeys.URSA_MAJOR: return context.getString(R.string.constellation_ursa_major_description);
            case ConstellationKeys.CASSIOPEIA: return context.getString(R.string.constellation_cassiopeia_description);
            case ConstellationKeys.LEO: return context.getString(R.string.constellation_leo_description);
            case ConstellationKeys.SCORPIUS: return context.getString(R.string.constellation_scorpius_description);
            case ConstellationKeys.CYGNUS: return context.getString(R.string.constellation_cygnus_description);
            case ConstellationKeys.LYRA: return context.getString(R.string.constellation_lyra_description);
            case ConstellationKeys.ANDROMEDA: return context.getString(R.string.constellation_andromeda_description);
            case ConstellationKeys.URSA_MINOR: return context.getString(R.string.constellation_ursa_minor_description);
            case ConstellationKeys.DRACO: return context.getString(R.string.constellation_draco_description);
            case ConstellationKeys.CEPHEUS: return context.getString(R.string.constellation_cepheus_description);
            case ConstellationKeys.PERSEUS: return context.getString(R.string.constellation_perseus_description);
            case ConstellationKeys.AURIGA: return context.getString(R.string.constellation_auriga_description);
            case ConstellationKeys.BOOTES: return context.getString(R.string.constellation_bootes_description);
            case ConstellationKeys.CORONA_BOREALIS: return context.getString(R.string.constellation_corona_borealis_description);
            case ConstellationKeys.HERCULES: return context.getString(R.string.constellation_hercules_description);
            case ConstellationKeys.SAGITTA: return context.getString(R.string.constellation_sagitta_description);
            case ConstellationKeys.AQUILA: return context.getString(R.string.constellation_aquila_description);
            case ConstellationKeys.DELPHINUS: return context.getString(R.string.constellation_delphinus_description);
            case ConstellationKeys.PEGASUS: return context.getString(R.string.constellation_pegasus_description);
            case ConstellationKeys.PISCES: return context.getString(R.string.constellation_pisces_description);
            case ConstellationKeys.ARIES: return context.getString(R.string.constellation_aries_description);
            case ConstellationKeys.TAURUS: return context.getString(R.string.constellation_taurus_description);
            case ConstellationKeys.GEMINI: return context.getString(R.string.constellation_gemini_description);
            case ConstellationKeys.CANCER: return context.getString(R.string.constellation_cancer_description);
            case ConstellationKeys.VIRGO: return context.getString(R.string.constellation_virgo_description);
            case ConstellationKeys.LIBRA: return context.getString(R.string.constellation_libra_description);
            default: return "";
        }
    }

    private int getImageResourceByKey() {
        switch (key) {
            case ConstellationKeys.ORION: return R.drawable.const_orion;
            case ConstellationKeys.URSA_MAJOR: return R.drawable.const_ursa_major;
            case ConstellationKeys.CASSIOPEIA: return R.drawable.const_cassiopeia;
            case ConstellationKeys.LEO: return R.drawable.const_leo;
            case ConstellationKeys.SCORPIUS: return R.drawable.const_scorpius;
            case ConstellationKeys.CYGNUS: return R.drawable.const_cygnus;
            case ConstellationKeys.LYRA: return R.drawable.const_lyra;
            case ConstellationKeys.ANDROMEDA: return R.drawable.const_andromeda;
            case ConstellationKeys.URSA_MINOR: return R.drawable.const_ursa_minor;
            case ConstellationKeys.DRACO: return R.drawable.const_draco;
            case ConstellationKeys.CEPHEUS: return R.drawable.const_cepheus;
            case ConstellationKeys.PERSEUS: return R.drawable.const_perseus;
            case ConstellationKeys.AURIGA: return R.drawable.const_auriga;
            case ConstellationKeys.BOOTES: return R.drawable.const_bootes;
            case ConstellationKeys.CORONA_BOREALIS: return R.drawable.const_corona_borealis;
            case ConstellationKeys.HERCULES: return R.drawable.const_hercules;
            case ConstellationKeys.SAGITTA: return R.drawable.const_sagitta;
            case ConstellationKeys.AQUILA: return R.drawable.const_aquila;
            case ConstellationKeys.DELPHINUS: return R.drawable.const_delphinus;
            case ConstellationKeys.PEGASUS: return R.drawable.const_pegasus;
            case ConstellationKeys.PISCES: return R.drawable.const_pisces;
            case ConstellationKeys.ARIES: return R.drawable.const_aries;
            case ConstellationKeys.TAURUS: return R.drawable.const_taurus;
            case ConstellationKeys.GEMINI: return R.drawable.const_gemini;
            case ConstellationKeys.CANCER: return R.drawable.const_cancer;
            case ConstellationKeys.VIRGO: return R.drawable.const_virgo;
            case ConstellationKeys.LIBRA: return R.drawable.const_libra;
            default: return android.R.drawable.ic_menu_gallery;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public boolean isSeen() { return isSeen; }
    public void setSeen(boolean seen) { isSeen = seen; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public int getStarCount() { return starCount; }
    public void setStarCount(int starCount) { this.starCount = starCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Constellation)) return false;
        Constellation that = (Constellation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}