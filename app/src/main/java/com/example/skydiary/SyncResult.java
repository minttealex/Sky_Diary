package com.example.skydiary;

import java.util.List;

public class SyncResult {
    private List<Note> notes;
    private List<String> deletedNoteIds;
    private String message;
    private boolean success;

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public List<String> getDeletedNoteIds() {
        return deletedNoteIds != null ? deletedNoteIds : new java.util.ArrayList<>();
    }

    public void setDeletedNoteIds(List<String> deletedNoteIds) {
        this.deletedNoteIds = deletedNoteIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}