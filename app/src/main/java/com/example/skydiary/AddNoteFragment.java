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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddNoteFragment extends Fragment {

    private EditText editNoteName;
    private EditText editNoteText;
    private FloatingActionButton btnSave;
    private ImageButton btnBack;
    private ImageButton btnMenu;

    private Calendar selectedDate;
    private Note currentNote; // null since adding

    private final List<String> selectedTags = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_note, container, false);
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

        selectedDate = Calendar.getInstance();

        btnSave.setOnClickListener(v -> saveNote());

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnMenu.setOnClickListener(v -> showPopupMenu());
    }

    private void showPopupMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), btnMenu);
        popup.getMenuInflater().inflate(R.menu.menu_add_note_options, popup.getMenu());
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
        }
        return false;
    }

    private void showChangeDateDialog() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (dp, year, month, day) -> selectedDate.set(year, month, day),
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void saveNote() {
        String name = editNoteName.getText().toString().trim();
        String text = editNoteText.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(requireContext(), getString(R.string.note_cannot_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            name = getString(R.string.note_from) + sdf.format(selectedDate.getTime());
        }

        Note newNote = new Note(name, text, selectedDate.getTimeInMillis(), new ArrayList<>(selectedTags));
        NoteStorage.getInstance(requireContext()).addNote(newNote);

        Toast.makeText(requireContext(), getString(R.string.note_saved), Toast.LENGTH_SHORT).show();

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void showEditTagsDialog() {
        NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
        ArrayList<String> allTagsList = new ArrayList<>(noteStorage.getAllTags());
        ArrayList<String> selectedTagsCopy = new ArrayList<>(selectedTags);

        TagEditFragment fragment = TagEditFragment.newInstance(allTagsList, selectedTagsCopy);
        fragment.setTagEditListener(tags -> {
            selectedTags.clear();
            selectedTags.addAll(tags);
            Toast.makeText(requireContext(), getString(R.string.tags_saved), Toast.LENGTH_SHORT).show();
        });
        fragment.show(getParentFragmentManager(), "TagEditFragment");
    }


}



