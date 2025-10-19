package com.example.skydiary;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class NoteEditorFragment extends BaseNoteFragment {

    private static final String ARG_NOTE_ID = "note_id";
    private static final String ARG_MODE = "mode";

    public static final int MODE_ADD = 0;
    public static final int MODE_EDIT = 1;

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

        // Load note if in edit mode
        Bundle args = getArguments();
        if (args != null) {
            int mode = args.getInt(ARG_MODE, MODE_ADD);
            if (mode == MODE_EDIT) {
                String noteId = args.getString(ARG_NOTE_ID);
                if (noteId != null) {
                    currentNote = NoteStorage.getInstance(requireContext()).getNoteById(noteId);
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
            // Edit mode - show save confirmation
            btnSave.setOnClickListener(v -> showSaveConfirmationDialog());
        } else {
            // Add mode - save directly
            btnSave.setOnClickListener(v -> saveNoteImplementation());
        }
    }

    @Override
    protected void saveNoteImplementation() {
        String name = editNoteName.getText().toString().trim();
        String location = editNoteLocation.getText().toString().trim();
        String text = editNoteText.getText().toString().trim();

        if (TextUtils.isEmpty(text) && noteImages.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.note_cannot_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
            String dateString = sdf.format(selectedDate.getTime());
            name = getString(R.string.note_from_format, dateString);
        }

        if (currentNote != null) {
            // Update existing note
            currentNote.setName(name);
            currentNote.setLocation(location);
            currentNote.setText(text);
            currentNote.setTimestamp(System.currentTimeMillis());
            currentNote.setTags(new ArrayList<>(selectedTags));
            currentNote.setImages(new ArrayList<>(noteImages));
            NoteStorage.getInstance(requireContext()).updateNote(currentNote);
        } else {
            // Create new note
            Note newNote = new Note(name, location, text, selectedDate.getTimeInMillis(),
                    new ArrayList<>(selectedTags), new ArrayList<>(noteImages));
            NoteStorage.getInstance(requireContext()).addNote(newNote);
        }

        Toast.makeText(requireContext(), getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void showSaveConfirmationDialog() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_save))
                .setMessage(getString(R.string.confirm_save_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> saveNoteImplementation())
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
