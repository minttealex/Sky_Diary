package com.example.skydiary;

import java.util.Objects;

public class NoteImage {
    private String id;
    private String imagePath;
    private int position; // Position in the note
    private float rotation; // Rotation in degrees
    private int originalWidth; // Original width for rotation calculations
    private int originalHeight; // Original height for rotation calculations

    public NoteImage() {
        this.id = java.util.UUID.randomUUID().toString();
        this.rotation = 0f;
        this.originalWidth = 0;
        this.originalHeight = 0;
    }

    public NoteImage(String imagePath) {
        this.id = java.util.UUID.randomUUID().toString();
        this.imagePath = imagePath;
        this.position = 0;
        this.rotation = 0f;
        this.originalWidth = 0;
        this.originalHeight = 0;
    }

    public NoteImage(String imagePath, int position) {
        this.id = java.util.UUID.randomUUID().toString();
        this.imagePath = imagePath;
        this.position = position;
        this.rotation = 0f;
        this.originalWidth = 0;
        this.originalHeight = 0;
    }

    // Getters and setters
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