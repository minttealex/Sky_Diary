package com.example.skydiary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class BaseNoteFragment extends Fragment {

    protected EditText editNoteName;
    protected EditText editNoteLocation;
    protected EditText editNoteText;
    protected ImageButton btnMenu;
    protected ImageButton btnGetLocation;
    protected LinearLayout imagesContainer;
    protected ScrollView scrollView;

    protected Calendar selectedDate;
    protected final List<String> selectedTags = new ArrayList<>();
    protected final List<NoteImage> noteImages = new ArrayList<>();
    protected Note currentNote;

    protected Uri currentCameraUri;
    private File currentCameraFile;
    private static final int LOCATION_PERMISSION_REQUEST = 2001;
    protected final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();

                    if (currentCameraUri != null) {
                        handleCameraImageFromFile();
                        currentCameraUri = null;
                    } else if (data != null) {
                        handleImagePickerResult(data);
                    }
                } else if (result.getResultCode() == androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED) {
                    cleanupCameraFiles();
                }

                if (currentCameraUri != null) {
                    requireContext().revokeUriPermission(currentCameraUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        editNoteLocation = view.findViewById(R.id.edit_note_location);
        editNoteText = view.findViewById(R.id.edit_note_text);
        ImageButton btnBack = view.findViewById(R.id.button_back);
        btnMenu = view.findViewById(R.id.button_menu);
        btnGetLocation = view.findViewById(R.id.button_get_location);
        scrollView = view.findViewById(R.id.scroll_view);

        setupImagesContainer();

        selectedDate = Calendar.getInstance();

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnMenu.setOnClickListener(v -> showPopupMenu());

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> requestCurrentLocation());
        }

        setupSpecificViews(view);

        if (currentNote != null) {
            loadExistingNoteData();
        }
    }

    protected void loadExistingNoteData() {
        editNoteName.setText(currentNote.getName());
        editNoteText.setText(currentNote.getText());
        selectedDate.setTimeInMillis(currentNote.getTimestamp());

        if (currentNote.getLocation() != null) {
            editNoteLocation.setText(currentNote.getLocation());
        }

        selectedTags.clear();
        if (currentNote.getTags() != null) {
            selectedTags.addAll(currentNote.getTags());
        }

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

            imagesContainer = new LinearLayout(requireContext());
            imagesContainer.setOrientation(LinearLayout.VERTICAL);
            imagesContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            imagesContainer.setPadding(0, 16, 0, 16);

            mainLayout.addView(imagesContainer);
        }
    }

    protected void displayExistingImages() {
        imagesContainer.removeAllViews();

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
                        currentNote.setTimestamp(selectedDate.getTimeInMillis());
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
            if (!((MainActivity) requireActivity()).checkCameraPermission()) {
                return;
            }

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            currentCameraFile = createImageFile();
            if (currentCameraFile != null) {
                currentCameraUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        currentCameraFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentCameraUri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

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
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error starting camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected File createImageFile() throws java.io.IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        assert storageDir != null;
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
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                addImageToNote(imageUri);
            }
        } else if (data.getData() != null) {
            Uri imageUri = data.getData();
            addImageToNote(imageUri);
        }
    }

    protected void handleCameraImageFromFile() {
        try {
            if (currentCameraFile != null && currentCameraFile.exists()) {
                String internalImagePath = currentCameraFile.getAbsolutePath();

                NoteImage noteImage = new NoteImage(internalImagePath, noteImages.size());
                noteImages.add(noteImage);

                ImageView imageView = createImageView(noteImage);
                imagesContainer.addView(imageView);

                if (currentNote != null) {
                    saveNoteSilently();
                }

                Toast.makeText(requireContext(), getString(R.string.image_added), Toast.LENGTH_SHORT).show();

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(currentCameraFile));
                requireContext().sendBroadcast(mediaScanIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error processing camera image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            cleanupCameraFiles();
        }
    }

    private void cleanupCameraFiles() {
        if (currentCameraFile != null) {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentCameraFile = null;
        }
        currentCameraUri = null;
    }

    protected void addImageToNote(Uri imageUri) {
        try {
            String internalImagePath;

            if (currentCameraFile != null && imageUri.toString().contains(currentCameraFile.getAbsolutePath())) {
                internalImagePath = currentCameraFile.getAbsolutePath();
            } else {
                NoteStorage noteStorage = NoteStorage.getInstance(requireContext());
                internalImagePath = noteStorage.saveImageToInternalStorage(requireContext(), imageUri);
            }

            if (internalImagePath != null) {
                NoteImage noteImage = new NoteImage(internalImagePath, noteImages.size());
                noteImages.add(noteImage);

                ImageView imageView = createImageView(noteImage);
                imagesContainer.addView(imageView);

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
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;

        imageView.setLayoutParams(params);

        try {
            Bitmap bitmap = loadAndScaleImage(noteImage.getImagePath());
            if (bitmap != null) {
                bitmap = correctImageOrientation(bitmap, noteImage.getImagePath());

                imageView.setImageBitmap(bitmap);

                ViewGroup.LayoutParams currentParams = imageView.getLayoutParams();
                currentParams.width = bitmap.getWidth();
                currentParams.height = bitmap.getHeight();
                imageView.setLayoutParams(currentParams);

                imageView.setRotation(noteImage.getRotation());

                noteImage.setOriginalWidth(bitmap.getWidth());
                noteImage.setOriginalHeight(bitmap.getHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), getString(R.string.error_displaying_image), Toast.LENGTH_SHORT).show();
        }

        setupImageInteractions(imageView);
        return imageView;
    }

    private Bitmap loadAndScaleImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int maxWidth = displayMetrics.widthPixels - 100;
            int maxHeight = (int) (displayMetrics.heightPixels * 0.7);

            float scale = Math.min((float) maxWidth / imageWidth, (float) maxHeight / imageHeight);
            scale = Math.min(scale, 1.0f);

            int scaledWidth = (int) (imageWidth * scale);
            int scaledHeight = (int) (imageHeight * scale);

            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateInSampleSize(options, scaledWidth, scaledHeight);
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            if (bitmap != null) {
                if (bitmap.getWidth() != scaledWidth || bitmap.getHeight() != scaledHeight) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
                    bitmap.recycle();
                    return scaledBitmap;
                }
            }
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap correctImageOrientation(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.postRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.postRotate(270);
                    matrix.postScale(-1, 1);
                    break;
                default:
                    return bitmap;
            }

            Bitmap correctedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return correctedBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    protected void setupImageInteractions(ImageView imageView) {
        imageView.setOnClickListener(v -> showImageOptionsDialog(imageView));
    }

    protected void rotateImage(ImageView imageView) {
        NoteImage noteImage = (NoteImage) imageView.getTag();

        float currentRotation = noteImage.getRotation();
        float newRotation = (currentRotation + 90) % 360;
        noteImage.setRotation(newRotation);

        imageView.animate()
                .rotation(newRotation)
                .setDuration(200)
                .start();

        updateImageDimensionsAfterRotation(imageView, noteImage, newRotation);

        String rotationText = getString(R.string.image_rotated, (int) newRotation);
        Toast.makeText(requireContext(), rotationText, Toast.LENGTH_SHORT).show();

        if (currentNote != null) {
            saveNoteSilently();
        }
    }

    protected void updateImageDimensionsAfterRotation(ImageView imageView, NoteImage noteImage, float newRotation) {
        if (newRotation % 180 == 90) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            int temp = params.width;
            params.width = params.height;
            params.height = temp;
            imageView.setLayoutParams(params);

            if (noteImage.getOriginalWidth() > 0 && noteImage.getOriginalHeight() > 0) {
                int tempOriginal = noteImage.getOriginalWidth();
                noteImage.setOriginalWidth(noteImage.getOriginalHeight());
                noteImage.setOriginalHeight(tempOriginal);
            }
        }
    }

    protected void showImageOptionsDialog(ImageView imageView) {
        String[] options = {
                getString(R.string.rotate),
                getString(R.string.move_up),
                getString(R.string.move_down),
                getString(R.string.save_to_gallery),
                getString(R.string.delete_picture),
                getString(R.string.cancel)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.image_options))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Rotate
                            rotateImage(imageView);
                            break;
                        case 1: // Move Up
                            moveImageUp(imageView);
                            break;
                        case 2: // Move Down
                            moveImageDown(imageView);
                            break;
                        case 3: // Save to Gallery - NEW CASE
                            saveImageToGallery(imageView);
                            break;
                        case 4: // Delete Picture
                            confirmDeleteImage(imageView);
                            break;
                    }
                })
                .show();
    }

    protected void confirmDeleteImage(ImageView imageView) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_picture))
                .setMessage(getString(R.string.delete_picture_confirmation))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> removeImage(imageView))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
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

    protected void updateImagePositions() {
        for (int i = 0; i < imagesContainer.getChildCount(); i++) {
            View child = imagesContainer.getChildAt(i);
            if (child.getTag() instanceof NoteImage) {
                NoteImage noteImage = (NoteImage) child.getTag();
                noteImage.setPosition(i);
            }
        }
    }

    protected void saveNoteSilently() {
        String name = editNoteName.getText().toString().trim();
        String location = editNoteLocation.getText().toString().trim();
        String text = editNoteText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            String dateString = android.text.format.DateFormat.format("dd MMM yyyy", currentNote.getTimestamp()).toString();
            name = getString(R.string.note_from_format, dateString);
        }

        currentNote.setName(name);
        currentNote.setLocation(location);
        currentNote.setText(text);

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

    protected void saveImageToGallery(ImageView imageView) {
        NoteImage noteImage = (NoteImage) imageView.getTag();

        try {
            File imageFile = new File(noteImage.getImagePath());
            if (imageFile.exists()) {
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File appDir = new File(picturesDir, "SkyDiary");
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "SkyDiary_" + timeStamp + ".jpg";
                File destFile = new File(appDir, fileName);

                // Copy the file
                FileInputStream in = new FileInputStream(imageFile);
                FileOutputStream out = new FileOutputStream(destFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();

                MediaScannerConnection.scanFile(
                        requireContext(),
                        new String[]{destFile.getAbsolutePath()},
                        new String[]{"image/jpeg"},
                        null
                );

                Toast.makeText(requireContext(), getString(R.string.image_saved_to_gallery), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_saving_image), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), getString(R.string.error_saving_image), Toast.LENGTH_SHORT).show();
        }
    }

    protected void requestCurrentLocation() {
        if (!LocationHelper.hasLocationPermission(requireContext())) {
            LocationHelper.requestLocationPermission(requireActivity(), LOCATION_PERMISSION_REQUEST);
            Toast.makeText(requireContext(), getString(R.string.location_permission_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!LocationHelper.isGpsEnabled(requireContext())) {
            Toast.makeText(requireContext(), getString(R.string.enable_gps), Toast.LENGTH_LONG).show();
            return;
        }

        getCurrentLocation();
    }

    protected void getCurrentLocation() {
        Toast.makeText(requireContext(), getString(R.string.getting_location), Toast.LENGTH_SHORT).show();

        try {
            LocationManager locationManager = (LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
            if (locationManager != null && LocationHelper.hasLocationPermission(requireContext())) {
                android.location.LocationListener locationListener = new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            String formattedLocation = LocationHelper.formatCoordinates(
                                    location.getLatitude(), location.getLongitude());
                            editNoteLocation.setText(formattedLocation);
                            Toast.makeText(requireContext(), getString(R.string.location_retrieved), Toast.LENGTH_SHORT).show();

                            locationManager.removeUpdates(this);
                        }
                    }

                    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override public void onProviderEnabled(String provider) {}
                    @Override public void onProviderDisabled(String provider) {}
                };

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location == null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                    if (location != null) {
                        String formattedLocation = LocationHelper.formatCoordinates(
                                location.getLatitude(), location.getLongitude());
                        editNoteLocation.setText(formattedLocation);
                        Toast.makeText(requireContext(), getString(R.string.location_retrieved), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.location_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.location_error), Toast.LENGTH_SHORT).show();
        }
    }

    // Handle permission results
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}