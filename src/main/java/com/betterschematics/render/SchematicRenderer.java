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

    public SchematicRenderer(SchematicManager manager) { this.manager = manager; }
    public void toggleRender() { renderEnabled = !renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

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
        VertexConsumer vc = buffers.getBuffer(RenderType.lines());

        BlockPos size = region.size;
        int count = 0;
        int max = 5000;
        for (int y = 0; y < size.getY() && count < max; y++) {
            for (int z = 0; z < size.getZ() && count < max; z++) {
                for (int x = 0; x < size.getX() && count < max; x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = region.getBlockState(local);
                    if (expected == null || expected.isAir()) continue;
                    BlockPos worldPos = manager.transformPos(local);
                    BlockState actual = mc.level.getBlockState(worldPos);
                    int c = expected.equals(actual) ? 0x8800FF00 : 0x88FF0000;
                    renderWireframeBox(vc, mat, worldPos, c);
                    count++;
                }
            }
        }
        buffers.endBatch();
        poseStack.popPose();
    }

    private void renderWireframeBox(VertexConsumer vc, Matrix4f mat, BlockPos pos, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;

        // Bottom face (4 edges)
        vc.addVertex(mat, x, y, z).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y, z).setColor(r, g, b, a);

        vc.addVertex(mat, x2, y, z).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y, z2).setColor(r, g, b, a);

        vc.addVertex(mat, x2, y, z2).setColor(r, g, b, a);
        vc.addVertex(mat, x, y, z2).setColor(r, g, b, a);

        vc.addVertex(mat, x, y, z2).setColor(r, g, b, a);
        vc.addVertex(mat, x, y, z).setColor(r, g, b, a);

        // Top face (4 edges)
        vc.addVertex(mat, x, y2, z).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z).setColor(r, g, b, a);

        vc.addVertex(mat, x2, y2, z).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);

        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        vc.addVertex(mat, x, y2, z2).setColor(r, g, b, a);

        vc.addVertex(mat, x, y2, z2).setColor(r, g, b, a);
        vc.addVertex(mat, x, y2, z).setColor(r, g, b, a);

        // Vertical edges (4 edges)
        vc.addVertex(mat, x, y, z).setColor(r, g, b, a);
        vc.addVertex(mat, x, y2, z).setColor(r, g, b, a);

        vc.addVertex(mat, x2, y, z).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z).setColor(r, g, b, a);

        vc.addVertex(mat, x2, y, z2).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);

        vc.addVertex(mat, x, y, z2).setColor(r, g, b, a);
        vc.addVertex(mat, x, y2, z2).setColor(r, g, b, a);
    }
}
