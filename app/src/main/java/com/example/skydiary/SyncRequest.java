package com.example.skydiary;

import java.util.List;

public class SyncRequest {
    private List<Note> notes;

    public SyncRequest() {
    }

    public SyncRequest(List<Note> notes) {
        this.notes = notes;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}