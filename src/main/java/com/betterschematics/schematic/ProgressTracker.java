package com.betterschematics.schematic;

public class ProgressTracker {
    private final SchematicData schematic;
    private int totalPlaced = 0;

    public ProgressTracker(SchematicData schematic) { this.schematic = schematic; }
    public int getTotalPlaced() { return totalPlaced; }
    public void incrementPlaced() { totalPlaced++; }
}