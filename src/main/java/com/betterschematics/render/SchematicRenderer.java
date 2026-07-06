package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class SchematicRenderer {
    private final SchematicManager manager;
    private boolean renderEnabled = true;
    private boolean outlineMode = true;

    public SchematicRenderer(SchematicManager manager) { this.manager = manager; }
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }
    public void toggleOutlineMode() { outlineMode = !outlineMode; }
    public boolean isOutlineMode() { return outlineMode; }

    public void render(PoseStack poseStack, Camera camera, float partialTick) {
        if (!renderEnabled) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();

        SchematicRegion region = data.getMainRegion();
        if (region == null) { poseStack.popPose(); return; }

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockPos size = region.size;

        // Render overall bounding box outline (yellow)
        if (outlineMode) {
            VertexConsumer outlineVc = buffers.getBuffer(RenderType.lines());
            BlockPos origin = manager.getPlacementOrigin();
            BlockPos endPos = manager.transformPos(new BlockPos(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
            // Adjust bounds to account for rotation/mirror
            int minX = Math.min(origin.getX(), endPos.getX());
            int minY = Math.min(origin.getY(), endPos.getY());
            int minZ = Math.min(origin.getZ(), endPos.getZ());
            int maxX = Math.max(origin.getX(), endPos.getX()) + 1;
            int maxY = Math.max(origin.getY(), endPos.getY()) + 1;
            int maxZ = Math.max(origin.getZ(), endPos.getZ()) + 1;
            renderFullWireframeBox(outlineVc, mat, minX, minY, minZ, maxX, maxY, maxZ, 0xFFFFDD00);
            buffers.endBatch(RenderType.lines());
        }

        // Render per-block wireframe overlay
        VertexConsumer vc = buffers.getBuffer(RenderType.lines());
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = region.getBlockState(local);
                    if (expected == null || expected.isAir()) continue;
                    BlockPos worldPos = manager.transformPos(local);
                    BlockState actual = mc.level.getBlockState(worldPos);
                    int color = expected.equals(actual) ? 0x6600FF00 : 0x66FF0000;
                    float bx = worldPos.getX(), by = worldPos.getY(), bz = worldPos.getZ();
                    renderFullWireframeBox(vc, mat, bx, by, bz, bx + 1, by + 1, bz + 1, color);
                }
            }
        }
        buffers.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private void renderFullWireframeBox(VertexConsumer vc, Matrix4f mat,
                                         float x1, float y1, float z1,
                                         float x2, float y2, float z2, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        // Bottom square (y1)
        addLine(vc, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(vc, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(vc, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(vc, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);

        // Top square (y2)
        addLine(vc, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(vc, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(vc, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(vc, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);

        // Vertical edges
        addLine(vc, mat, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(vc, mat, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(vc, mat, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(vc, mat, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private void addLine(VertexConsumer vc, Matrix4f mat,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
    }
}
