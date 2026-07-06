package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
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

    public SchematicRenderer(SchematicManager manager) { this.manager = manager; }
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

    public void render(Camera camera, DeltaTracker timer) {
        if (!renderEnabled) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = new PoseStack();

        // Set up world-to-camera transform
        poseStack.mulPose(camera.rotation());
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();

        SchematicRegion region = data.getMainRegion();
        if (region == null) return;

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockPos size = region.size;

        // Yellow bounding box outline
        VertexConsumer outlineVc = buffers.getBuffer(RenderType.lines());
        BlockPos origin = manager.getPlacementOrigin();
        BlockPos endPos = manager.transformPos(new BlockPos(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
        int minX = Math.min(origin.getX(), endPos.getX());
        int minY = Math.min(origin.getY(), endPos.getY());
        int minZ = Math.min(origin.getZ(), endPos.getZ());
        int maxX = Math.max(origin.getX(), endPos.getX()) + 1;
        int maxY = Math.max(origin.getY(), endPos.getY()) + 1;
        int maxZ = Math.max(origin.getZ(), endPos.getZ()) + 1;
        renderFullWireframeBox(outlineVc, mat, minX, minY, minZ, maxX, maxY, maxZ, 0xFFFFDD00);
        buffers.endBatch(RenderType.lines());

        // Per-block wireframe (green = correct, red = mismatch)
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
                    renderFullWireframeBox(vc, mat, worldPos.getX(), worldPos.getY(), worldPos.getZ(),
                            worldPos.getX() + 1, worldPos.getY() + 1, worldPos.getZ() + 1, color);
                }
            }
        }
        buffers.endBatch(RenderType.lines());
    }

    private void renderFullWireframeBox(VertexConsumer vc, Matrix4f mat,
                                         float x1, float y1, float z1,
                                         float x2, float y2, float z2, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        addLine(vc, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(vc, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(vc, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(vc, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);
        addLine(vc, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(vc, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(vc, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(vc, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);
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
