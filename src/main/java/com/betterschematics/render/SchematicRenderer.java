package com.betterschematics.render;

import com.betterschematics.schematic.SchematicManager;

public class SchematicRenderer {
    private final SchematicManager manager;
    private boolean renderEnabled = true;

    public SchematicRenderer(SchematicManager manager) { this.manager = manager; }
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

    // World rendering temporarily disabled for EventBus 7 / Forge 1.21.11 migration.
    // RenderLevelStageEvent was removed; render will be called from GUI overlay or
    // a future rendering hook once the correct event is identified.
    public void render(com.mojang.blaze3d.vertex.PoseStack ps, net.minecraft.client.Camera cam, float pt) {
        // TODO: re-implement wireframe rendering
    }
}
