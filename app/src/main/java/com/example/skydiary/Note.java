package com.example.skydiary;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Note {
    private String id;
    private String userId;
    private String name;
    private String location;
    private String text;
    private long timestamp;
    private List<String> tags;
    private List<NoteImage> images;
    private Date createdAt;
    private Date updatedAt;

    public Note() {
        this.id = UUID.randomUUID().toString();
        this.tags = new ArrayList<>();
        this.images = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Note(String name, String text, long timestamp) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = new ArrayList<>();
        this.images = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Note(String name, String text, long timestamp, List<String> tags) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.images = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Note(String name, String text, long timestamp, List<String> tags, List<NoteImage> images) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.images = images != null ? images : new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Note(String name, String location, String text, long timestamp, List<String> tags, List<NoteImage> images) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.images = images != null ? images : new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Note(String id, String userId, String name, String location, String text, long timestamp,
                List<String> tags, List<NoteImage> images, Date createdAt, Date updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.userId = userId;
        this.name = name;
        this.location = location;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.images = images != null ? images : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.updatedAt = updatedAt != null ? updatedAt : new Date();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public List<String> getTags() { return tags == null ? new ArrayList<>() : tags; }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

    public List<NoteImage> getImages() {
        return images == null ? new ArrayList<>() : images;
    }

    public void setImages(List<NoteImage> images) {
        this.images = images != null ? images : new ArrayList<>();
    }

    public Date getCreatedAt() { return createdAt != null ? createdAt : new Date(); }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt != null ? updatedAt : new Date(); }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    @NonNull
    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                ", tags=" + tags +
                ", images=" + images +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
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