package com.example.skydiary;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Note {
    private String id;
    private String name;
    private String text;
    private long timestamp;
    private List<String> tags;
    private List<NoteImage> images; // New field for images

    // Default constructor for Gson
    public Note() {
        this.id = UUID.randomUUID().toString();
        this.tags = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    public Note(String name, String text, long timestamp) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    public Note(String name, String text, long timestamp, List<String> tags) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.images = new ArrayList<>();
    }

    public Note(String name, String text, long timestamp, List<String> tags, List<NoteImage> images) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.images = images != null ? images : new ArrayList<>();
    }

    // Add getter and setter for images
    public List<NoteImage> getImages() {
        return images == null ? new ArrayList<>() : images;
    }

    public void setImages(List<NoteImage> images) {
        this.images = images != null ? images : new ArrayList<>();
    }

    // Rest of your existing methods remain the same...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public List<String> getTags() { return tags == null ? new ArrayList<>() : tags; }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

    @NonNull
    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                ", tags=" + tags +
                ", images=" + images +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;
        Note note = (Note) o;
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
