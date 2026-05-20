package com.example.skydiary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    // Reduced dimensions to keep base64 under Firestore's 1 MiB limit
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;

    // Start with a reasonable quality – will be lowered automatically if still too large
    private static final int INITIAL_QUALITY = 70;
    private static final int MAX_BASE64_BYTES = 900_000; // ~900 KB, safely below 1 MiB

    /**
     * Compresses and encodes an image file to a base64 string,
     * ensuring the final string does not exceed Firestore's 1 MiB document limit.
     */
    public static String compressAndEncodeToBase64(String imagePath) {
        try {
            // 1. Decode the image with downsampling and scaling
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            if (bitmap == null) {
                return null;
            }

            Bitmap scaledBitmap = scaleBitmapIfNeeded(bitmap, MAX_WIDTH, MAX_HEIGHT);
            if (scaledBitmap != bitmap) {
                bitmap.recycle();
                bitmap = scaledBitmap;
            }

            // 2. Compress to JPEG and adjust quality until size is under the limit
            int quality = INITIAL_QUALITY;
            byte[] byteArray;
            do {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                byteArray = baos.toByteArray();

                if (byteArray.length <= MAX_BASE64_BYTES) {
                    break; // small enough
                }
                quality -= 10; // reduce quality step by step
            } while (quality > 10);

            bitmap.recycle();

            // If even the lowest quality is too large, return null to let the caller handle it
            if (byteArray.length > MAX_BASE64_BYTES) {
                Log.e(TAG, "Image still too large after max compression – discarding");
                return null;
            }

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    public static String saveBase64ToFile(String base64Data, File outputFile) throws IOException {
        byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(decodedBytes);
        }
        return outputFile.getAbsolutePath();
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    private static Bitmap scaleBitmapIfNeeded(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}