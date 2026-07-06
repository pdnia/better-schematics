package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
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

        Vec3 camPos = camera.position();
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(camera.rotation());
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();

        SchematicRegion region = data.getMainRegion();
        if (region == null) return;
        BlockPos size = region.size;

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tess = Tesselator.getInstance();

        BlockPos origin = manager.getPlacementOrigin();
        BlockPos endPos = manager.transformPos(new BlockPos(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
        int minX = Math.min(origin.getX(), endPos.getX());
        int minY = Math.min(origin.getY(), endPos.getY());
        int minZ = Math.min(origin.getZ(), endPos.getZ());
        int maxX = Math.max(origin.getX(), endPos.getX()) + 1;
        int maxY = Math.max(origin.getY(), endPos.getY()) + 1;
        int maxZ = Math.max(origin.getZ(), endPos.getZ()) + 1;

        BufferBuilder bb = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        addWireframeBox(bb, mat, minX, minY, minZ, maxX, maxY, maxZ, 1f, 1f, 0.867f, 0.5f);
        BufferUploader.drawWithShader(bb.buildOrThrow());

        bb = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = region.getBlockState(local);
                    if (expected == null || expected.isAir()) continue;
                    BlockPos worldPos = manager.transformPos(local);
                    BlockState actual = mc.level.getBlockState(worldPos);
                    boolean match = expected.equals(actual);
                    addWireframeBox(bb, mat, worldPos.getX(), worldPos.getY(), worldPos.getZ(),
                            worldPos.getX() + 1, worldPos.getY() + 1, worldPos.getZ() + 1,
                            0f, match ? 0.4f : 0f, match ? 0f : 0.4f, 0.4f);
                }
            }
        }
        BufferUploader.drawWithShader(bb.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }

    private void addWireframeBox(BufferBuilder bb, Matrix4f mat,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float r, float g, float b, float a) {
        bb.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y1, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y1, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y1, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y1, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y1, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y1, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y2, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y2, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y2, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y2, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y2, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y2, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y2, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y1, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y2, z1).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y1, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y1, z2).setColor(r, g, b, a);
        bb.addVertex(mat, x1, y2, z2).setColor(r, g, b, a);
    }
}
