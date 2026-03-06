package com.example.skydiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class NoteEditorFragment extends BaseNoteFragment {

    private static final String ARG_NOTE_ID = "note_id";
    private static final String ARG_MODE = "mode";

    public static final int MODE_ADD = 0;
    public static final int MODE_EDIT = 1;

    private FirebaseFirestore db;
    private String userId;
    private NoteStorage localStorage;

    public static NoteEditorFragment newInstance() {
        return newInstance(null, MODE_ADD);
    }

    public static NoteEditorFragment newInstance(String noteId) {
        return newInstance(noteId, MODE_EDIT);
    }

    private static NoteEditorFragment newInstance(String noteId, int mode) {
        NoteEditorFragment fragment = new NoteEditorFragment();
        Bundle args = new Bundle();
        if (noteId != null) {
            args.putString(ARG_NOTE_ID, noteId);
        }
        args.putInt(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_note_detail;
    }

    @Override
    protected int getMenuResource() {
        return currentNote != null ? R.menu.menu_note_options : R.menu.menu_add_note_options;
    }

    @Override
    protected void setupSpecificViews(View view) {
        FloatingActionButton btnSave = view.findViewById(R.id.button_save_note);

        db = FirebaseManager.getInstance().getDb();
        localStorage = NoteStorage.getInstance(requireContext());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }

        Bundle args = getArguments();
        if (args != null) {
            int mode = args.getInt(ARG_MODE, MODE_ADD);
            if (mode == MODE_EDIT) {
                String noteId = args.getString(ARG_NOTE_ID);
                if (noteId != null) {
                    currentNote = localStorage.getNoteById(noteId);
                    if (currentNote == null) {
                        Toast.makeText(requireContext(), getString(R.string.note_not_found), Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                        return;
                    }
                    loadExistingNoteData();
                }
            }
        }

        if (currentNote != null) {
            btnSave.setOnClickListener(v -> showSaveConfirmationDialog());
        } else {
            btnSave.setOnClickListener(v -> saveNoteToFirestore());
        }
    }

    private void showSaveConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_save))
                .setMessage(getString(R.string.confirm_save_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> saveNoteImplementation())
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    @Override
    protected void saveNoteImplementation() {
        saveNoteToFirestore();
    }

    private void saveNoteToFirestore() {
        String name = editNoteName.getText().toString().trim();
        String location = editNoteLocation.getText().toString().trim();
        String text = editNoteText.getText().toString().trim();

        if (TextUtils.isEmpty(text) && noteImages.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.note_cannot_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String dateString = sdf.format(selectedDate.getTime());
            name = getString(R.string.note_from_format, dateString);
        }

        final String noteId = (currentNote != null) ? currentNote.getId() : UUID.randomUUID().toString();
        final Date now = new Date();

        if (currentNote == null) {
            currentNote = new Note();
            currentNote.setId(noteId);
            if (userId != null) currentNote.setUserId(userId);
            currentNote.setCreatedAt(dateToString(now));
        }

        currentNote.setName(name);
        currentNote.setLocation(location);
        currentNote.setText(text);
        currentNote.setTimestamp(selectedDate.getTimeInMillis());
        currentNote.setTags(new ArrayList<>(selectedTags));
        currentNote.setImages(new ArrayList<>(noteImages));
        currentNote.setUpdatedAt(dateToString(now));

        localStorage.updateNote(currentNote);

        if (userId != null) {
            Map<String, Object> noteMap = new HashMap<>();
            noteMap.put("userId", userId);
            noteMap.put("name", name);
            noteMap.put("location", location);
            noteMap.put("text", text);
            noteMap.put("timestamp", selectedDate.getTimeInMillis());
            noteMap.put("tags", new ArrayList<>(selectedTags));

            List<Map<String, Object>> imageMaps = new ArrayList<>();
            for (NoteImage img : noteImages) {
                Map<String, Object> imgMap = new HashMap<>();
                imgMap.put("id", img.getId());
                imgMap.put("position", img.getPosition());
                imgMap.put("rotation", img.getRotation());
                imgMap.put("originalWidth", img.getOriginalWidth());
                imgMap.put("originalHeight", img.getOriginalHeight());

                String base64 = ImageUtils.compressAndEncodeToBase64(img.getImagePath());
                imgMap.put("imageData", base64 != null ? base64 : "");
                imageMaps.add(imgMap);
            }
            noteMap.put("images", imageMaps);

            noteMap.put("createdAt", stringToDate(currentNote.getCreatedAt()));
            noteMap.put("updatedAt", now);
            noteMap.put("isDeleted", false);

            db.collection("notes").document(noteId)
                    .set(noteMap, SetOptions.merge())
                    .addOnFailureListener(e -> Log.e("NoteEditor", "Firestore save failed", e));
        }

        Toast.makeText(requireContext(), getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void addImageToNote(Uri imageUri) {
        try {
            String internalImagePath;
            if (currentCameraFile != null && imageUri.toString().contains(currentCameraFile.getAbsolutePath())) {
                internalImagePath = currentCameraFile.getAbsolutePath();
            } else {
                internalImagePath = localStorage.saveImageToInternalStorage(requireContext(), imageUri);
            }

            if (internalImagePath != null) {
                NoteImage noteImage = new NoteImage(internalImagePath, noteImages.size());
                noteImages.add(noteImage);
                createAndAddImageView(noteImage);
                if (currentNote != null) {
                    saveNoteSilently();
                }
                Toast.makeText(requireContext(), getString(R.string.image_added), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void handleCameraImageFromFile() {
        try {
            if (currentCameraFile != null && currentCameraFile.exists()) {
                NoteImage noteImage = new NoteImage(currentCameraFile.getAbsolutePath(), noteImages.size());
                noteImages.add(noteImage);
                createAndAddImageView(noteImage);
                if (currentNote != null) {
                    saveNoteSilently();
                }
                Toast.makeText(requireContext(), getString(R.string.image_added), Toast.LENGTH_SHORT).show();
                requireContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(currentCameraFile)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error processing camera image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            cleanupCameraFiles();
        }
    }

    @Override
    protected void saveNoteSilently() {
        if (currentNote == null) return;

        String name = editNoteName.getText().toString().trim();
        String location = editNoteLocation.getText().toString().trim();
        String text = editNoteText.getText().toString().trim();

        if (name.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String dateString = sdf.format(selectedDate.getTime());
            name = getString(R.string.note_from_format, dateString);
        }

        final Date now = new Date();

        currentNote.setName(name);
        currentNote.setLocation(location);
        currentNote.setText(text);
        currentNote.setTimestamp(selectedDate.getTimeInMillis());
        currentNote.setTags(new ArrayList<>(selectedTags));
        currentNote.setImages(new ArrayList<>(noteImages));
        currentNote.setUpdatedAt(dateToString(now));

        localStorage.updateNote(currentNote);

        if (userId != null) {
            Map<String, Object> noteMap = new HashMap<>();
            noteMap.put("userId", userId);
            noteMap.put("name", name);
            noteMap.put("location", location);
            noteMap.put("text", text);
            noteMap.put("timestamp", selectedDate.getTimeInMillis());
            noteMap.put("tags", new ArrayList<>(selectedTags));

            List<Map<String, Object>> imageMaps = new ArrayList<>();
            for (NoteImage img : noteImages) {
                Map<String, Object> imgMap = new HashMap<>();
                imgMap.put("id", img.getId());
                imgMap.put("position", img.getPosition());
                imgMap.put("rotation", img.getRotation());
                imgMap.put("originalWidth", img.getOriginalWidth());
                imgMap.put("originalHeight", img.getOriginalHeight());

                String base64 = ImageUtils.compressAndEncodeToBase64(img.getImagePath());
                imgMap.put("imageData", base64 != null ? base64 : "");
                imageMaps.add(imgMap);
            }
            noteMap.put("images", imageMaps);

            noteMap.put("createdAt", stringToDate(currentNote.getCreatedAt()));
            noteMap.put("updatedAt", now);
            noteMap.put("isDeleted", currentNote.isDeleted());
            if (currentNote.getDeletedAt() != null) {
                noteMap.put("deletedAt", stringToDate(currentNote.getDeletedAt()));
            }

            db.collection("notes").document(currentNote.getId())
                    .set(noteMap, SetOptions.merge())
                    .addOnFailureListener(e -> Log.e("NoteEditor", "Silent save failed", e));
        }
    }

    private String dateToString(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    private Date stringToDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdf.parse(dateStr);
        } catch (java.text.ParseException e) {
            return new Date();
        }
    }
}