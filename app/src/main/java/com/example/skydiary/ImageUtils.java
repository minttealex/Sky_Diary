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
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;
    private static final int COMPRESS_QUALITY = 80;

    public static String compressAndEncodeToBase64(String imagePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) return null;

            Bitmap scaledBitmap = scaleBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT);
            if (scaledBitmap != bitmap) {
                bitmap.recycle();
                bitmap = scaledBitmap;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, baos);
            byte[] byteArray = baos.toByteArray();
            bitmap.recycle();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return null;
        }
    }

    public static String saveBase64ToFile(String base64Data, File outputFile) throws IOException {
        byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(decodedBytes);
        fos.close();
        return outputFile.getAbsolutePath();
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        if (scale >= 1.0f) return bitmap;

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}