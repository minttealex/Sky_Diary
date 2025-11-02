package com.example.skydiary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainFragment extends Fragment {

    private Uri currentCameraUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                requireActivity();
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    if (currentCameraUri != null) {
                        handleCameraImageFromFile();
                        currentCameraUri = null;
                    } else if (data != null) {
                        handleImagePickerResult(data);
                    }
                } else {
                    requireActivity();
                    if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        cleanupCameraFiles();
                    }
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

        MaterialButton btnConstellations = view.findViewById(R.id.btn_constellations);
        MaterialButton btnNewNote = view.findViewById(R.id.btn_new_note);
        MaterialButton btnNewPicture = view.findViewById(R.id.btn_new_picture);

        btnConstellations.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ConstellationsFragment())
                        .addToBackStack("main_to_constellations")
                        .commit());

        btnNewNote.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, NoteEditorFragment.newInstance())
                        .addToBackStack("main_to_note_editor")
                        .commit());

        btnNewPicture.setOnClickListener(v -> showImageSourceDialog());
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
                case 3: // Cancel
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File photoFile = createImageFile();
            currentCameraUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentCameraUri);

            List<android.content.pm.ResolveInfo> resolvedIntentActivities = requireContext()
                    .getPackageManager().queryIntentActivities(takePictureIntent,
                            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);

            for (android.content.pm.ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                requireContext().grantUriPermission(
                        resolvedIntentInfo.activityInfo.packageName,
                        currentCameraUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            }

            imagePickerLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    getString(R.string.error_starting_camera),
                    Toast.LENGTH_SHORT).show();
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
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                imageUris.add(imageUri);
            }
        } else if (data.getData() != null) {
            Uri imageUri = data.getData();
            imageUris.add(imageUri);
        }

        if (!imageUris.isEmpty()) {
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

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(currentCameraUri);
                    requireContext().sendBroadcast(mediaScanIntent);
                } else {
                    Toast.makeText(requireContext(),
                            getString(R.string.error_loading_image),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    getString(R.string.error_processing_camera_image),
                    Toast.LENGTH_SHORT).show();
        } finally {
            cleanupCameraFiles();
        }
    }

    private void createNoteWithImages(List<Uri> imageUris) {
        List<NoteImage> noteImages = new ArrayList<>();
        for (int i = 0; i < imageUris.size(); i++) {
            noteImages.add(new NoteImage(imageUris.get(i).toString(), i));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
        String dateString = sdf.format(new Date());
        String noteName = getString(R.string.note_from_format, dateString);

        Note newNote = new Note(
                noteName,
                "",
                System.currentTimeMillis(),
                new ArrayList<>(),
                noteImages
        );

        NoteStorage.getInstance(requireContext()).addNote(newNote);

        if (imageUris.size() == 1) {
            Toast.makeText(requireContext(),
                    getString(R.string.image_note_created_single),
                    Toast.LENGTH_SHORT).show();
        } else {
            String message = getString(R.string.image_note_created_multiple, imageUris.size());
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotesFragment())
                .commit();
    }

    private void cleanupCameraFiles() {
        if (currentCameraUri != null) {
            try {
                File cameraFile = new File(Objects.requireNonNull(currentCameraUri.getPath()));
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