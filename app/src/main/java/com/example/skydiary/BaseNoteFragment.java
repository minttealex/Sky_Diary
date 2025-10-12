package com.example.skydiary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class BaseNoteFragment extends Fragment {

    protected EditText editNoteName;
    protected EditText editNoteText;
    protected ImageButton btnMenu;
    protected LinearLayout imagesContainer;
    protected ScrollView scrollView;

    protected Calendar selectedDate;
    protected final List<String> selectedTags = new ArrayList<>();
    protected final List<NoteImage> noteImages = new ArrayList<>();
    protected Note currentNote;

    protected static final float MIN_IMAGE_SIZE_DP = 100f;
    protected static final float MAX_IMAGE_SIZE_DP = 300f;

    protected Uri currentCameraUri;

    // Modern Activity Result API
    protected final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();

                    if (currentCameraUri != null) {
                        // Handle camera image (saved to file)
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

                if (currentCameraUri != null) {
                    requireContext().revokeUriPermission(currentCameraUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
    );

    protected abstract int getLayoutResource();
    protected abstract void setupSpecificViews(View view);
    protected abstract void saveNoteImplementation();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResource(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editNoteName = view.findViewById(R.id.edit_note_name);
        editNoteText = view.findViewById(R.id.edit_note_text);
        ImageButton btnBack = view.findViewById(R.id.button_back);
        btnMenu = view.findViewById(R.id.button_menu);
        scrollView = view.findViewById(R.id.scroll_view);

        // Initialize images container
        setupImagesContainer();

        selectedDate = Calendar.getInstance();

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnMenu.setOnClickListener(v -> showPopupMenu());

        setupSpecificViews(view);

        // Load existing data if editing
        if (currentNote != null) {
            loadExistingNoteData();
        }
    }

    protected void loadExistingNoteData() {
        editNoteName.setText(currentNote.getName());
        editNoteText.setText(currentNote.getText());
        selectedDate.setTimeInMillis(currentNote.getTimestamp());

        // Initialize selected tags from current note
        selectedTags.clear();
        if (currentNote.getTags() != null) {
            selectedTags.addAll(currentNote.getTags());
        }

        // Load images from current note
        noteImages.clear();
        if (currentNote.getImages() != null) {
            noteImages.addAll(currentNote.getImages());
            displayExistingImages();
        }
    }

    protected void setupImagesContainer() {
        View scrollChild = scrollView.getChildAt(0);
        if (scrollChild instanceof LinearLayout) {
            LinearLayout mainLayout = (LinearLayout) scrollChild;

            // Create images container
            imagesContainer = new LinearLayout(requireContext());
            imagesContainer.setOrientation(LinearLayout.VERTICAL);
            imagesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            imagesContainer.setPadding(0, 16, 0, 16);

            // Add images container after the text fields
            mainLayout.addView(imagesContainer);
        }
    }

    protected void displayExistingImages() {
        imagesContainer.removeAllViews();

        // Sort images by position using Comparator
        List<NoteImage> sortedImages = new ArrayList<>(noteImages);
        sortedImages.sort(Comparator.comparingInt(NoteImage::getPosition));

        for (NoteImage noteImage : sortedImages) {
            ImageView imageView = createImageView(noteImage);
            imagesContainer.addView(imageView);
        }
    }

    protected void showPopupMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), btnMenu);
        popup.getMenuInflater().inflate(getMenuResource(), popup.getMenu());
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.show();
    }

    protected abstract int getMenuResource();

    protected boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_date) {
            showChangeDateDialog();
            return true;
        } else if (id == R.id.action_add_picture) {
            showImageSourceDialog();
            return true;
        } else if (id == R.id.action_edit_tags) {
            showEditTagsDialog();
            return true;
        } else if (id == R.id.action_delete_note && currentNote != null) {
            confirmDeleteNote();
            return true;
        }
        return false;
    }

    protected void showChangeDateDialog() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (dp, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    if (currentNote != null) {
                        currentNote.setTimestamp(System.currentTimeMillis());
                        Toast.makeText(requireContext(), getString(R.string.date_changed), Toast.LENGTH_SHORT).show();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    protected void confirmDeleteNote() {
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

    protected void showImageSourceDialog() {
        String[] options = {
                getString(R.string.take_photo),
                getString(R.string.choose_from_gallery),
                getString(R.string.choose_multiple_from_gallery),
                getString(R.string.cancel)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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

    protected void dispatchTakePictureIntent() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File photoFile = createImageFile();
            if (photoFile != null) {
                currentCameraUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentCameraUri);

                List<android.content.pm.ResolveInfo> resolvedIntentActivities = requireContext()
                        .getPackageManager().queryIntentActivities(takePictureIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);

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
            Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show();
        }
    }

    protected File createImageFile() throws java.io.IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new java.util.Date());
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

    protected void dispatchPickPictureIntent(boolean multiple) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (multiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        Intent chooser = Intent.createChooser(intent, getString(R.string.select_picture));
        imagePickerLauncher.launch(chooser);
    }

    protected void handleImagePickerResult(Intent data) {
        if (data.getClipData() != null) {
            // Multiple images selected
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                addImageToNote(imageUri);
            }
        } else if (data.getData() != null) {
            // Single image selected
            Uri imageUri = data.getData();
            addImageToNote(imageUri);
        }
    }

    protected void handleCameraImageFromFile() {
        try {
            if (currentCameraUri != null) {
                NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
                String internalImagePath = noteStorage.saveImageToInternalStorage(requireContext(), currentCameraUri);

                if (internalImagePath != null) {
                    addImageToNote(Uri.parse(internalImagePath));

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
            Toast.makeText(requireContext(), "Error processing camera image", Toast.LENGTH_SHORT).show();
        }
    }

    protected void addImageToNote(Uri imageUri) {
        try {
            // Save image to internal storage
            NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
            String internalImagePath = noteStorage.saveImageToInternalStorage(requireContext(), imageUri);

            if (internalImagePath != null) {
                NoteImage noteImage = new NoteImage(internalImagePath, noteImages.size());
                noteImages.add(noteImage);

                ImageView imageView = createImageView(noteImage);
                imagesContainer.addView(imageView);

                // Auto-save if editing existing note
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

    protected ImageView createImageView(NoteImage noteImage) {
        ImageView imageView = new ImageView(requireContext());
        imageView.setTag(noteImage);
        imageView.setContentDescription(getString(R.string.image_content_description));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;

        if (noteImage.getWidth() > 0 && noteImage.getHeight() > 0) {
            params.width = (int) noteImage.getWidth();
            params.height = (int) noteImage.getHeight();
        }

        imageView.setLayoutParams(params);

        try {
            NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
            Uri imageUri = noteStorage.getImageUri(requireContext(), noteImage.getImagePath());

            if (imageUri != null) {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (bitmap != null) {
                    if (noteImage.getWidth() <= 0 || noteImage.getHeight() <= 0) {
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        float screenWidth = displayMetrics.widthPixels - 100;
                        float scaleFactor = Math.min(1.0f, screenWidth / bitmap.getWidth());

                        int width = (int) (bitmap.getWidth() * scaleFactor);
                        int height = (int) (bitmap.getHeight() * scaleFactor);

                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        imageView.setImageBitmap(scaledBitmap);

                        noteImage.setWidth(width);
                        noteImage.setHeight(height);
                    } else {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) noteImage.getWidth(), (int) noteImage.getHeight(), true);
                        imageView.setImageBitmap(scaledBitmap);
                    }

                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), getString(R.string.error_displaying_image), Toast.LENGTH_SHORT).show();
            return imageView;
        }

        setupImageInteractions(imageView);
        return imageView;
    }

    protected void setupImageInteractions(ImageView imageView) {
        imageView.setOnClickListener(v -> showImageOptionsDialog(imageView));

        imageView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.remove_image))
                    .setMessage(getString(R.string.remove_image_confirmation))
                    .setPositiveButton(getString(R.string.remove), (dialog, which) -> removeImage(imageView))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        });

        setupDragListener(imageView);
    }

    protected void setupDragListener(ImageView imageView) {
        final float[] startY = new float[1];
        final int[] originalIndex = new int[1];
        final boolean[] isDragging = new boolean[1];

        imageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY[0] = event.getRawY();
                    originalIndex[0] = imagesContainer.indexOfChild(imageView);
                    v.setAlpha(0.7f);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float currentY = event.getRawY();
                    float deltaY = currentY - startY[0];

                    if (Math.abs(deltaY) > 20) {
                        isDragging[0] = true;
                        v.setTranslationY(deltaY);

                        int newIndex = findNewIndex(v, deltaY);
                        if (newIndex != originalIndex[0]) {
                            swapImages(originalIndex[0], newIndex);
                            originalIndex[0] = newIndex;
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    v.setTranslationY(0);
                    if (isDragging[0]) {
                        isDragging[0] = false;
                        if (currentNote != null) {
                            saveNoteSilently();
                        }
                        v.announceForAccessibility(getString(R.string.image_reordered));
                        return true;
                    } else {
                        v.performClick();
                    }
                    break;
            }
            return false;
        });
    }

    protected void removeImage(ImageView imageView) {
        NoteImage noteImage = (NoteImage) imageView.getTag();
        noteImages.remove(noteImage);
        imagesContainer.removeView(imageView);
        updateImagePositions();
        if (currentNote != null) {
            saveNoteSilently();
        }
        Toast.makeText(requireContext(), getString(R.string.image_removed), Toast.LENGTH_SHORT).show();
    }

    protected void showImageOptionsDialog(ImageView imageView) {
        String[] options = {
                getString(R.string.resize),
                getString(R.string.move_up),
                getString(R.string.move_down),
                getString(R.string.cancel)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.image_options))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Resize
                            showResizeDialog(imageView);
                            break;
                        case 1: // Move Up
                            moveImageUp(imageView);
                            break;
                        case 2: // Move Down
                            moveImageDown(imageView);
                            break;
                    }
                })
                .show();
    }

    protected void showResizeDialog(ImageView imageView) {
        NoteImage noteImage = (NoteImage) imageView.getTag();
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_resize_image, null);
        SeekBar seekbarWidth = dialogView.findViewById(R.id.seekbar_width);
        SeekBar seekbarHeight = dialogView.findViewById(R.id.seekbar_height);
        TextView textWidth = dialogView.findViewById(R.id.text_width);
        TextView textHeight = dialogView.findViewById(R.id.text_height);

        int currentWidth = (int) noteImage.getWidth();
        int currentHeight = (int) noteImage.getHeight();

        int originalWidth = getOriginalImageWidth(noteImage);
        int originalHeight = getOriginalImageHeight(noteImage);

        int widthProgress = currentWidth > 0 ? (int) ((currentWidth / (float) originalWidth) * 50) : 50;
        int heightProgress = currentHeight > 0 ? (int) ((currentHeight / (float) originalHeight) * 50) : 50;

        seekbarWidth.setProgress(widthProgress);
        seekbarHeight.setProgress(heightProgress);

        textWidth.setText(getString(R.string.width_label, currentWidth));
        textHeight.setText(getString(R.string.height_label, currentHeight));

        seekbarWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int newWidth = (int) (originalWidth * (progress / 50.0f));
                    textWidth.setText(getString(R.string.width_label, newWidth));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int newHeight = (int) (originalHeight * (progress / 50.0f));
                    textHeight.setText(getString(R.string.height_label, newHeight));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.resize))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.apply), (dialog, which) -> applyImageSize(imageView, noteImage, seekbarWidth.getProgress(), seekbarHeight.getProgress()))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    protected int getOriginalImageWidth(NoteImage noteImage) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(Uri.parse(noteImage.getImagePath()))) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            return options.outWidth;
        } catch (Exception e) {
            return (int) noteImage.getWidth();
        }
    }

    protected int getOriginalImageHeight(NoteImage noteImage) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(Uri.parse(noteImage.getImagePath()))) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            return options.outHeight;
        } catch (Exception e) {
            return (int) noteImage.getHeight();
        }
    }

    protected void moveImageUp(ImageView imageView) {
        int currentIndex = imagesContainer.indexOfChild(imageView);
        if (currentIndex > 0) {
            imagesContainer.removeViewAt(currentIndex);
            imagesContainer.addView(imageView, currentIndex - 1);
            updateImagePositions();
            if (currentNote != null) {
                saveNoteSilently();
            }
            Toast.makeText(requireContext(), getString(R.string.image_moved_up), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), getString(R.string.image_at_top), Toast.LENGTH_SHORT).show();
        }
    }

    protected void moveImageDown(ImageView imageView) {
        int currentIndex = imagesContainer.indexOfChild(imageView);
        if (currentIndex < imagesContainer.getChildCount() - 1) {
            imagesContainer.removeViewAt(currentIndex);
            imagesContainer.addView(imageView, currentIndex + 1);
            updateImagePositions();
            if (currentNote != null) {
                saveNoteSilently();
            }
            Toast.makeText(requireContext(), getString(R.string.image_moved_down), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), getString(R.string.image_at_bottom), Toast.LENGTH_SHORT).show();
        }
    }

    protected int findNewIndex(View draggedView, float deltaY) {
        int currentIndex = imagesContainer.indexOfChild(draggedView);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float threshold = 50 * metrics.density;

        if (deltaY < -threshold && currentIndex > 0) {
            return currentIndex - 1;
        } else if (deltaY > threshold && currentIndex < imagesContainer.getChildCount() - 1) {
            return currentIndex + 1;
        }
        return currentIndex;
    }

    protected void swapImages(int fromIndex, int toIndex) {
        View fromView = imagesContainer.getChildAt(fromIndex);
        View toView = imagesContainer.getChildAt(toIndex);

        imagesContainer.removeViewAt(fromIndex);
        if (toIndex > fromIndex) {
            imagesContainer.removeViewAt(toIndex - 1);
        } else {
            imagesContainer.removeViewAt(toIndex);
        }

        if (toIndex > fromIndex) {
            imagesContainer.addView(fromView, toIndex);
            imagesContainer.addView(toView, fromIndex);
        } else {
            imagesContainer.addView(toView, fromIndex);
            imagesContainer.addView(fromView, toIndex);
        }

        updateImagePositions();
    }

    protected void updateImagePositions() {
        for (int i = 0; i < imagesContainer.getChildCount(); i++) {
            View child = imagesContainer.getChildAt(i);
            if (child.getTag() instanceof NoteImage) {
                NoteImage noteImage = (NoteImage) child.getTag();
                noteImage.setPosition(i);
            }
        }
    }

    protected void applyImageSize(ImageView imageView, NoteImage noteImage, int widthProgress, int heightProgress) {
        float scaleWidth = widthProgress / 50.0f;
        float scaleHeight = heightProgress / 50.0f;

        int originalWidth = getOriginalImageWidth(noteImage);
        int originalHeight = getOriginalImageHeight(noteImage);

        int newWidth = (int) (originalWidth * scaleWidth);
        int newHeight = (int) (originalHeight * scaleHeight);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int minSize = (int) (MIN_IMAGE_SIZE_DP * metrics.density);
        int maxSize = (int) (MAX_IMAGE_SIZE_DP * metrics.density);

        newWidth = Math.max(minSize, Math.min(maxSize, newWidth));
        newHeight = Math.max(minSize, Math.min(maxSize, newHeight));

        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = newWidth;
        params.height = newHeight;
        imageView.setLayoutParams(params);
        imageView.requestLayout();

        noteImage.setWidth(newWidth);
        noteImage.setHeight(newHeight);

        Toast.makeText(requireContext(), getString(R.string.image_resized), Toast.LENGTH_SHORT).show();
    }

    protected void saveNoteSilently() {
        String name = editNoteName.getText().toString().trim();
        String text = editNoteText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            String dateString = android.text.format.DateFormat.format("dd MMM yyyy", currentNote.getTimestamp()).toString();
            name = getString(R.string.note_from_format, dateString);
        }

        currentNote.setName(name);
        currentNote.setText(text);
        currentNote.setTimestamp(System.currentTimeMillis());
        currentNote.setTags(new ArrayList<>(selectedTags));
        currentNote.setImages(new ArrayList<>(noteImages));

        NoteStorage.getInstance(requireContext()).updateNote(currentNote);
    }

    protected void showEditTagsDialog() {
        NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
        ArrayList<String> allTagsList = new ArrayList<>(noteStorage.getAllTags());
        ArrayList<String> currentSelectedTags = new ArrayList<>(selectedTags);

        TagEditFragment fragment = TagEditFragment.newInstance(allTagsList, currentSelectedTags);
        fragment.setTagEditListener(tags -> {
            selectedTags.clear();
            selectedTags.addAll(tags);

            if (currentNote != null) {
                currentNote.setTags(new ArrayList<>(selectedTags));
                NoteStorage.getInstance(requireContext()).updateNote(currentNote);
            }

            String message = getString(R.string.tags_updated_format, tags.size());
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
        fragment.show(getParentFragmentManager(), "TagEditFragment");
    }
}
