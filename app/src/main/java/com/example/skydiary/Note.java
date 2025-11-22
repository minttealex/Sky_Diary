package com.example.skydiary;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
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
    private String createdAt;
    private String updatedAt;

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

    public Note() {
        this.id = UUID.randomUUID().toString();
        this.tags = new ArrayList<>();
        this.images = new ArrayList<>();
        Date now = new Date();
        this.createdAt = formatDate(now);
        this.updatedAt = this.createdAt;
        this.timestamp = now.getTime();
    }

    public Note(String name, String text, long timestamp) {
        this();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.updatedAt = formatDate(new Date());
    }

    public Note(String name, String text, long timestamp, List<String> tags) {
        this();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.updatedAt = formatDate(new Date());
    }

    public Note(String name, String text, long timestamp, List<String> tags, List<NoteImage> images) {
        this();
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.updatedAt = formatDate(new Date());
    }

    public Note(String name, String location, String text, long timestamp, List<String> tags, List<NoteImage> images) {
        this();
        this.name = name;
        this.location = location;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.updatedAt = formatDate(new Date());
    }

    public Note(String id, String userId, String name, String location, String text, long timestamp,
                List<String> tags, List<NoteImage> images, Date createdAt, Date updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.userId = userId;
        this.name = name;
        this.location = location;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.createdAt = createdAt != null ? formatDate(createdAt) : formatDate(new Date());
        this.updatedAt = updatedAt != null ? formatDate(updatedAt) : this.createdAt;
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TIME_ZONE);
        return sdf.format(date);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateTimestamp();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        updateTimestamp();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        updateTimestamp();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        updateTimestamp();
    }

    public List<String> getTags() {
        return tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        updateTimestamp();
    }

    public List<NoteImage> getImages() {
        return images == null ? new ArrayList<>() : new ArrayList<>(images);
    }

    public void setImages(List<NoteImage> images) {
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        updateTimestamp();
    }

    public String getCreatedAt() {
        if (createdAt == null) {
            createdAt = formatDate(new Date());
        }
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = formatDate(createdAt);
    }

    public String getUpdatedAt() {
        if (updatedAt == null) {
            updatedAt = getCreatedAt();
        }
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = formatDate(updatedAt);
    }

    private void updateTimestamp() {
        this.updatedAt = formatDate(new Date());
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag.trim())) {
            tags.add(tag.trim());
            updateTimestamp();
        }
    }

    public void removeTag(String tag) {
        if (tags != null && tag != null) {
            tags.remove(tag.trim());
            updateTimestamp();
        }
    }

    public void addImage(NoteImage image) {
        if (images == null) {
            images = new ArrayList<>();
        }
        if (image != null && !images.contains(image)) {
            images.add(image);
            updateTimestamp();
        }
    }

    public void removeImage(NoteImage image) {
        if (images != null && image != null) {
            images.remove(image);
            updateTimestamp();
        }
    }

    public boolean hasTag(String tag) {
        return tags != null && tag != null && tags.contains(tag.trim());
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

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
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
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