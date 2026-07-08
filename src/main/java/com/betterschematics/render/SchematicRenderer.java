package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
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

    private static final RenderType GHOST_LINES = RenderType.create(
        "betterschematics:ghost_lines",
        RenderSetup.builder(RenderPipelines.LINES).createRenderSetup()
    );

    public SchematicRenderer(SchematicManager manager) { this.manager = manager; }
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

    /** Stable hash-based color - safe, won't crash */
    private static float[] colorFor(BlockState bs) {
        int h = bs.getBlock().getDescriptionId().hashCode();
        return new float[]{
            ((h & 0xFF) / 255f) * 0.55f + 0.45f,
            (((h >> 8) & 0xFF) / 255f) * 0.55f + 0.45f,
            (((h >> 16) & 0xFF) / 255f) * 0.55f + 0.45f,
            0.7f
        };
    }

    public void render(Camera camera) {
        if (!renderEnabled) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        SchematicRegion region = data.getMainRegion();
        if (region == null) return;

        Vec3 camPos = camera.position();
        PoseStack pose = new PoseStack();
        pose.mulPose(camera.rotation());
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = pose.last().pose();

        BlockPos size = region.size;
        BlockPos origin = manager.getPlacementOrigin();
        BlockPos end = manager.transformPos(new BlockPos(size.getX()-1, size.getY()-1, size.getZ()-1));
        int minX = Math.min(origin.getX(), end.getX());
        int minY = Math.min(origin.getY(), end.getY());
        int minZ = Math.min(origin.getZ(), end.getZ());
        int maxX = Math.max(origin.getX(), end.getX())+1;
        int maxY = Math.max(origin.getY(), end.getY())+1;
        int maxZ = Math.max(origin.getZ(), end.getZ())+1;

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(GHOST_LINES);

        // Gold outline box
        addWireframeBox(vc, mat, minX, minY, minZ, maxX, maxY, maxZ, 1f, 0.85f, OF, 0.9f);

        // Per-block wireframes
        for (int y = 0; y < size.getY(); y++)
            for (int z = 0; z < size.getZ(); z++)
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected;
                    try { expected = region.getBlockState(local); }
                    catch (Exception e) { continue; }
                    if (expected == null || expected.isAir()) continue;
                    BlockPos wp = manager.transformPos(local);
                    float[] c = colorFor(expected);
                    addWireframeBox(vc, mat, wp.getX(), wp.getY(), wp.getZ(),
                            wp.getX()+1, wp.getY()+1, wp.getZ()+1, c[0], c[1], c[2], c[3]);
                }
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