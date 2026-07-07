package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class SchematicRenderer {
    private final SchematicManager manager;
    private boolean renderEnabled = true;

    private static final RenderType LINES_TYPE = RenderType.create(
        "betterschematics:lines",
        RenderSetup.builder(RenderPipelines.LINES).createRenderSetup()
    );

    public SchematicRenderer(SchematicManager manager) { this.manager = manager; }
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

    public void render(Camera camera, DeltaTracker timer) {
        if (!renderEnabled) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camPos = camera.position();
        Matrix4f mat = new Matrix4f(); // identity - pipeline handles view-projection

        SchematicRegion region = data.getMainRegion();
        if (region == null) return;
        BlockPos size = region.size;

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockPos origin = manager.getPlacementOrigin();
        BlockPos endPos = manager.transformPos(new BlockPos(size.getX()-1, size.getY()-1, size.getZ()-1));
        int minX = Math.min(origin.getX(), endPos.getX());
        int minY = Math.min(origin.getY(), endPos.getY());
        int minZ = Math.min(origin.getZ(), endPos.getZ());
        int maxX = Math.max(origin.getX(), endPos.getX())+1;
        int maxY = Math.max(origin.getY(), endPos.getY())+1;
        int maxZ = Math.max(origin.getZ(), endPos.getZ())+1;

        // Outline box
        VertexConsumer outlineVc = buffers.getBuffer(LINES_TYPE);
        addWireframeBox(outlineVc, mat,
            (float)(minX - camPos.x), (float)(minY - camPos.y), (float)(minZ - camPos.z),
            (float)(maxX - camPos.x), (float)(maxY - camPos.y), (float)(maxZ - camPos.z),
            1f, 1f, 0.867f, 0.5f);
        buffers.endBatch(LINES_TYPE);

        // Per-block wireframes
        VertexConsumer vc = buffers.getBuffer(LINES_TYPE);
        for (int y = 0; y < size.getY(); y++)
            for (int z = 0; z < size.getZ(); z++)
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = region.getBlockState(local);
                    if (expected == null || expected.isAir()) continue;
                    BlockPos worldPos = manager.transformPos(local);
                    BlockState actual = mc.level.getBlockState(worldPos);
                    boolean match = expected.equals(actual);
                    float wx1 = (float)(worldPos.getX() - camPos.x);
                    float wy1 = (float)(worldPos.getY() - camPos.y);
                    float wz1 = (float)(worldPos.getZ() - camPos.z);
                    addWireframeBox(vc, mat, wx1, wy1, wz1, wx1+1, wy1+1, wz1+1,
                            0f, match?0.4f:0f, match?0f:0.4f, 0.4f);
                }
        buffers.endBatch(LINES_TYPE);
    }

    private void addWireframeBox(VertexConsumer vc, Matrix4f mat,
                                  float x1, float y1, float z1, float x2, float y2, float z2,
                                  float r, float g, float b, float a) {
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
                         float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(0f, 1f, 0f).setLineWidth(1f);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setNormal(0f, 1f, 0f).setLineWidth(1f);
    }
}
