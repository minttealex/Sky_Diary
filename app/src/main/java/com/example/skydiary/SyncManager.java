package com.example.skydiary;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class SyncManager {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

    private final FirebaseFirestore db;
    private final NoteStorage noteStorage;
    private final Context context;

    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface DataCheckCallback {
        void onResult(boolean hasData);
        void onError(String error);
    }

    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseManager.getInstance().getDb();
        this.noteStorage = NoteStorage.getInstance(context);
    }

    public void checkUserHasData(String uid, DataCheckCallback callback) {
        db.collection("notes").whereEqualTo("userId", uid).limit(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasData = !task.getResult().isEmpty();
                        callback.onResult(hasData);
                    } else {
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Check failed");
                    }
                });
    }

    public void downloadAndReplace(SyncCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) {
            callback.onError("Not logged in");
            return;
        }

        db.collection("notes").whereEqualTo("userId", uid).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Note> cloudNotes = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Note note = documentSnapshotToNote(doc);
                        cloudNotes.add(note);
                    }
                    noteStorage.saveNotes(cloudNotes);
                    callback.onSuccess("Downloaded " + cloudNotes.size() + " notes");
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void uploadLocalNotes(String uid, SyncCallback callback) {
        List<Note> localNotes = noteStorage.getAllNotes();
        if (localNotes.isEmpty()) {
            callback.onSuccess("No local notes to upload");
            return;
        }

        List<Task<Void>> tasks = new ArrayList<>();
        for (Note note : localNotes) {
            Map<String, Object> noteData = noteToMap(note, uid);
            tasks.add(db.collection("notes").document(note.getId()).set(noteData));
        }

        Tasks.whenAllSuccess(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess("Uploaded " + localNotes.size() + " notes");
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Upload failed";
                callback.onError(error);
            }
        });
    }

    public void syncAll(SyncCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) {
            callback.onError("Not logged in");
            return;
        }

        uploadLocalNotes(uid, new SyncCallback() {
            @Override
            public void onSuccess(String message) {
                downloadAndReplace(new SyncCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        callback.onSuccess("Sync complete");
                    }
                    @Override
                    public void onError(String error) {
                        callback.onError("Download failed: " + error);
                    }
                });
            }
            @Override
            public void onError(String error) {
                callback.onError("Upload failed: " + error);
            }
        });
    }

    private Map<String, Object> noteToMap(Note note, String uid) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", uid);
        map.put("name", note.getName());
        map.put("location", note.getLocation());
        map.put("text", note.getText());
        map.put("timestamp", note.getTimestamp());
        map.put("tags", note.getTags());

        List<Map<String, Object>> imageMaps = new ArrayList<>();
        for (NoteImage img : note.getImages()) {
            Map<String, Object> imgMap = new HashMap<>();
            imgMap.put("id", img.getId());
            imgMap.put("position", img.getPosition());
            imgMap.put("rotation", img.getRotation());
            imgMap.put("originalWidth", img.getOriginalWidth());
            imgMap.put("originalHeight", img.getOriginalHeight());

            String base64 = ImageUtils.compressAndEncodeToBase64(img.getImagePath());
            imgMap.put("imageData", base64 != null ? base64 : "");
            imageMaps.add(imgMap);
        }
        map.put("images", imageMaps);

        map.put("createdAt", stringToDate(note.getCreatedAt()));
        map.put("updatedAt", new Date());
        map.put("isDeleted", note.isDeleted());
        if (note.getDeletedAt() != null) {
            map.put("deletedAt", stringToDate(note.getDeletedAt()));
        }
        return map;
    }

    private Note documentSnapshotToNote(DocumentSnapshot doc) {
        Note note = new Note();
        note.setId(doc.getId());
        note.setUserId(doc.getString("userId"));
        note.setName(doc.getString("name"));
        note.setLocation(doc.getString("location"));
        note.setText(doc.getString("text"));
        Long ts = doc.getLong("timestamp");
        if (ts != null) note.setTimestamp(ts);
        note.setTags((List<String>) doc.get("tags"));

        List<Map<String, Object>> imageMaps = (List<Map<String, Object>>) doc.get("images");
        List<NoteImage> images = new ArrayList<>();
        if (imageMaps != null) {
            for (Map<String, Object> map : imageMaps) {
                String id = (String) map.get("id");
                Long posLong = (Long) map.get("position");
                int position = posLong != null ? posLong.intValue() : 0;
                Double rotDouble = (Double) map.get("rotation");
                float rotation = rotDouble != null ? rotDouble.floatValue() : 0f;
                Long widthLong = (Long) map.get("originalWidth");
                int width = widthLong != null ? widthLong.intValue() : 0;
                Long heightLong = (Long) map.get("originalHeight");
                int height = heightLong != null ? heightLong.intValue() : 0;
                String base64 = (String) map.get("imageData");

                if (base64 != null && !base64.isEmpty()) {
                    String localPath = noteStorage.saveBase64ImageToInternalStorage(base64);
                    if (localPath != null) {
                        NoteImage img = new NoteImage(id, localPath, position, rotation, width, height);
                        images.add(img);
                    }
                }
            }
        }
        note.setImages(images);

        Object createdAt = doc.get("createdAt");
        if (createdAt instanceof com.google.firebase.Timestamp) {
            note.setCreatedAt(dateToString(((com.google.firebase.Timestamp) createdAt).toDate()));
        } else if (createdAt instanceof String) {
            note.setCreatedAt((String) createdAt);
        }

        Object updatedAt = doc.get("updatedAt");
        if (updatedAt instanceof com.google.firebase.Timestamp) {
            note.setUpdatedAt(dateToString(((com.google.firebase.Timestamp) updatedAt).toDate()));
        } else if (updatedAt instanceof String) {
            note.setUpdatedAt((String) updatedAt);
        }

        Boolean deleted = doc.getBoolean("isDeleted");
        if (deleted != null) note.setDeleted(deleted);

        Object deletedAt = doc.get("deletedAt");
        if (deletedAt instanceof com.google.firebase.Timestamp) {
            note.setDeletedAt(dateToString(((com.google.firebase.Timestamp) deletedAt).toDate()));
        } else if (deletedAt instanceof String) {
            note.setDeletedAt((String) deletedAt);
        }

        return note;
    }

    private String dateToString(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TIME_ZONE);
        return sdf.format(date);
    }

    private Date stringToDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            sdf.setTimeZone(TIME_ZONE);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return new Date();
        }
    }

    private String getCurrentUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }
}