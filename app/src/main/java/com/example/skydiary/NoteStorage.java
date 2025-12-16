package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteStorage {
    private static final String TAG = "NoteStorage";
    private static final String PREFS_NAME = "notes_prefs";
    private static final String NOTES_KEY = "notes";
    private static final String DELETED_NOTES_KEY = "deleted_notes";
    private static final String LAST_SYNC_KEY = "last_sync";
    private static final String TAGS_KEY = "tags";

    private static NoteStorage instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private NoteStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder().create();
    }

    public static synchronized NoteStorage getInstance(Context context) {
        if (instance == null) {
            instance = new NoteStorage(context.getApplicationContext());
        }
        return instance;
    }

    public List<Note> getAllNotes() {
        try {
            String json = prefs.getString(NOTES_KEY, "[]");
            Type listType = new TypeToken<List<Note>>() {}.getType();
            List<Note> notes = gson.fromJson(json, listType);
            return notes != null ? notes : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error reading notes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Note> getNotes() {
        List<Note> allNotes = getAllNotes();
        List<Note> activeNotes = new ArrayList<>();
        for (Note note : allNotes) {
            if (!note.isDeleted()) {
                activeNotes.add(note);
            }
        }
        return activeNotes;
    }

    public void addNote(Note note) {
        List<Note> allNotes = getAllNotes();
        allNotes.add(note);
        saveNotes(allNotes);
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

    public void updateNote(Note currentNote) {
        List<Note> allNotes = getAllNotes();
        boolean found = false;
        for (int i = 0; i < allNotes.size(); i++) {
            if (allNotes.get(i).getId().equals(currentNote.getId())) {
                allNotes.set(i, currentNote);
                found = true;
                break;
            }
        }
        if (!found) {
            allNotes.add(currentNote);
        }
        saveNotes(allNotes);
    }

    public Set<String> getAllTags() {
        Set<String> tags = new HashSet<>();
        List<Note> notes = getNotes();
        for (Note note : notes) {
            if (note.getTags() != null) {
                tags.addAll(note.getTags());
            }
        }

        Set<String> savedTags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        tags.addAll(savedTags);
        return tags;
    }

    public void addTag(String newTag) {
        if (newTag == null || newTag.trim().isEmpty()) return;
        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        Set<String> newTags = new HashSet<>(tags);
        if (newTags.add(newTag.trim())) {
            prefs.edit().putStringSet(TAGS_KEY, newTags).apply();
        }
    }

    public void deleteTag(String tag) {
        if (tag == null) return;

        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        Set<String> newTags = new HashSet<>(tags);
        if (newTags.remove(tag.trim())) {
            prefs.edit().putStringSet(TAGS_KEY, newTags).apply();
        }

        List<Note> allNotes = getAllNotes();
        boolean notesUpdated = false;
        for (Note note : allNotes) {
            if (note.getTags() != null && note.getTags().contains(tag)) {
                note.getTags().remove(tag);
                notesUpdated = true;
            }
        }
        if (notesUpdated) {
            saveNotes(allNotes);
        }
    }

    public void renameTag(String oldTag, String newTag) {
        if (oldTag == null || newTag == null || newTag.trim().isEmpty()) return;

        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        Set<String> newTags = new HashSet<>(tags);
        if (newTags.remove(oldTag.trim())) {
            newTags.add(newTag.trim());
            prefs.edit().putStringSet(TAGS_KEY, newTags).apply();
        }

        List<Note> allNotes = getAllNotes();
        boolean notesUpdated = false;
        for (Note note : allNotes) {
            if (note.getTags() != null && note.getTags().contains(oldTag)) {
                note.getTags().remove(oldTag);
                note.getTags().add(newTag);
                notesUpdated = true;
            }
        }
        if (notesUpdated) {
            saveNotes(allNotes);
        }
    }

    public void deleteNote(Note note) {
        if (note == null) return;

        note.setDeleted(true);
        updateNote(note);

        markNoteAsDeleted(note.getId());
    }

    public Note getNoteById(String noteId) {
        if (noteId == null) return null;
        List<Note> allNotes = getAllNotes();
        for (Note note : allNotes) {
            if (noteId.equals(note.getId())) {
                return note;
            }
        }
        return null;
    }

    public String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            File imagesDir = new File(context.getFilesDir(), "note_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            String filename = "image_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(imagesDir, filename);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while (true) {
                assert inputStream != null;
                if ((bytesRead = inputStream.read(buffer)) == -1) break;
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void markNoteAsDeleted(String noteId) {
        if (noteId == null) return;

        Set<String> deletedNotes = prefs.getStringSet(DELETED_NOTES_KEY, new HashSet<>());
        Set<String> newDeletedNotes = new HashSet<>(deletedNotes);
        newDeletedNotes.add(noteId);

        prefs.edit().putStringSet(DELETED_NOTES_KEY, newDeletedNotes).apply();
        Log.d(TAG, "Marked note as deleted: " + noteId);
    }

    public List<String> getDeletedNoteIds() {
        Set<String> deletedNotes = prefs.getStringSet(DELETED_NOTES_KEY, new HashSet<>());
        return new ArrayList<>(deletedNotes);
    }

    public void clearDeletedNotes() {
        prefs.edit().remove(DELETED_NOTES_KEY).apply();
        Log.d(TAG, "Cleared deleted notes tracking");
    }

    public long getLastSyncTime() {
        return prefs.getLong(LAST_SYNC_KEY, 0);
    }

    public void setLastSyncTime(long timestamp) {
        prefs.edit().putLong(LAST_SYNC_KEY, timestamp).apply();
    }

    public void clearAllData() {
        try {
            prefs.edit().putString(NOTES_KEY, "[]").apply();

            prefs.edit().remove(DELETED_NOTES_KEY).apply();

            prefs.edit().remove(LAST_SYNC_KEY).apply();

            prefs.edit().remove(TAGS_KEY).apply();

            Log.d(TAG, "Cleared all local note data");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing data: " + e.getMessage());
        }
    }
    public void clearTags() {
        try {
            prefs.edit().remove(TAGS_KEY).apply();
            Log.d(TAG, "Cleared all tags");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing tags: " + e.getMessage());
        }
    }
}