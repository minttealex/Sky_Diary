package com.example.skydiary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteDetailFragment extends Fragment {

    private static final String ARG_NOTE_TIMESTAMP = "note_timestamp";

    private EditText editNoteName;
    private EditText editNoteText;
    private FloatingActionButton btnSave;
    private ImageButton btnBack, btnMenu;

    private Note currentNote;
    private final Calendar selectedDate = Calendar.getInstance();

    public static NoteDetailFragment newInstance(long timestamp) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_NOTE_TIMESTAMP, timestamp);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editNoteName = view.findViewById(R.id.edit_note_name);
        editNoteText = view.findViewById(R.id.edit_note_text);
        btnSave = view.findViewById(R.id.button_save_note);
        btnBack = view.findViewById(R.id.button_back);
        btnMenu = view.findViewById(R.id.button_menu);

        long timestamp;
        if (getArguments() != null) {
            timestamp = getArguments().getLong(ARG_NOTE_TIMESTAMP);
        } else {
            timestamp = 0;
        }

        currentNote = NoteStorage.getInstance(requireContext())
                .getNotes()
                .stream()
                .filter(note -> note.getTimestamp() == timestamp)
                .findFirst()
                .orElse(null);

        if (currentNote == null) {
            Toast.makeText(requireContext(), getString(R.string.note_not_found), Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        editNoteName.setText(currentNote.getName());
        editNoteText.setText(currentNote.getText());
        selectedDate.setTimeInMillis(currentNote.getTimestamp());

        btnSave.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.confirm_save))
                    .setMessage(getString(R.string.confirm_save_message))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> saveNote())
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        });

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnMenu.setOnClickListener(v -> showPopupMenu());
    }

    private void showPopupMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), btnMenu);
        popup.getMenuInflater().inflate(R.menu.menu_note_options, popup.getMenu());
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.show();
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_date) {
            showChangeDateDialog();
            return true;
        } else if (id == R.id.action_add_picture) {
            Toast.makeText(requireContext(), "Add picture feature coming soon", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_edit_tags) {
            showEditTagsDialog();
            return true;
        } else if (id == R.id.action_delete_note) {
            confirmDeleteNote();
            return true;
        }
        return false;
    }

    private void showChangeDateDialog() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (dp, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    currentNote.setTimestamp(selectedDate.getTimeInMillis());
                    Toast.makeText(requireContext(), getString(R.string.date_changed), Toast.LENGTH_SHORT).show();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void confirmDeleteNote() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.confirm_delete_message))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    NoteStorage.getInstance(requireContext()).deleteNote(currentNote);
                    Toast.makeText(requireContext(), getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void saveNote() {
        String name = editNoteName.getText().toString().trim();
        String text = editNoteText.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(requireContext(), getString(R.string.note_cannot_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.note_from) + android.text.format.DateFormat.format("dd MMM yyyy", currentNote.getTimestamp());
        }

        currentNote.setName(name);
        currentNote.setText(text);
        currentNote.setTimestamp(selectedDate.getTimeInMillis());
        if (currentNote.getTags() == null) currentNote.setTags(new ArrayList<>());

        NoteStorage.getInstance(requireContext()).updateNote(currentNote);

        Toast.makeText(requireContext(), getString(R.string.note_saved), Toast.LENGTH_SHORT).show();

        // Notify notes fragment to reload on resume
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void showEditTagsDialog() {
        NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
        ArrayList<String> allTagsList = new ArrayList<>(noteStorage.getAllTags());
        ArrayList<String> selectedTags = currentNote.getTags() != null ?
                new ArrayList<>(currentNote.getTags()) : new ArrayList<>();

        TagEditFragment fragment = TagEditFragment.newInstance(allTagsList, selectedTags);
        fragment.setTagEditListener(tags -> {
            currentNote.setTags(new ArrayList<>(tags));
            NoteStorage.getInstance(requireContext()).updateNote(currentNote);
            Toast.makeText(requireContext(), getString(R.string.tags_saved), Toast.LENGTH_SHORT).show();
        });
        fragment.show(getParentFragmentManager(), "TagEditFragment");
    }

}



