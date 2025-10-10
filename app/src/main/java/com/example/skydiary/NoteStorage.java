package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteStorage {
    private static final String PREFS_NAME = "notes_prefs";
    private static final String NOTES_KEY = "notes";

    private static NoteStorage instance;
    private SharedPreferences prefs;
    private Gson gson;
    private static final String TAGS_KEY = "tags";


    private NoteStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized NoteStorage getInstance(Context context) {
        if (instance == null) {
            instance = new NoteStorage(context.getApplicationContext());
        }
        return instance;
    }

    public List<Note> getNotes() {
        String json = prefs.getString(NOTES_KEY, "[]");
        Type listType = new TypeToken<List<Note>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    public void addNote(Note note) {
        List<Note> notes = getNotes();
        notes.add(note);
        saveNotes(notes);
    }

    public void saveNotes(List<Note> notes) {
        String json = gson.toJson(notes);
        prefs.edit().putString(NOTES_KEY, json).apply();
    }


    public List<Note> getNotesByDate(long dateStartMs, long dateEndMs) {
        List<Note> filtered = new ArrayList<>();
        for (Note note : getNotes()) {
            long ts = note.getTimestamp();
            if (ts >= dateStartMs && ts < dateEndMs) {
                filtered.add(note);
            }
        }
        return filtered;
    }

    public Set<Long> getNoteDates() {
        Set<Long> dates = new HashSet<>();
        Calendar calendar = Calendar.getInstance();
        for (Note note : getNotes()) {
            calendar.setTimeInMillis(note.getTimestamp());
            // Normalize date (clear time fields)
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            dates.add(calendar.getTimeInMillis());
        }
        return dates;
    }

    public void updateNote(Note currentNote) {
        List<Note> notes = getNotes();
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getTimestamp() == currentNote.getTimestamp()) {
                notes.set(i, currentNote);
                saveNotes(notes);
                return;
            }
        }
        // If note not found, add it
        addNote(currentNote);
    }

    public Set<String> getAllTags() {
        Set<String> tags = new HashSet<>();
        List<Note> notes = getNotes();
        for (Note note : notes) {
            if (note.getTags() != null) {
                tags.addAll(note.getTags());
            }
        }

        // Load saved tags from prefs (union with tags from notes)
        Set<String> savedTags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        if (savedTags != null) {
            tags.addAll(savedTags);
        }
        return tags;
    }

    // Add a new tag globally and persist
    public void addTag(String newTag) {
        if (newTag == null || newTag.trim().isEmpty()) return;
        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        if (tags == null) tags = new HashSet<>();
        if (tags.add(newTag)) {
            prefs.edit().putStringSet(TAGS_KEY, tags).apply();
        }
    }

    // Delete tag
    public void deleteTag(String tag) {
        if (tag == null) return;
        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        if (tags != null && tags.remove(tag.trim())) {
            prefs.edit().putStringSet(TAGS_KEY, tags).apply();
        }
    }

    // Rename tag
    public void renameTag(String oldTag, String newTag) {
        if (oldTag == null || newTag == null || newTag.trim().isEmpty()) return;
        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        if (tags != null) {
            boolean changed = false;
            if (tags.remove(oldTag.trim())) changed = true;
            if (tags.add(newTag.trim())) changed = true;
            if (changed) {
                prefs.edit().putStringSet(TAGS_KEY, tags).apply();
            }
        }
    }


    public void deleteNote(Note note) {
        if (note == null) return;
        List<Note> notes = getNotes();
        boolean removed = notes.removeIf(n -> n.getTimestamp() == note.getTimestamp());
        if (removed) {
            saveNotes(notes);
        }
    }

}

