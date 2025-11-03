package com.example.skydiary;

public class ConstellationResources {
    public static int getConstellationImageResource(String constellationName) {
        switch (constellationName) {
            case "Orion": return R.drawable.const_orion;
            case "Ursa Major": return R.drawable.const_ursa_major;
            case "Cassiopeia": return R.drawable.const_cassiopeia;
            case "Leo": return R.drawable.const_leo;
            case "Scorpius": return R.drawable.const_scorpius;
            case "Cygnus": return R.drawable.const_cygnus;
            case "Lyra": return R.drawable.const_lyra;
            case "Andromeda": return R.drawable.const_andromeda;
            case "Ursa Minor": return R.drawable.const_ursa_minor;
            case "Draco": return R.drawable.const_draco;
            case "Cepheus": return R.drawable.const_cepheus;
            case "Perseus": return R.drawable.const_perseus;
            case "Auriga": return R.drawable.const_auriga;
            case "Bootes": return R.drawable.const_bootes;
            case "Corona Borealis": return R.drawable.const_corona_borealis;
            case "Hercules": return R.drawable.const_hercules;
            case "Sagitta": return R.drawable.const_sagitta;
            case "Aquila": return R.drawable.const_aquila;
            case "Delphinus": return R.drawable.const_delphinus;
            case "Pegasus": return R.drawable.const_pegasus;
            case "Pisces": return R.drawable.const_pisces;
            case "Aries": return R.drawable.const_aries;
            case "Taurus": return R.drawable.const_taurus;
            case "Gemini": return R.drawable.const_gemini;
            case "Cancer": return R.drawable.const_cancer;
            case "Virgo": return R.drawable.const_virgo;
            case "Libra": return R.drawable.const_libra;

            default: return android.R.drawable.ic_menu_gallery;
        }
    }
}