package com.example.skydiary;

import java.util.Objects;

public class NoteImage {
    private String id;
    private final String imagePath;
    private int position;
    private float rotation;
    private int originalWidth;
    private int originalHeight;

    public NoteImage(String imagePath, int position) {
        this.id = java.util.UUID.randomUUID().toString();
        this.imagePath = imagePath;
        this.position = position;
        this.rotation = 0f;
        this.originalWidth = 0;
        this.originalHeight = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImagePath() { return imagePath; }
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