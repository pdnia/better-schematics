package com.betterschematics.render;

import com.betterschematics.schematic.SchematicManager;

public class SchematicRenderer {
    private final SchematicManager manager;
    private boolean renderEnabled = true;

    public SchematicRenderer(SchematicManager manager) {
        this.manager = manager;
    }

    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }
}