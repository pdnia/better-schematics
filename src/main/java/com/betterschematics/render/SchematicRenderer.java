package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
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

    public void renderWireframe() {
        if (!renderEnabled) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        SchematicRegion region = data.getMainRegion();
        if (region == null) return;

        Vec3 camPos = mc.player.getEyePosition();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();

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
    }

    private void renderWireframeBox(VertexConsumer vc, Matrix4f mat, BlockPos pos, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;
        addLine(vc, mat, x, y, z, x2, y, z, r, g, b, a);
        addLine(vc, mat, x2, y, z, x2, y, z2, r, g, b, a);
        addLine(vc, mat, x2, y, z2, x, y, z2, r, g, b, a);
        addLine(vc, mat, x, y, z2, x, y, z, r, g, b, a);
        addLine(vc, mat, x, y2, z, x2, y2, z, r, g, b, a);
        addLine(vc, mat, x2, y2, z, x2, y2, z2, r, g, b, a);
        addLine(vc, mat, x2, y2, z2, x, y2, z2, r, g, b, a);
        addLine(vc, mat, x, y2, z2, x, y2, z, r, g, b, a);
        addLine(vc, mat, x, y, z, x, y2, z, r, g, b, a);
        addLine(vc, mat, x2, y, z, x2, y2, z, r, g, b, a);
        addLine(vc, mat, x2, y, z2, x2, y2, z2, r, g, b, a);
        addLine(vc, mat, x, y, z2, x, y2, z2, r, g, b, a);
    }

    private void addLine(VertexConsumer vc, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
    }
}