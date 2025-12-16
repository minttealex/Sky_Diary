package com.example.skydiary;

import java.util.List;

public class ConstellationSyncRequest {
    private List<UserConstellation> constellations;

    public ConstellationSyncRequest(List<UserConstellation> constellations) {
        this.constellations = constellations;
    }

    public List<UserConstellation> getConstellations() { return constellations; }
    public void setConstellations(List<UserConstellation> constellations) { this.constellations = constellations; }
}