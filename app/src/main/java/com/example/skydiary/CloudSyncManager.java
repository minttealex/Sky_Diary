package com.example.skydiary;

import android.content.Context;
import android.util.Log;
import java.util.List;

public class CloudSyncManager {
    private static final String TAG = "CloudSyncManager";

    private final NetworkManager networkManager;
    private final NoteStorage noteStorage;

    public CloudSyncManager(Context context) {
        this.networkManager = NetworkManager.getInstance(context);
        this.noteStorage = NoteStorage.getInstance(context);
    }

    public void syncAllData(final SyncCallback callback) {
        if (!networkManager.isLoggedIn()) {
            Log.w(TAG, "Sync failed: User not logged in");
            callback.onSyncError("User not logged in");
            return;
        }

        List<Note> localNotes = noteStorage.getNotes();
        Log.d(TAG, "Starting sync with " + localNotes.size() + " local notes");

        networkManager.syncNotes(localNotes, new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(SyncResult result) {
                // Update local storage with server data
                if (result.getNotes() != null) {
                    noteStorage.saveNotes(result.getNotes());
                    Log.d(TAG, "Sync completed, saved " + result.getNotes().size() + " notes");
                    callback.onSyncComplete(true, "Sync completed successfully");
                } else {
                    Log.w(TAG, "Sync completed but no notes received");
                    callback.onSyncComplete(true, "Sync completed but no notes received");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Sync failed: " + error);
                callback.onSyncError("Sync failed: " + error);
            }
        });
    }

    public void uploadNotes(final SyncCallback callback) {
        if (!networkManager.isLoggedIn()) {
            callback.onSyncError("User not logged in");
            return;
        }

        List<Note> localNotes = noteStorage.getNotes();
        Log.d(TAG, "Uploading " + localNotes.size() + " notes");

        networkManager.syncNotes(localNotes, new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(SyncResult result) {
                Log.d(TAG, "Upload completed successfully");
                callback.onSyncComplete(true, "Upload completed");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Upload failed: " + error);
                callback.onSyncError("Upload failed: " + error);
            }
        });
    }

    public void downloadNotes(final SyncCallback callback) {
        if (!networkManager.isLoggedIn()) {
            callback.onSyncError("User not logged in");
            return;
        }

        Log.d(TAG, "Downloading notes from server");
        networkManager.syncNotes(java.util.Collections.emptyList(), new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(SyncResult result) {
                if (result.getNotes() != null) {
                    noteStorage.saveNotes(result.getNotes());
                    Log.d(TAG, "Download completed, saved " + result.getNotes().size() + " notes");
                    callback.onSyncComplete(true, "Download completed successfully");
                } else {
                    Log.w(TAG, "Download completed but no notes received");
                    callback.onSyncComplete(true, "Download completed but no notes received");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Download failed: " + error);
                callback.onSyncError("Download failed: " + error);
            }
        });
    }

    public interface SyncCallback {
        void onSyncComplete(boolean success, String message);
        void onSyncError(String error);
    }
}