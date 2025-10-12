package com.example.skydiary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    // Modern Activity Result API for image picking
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == androidx.appcompat.app.AppCompatActivity.RESULT_OK && result.getData() != null) {
                    handleImagePickerResult(result.getData());
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button addNoteButton = view.findViewById(R.id.add_note_button);
        Button addPictureButton = view.findViewById(R.id.add_picture_button);

        addNoteButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, NoteEditorFragment.newInstance())
                        .addToBackStack(null)
                        .commit());

        addPictureButton.setOnClickListener(v -> showImageSourceDialog());
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
                    dispatchTakePictureIntent();
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            imagePickerLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show();
        }
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

    private void createNoteWithImages(List<Uri> imageUris) {
        // Create a new note with the selected images
        List<NoteImage> noteImages = new ArrayList<>();
        for (int i = 0; i < imageUris.size(); i++) {
            noteImages.add(new NoteImage(imageUris.get(i).toString(), i));
        }

        // Create a note with just images (empty text)
        Note newNote = new Note(
                getString(R.string.note_with_images),
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

        // Navigate to notes list to show the new note
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotesFragment())
                .commit();
    }
}

