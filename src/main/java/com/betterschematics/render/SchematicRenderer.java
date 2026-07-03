package com.betterschematics.render;

import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Camera;

public class SchematicRenderer {
    private final SchematicManager manager;
    private boolean renderEnabled = true;
    private final MinimapRenderer minimapRenderer;

    public SchematicRenderer(SchematicManager manager) {
        this.manager = manager;
        this.minimapRenderer = new MinimapRenderer(manager);
    }

    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }
    public MinimapRenderer getMinimapRenderer() { return minimapRenderer; }

    public void render(Com.mojang.blaze3d.vertex.PoseStack poseStack, Camera camera, float partialTick) {
        // TODO: implement actual ghost block rendering
    }
}