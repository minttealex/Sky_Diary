package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class NoteStorage {
    private static final String PREFS_NAME = "notes_prefs";
    private static final String NOTES_KEY = "notes";
    private static final String MIGRATION_KEY = "migration_done";

    private static NoteStorage instance;
    private SharedPreferences prefs;
    private Gson gson;
    private static final String TAGS_KEY = "tags";

    private NoteStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder().create();
        ensureMigration();
    }

    public static synchronized NoteStorage getInstance(Context context) {
        if (instance == null) {
            instance = new NoteStorage(context.getApplicationContext());
        }
        return instance;
    }

    private void ensureMigration() {
        boolean migrationDone = prefs.getBoolean(MIGRATION_KEY, false);
        if (!migrationDone) {
            migrateNotes();
            prefs.edit().putBoolean(MIGRATION_KEY, true).apply();
        }
    }

    private void migrateNotes() {
        String json = prefs.getString(NOTES_KEY, "[]");
        Type listType = new TypeToken<List<OldNote>>() {}.getType();
        List<OldNote> oldNotes = gson.fromJson(json, listType);

        if (oldNotes != null && !oldNotes.isEmpty()) {
            List<Note> newNotes = new ArrayList<>();
            for (OldNote oldNote : oldNotes) {
                Note newNote = new Note();
                newNote.setId(UUID.randomUUID().toString());
                newNote.setName(oldNote.name);
                newNote.setLocation(oldNote.location);
                newNote.setText(oldNote.text);
                newNote.setTimestamp(oldNote.timestamp);
                newNote.setTags(oldNote.tags != null ? oldNote.tags : new ArrayList<>());
                newNotes.add(newNote);
            }
            saveNotes(newNotes);
        }
    }

    // Helper class to deserialize old note format
    private static class OldNote {
        private String name;
        private String location;
        private String text;
        private long timestamp;
        private List<String> tags;
    }

    public List<Note> getNotes() {
        String json = prefs.getString(NOTES_KEY, "[]");
        Type listType = new TypeToken<List<Note>>() {}.getType();
        List<Note> notes = gson.fromJson(json, listType);
        return notes != null ? notes : new ArrayList<>();
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
            if (notes.get(i).getId().equals(currentNote.getId())) {
                notes.set(i, currentNote);
                saveNotes(notes);
                return;
            }
        }
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

        Set<String> savedTags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        if (savedTags != null) {
            tags.addAll(savedTags);
        }
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

    // Delete tag from both global tags and from all notes
    public void deleteTag(String tag) {
        if (tag == null) return;

        // Remove from global tags
        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        Set<String> newTags = new HashSet<>(tags);
        if (newTags.remove(tag.trim())) {
            prefs.edit().putStringSet(TAGS_KEY, newTags).apply();
        }

        // Remove from all notes
        List<Note> notes = getNotes();
        boolean notesUpdated = false;
        for (Note note : notes) {
            if (note.getTags() != null && note.getTags().contains(tag)) {
                note.getTags().remove(tag);
                notesUpdated = true;
            }
        }
        if (notesUpdated) {
            saveNotes(notes);
        }
    }

    public void renameTag(String oldTag, String newTag) {
        if (oldTag == null || newTag == null || newTag.trim().isEmpty()) return;

        // Update global tags
        Set<String> tags = prefs.getStringSet(TAGS_KEY, new HashSet<>());
        Set<String> newTags = new HashSet<>(tags);
        if (newTags.remove(oldTag.trim())) {
            newTags.add(newTag.trim());
            prefs.edit().putStringSet(TAGS_KEY, newTags).apply();
        }

        // Update all notes
        List<Note> notes = getNotes();
        boolean notesUpdated = false;
        for (Note note : notes) {
            if (note.getTags() != null && note.getTags().contains(oldTag)) {
                note.getTags().remove(oldTag);
                note.getTags().add(newTag);
                notesUpdated = true;
            }
        }
        if (notesUpdated) {
            saveNotes(notes);
        }
    }

    public void deleteNote(Note note) {
        if (note == null) return;
        List<Note> notes = getNotes();
        boolean removed = notes.removeIf(n -> n.getId().equals(note.getId()));
        if (removed) {
            saveNotes(notes);
        }
    }

    public Note getNoteById(String noteId) {
        if (noteId == null) return null;
        List<Note> notes = getNotes();
        for (Note note : notes) {
            if (noteId.equals(note.getId())) {
                return note;
            }
        }
        return null;
    }

    public void cleanupOrphanedImages(Context context, List<Note> currentNotes) {
        // Get all image paths currently in use
        Set<String> usedImagePaths = new HashSet<>();
        for (Note note : currentNotes) {
            if (note.getImages() != null) {
                for (NoteImage noteImage : note.getImages()) {
                    usedImagePaths.add(noteImage.getImagePath());
                }
            }
        }

        // Delete unused images
        File imagesDir = new File(context.getFilesDir(), "note_images");
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            File[] imageFiles = imagesDir.listFiles();
            if (imageFiles != null) {
                for (File imageFile : imageFiles) {
                    if (!usedImagePaths.contains(imageFile.getAbsolutePath())) {
                        imageFile.delete();
                    }
                }
            }
        }
    }

    public String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            // Create images directory if it doesn't exist
            File imagesDir = new File(context.getFilesDir(), "note_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            // Generate unique filename
            String filename = "image_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(imagesDir, filename);

            // Copy the image
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            // Return the file path that can be used with FileProvider
            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Uri getImageUri(Context context, String internalPath) {
        if (internalPath == null) return null;

        File imageFile = new File(internalPath);
        if (imageFile.exists()) {
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    imageFile
            );
        }
        return null;
    }
}

