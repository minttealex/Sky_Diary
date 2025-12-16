package com.example.skydiary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SyncRequest {
    private List<Note> notes;
    private List<String> deletedNoteIds;
    private String lastSyncAt;

    public SyncRequest() {
        this.notes = new ArrayList<>();
        this.deletedNoteIds = new ArrayList<>();
    }

    public SyncRequest(List<Note> notes) {
        this.notes = notes != null ? notes : new ArrayList<>();
        this.deletedNoteIds = new ArrayList<>();
        this.lastSyncAt = formatDate(new Date());
    }

    public SyncRequest(List<Note> notes, List<String> deletedNoteIds, Date lastSyncAt) {
        this.notes = notes != null ? notes : new ArrayList<>();
        this.deletedNoteIds = deletedNoteIds != null ? deletedNoteIds : new ArrayList<>();
        this.lastSyncAt = lastSyncAt != null ? formatDate(lastSyncAt) : formatDate(new Date());
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public List<Note> getNotes() { return notes; }
    public void setNotes(List<Note> notes) { this.notes = notes; }

    public List<String> getDeletedNoteIds() { return deletedNoteIds; }
    public void setDeletedNoteIds(List<String> deletedNoteIds) { this.deletedNoteIds = deletedNoteIds; }

    public String getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(String lastSyncAt) { this.lastSyncAt = lastSyncAt; }
}