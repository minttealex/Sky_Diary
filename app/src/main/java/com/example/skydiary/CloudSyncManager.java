package com.example.skydiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudSyncManager {
    private static final String TAG = "CloudSyncManager";

    private final NetworkManager networkManager;
    private final NoteStorage noteStorage;
    private final Context context;

    public CloudSyncManager(Context context) {
        this.networkManager = NetworkManager.getInstance(context);
        this.noteStorage = NoteStorage.getInstance(context);
        this.context = context;
    }

    public void syncOnLogin(final SyncCallback callback) {
        if (!networkManager.isLoggedIn()) {
            callback.onSyncError("User not logged in");
            return;
        }

        checkIfUserHasDataOnServer(new DataCheckCallback() {
            @Override
            public void onUserHasData() {
                Log.d(TAG, "Existing account detected, clearing local data and downloading server data");
                clearLocalUserData();
                downloadAllData(callback);
            }

            @Override
            public void onUserIsNew() {
                List<Note> localNotes = noteStorage.getAllNotes();
                if (!localNotes.isEmpty()) {
                    Log.d(TAG, "New account with local data, uploading " + localNotes.size() + " notes");
                    uploadLocalNotesAndSync(localNotes, callback);
                } else {
                    Log.d(TAG, "New account without local data, doing nothing");
                    callback.onSyncComplete(true, "New account created");
                }
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "Error checking user data, assuming new account: " + error);
                onUserIsNew();
            }
        });
    }

    private void clearLocalUserData() {
        noteStorage.saveNotes(new ArrayList<>());

        SharedPreferences prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("tags");
        editor.apply();

        ConstellationStorage constellationStorage = ConstellationStorage.getInstance(context);
        List<Constellation> constellations = constellationStorage.getConstellations();
        for (Constellation constellation : constellations) {
            constellation.setSeen(false);
            constellation.setFavorite(false);
            constellationStorage.updateConstellation(constellation);
        }

        Log.d(TAG, "Cleared all local user data");
    }

    private void checkIfUserHasDataOnServer(final DataCheckCallback callback) {
        networkManager.getNotes(new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(List<Note> result) {
                if (result != null && !result.isEmpty()) {
                    callback.onUserHasData();
                } else {
                    networkManager.getConstellations(new NetworkManager.ApiCallback<>() {
                        @Override
                        public void onSuccess(List<UserConstellation> constellations) {
                            if (constellations != null && !constellations.isEmpty()) {
                                callback.onUserHasData();
                            } else {
                                callback.onUserIsNew();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            callback.onUserIsNew();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private void uploadLocalNotesAndSync(List<Note> localNotes, final SyncCallback callback) {
        Log.d(TAG, "Uploading " + localNotes.size() + " local notes to server");

        SyncRequest request = new SyncRequest(localNotes, new ArrayList<>(), null);
        networkManager.uploadLocalNotes(request, new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(SyncResult result) {
                Log.d(TAG, "Local notes upload successful");
                downloadAllData(callback);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Local notes upload failed: " + error);
                downloadAllData(callback);
            }
        });
    }

    public void syncAllData(final SyncCallback callback) {
        if (!networkManager.isLoggedIn()) {
            Log.w(TAG, "Sync failed: User not logged in");
            callback.onSyncError("User not logged in");
            return;
        }

        List<Note> localNotes = noteStorage.getAllNotes();
        List<String> deletedNoteIds = noteStorage.getDeletedNoteIds();
        long lastSyncTime = noteStorage.getLastSyncTime();
        Date lastSyncAt = lastSyncTime > 0 ? new Date(lastSyncTime) : null;

        Log.d(TAG, "Starting full sync with " + localNotes.size() + " local notes, " +
                deletedNoteIds.size() + " deletions, last sync: " + lastSyncAt);

        networkManager.syncNotes(localNotes, deletedNoteIds, lastSyncAt, new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(SyncResult result) {
                handleSyncResult(result, callback);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Notes sync failed: " + error);
                callback.onSyncError("Sync failed: " + error);
            }
        });
    }

    private void handleSyncResult(SyncResult result, final SyncCallback callback) {
        if (result.getNotes() != null) {
            noteStorage.saveNotes(result.getNotes());

            List<String> deletedIds = result.getDeletedNoteIds();
            if (deletedIds != null && !deletedIds.isEmpty()) {
                Log.d(TAG, "Processing " + deletedIds.size() + " deleted note IDs from server");
                for (String deletedId : deletedIds) {
                    Note note = noteStorage.getNoteById(deletedId);
                    if (note != null) {
                        note.setDeleted(true);
                        noteStorage.updateNote(note);
                        Log.d(TAG, "Marked note as deleted from server: " + deletedId);
                    }
                }
            }

            noteStorage.clearDeletedNotes();
            noteStorage.setLastSyncTime(System.currentTimeMillis());

            Log.d(TAG, "Notes sync completed, saved " + result.getNotes().size() + " notes from server");

            syncConstellations(new SyncCallback() {
                @Override
                public void onSyncComplete(boolean success, String message) {
                    callback.onSyncComplete(true, "Full sync completed: " + message);
                }

                @Override
                public void onSyncError(String error) {
                    callback.onSyncComplete(true, "Notes synced but constellations failed: " + error);
                }
            });
        } else {
            Log.e(TAG, "SERVER ERROR: Sync successful but returned empty notes array!");
            callback.onSyncError("Server error: Sync returned no data. Local notes preserved.");
        }
    }

    private void downloadAllData(final SyncCallback callback) {
        Log.d(TAG, "Downloading all data from server");

        List<Note> currentNotes = noteStorage.getAllNotes();
        List<String> currentDeletions = noteStorage.getDeletedNoteIds();

        networkManager.syncNotes(currentNotes, currentDeletions, null, new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(SyncResult result) {
                if (result.getNotes() != null && !result.getNotes().isEmpty()) {
                    noteStorage.saveNotes(result.getNotes());

                    List<String> deletedIds = result.getDeletedNoteIds();
                    if (deletedIds != null) {
                        for (String deletedId : deletedIds) {
                            Note note = noteStorage.getNoteById(deletedId);
                            if (note != null) {
                                note.setDeleted(true);
                                noteStorage.updateNote(note);
                            }
                        }
                    }

                    noteStorage.setLastSyncTime(System.currentTimeMillis());
                    Log.d(TAG, "Download completed, saved " + result.getNotes().size() + " notes");

                    downloadConstellations(new SyncCallback() {
                        @Override
                        public void onSyncComplete(boolean success, String message) {
                            callback.onSyncComplete(true, "Download completed successfully");
                        }

                        @Override
                        public void onSyncError(String error) {
                            callback.onSyncComplete(true, "Download completed but constellation sync failed");
                        }
                    });
                } else {
                    Log.w(TAG, "Download completed but no notes received from server");
                    callback.onSyncComplete(true, "Download completed but no data on server");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Download failed: " + error);
                callback.onSyncError("Download failed: " + error);
            }
        });
    }

    private void downloadConstellations(final SyncCallback callback) {
        networkManager.syncConstellations(new ArrayList<>(), new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(ConstellationSyncResult result) {
                if (result.getConstellations() != null && !result.getConstellations().isEmpty()) {
                    updateLocalConstellations(result.getConstellations());
                    Log.d(TAG, "Constellation download completed, updated " + result.getConstellations().size() + " constellations");
                    callback.onSyncComplete(true, "Constellation download successful");
                } else {
                    Log.w(TAG, "No constellations received from server");
                    callback.onSyncComplete(true, "No constellation data on server");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Constellation download failed: " + error);
                callback.onSyncError("Constellation download failed: " + error);
            }
        });
    }

    private void syncConstellations(final SyncCallback callback) {
        ConstellationStorage constellationStorage = ConstellationStorage.getInstance(context);
        List<Constellation> localConstellations = constellationStorage.getConstellations();

        List<UserConstellation> userConstellations = new ArrayList<>();
        for (Constellation constellation : localConstellations) {
            UserConstellation uc = new UserConstellation(
                    constellation.getKey(),
                    constellation.isSeen(),
                    constellation.isFavorite()
            );
            userConstellations.add(uc);
        }

        Log.d(TAG, "Syncing " + userConstellations.size() + " constellations");

        networkManager.syncConstellations(userConstellations, new NetworkManager.ApiCallback<>() {
            @Override
            public void onSuccess(ConstellationSyncResult result) {
                if (result.getConstellations() != null && !result.getConstellations().isEmpty()) {
                    updateLocalConstellations(result.getConstellations());
                    Log.d(TAG, "Constellation sync completed, updated " + result.getConstellations().size() + " constellations");
                    callback.onSyncComplete(true, "Constellation sync successful");
                } else {
                    Log.w(TAG, "Constellation sync completed but no data received");
                    callback.onSyncComplete(true, "Constellation sync completed but no data received");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Constellation sync failed: " + error);
                callback.onSyncError("Constellation sync failed: " + error);
            }
        });
    }

    private void updateLocalConstellations(List<UserConstellation> serverConstellations) {
        ConstellationStorage constellationStorage = ConstellationStorage.getInstance(context);
        List<Constellation> localConstellations = constellationStorage.getConstellations();

        Map<String, UserConstellation> serverMap = new HashMap<>();
        for (UserConstellation serverConstellation : serverConstellations) {
            serverMap.put(serverConstellation.getConstellationKey(), serverConstellation);
        }

        int updatedCount = 0;
        for (Constellation localConstellation : localConstellations) {
            UserConstellation serverConstellation = serverMap.get(localConstellation.getKey());
            if (serverConstellation != null) {
                localConstellation.setSeen(serverConstellation.isSeen());
                localConstellation.setFavorite(serverConstellation.isFavorite());
                constellationStorage.updateConstellation(localConstellation);
                updatedCount++;
            }
        }

        Log.d(TAG, "Updated " + updatedCount + " local constellations with server data");
    }

    public interface SyncCallback {
        void onSyncComplete(boolean success, String message);
        void onSyncError(String error);
    }

    private interface DataCheckCallback {
        void onUserHasData();
        void onUserIsNew();
        void onError(String error);
    }
}