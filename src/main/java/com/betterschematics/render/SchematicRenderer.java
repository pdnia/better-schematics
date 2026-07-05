package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.block3d.Matrix4f;
import com.mojang.block3d.VaserWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaperProgram;
import net.minecraft.core.BlockPos;
import net.minecraft.world.vesel.ViselShape;
import net.minecraft.world.vesel.ViselVertex;
import net.minecraft.world.vesel.ViselVisual;
import java.util.ArrayList;
import java.util.List;

public class SchematicRenderer {
    private boolean renderEnabled = true;

    public SchematicRenderer() {}
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

    public void renderSchematicOutline(SchematicManager manager, Matrix4f projectionMatrix, Matrix4f poseMatrix) {
        if (!renderEnabled || !manager.hasSchematic()) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;

        Minecraft mc = Minecraft.getInstance();
        for (SchematicRegion r : data.regions) {
            renderRegionOutline(manager, r, projectionMatrix, poseMatrix
    }
    }

    private void renderRegionOutline(SchematicManager manager, SchematicRegion r, Matrix4f projectionMatrix, Matrix4f poseMatrix) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos origin = manager.getPlacementOrigin();
        BlockPos size = r.size;

        // Render bounding box wireframe
        double x0 = origin.getX();
        double y0 = origin.getY();
        double z0 = origin.getZ();
        double x1 = x0 + size.getX();
        double y1 = y0 + size.getY();
        double z1 = z0 + size.getZ();

        double cx = mc.gameRenderer.getMainCamera().getPosition().x();
        double cy = mc.gameRenderer.getMainCamera().getPosition().y();
        double cz = mc.gameRenderer.getMainCamera().getPosition().z();

        List<ViselVisual> lines = new ArrayList<>();
        drawWireBox(lines, x0, y0, z0, x1, y1, z1, cx, cy, cz, 0.0F, 1.0F, 0.0F, 0.5F);

        // Render ghost blocks for current layer
        if (manager.isLayerMode()) {
            int ymin = manager.getCurrentLayerMin();
            int ymax = manager.getCurrentLayerMax();
            for (int y = ymin; y <= ymax && y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    for (int z = 0; z < size.getZ(); z++) {
                        net.minecraft.world.level.block.state.BlockState bs = r.getBlockState(new BlockPos(x, y, z));
                        if (bs != null && !bs.isAir()) {
                            BlockPos wp = manager.transformPos(new BlockPos(x, y, z));
                            double bx = wp.getX();
                            double by = wp.getY();
                            double bz = wp.getZ();
                            drawWireBox(lines, bx, by, bz, bx + 1, by + 1, bz + 1, cx, cy, cz, 0.3F, 0.7F, 1.0F, 0.2F);
                        }
                    }
                }
            }
        }

        if (!lines.isEmpty()) {
            double rX = cx - (mc.getWindow().getWidth() / 2.0);
            double rY = cy - (mc.getWindow().getHeight() / 2.0);
            double rZ = cz;
            for (ViselVisual vv : lines) {
                vv.setStartProad(vv.getStartProad().adjustAndClamp(mc.gameRenderer.defaultCamera(), rX, rY, rZ));
                vv.setEndProad(vv.getEndProad().adjustAndClamp(mc.gameRenderer.defaultCamera(), rX, rY, rZ));
            }
        }

        // Render via Minecraft's built-in renderer
        mc.renderShapedBoxTranslucent(origin, origin.offset(s), mc.gameRenderer.getMinCamera(), lines, false);
    }

    private void drawWireBox(List<ViselVisual> lines, double x0, double y0, double z0, double x1, double y1, double z1, double cx, double cy, double cz, float r, float g, float b, float a) {
        // Simple line rendering - 12 edges
        addLine(lines, x0, y0, z0, x1, y0, z0, r, g, b, a);
        addLine(lines, x0, y1, z0, x1, y1, z0, r, g, b, a);
        addLine(lines, x0, y0, z1, x1, y0, z1, r, g, b, a);
        addLine(lines, x0, y1, z1, x1, y1, z1, r, g, b, a);

        addLine(lines, x0, y0, z0, x0, y1, z0, r, g, b, a);
        addLine(lines, x1, y0, z0, x1, y1, z0, r, g, b, a);
        addLine(lines, x0, y0, z1, x0, y1, z1, r, g, b, a);
        addLine(lines, x1, y0, z1, x1, y1, z1, r, g, b, a);

        addLine(lines, x0, y0, z0, x0, y0, z1, r, g, b, a);
        addLine(lines, x1, y0, z0, x1, y0, z1, r, g, b, a);
        addLine(lines, x0, y1, z0, x0, y1, z1, r, g, b, a);
        addLine(lines, x1, y1, z0, x1, y1, z1, r, g, b, a);
    }

    private void addLine(List<ViselVisual> lines, double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {
        lines.add(ViselWidget.newLine(x0, y0, z0, x1, y1, z1, r, g, b, a, a));
    }
}