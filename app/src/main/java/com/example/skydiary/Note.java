package com.example.skydiary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Note {
    private String name;
    private String text;
    private long timestamp;  // milliseconds from epoch
    private List<String> tags; // Add tags field

    public Note(String name, String text, long timestamp) {
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = new ArrayList<>();
    }

    public Note(String name, String text, long timestamp, List<String> tags) {
        this.name = name;
        this.text = text;
        this.timestamp = timestamp;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<String> getTags() {
        return tags == null ? new ArrayList<>() : tags;
    }

    public void setTags(ArrayList<String> strings) {
        this.tags = strings != null ? strings : new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Note{" +
                "text='" + text + '\'' +
                ", timestamp=" + timestamp +
                ", tags=" + tags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;
        Note note = (Note) o;
        return timestamp == note.timestamp &&
                Objects.equals(text, note.text) &&
                Objects.equals(tags, note.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, timestamp, tags);
    }
}
