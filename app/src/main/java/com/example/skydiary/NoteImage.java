package com.example.skydiary;

import java.util.Objects;
import java.util.UUID;

public class NoteImage {
    private String id;
    private String imagePath;
    private int position;
    private float rotation;
    private int originalWidth;
    private int originalHeight;

    public NoteImage(String imagePath, int position) {
        this.id = UUID.randomUUID().toString();
        this.imagePath = imagePath;
        this.position = position;
        this.rotation = 0f;
        this.originalWidth = 0;
        this.originalHeight = 0;
    }

    public NoteImage(String id, String imagePath, int position, float rotation, int originalWidth, int originalHeight) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.imagePath = imagePath;
        this.position = position;
        this.rotation = rotation;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }

    public int getOriginalWidth() { return originalWidth; }
    public void setOriginalWidth(int originalWidth) { this.originalWidth = originalWidth; }

    public int getOriginalHeight() { return originalHeight; }
    public void setOriginalHeight(int originalHeight) { this.originalHeight = originalHeight; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteImage)) return false;
        NoteImage noteImage = (NoteImage) o;
        return Objects.equals(id, noteImage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}