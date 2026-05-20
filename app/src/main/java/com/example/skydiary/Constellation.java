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
            // Northern
            case ConstellationKeys.ANDROMEDA: return context.getString(R.string.constellation_andromeda);
            case ConstellationKeys.AQUILA: return context.getString(R.string.constellation_aquila);
            case ConstellationKeys.ARIES: return context.getString(R.string.constellation_aries);
            case ConstellationKeys.AURIGA: return context.getString(R.string.constellation_auriga);
            case ConstellationKeys.BOOTES: return context.getString(R.string.constellation_bootes);
            case ConstellationKeys.CAMELOPARDALIS: return context.getString(R.string.constellation_camelopardalis);
            case ConstellationKeys.CANCER: return context.getString(R.string.constellation_cancer);
            case ConstellationKeys.CANES_VENATICI: return context.getString(R.string.constellation_canes_venatici);
            case ConstellationKeys.CANIS_MINOR: return context.getString(R.string.constellation_canis_minor);
            case ConstellationKeys.CASSIOPEIA: return context.getString(R.string.constellation_cassiopeia);
            case ConstellationKeys.CEPHEUS: return context.getString(R.string.constellation_cepheus);
            case ConstellationKeys.COMA_BERENICES: return context.getString(R.string.constellation_coma_berenices);
            case ConstellationKeys.CORONA_BOREALIS: return context.getString(R.string.constellation_corona_borealis);
            case ConstellationKeys.CYGNUS: return context.getString(R.string.constellation_cygnus);
            case ConstellationKeys.DELPHINUS: return context.getString(R.string.constellation_delphinus);
            case ConstellationKeys.DRACO: return context.getString(R.string.constellation_draco);
            case ConstellationKeys.EQUULEUS: return context.getString(R.string.constellation_equuleus);
            case ConstellationKeys.GEMINI: return context.getString(R.string.constellation_gemini);
            case ConstellationKeys.HERCULES: return context.getString(R.string.constellation_hercules);
            case ConstellationKeys.LACERTA: return context.getString(R.string.constellation_lacerta);
            case ConstellationKeys.LEO: return context.getString(R.string.constellation_leo);
            case ConstellationKeys.LEO_MINOR: return context.getString(R.string.constellation_leo_minor);
            case ConstellationKeys.LIBRA: return context.getString(R.string.constellation_libra);
            case ConstellationKeys.LYNX: return context.getString(R.string.constellation_lynx);
            case ConstellationKeys.LYRA: return context.getString(R.string.constellation_lyra);
            case ConstellationKeys.ORION: return context.getString(R.string.constellation_orion);
            case ConstellationKeys.PEGASUS: return context.getString(R.string.constellation_pegasus);
            case ConstellationKeys.PERSEUS: return context.getString(R.string.constellation_perseus);
            case ConstellationKeys.PISCES: return context.getString(R.string.constellation_pisces);
            case ConstellationKeys.SAGITTA: return context.getString(R.string.constellation_sagitta);
            case ConstellationKeys.SCORPIUS: return context.getString(R.string.constellation_scorpius);
            case ConstellationKeys.TAURUS: return context.getString(R.string.constellation_taurus);
            case ConstellationKeys.TRIANGULUM: return context.getString(R.string.constellation_triangulum);
            case ConstellationKeys.URSA_MAJOR: return context.getString(R.string.constellation_ursa_major);
            case ConstellationKeys.URSA_MINOR: return context.getString(R.string.constellation_ursa_minor);
            case ConstellationKeys.VIRGO: return context.getString(R.string.constellation_virgo);
            case ConstellationKeys.VULPECULA: return context.getString(R.string.constellation_vulpecula);

            // Southern (Keyser & de Houtman)
            case ConstellationKeys.APUS: return context.getString(R.string.constellation_apus);
            case ConstellationKeys.CHAMAELEON: return context.getString(R.string.constellation_chamaeleon);
            case ConstellationKeys.DORADO: return context.getString(R.string.constellation_dorado);
            case ConstellationKeys.GRUS: return context.getString(R.string.constellation_grus);
            case ConstellationKeys.HYDRUS: return context.getString(R.string.constellation_hydrus);
            case ConstellationKeys.INDUS: return context.getString(R.string.constellation_indus);
            case ConstellationKeys.MUSCA: return context.getString(R.string.constellation_musca);
            case ConstellationKeys.PAVO: return context.getString(R.string.constellation_pavo);
            case ConstellationKeys.PHOENIX: return context.getString(R.string.constellation_phoenix);
            case ConstellationKeys.TRIANGULUM_AUSTRALE: return context.getString(R.string.constellation_triangulum_australe);
            case ConstellationKeys.TUCANA: return context.getString(R.string.constellation_tucana);
            case ConstellationKeys.VOLANS: return context.getString(R.string.constellation_volans);

            // Southern (Lacaille)
            case ConstellationKeys.ANTLIA: return context.getString(R.string.constellation_antlia);
            case ConstellationKeys.CAELUM: return context.getString(R.string.constellation_caelum);
            case ConstellationKeys.CIRCINUS: return context.getString(R.string.constellation_circinus);
            case ConstellationKeys.FORNAX: return context.getString(R.string.constellation_fornax);
            case ConstellationKeys.HOROLOGIUM: return context.getString(R.string.constellation_horologium);
            case ConstellationKeys.MENSA: return context.getString(R.string.constellation_mensa);
            case ConstellationKeys.MICROSCOPIUM: return context.getString(R.string.constellation_microscopium);
            case ConstellationKeys.NORMA: return context.getString(R.string.constellation_norma);
            case ConstellationKeys.OCTANS: return context.getString(R.string.constellation_octans);
            case ConstellationKeys.PICTOR: return context.getString(R.string.constellation_pictor);
            case ConstellationKeys.PYXIS: return context.getString(R.string.constellation_pyxis);
            case ConstellationKeys.RETICULUM: return context.getString(R.string.constellation_reticulum);
            case ConstellationKeys.SCULPTOR: return context.getString(R.string.constellation_sculptor);
            case ConstellationKeys.TELESCOPIUM: return context.getString(R.string.constellation_telescopium);

            // Other southern / equatorial
            case ConstellationKeys.AQUARIUS: return context.getString(R.string.constellation_aquarius);
            case ConstellationKeys.ARA: return context.getString(R.string.constellation_ara);
            case ConstellationKeys.CANIS_MAJOR: return context.getString(R.string.constellation_canis_major);
            case ConstellationKeys.CAPRICORNUS: return context.getString(R.string.constellation_capricornus);
            case ConstellationKeys.CARINA: return context.getString(R.string.constellation_carina);
            case ConstellationKeys.CENTAURUS: return context.getString(R.string.constellation_centaurus);
            case ConstellationKeys.CETUS: return context.getString(R.string.constellation_cetus);
            case ConstellationKeys.COLUMBA: return context.getString(R.string.constellation_columba);
            case ConstellationKeys.CORONA_AUSTRALIS: return context.getString(R.string.constellation_corona_australis);
            case ConstellationKeys.CORVUS: return context.getString(R.string.constellation_corvus);
            case ConstellationKeys.CRATER: return context.getString(R.string.constellation_crater);
            case ConstellationKeys.CRUX: return context.getString(R.string.constellation_crux);
            case ConstellationKeys.ERIDANUS: return context.getString(R.string.constellation_eridanus);
            case ConstellationKeys.HYDRA: return context.getString(R.string.constellation_hydra);
            case ConstellationKeys.LEPUS: return context.getString(R.string.constellation_lepus);
            case ConstellationKeys.LUPUS: return context.getString(R.string.constellation_lupus);
            case ConstellationKeys.MONOCEROS: return context.getString(R.string.constellation_monoceros);
            case ConstellationKeys.OPHIUCHUS: return context.getString(R.string.constellation_ophiuchus);
            case ConstellationKeys.PISCIS_AUSTRINUS: return context.getString(R.string.constellation_piscis_austrinus);
            case ConstellationKeys.PUPPIS: return context.getString(R.string.constellation_puppis);
            case ConstellationKeys.SAGITTARIUS: return context.getString(R.string.constellation_sagittarius);
            case ConstellationKeys.SCUTUM: return context.getString(R.string.constellation_scutum);
            case ConstellationKeys.SERPENS: return context.getString(R.string.constellation_serpens);
            case ConstellationKeys.SEXTANS: return context.getString(R.string.constellation_sextans);
            case ConstellationKeys.VELA: return context.getString(R.string.constellation_vela);

            default: return key;
        }
    }

    private String getLocalizedDescription(Context context) {
        switch (key) {
            // Northern
            case ConstellationKeys.ANDROMEDA: return context.getString(R.string.constellation_andromeda_description);
            case ConstellationKeys.AQUILA: return context.getString(R.string.constellation_aquila_description);
            case ConstellationKeys.ARIES: return context.getString(R.string.constellation_aries_description);
            case ConstellationKeys.AURIGA: return context.getString(R.string.constellation_auriga_description);
            case ConstellationKeys.BOOTES: return context.getString(R.string.constellation_bootes_description);
            case ConstellationKeys.CAMELOPARDALIS: return context.getString(R.string.constellation_camelopardalis_description);
            case ConstellationKeys.CANCER: return context.getString(R.string.constellation_cancer_description);
            case ConstellationKeys.CANES_VENATICI: return context.getString(R.string.constellation_canes_venatici_description);
            case ConstellationKeys.CANIS_MINOR: return context.getString(R.string.constellation_canis_minor_description);
            case ConstellationKeys.CASSIOPEIA: return context.getString(R.string.constellation_cassiopeia_description);
            case ConstellationKeys.CEPHEUS: return context.getString(R.string.constellation_cepheus_description);
            case ConstellationKeys.COMA_BERENICES: return context.getString(R.string.constellation_coma_berenices_description);
            case ConstellationKeys.CORONA_BOREALIS: return context.getString(R.string.constellation_corona_borealis_description);
            case ConstellationKeys.CYGNUS: return context.getString(R.string.constellation_cygnus_description);
            case ConstellationKeys.DELPHINUS: return context.getString(R.string.constellation_delphinus_description);
            case ConstellationKeys.DRACO: return context.getString(R.string.constellation_draco_description);
            case ConstellationKeys.EQUULEUS: return context.getString(R.string.constellation_equuleus_description);
            case ConstellationKeys.GEMINI: return context.getString(R.string.constellation_gemini_description);
            case ConstellationKeys.HERCULES: return context.getString(R.string.constellation_hercules_description);
            case ConstellationKeys.LACERTA: return context.getString(R.string.constellation_lacerta_description);
            case ConstellationKeys.LEO: return context.getString(R.string.constellation_leo_description);
            case ConstellationKeys.LEO_MINOR: return context.getString(R.string.constellation_leo_minor_description);
            case ConstellationKeys.LIBRA: return context.getString(R.string.constellation_libra_description);
            case ConstellationKeys.LYNX: return context.getString(R.string.constellation_lynx_description);
            case ConstellationKeys.LYRA: return context.getString(R.string.constellation_lyra_description);
            case ConstellationKeys.ORION: return context.getString(R.string.constellation_orion_description);
            case ConstellationKeys.PEGASUS: return context.getString(R.string.constellation_pegasus_description);
            case ConstellationKeys.PERSEUS: return context.getString(R.string.constellation_perseus_description);
            case ConstellationKeys.PISCES: return context.getString(R.string.constellation_pisces_description);
            case ConstellationKeys.SAGITTA: return context.getString(R.string.constellation_sagitta_description);
            case ConstellationKeys.SCORPIUS: return context.getString(R.string.constellation_scorpius_description);
            case ConstellationKeys.TAURUS: return context.getString(R.string.constellation_taurus_description);
            case ConstellationKeys.TRIANGULUM: return context.getString(R.string.constellation_triangulum_description);
            case ConstellationKeys.URSA_MAJOR: return context.getString(R.string.constellation_ursa_major_description);
            case ConstellationKeys.URSA_MINOR: return context.getString(R.string.constellation_ursa_minor_description);
            case ConstellationKeys.VIRGO: return context.getString(R.string.constellation_virgo_description);
            case ConstellationKeys.VULPECULA: return context.getString(R.string.constellation_vulpecula_description);

            // Southern (Keyser & de Houtman)
            case ConstellationKeys.APUS: return context.getString(R.string.constellation_apus_description);
            case ConstellationKeys.CHAMAELEON: return context.getString(R.string.constellation_chamaeleon_description);
            case ConstellationKeys.DORADO: return context.getString(R.string.constellation_dorado_description);
            case ConstellationKeys.GRUS: return context.getString(R.string.constellation_grus_description);
            case ConstellationKeys.HYDRUS: return context.getString(R.string.constellation_hydrus_description);
            case ConstellationKeys.INDUS: return context.getString(R.string.constellation_indus_description);
            case ConstellationKeys.MUSCA: return context.getString(R.string.constellation_musca_description);
            case ConstellationKeys.PAVO: return context.getString(R.string.constellation_pavo_description);
            case ConstellationKeys.PHOENIX: return context.getString(R.string.constellation_phoenix_description);
            case ConstellationKeys.TRIANGULUM_AUSTRALE: return context.getString(R.string.constellation_triangulum_australe_description);
            case ConstellationKeys.TUCANA: return context.getString(R.string.constellation_tucana_description);
            case ConstellationKeys.VOLANS: return context.getString(R.string.constellation_volans_description);

            // Southern (Lacaille)
            case ConstellationKeys.ANTLIA: return context.getString(R.string.constellation_antlia_description);
            case ConstellationKeys.CAELUM: return context.getString(R.string.constellation_caelum_description);
            case ConstellationKeys.CIRCINUS: return context.getString(R.string.constellation_circinus_description);
            case ConstellationKeys.FORNAX: return context.getString(R.string.constellation_fornax_description);
            case ConstellationKeys.HOROLOGIUM: return context.getString(R.string.constellation_horologium_description);
            case ConstellationKeys.MENSA: return context.getString(R.string.constellation_mensa_description);
            case ConstellationKeys.MICROSCOPIUM: return context.getString(R.string.constellation_microscopium_description);
            case ConstellationKeys.NORMA: return context.getString(R.string.constellation_norma_description);
            case ConstellationKeys.OCTANS: return context.getString(R.string.constellation_octans_description);
            case ConstellationKeys.PICTOR: return context.getString(R.string.constellation_pictor_description);
            case ConstellationKeys.PYXIS: return context.getString(R.string.constellation_pyxis_description);
            case ConstellationKeys.RETICULUM: return context.getString(R.string.constellation_reticulum_description);
            case ConstellationKeys.SCULPTOR: return context.getString(R.string.constellation_sculptor_description);
            case ConstellationKeys.TELESCOPIUM: return context.getString(R.string.constellation_telescopium_description);

            // Other southern / equatorial
            case ConstellationKeys.AQUARIUS: return context.getString(R.string.constellation_aquarius_description);
            case ConstellationKeys.ARA: return context.getString(R.string.constellation_ara_description);
            case ConstellationKeys.CANIS_MAJOR: return context.getString(R.string.constellation_canis_major_description);
            case ConstellationKeys.CAPRICORNUS: return context.getString(R.string.constellation_capricornus_description);
            case ConstellationKeys.CARINA: return context.getString(R.string.constellation_carina_description);
            case ConstellationKeys.CENTAURUS: return context.getString(R.string.constellation_centaurus_description);
            case ConstellationKeys.CETUS: return context.getString(R.string.constellation_cetus_description);
            case ConstellationKeys.COLUMBA: return context.getString(R.string.constellation_columba_description);
            case ConstellationKeys.CORONA_AUSTRALIS: return context.getString(R.string.constellation_corona_australis_description);
            case ConstellationKeys.CORVUS: return context.getString(R.string.constellation_corvus_description);
            case ConstellationKeys.CRATER: return context.getString(R.string.constellation_crater_description);
            case ConstellationKeys.CRUX: return context.getString(R.string.constellation_crux_description);
            case ConstellationKeys.ERIDANUS: return context.getString(R.string.constellation_eridanus_description);
            case ConstellationKeys.HYDRA: return context.getString(R.string.constellation_hydra_description);
            case ConstellationKeys.LEPUS: return context.getString(R.string.constellation_lepus_description);
            case ConstellationKeys.LUPUS: return context.getString(R.string.constellation_lupus_description);
            case ConstellationKeys.MONOCEROS: return context.getString(R.string.constellation_monoceros_description);
            case ConstellationKeys.OPHIUCHUS: return context.getString(R.string.constellation_ophiuchus_description);
            case ConstellationKeys.PISCIS_AUSTRINUS: return context.getString(R.string.constellation_piscis_austrinus_description);
            case ConstellationKeys.PUPPIS: return context.getString(R.string.constellation_puppis_description);
            case ConstellationKeys.SAGITTARIUS: return context.getString(R.string.constellation_sagittarius_description);
            case ConstellationKeys.SCUTUM: return context.getString(R.string.constellation_scutum_description);
            case ConstellationKeys.SERPENS: return context.getString(R.string.constellation_serpens_description);
            case ConstellationKeys.SEXTANS: return context.getString(R.string.constellation_sextans_description);
            case ConstellationKeys.VELA: return context.getString(R.string.constellation_vela_description);

            default: return "";
        }
    }

    private int getImageResourceByKey() {
        switch (key) {
            // Northern
            case ConstellationKeys.ANDROMEDA: return R.drawable.const_andromeda;
            case ConstellationKeys.AQUILA: return R.drawable.const_aquila;
            case ConstellationKeys.ARIES: return R.drawable.const_aries;
            case ConstellationKeys.AURIGA: return R.drawable.const_auriga;
            case ConstellationKeys.BOOTES: return R.drawable.const_bootes;
            case ConstellationKeys.CAMELOPARDALIS: return R.drawable.const_camelopardalis;
            case ConstellationKeys.CANCER: return R.drawable.const_cancer;
            case ConstellationKeys.CANES_VENATICI: return R.drawable.const_canes_venatici;
            case ConstellationKeys.CANIS_MINOR: return R.drawable.const_canis_minor;
            case ConstellationKeys.CASSIOPEIA: return R.drawable.const_cassiopeia;
            case ConstellationKeys.CEPHEUS: return R.drawable.const_cepheus;
            case ConstellationKeys.COMA_BERENICES: return R.drawable.const_coma_berenices;
            case ConstellationKeys.CORONA_BOREALIS: return R.drawable.const_corona_borealis;
            case ConstellationKeys.CYGNUS: return R.drawable.const_cygnus;
            case ConstellationKeys.DELPHINUS: return R.drawable.const_delphinus;
            case ConstellationKeys.DRACO: return R.drawable.const_draco;
            case ConstellationKeys.EQUULEUS: return R.drawable.const_equuleus;
            case ConstellationKeys.GEMINI: return R.drawable.const_gemini;
            case ConstellationKeys.HERCULES: return R.drawable.const_hercules;
            case ConstellationKeys.LACERTA: return R.drawable.const_lacerta;
            case ConstellationKeys.LEO: return R.drawable.const_leo;
            case ConstellationKeys.LEO_MINOR: return R.drawable.const_leo_minor;
            case ConstellationKeys.LIBRA: return R.drawable.const_libra;   // <-- ADDED
            case ConstellationKeys.LYNX: return R.drawable.const_lynx;
            case ConstellationKeys.LYRA: return R.drawable.const_lyra;
            case ConstellationKeys.ORION: return R.drawable.const_orion;
            case ConstellationKeys.PEGASUS: return R.drawable.const_pegasus;
            case ConstellationKeys.PERSEUS: return R.drawable.const_perseus;
            case ConstellationKeys.PISCES: return R.drawable.const_pisces;
            case ConstellationKeys.SAGITTA: return R.drawable.const_sagitta;
            case ConstellationKeys.SCORPIUS: return R.drawable.const_scorpius;
            case ConstellationKeys.TAURUS: return R.drawable.const_taurus;
            case ConstellationKeys.TRIANGULUM: return R.drawable.const_triangulum;
            case ConstellationKeys.URSA_MAJOR: return R.drawable.const_ursa_major;
            case ConstellationKeys.URSA_MINOR: return R.drawable.const_ursa_minor;
            case ConstellationKeys.VIRGO: return R.drawable.const_virgo;
            case ConstellationKeys.VULPECULA: return R.drawable.const_vulpecula;

            // Southern (Keyser & de Houtman)
            case ConstellationKeys.APUS: return R.drawable.const_apus;
            case ConstellationKeys.CHAMAELEON: return R.drawable.const_chamaeleon;
            case ConstellationKeys.DORADO: return R.drawable.const_dorado;
            case ConstellationKeys.GRUS: return R.drawable.const_grus;
            case ConstellationKeys.HYDRUS: return R.drawable.const_hydrus;
            case ConstellationKeys.INDUS: return R.drawable.const_indus;
            case ConstellationKeys.MUSCA: return R.drawable.const_musca;
            case ConstellationKeys.PAVO: return R.drawable.const_pavo;
            case ConstellationKeys.PHOENIX: return R.drawable.const_phoenix;
            case ConstellationKeys.TRIANGULUM_AUSTRALE: return R.drawable.const_triangulum_australe;
            case ConstellationKeys.TUCANA: return R.drawable.const_tucana;
            case ConstellationKeys.VOLANS: return R.drawable.const_volans;

            // Southern (Lacaille)
            case ConstellationKeys.ANTLIA: return R.drawable.const_antlia;
            case ConstellationKeys.CAELUM: return R.drawable.const_caelum;
            case ConstellationKeys.CIRCINUS: return R.drawable.const_circinus;
            case ConstellationKeys.FORNAX: return R.drawable.const_fornax;
            case ConstellationKeys.HOROLOGIUM: return R.drawable.const_horologium;
            case ConstellationKeys.MENSA: return R.drawable.const_mensa;
            case ConstellationKeys.MICROSCOPIUM: return R.drawable.const_microscopium;
            case ConstellationKeys.NORMA: return R.drawable.const_norma;
            case ConstellationKeys.OCTANS: return R.drawable.const_octans;
            case ConstellationKeys.PICTOR: return R.drawable.const_pictor;
            case ConstellationKeys.PYXIS: return R.drawable.const_pyxis;
            case ConstellationKeys.RETICULUM: return R.drawable.const_reticulum;
            case ConstellationKeys.SCULPTOR: return R.drawable.const_sculptor;
            case ConstellationKeys.TELESCOPIUM: return R.drawable.const_telescopium;

            // Other southern / equatorial
            case ConstellationKeys.AQUARIUS: return R.drawable.const_aquarius;
            case ConstellationKeys.ARA: return R.drawable.const_ara;
            case ConstellationKeys.CANIS_MAJOR: return R.drawable.const_canis_major;
            case ConstellationKeys.CAPRICORNUS: return R.drawable.const_capricornus;
            case ConstellationKeys.CARINA: return R.drawable.const_carina;
            case ConstellationKeys.CENTAURUS: return R.drawable.const_centaurus;
            case ConstellationKeys.CETUS: return R.drawable.const_cetus;
            case ConstellationKeys.COLUMBA: return R.drawable.const_columba;
            case ConstellationKeys.CORONA_AUSTRALIS: return R.drawable.const_corona_australis;
            case ConstellationKeys.CORVUS: return R.drawable.const_corvus;
            case ConstellationKeys.CRATER: return R.drawable.const_crater;
            case ConstellationKeys.CRUX: return R.drawable.const_crux;
            case ConstellationKeys.ERIDANUS: return R.drawable.const_eridanus;
            case ConstellationKeys.HYDRA: return R.drawable.const_hydra;
            case ConstellationKeys.LEPUS: return R.drawable.const_lepus;
            case ConstellationKeys.LUPUS: return R.drawable.const_lupus;
            case ConstellationKeys.MONOCEROS: return R.drawable.const_monoceros;
            case ConstellationKeys.OPHIUCHUS: return R.drawable.const_ophiuchus;
            case ConstellationKeys.PISCIS_AUSTRINUS: return R.drawable.const_piscis_austrinus;
            case ConstellationKeys.PUPPIS: return R.drawable.const_puppis;
            case ConstellationKeys.SAGITTARIUS: return R.drawable.const_sagittarius;
            case ConstellationKeys.SCUTUM: return R.drawable.const_scutum;
            case ConstellationKeys.SERPENS: return R.drawable.const_serpens;
            case ConstellationKeys.SEXTANS: return R.drawable.const_sextans;
            case ConstellationKeys.VELA: return R.drawable.const_vela;

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