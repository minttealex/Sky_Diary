package com.example.skydiary;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotesFragment extends Fragment implements NotesAdapter.OnItemClickListener {

    private EditText searchBar;
    private ImageButton btnAddTag;
    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private FloatingActionButton fabAdd;
    private List<Note> allNotes;
    private List<String> selectedTags = new ArrayList<>();
    private LinearLayout tagsContainer;
    private TextView tvNoMatches;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedTags.clear();

        searchBar = view.findViewById(R.id.search_bar);
        btnAddTag = view.findViewById(R.id.btn_add_tag);
        recyclerView = view.findViewById(R.id.recycler_notes);
        fabAdd = view.findViewById(R.id.fab_add);
        tagsContainer = view.findViewById(R.id.tags_container);
        tvNoMatches = view.findViewById(R.id.tv_no_matches);

        // Initialize adapter early to avoid "No adapter attached" errors
        notesAdapter = new NotesAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(notesAdapter);

        btnAddTag.setOnClickListener(v -> {
            NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
            ArrayList<String> allTagsList = new ArrayList<>(noteStorage.getAllTags());
            ArrayList<String> selectedTagsCopy = new ArrayList<>(selectedTags);

            TagEditFragment fragment = TagEditFragment.newInstance(allTagsList, selectedTagsCopy);
            fragment.setTagEditListener(tags -> {
                selectedTags.clear();
                selectedTags.addAll(tags);
                loadTags();
                filterNotes();
                Toast.makeText(requireContext(), "Filtering by " + tags.size() + " tags", Toast.LENGTH_SHORT).show();
            });
            fragment.show(getParentFragmentManager(), "TagEditFragment");
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), fabAdd);
            popupMenu.inflate(R.menu.menu_fab_add);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_add_note) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new AddNoteFragment())
                            .addToBackStack("notes_to_add")
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.action_add_picture) {
                    Toast.makeText(requireContext(), "Add Picture pressed (not implemented)", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        // Load initial data
        refreshAllData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        Log.d("NotesFragment", "Refreshing notes on resume");
        refreshAllData();
    }

    private void refreshAllData() {
        loadNotes();
        loadTags();
        filterNotes();
    }

    @Override
    public void onItemClick(Note note) {
        if (note != null && note.getId() != null) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, NoteDetailFragment.newInstance(note.getId()))
                    .addToBackStack("notes_to_detail")
                    .commit();
        } else {
            Log.e("NotesFragment", "Note or note ID is null");
            Toast.makeText(requireContext(), "Error: Could not open note", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNotes() {
        allNotes = NoteStorage.getInstance(requireContext()).getNotes();
        Log.d("NotesFragment", "Loaded " + allNotes.size() + " notes");
    }

    private void loadTags() {
        Set<String> allTagsSet = NoteStorage.getInstance(requireContext()).getAllTags();
        tagsContainer.removeAllViews();

        for (String tag : allTagsSet) {
            TextView tagView = new TextView(requireContext());
            tagView.setText(tag);
            tagView.setPadding(20, 10, 20, 10);
            if (selectedTags.contains(tag)) {
                tagView.setBackgroundResource(R.drawable.tag_selected_background);
            } else {
                tagView.setBackgroundResource(R.drawable.tag_background);
            }
            tagView.setTextColor(getResources().getColor(android.R.color.black));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 0, 10, 0);
            tagView.setLayoutParams(params);

            tagView.setOnClickListener(v -> {
                if (selectedTags.contains(tag)) {
                    selectedTags.remove(tag);
                    tagView.setBackgroundResource(R.drawable.tag_background);
                } else {
                    selectedTags.add(tag);
                    tagView.setBackgroundResource(R.drawable.tag_selected_background);
                }
                filterNotes();
            });

            tagsContainer.addView(tagView);
        }
    }

    private void filterNotes() {
        if (allNotes == null) {
            allNotes = new ArrayList<>();
        }

        String searchText = searchBar.getText().toString().toLowerCase().trim();
        String[] keywords = searchText.isEmpty() ? new String[0] : searchText.split("\\s+");
        List<Note> filteredNotes = new ArrayList<>();

        for (Note note : allNotes) {
            String name = note.getName() != null ? note.getName().toLowerCase() : "";
            String text = note.getText() != null ? note.getText().toLowerCase() : "";

            // Search filter
            boolean matchesSearch = true;
            if (keywords.length > 0) {
                for (String kw : keywords) {
                    if (!name.contains(kw) && !text.contains(kw)) {
                        matchesSearch = false;
                        break;
                    }
                }
            }
            if (!matchesSearch) continue;

            // Tag filter - show notes that have ALL selected tags
            if (!selectedTags.isEmpty()) {
                if (note.getTags() == null || note.getTags().isEmpty()) {
                    continue;
                }
                boolean hasAllTags = true;
                for (String selectedTag : selectedTags) {
                    if (!note.getTags().contains(selectedTag)) {
                        hasAllTags = false;
                        break;
                    }
                }
                if (!hasAllTags) {
                    continue;
                }
            }
            filteredNotes.add(note);
        }

        notesAdapter.updateList(filteredNotes);
        showNoMatchesMessage(filteredNotes.isEmpty());

        Log.d("NotesFragment", "Filtered to " + filteredNotes.size() + " notes");
    }

    private void showNoMatchesMessage(boolean show) {
        tvNoMatches.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
