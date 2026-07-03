package com.betterschematics.schematic;

import net.minecraft.core.BlockPos;

public class SchematicManager {
    private SchematicData activeSchematic;
    private String buildDirection = "N";
    private int currentLayer = 0;
    private int visibleLayerCount = 1;

    public SchematicData getActiveSchematic() { return activeSchematic; }
    public void setActiveSchematic(SchematicData data) { this.activeSchematic = data; }
    public String getBuildDirection() { return buildDirection; }
    public void setBuildDirection(String d) { buildDirection = d; }
    public int getCurrentLayer() { return currentLayer; }
    public void setCurrentLayer(int l) { currentLayer = l; }
    public int getVisibleLayerCount() { return visibleLayerCount; }
    public void setVisibleLayerCount(int c) { visibleLayerCount = c; }
}