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

    private static final RenderType LINES_TYPE = RenderType.create(
        "betterschematics:lines",
        RenderSetup.builder(RenderPipelines.LINES).createRenderSetup()
    );

    public SchematicRenderer(SchematicManager manager) { this.manager = manager; }
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

    public void render(Camera camera, float partialTick) {
        if (!renderEnabled) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camPos = camera.position();
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(camera.rotation());
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();

        SchematicRegion region = data.getMainRegion();
        if (region == null) return;
        BlockPos size = region.size;

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockPos origin = manager.getPlacementOrigin();

        // === BOUNDING BOX (yellow) ===
        BlockPos endPos = manager.transformPos(new BlockPos(size.getX()-1, size.getY()-1, size.getZ()-1));
        int minX = Math.min(origin.getX(), endPos.getX());
        int minY = Math.min(origin.getY(), endPos.getY());
        int minZ = Math.min(origin.getZ(), endPos.getZ());
        int maxX = Math.max(origin.getX(), endPos.getX())+1;
        int maxY = Math.max(origin.getY(), endPos.getY())+1;
        int maxZ = Math.max(origin.getZ(), endPos.getZ())+1;
        VertexConsumer outlineVc = buffers.getBuffer(LINES_TYPE);
        addWireframeBox(outlineVc, pose, mat, minX, minY, minZ, maxX, maxY, maxZ, 255, 255, 127, 128);
        buffers.endBatch(LINES_TYPE);

        // === COLOR-CODED WIREFRAME PER BLOCK TYPE ===
        VertexConsumer vc = buffers.getBuffer(LINES_TYPE);
        for (int y = 0; y < size.getY(); y++)
            for (int z = 0; z < size.getZ(); z++)
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = region.getBlockState(local);
                    if (expected == null || expected.isAir()) continue;
                    BlockPos worldPos = manager.transformPos(local);
                    BlockState actual = mc.level.getBlockState(worldPos);
                    if (expected.equals(actual)) continue;

                    float[] c = blockColor(expected);
                    addWireframeBox(vc, pose, mat,
                        worldPos.getX(), worldPos.getY(), worldPos.getZ(),
                        worldPos.getX()+1, worldPos.getY()+1, worldPos.getZ()+1,
                        (int)(c[0]*255), (int)(c[1]*255), (int)(c[2]*255), 140);
                }
        buffers.endBatch(LINES_TYPE);
    }

    private static float[] blockColor(BlockState state) {
        int h = state.getBlock().getDescriptionId().hashCode();
        float hue = ((h & 0x7FFFFFFF) % 360) / 360f;
        return hsvToRgb(hue, 0.7f, 1.0f);
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0: return new float[]{v, t, p};
            case 1: return new float[]{q, v, p};
            case 2: return new float[]{p, v, t};
            case 3: return new float[]{p, q, v};
            case 4: return new float[]{t, p, v};
            default:return new float[]{v, p, q};
        }
    }

    private void addWireframeBox(VertexConsumer vc, PoseStack.Pose pose, Matrix4f mat,
                                  float x1, float y1, float z1, float x2, float y2, float z2,
                                  int r, int g, int b, int a) {
        addLine(vc, pose, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(vc, pose, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(vc, pose, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(vc, pose, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);
        addLine(vc, pose, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(vc, pose, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(vc, pose, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(vc, pose, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);
        addLine(vc, pose, mat, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(vc, pose, mat, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(vc, pose, mat, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(vc, pose, mat, x1, y1, z2, x1, y2, z2, r, g, b, a);
    }

    private void addLine(VertexConsumer vc, PoseStack.Pose pose, Matrix4f mat,
                         float x1, float y1, float z1, float x2, float y2, float z2,
                         int r, int g, int b, int a) {
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, 0, 1, 0).setLineWidth(2.0f);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, 0, 1, 0).setLineWidth(2.0f);
    }
}
