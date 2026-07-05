package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.block3d.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import com.mojang.block3d.Vector3f;

public class SchematicRenderer {
    private boolean renderEnabled = true;

    public SchematicRenderer() {}
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

    public void renderSchematicOutline(SchematicManager manager, Matrix4f projectionMatrix, Matrix4f poseMatrix, Matrix4f cameraMatrix) {
        if (!renderEnabled || !manager.hasSchematic()) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;

        Minecraft mc = Minecraft.getInstance();
        Vector3f cameraPos = mc.getCameraEntity().getPosition(0);

        for (SchematicRegion r : data.regions) {
            renderRegionOutline(manager, r, cameraPos, projectionMatrix, poseMatrix, cameraMatrix);
        }
    }

    private void renderRegionOutline(SchematicManager manager, SchematicRegion r, Vector3f cameraPos, Matrix4f projectionMatrix, Matrix4f poseMatrix, Matrix4f cameraMatrix) {
        BlockPos origin = manager.getPlacementOrigin();
        BlockPos size = r.size;

        // Draw bounding box using LevelRenderer.renderLineBox
        double x0 = origin.getX() - cameraPos.x();
        double y0 = origin.getY() - cameraPos.y();
        double z0 = origin.getZ() - cameraPos.z();
        double x1 = x0 + size.getX();
        double y1 = y0 + size.getY();
        double z1 = z0 + size.getZ();

        // Green wireframe box
        LevelRenderer.renderLineBox(projectionMatrix, poseMatrix, cameraMatrix, x0, y0, z0, x1, y1, z1, 0F, 1.0F, 0.0F, 0.5F);

        // Render ghost blocks for current layer in layer mode
        if (manager.isLayerMode()) {
            int ymin = manager.getCurrentLayerMin();
            int ymax = manager.getCurrentLayerMax();
            for (int y = ymin; y <= ymax && y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    for (int z = 0; z < size.getZ(); z++) {
                        net.minecraft.world.level.block.state.BlockState bs = r.getBlockState(new BlockPos(x, y, z));
                        if (bs != null && !bs.isAir()) {
                            BlockPos wp = manager.transformPos(new BlockPos(x, y, z));
                            double bx = wp.getX() - cameraPos.x();
                            double by = wp.getY() - cameraPos.y();
                            double bz = wp.getZ() - cameraPos.z();
                            // Blue ghost block
                            LevelRenderer.renderLineBox(projectionMatrix, poseMatrix, cameraMatrix, bx, by, bz, bx + 1, by + 1, bz + 1, 0.3F, 0.7F, 1.0F, 0.3F);
                        }
                    }
                }
            }
        }
    }
}