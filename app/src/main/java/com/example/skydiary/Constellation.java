package com.example.skydiary;

import java.util.Objects;
import java.util.UUID;

public class Constellation {
    private String id;
    private String name;
    private String description;
    private boolean isSeen;
    private boolean isFavorite;
    private int starCount;
    private int imageResId;

    public Constellation() {
        this.id = UUID.randomUUID().toString();
    }

    public Constellation(String name, String description, int starCount, int imageResId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.starCount = starCount;
        this.imageResId = imageResId;
        this.isSeen = false;
        this.isFavorite = false;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isSeen() { return isSeen; }
    public void setSeen(boolean seen) { isSeen = seen; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public int getStarCount() { return starCount; }

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