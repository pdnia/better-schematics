package com.betterschematics.render;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renders schematic overlays in-world:
 * - Ghost blocks for the schematic
 * - Green/red overlays for correct/wrong blocks
 * - Next-block indicator
 */
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
        Tesselator t = Tesselator.getInstance();
        BufferBuilder bb = t.begin(VertexFormat.Mode.QUADS, VertexFormats.POSITION_COLOR);

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();

        SchematicRegion region = data.getMainRegion();
        if (region == null) { poseStack.popPose(); return; }

        BlockPos size = region.getSize();
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
                    boolean correct = expected.equals(actual);
                    renderGhostBlock(bb, mat, worldPos, correct ? 0x4400FF00 : 0x44FF0000, 0.4f);
                    count++;
                }
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawUpload(bb.build(), VertexFormats.POSITION_COLOR);
        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private void renderGhostBlock(BufferBuilder bb, Matrix4f mat, BlockPos pos, int color, float alpha) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f * alpha;
        float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;
        bb.vertex(mat, x, y2, z).color(r, g, b, a);
        bb.vertex(mat, x, y2, z2).color(r, g, b, a);
        bb.vertex(mat, x2, y2, z2).color(r, g, b, a);
        bb.vertex(mat, x, y2, z).color(r, g, b, a);
        bb.vertex(mat, x2, y2, z2).color(r, g, b, a);
        bb.vertex(mat, x2, y2, z).color(r, g, b, a);
    }
}