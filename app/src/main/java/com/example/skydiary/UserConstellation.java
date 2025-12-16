package com.example.skydiary;

public class UserConstellation {
    private String id;
    private final String constellationKey;
    private final boolean isSeen;
    private final boolean isFavorite;

    public UserConstellation(String constellationKey, boolean isSeen, boolean isFavorite) {
        this.constellationKey = constellationKey;
        this.isSeen = isSeen;
        this.isFavorite = isFavorite;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConstellationKey() { return constellationKey; }

    public boolean isSeen() { return isSeen; }

    public boolean isFavorite() { return isFavorite; }

}