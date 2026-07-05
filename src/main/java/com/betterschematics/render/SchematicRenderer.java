package com.betterschematics.render;

/**
 * Schematic renderer - placeholder for Forge 1.21.11
 */
public class SchematicRenderer {
    private boolean renderEnabled = true;
    public SchematicRenderer() {}
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }
}