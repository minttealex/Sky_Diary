package com.example.skydiary;

import java.util.List;

public class ConstellationSyncResult {
    private List<UserConstellation> constellations;
    private String message;
    private boolean success;

    public List<UserConstellation> getConstellations() { return constellations; }
    public void setConstellations(List<UserConstellation> constellations) { this.constellations = constellations; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}