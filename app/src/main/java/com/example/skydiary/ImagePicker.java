package com.example.skydiary;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImagePicker {
    public static final int MAX_IMAGE_SIZE_MB = 10;

    private final Fragment fragment;
    private final Activity activity;
    private String currentPhotoPath;

    // Activity result launchers for modern approach
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickPictureLauncher;
    private ActivityResultLauncher<Intent> pickMultiplePicturesLauncher;

    public ImagePicker(Fragment fragment) {
        this.fragment = fragment;
        this.activity = fragment.requireActivity();
        initializeLaunchers();
    }

    public ImagePicker(Activity activity) {
        this.fragment = null;
        this.activity = activity;
        initializeLaunchers();
    }

    private void initializeLaunchers() {
        if (fragment != null) {
            takePictureLauncher = fragment.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> handleTakePictureResult(result.getResultCode())
            );

            pickPictureLauncher = fragment.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> handlePickPictureResult(result.getResultCode(), result.getData(), false)
            );

            pickMultiplePicturesLauncher = fragment.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> handlePickPictureResult(result.getResultCode(), result.getData(), true)
            );
        }
    }

    public void showImageSourceDialog() {
        String[] options = {
                activity.getString(R.string.take_photo),
                activity.getString(R.string.choose_from_gallery),
                activity.getString(R.string.choose_multiple_from_gallery),
                activity.getString(R.string.cancel)
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.add_picture));
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
                case 3: // Cancel
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity,
                        activity.getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Grant temporary read permission to the camera app
                List<ResolveInfo> resolvedIntentActivities = activity.getPackageManager()
                        .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                    String packageName = resolvedIntentInfo.activityInfo.packageName;
                    activity.grantUriPermission(packageName, photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                if (takePictureLauncher != null) {
                    takePictureLauncher.launch(takePictureIntent);
                } else {
                    activity.startActivityForResult(takePictureIntent, 1001); // Fallback
                }
            }
        }
    }

    private void dispatchPickPictureIntent(boolean multiple) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (multiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        // Create chooser intent to show all available apps
        Intent chooser = Intent.createChooser(intent, activity.getString(R.string.select_picture));

        if (multiple && pickMultiplePicturesLauncher != null) {
            pickMultiplePicturesLauncher.launch(chooser);
        } else if (!multiple && pickPictureLauncher != null) {
            pickPictureLauncher.launch(chooser);
        } else {
            activity.startActivityForResult(chooser, multiple ? 1003 : 1002); // Fallback
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException ex) {
            Toast.makeText(activity, R.string.error_creating_image_file, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void handleTakePictureResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK && currentPhotoPath != null) {
            File file = new File(currentPhotoPath);
            if (file.exists()) {
                // Add to gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                activity.sendBroadcast(mediaScanIntent);
            }
        }
    }

    private void handlePickPictureResult(int resultCode, Intent data, boolean multiple) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            List<Uri> imageUris = new ArrayList<>();

            if (multiple && data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    if (isImageFileValid(imageUri)) {
                        imageUris.add(imageUri);
                    }
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                if (isImageFileValid(imageUri)) {
                    imageUris.add(imageUri);
                }
            }

            // Notify listener or handle images
            if (imagePickerListener != null && !imageUris.isEmpty()) {
                imagePickerListener.onImagesSelected(imageUris);
            }
        }
    }

    // Legacy method for fragments still using onActivityResult
    public List<Uri> handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return null;
        }

        List<Uri> imageUris = new ArrayList<>();

        switch (requestCode) {
            case 1001: // Take picture
                if (currentPhotoPath != null) {
                    File file = new File(currentPhotoPath);
                    if (file.exists()) {
                        Uri contentUri = FileProvider.getUriForFile(activity,
                                activity.getPackageName() + ".fileprovider",
                                file);
                        imageUris.add(contentUri);

                        // Add to gallery
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(contentUri);
                        activity.sendBroadcast(mediaScanIntent);
                    }
                }
                break;

            case 1002: // Pick single picture
            case 1003: // Pick multiple pictures
                if (data != null) {
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            if (isImageFileValid(imageUri)) {
                                imageUris.add(imageUri);
                            }
                        }
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        if (isImageFileValid(imageUri)) {
                            imageUris.add(imageUri);
                        }
                    }
                }
                break;
        }

        return imageUris;
    }

    private boolean isImageFileValid(Uri imageUri) {
        try {
            ContentResolver resolver = activity.getContentResolver();
            try (java.io.InputStream input = resolver.openInputStream(imageUri)) {
                if (input != null) {
                    long size = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try (android.content.res.AssetFileDescriptor afd = resolver.openAssetFileDescriptor(imageUri, "r")) {
                            if (afd != null) {
                                size = afd.getLength();
                            }
                        }
                    } else {
                        // Fallback for older versions
                        android.database.Cursor cursor = resolver.query(imageUri,
                                new String[]{MediaStore.Images.ImageColumns.SIZE}, null, null, null);
                        if (cursor != null) {
                            try (cursor) {
                                if (cursor.moveToFirst()) {
                                    size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE));
                                }
                            }
                        }
                    }

                    // Check file size (10MB limit)
                    if (size > MAX_IMAGE_SIZE_MB * 1024 * 1024) {
                        String message = activity.getString(R.string.image_too_large, MAX_IMAGE_SIZE_MB);
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            Toast.makeText(activity, R.string.error_loading_image_file, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    // Listener interface for modern approach
    public interface ImagePickerListener {
        void onImagesSelected(List<Uri> imageUris);
    }

    private ImagePickerListener imagePickerListener;

    public void setImagePickerListener(ImagePickerListener listener) {
        this.imagePickerListener = listener;
    }
}