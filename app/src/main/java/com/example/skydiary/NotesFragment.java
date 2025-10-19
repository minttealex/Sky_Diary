package com.example.skydiary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NotesFragment extends Fragment implements NotesAdapter.OnItemClickListener {

    private EditText searchBar;
    private NotesAdapter notesAdapter;
    private FloatingActionButton fabAdd;
    private List<Note> allNotes;
    private final List<String> selectedTags = new ArrayList<>();
    private LinearLayout tagsContainer;
    private TextView tvNoMatches;
    private Uri currentCameraUri;

    // Modern Activity Result API for image picking
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();

                    if (currentCameraUri != null) {
                        // Handle camera image
                        handleCameraImageFromFile();
                        currentCameraUri = null;
                    } else if (data != null) {
                        // Handle gallery images
                        handleImagePickerResult(data);
                    }
                } else if (result.getResultCode() == androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED) {
                    // Camera was cancelled, clean up
                    if (currentCameraUri != null) {
                        try {
                            File cameraFile = new File(currentCameraUri.getPath());
                            if (cameraFile.exists()) {
                                cameraFile.delete();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        currentCameraUri = null;
                    }
                }
            }
    );

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
        ImageButton btnAddTag = view.findViewById(R.id.btn_add_tag);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_notes);
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
                String message = getString(R.string.filtering_by_tags_format, tags.size());
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
                int itemId = item.getItemId();
                if (itemId == R.id.action_add_note) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, NoteEditorFragment.newInstance())
                            .addToBackStack("notes_to_add")
                            .commit();
                    return true;
                } else if (itemId == R.id.action_add_picture) {
                    showImageSourceDialog();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        // Load initial data
        refreshAllData();
    }

    private void showImageSourceDialog() {
        String[] options = {
                getString(R.string.take_photo),
                getString(R.string.choose_from_gallery),
                getString(R.string.choose_multiple_from_gallery),
                getString(R.string.cancel)
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.add_picture));
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Take Photo
                    if (((MainActivity) requireActivity()).checkCameraPermission()) {
                        dispatchTakePictureIntent();
                    }
                    break;
                case 1: // Choose from Gallery
                    dispatchPickPictureIntent(false);
                    break;
                case 2: // Choose Multiple
                    dispatchPickPictureIntent(true);
                    break;
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File photoFile = createImageFile();
            if (photoFile != null) {
                currentCameraUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentCameraUri);

                // Grant permissions
                List<android.content.pm.ResolveInfo> resolvedIntentActivities = requireContext()
                        .getPackageManager().queryIntentActivities(takePictureIntent,
                                android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);

                for (android.content.pm.ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                    requireContext().grantUriPermission(
                            resolvedIntentInfo.activityInfo.packageName,
                            currentCameraUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                }

                imagePickerLauncher.launch(takePictureIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), getString(R.string.error_starting_camera), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void dispatchPickPictureIntent(boolean multiple) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (multiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        Intent chooser = Intent.createChooser(intent, getString(R.string.select_picture));
        imagePickerLauncher.launch(chooser);
    }

    private void handleImagePickerResult(Intent data) {
        List<Uri> imageUris = new ArrayList<>();

        if (data.getClipData() != null) {
            // Multiple images selected
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                imageUris.add(imageUri);
            }
        } else if (data.getData() != null) {
            // Single image selected
            Uri imageUri = data.getData();
            imageUris.add(imageUri);
        }

        if (!imageUris.isEmpty()) {
            // Create a new note with the selected images
            createNoteWithImages(imageUris);
        }
    }

    private void handleCameraImageFromFile() {
        try {
            if (currentCameraUri != null) {
                NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
                String internalImagePath = noteStorage.saveImageToInternalStorage(requireContext(), currentCameraUri);

                if (internalImagePath != null) {
                    List<Uri> imageUris = new ArrayList<>();
                    imageUris.add(Uri.parse(internalImagePath));
                    createNoteWithImages(imageUris);

                    // Scan the file so it appears in gallery
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(currentCameraUri);
                    requireContext().sendBroadcast(mediaScanIntent);
                } else {
                    Toast.makeText(requireContext(), getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), getString(R.string.error_processing_camera_image), Toast.LENGTH_SHORT).show();
        }
    }

    private void createNoteWithImages(List<Uri> imageUris) {
        // Create a new note with the selected images
        List<NoteImage> noteImages = new ArrayList<>();
        for (int i = 0; i < imageUris.size(); i++) {
            noteImages.add(new NoteImage(imageUris.get(i).toString(), i));
        }

        // Create note name with date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
        String dateString = sdf.format(new Date());
        String noteName = getString(R.string.note_from_format, dateString);

        // Create a note with just images (empty text)
        Note newNote = new Note(
                noteName,
                "",
                System.currentTimeMillis(),
                new ArrayList<>(),
                noteImages
        );

        // Save the note
        NoteStorage.getInstance(requireContext()).addNote(newNote);

        // Show success message
        if (imageUris.size() == 1) {
            Toast.makeText(requireContext(), getString(R.string.image_note_created_single), Toast.LENGTH_SHORT).show();
        } else {
            String message = getString(R.string.image_note_created_multiple, imageUris.size());
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }

        // Refresh the notes list
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
                    .replace(R.id.fragment_container, NoteEditorFragment.newInstance(note.getId()))
                    .addToBackStack("notes_to_detail")
                    .commit();
        } else {
            Log.e("NotesFragment", "Note or note ID is null");
            Toast.makeText(requireContext(), getString(R.string.error_could_not_open_note), Toast.LENGTH_SHORT).show();
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
            tagView.setTextColor(getResources().getColor(android.R.color.black, requireContext().getTheme()));
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
            for (String kw : keywords) {
                if (!name.contains(kw) && !text.contains(kw)) {
                    matchesSearch = false;
                    break;
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