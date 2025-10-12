package com.example.skydiary;

import java.util.Objects;

public class NoteImage {
    private String id;
    private String imagePath;
    private int position; // Position in the note
    private float width; // Custom width (0 = auto)
    private float height; // Custom height (0 = auto)

    public NoteImage() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public NoteImage(String imagePath) {
        this.id = java.util.UUID.randomUUID().toString();
        this.imagePath = imagePath;
        this.position = 0;
        this.width = 0;
        this.height = 0;
    }

    public NoteImage(String imagePath, int position) {
        this.id = java.util.UUID.randomUUID().toString();
        this.imagePath = imagePath;
        this.position = position;
        this.width = 0;
        this.height = 0;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

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
