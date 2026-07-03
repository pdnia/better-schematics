package com.betterschematics.render;

import com.betterschematics.BetterSchematics;
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
import net.minecraft.world.phys.AABB;
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

    public SchematicRenderer(SchematicManager manager) {
        this.manager = manager;
    }

    public void toggleRender() { this.renderEnabled = !this.renderEnabled; }
    public boolean isRenderEnabled() { return renderEnabled; }

    public void render(PoseStack poseStack, Camera camera, float partialTick) {
        if (!renderEnabled || !manager.hasSchematic()) return;

        Minecraft mc = Minecraft.getInstance();
        SchematicData data = manager.getSchematic();
        if (data == null || mc.level == null) return;

        var region = data.getMainRegion();
        if (region == null || region.blocks == null) return;

        Vec3 camPos = camera.getPosition();

        double maxDist = BetterSchematicsConfig.RENDER_DISTANCE.get();
        double maxDistSq = maxDist * maxDist;

        int renderCount = 0;
        int maxBlocksToRender = 20000;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.BLOCK);

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        Matrix4f mat = poseStack.last().pose();
        float alpha = (float) BetterSchematicsConfig.RENDER_ALPHA.get();

        for (int y = 0; y < region.height && renderCount < maxBlocksToRender; y++) {
            if (manager.isLayerMode()) {
                if (y < manager.getCurrentLayerMin() || y > manager.getCurrentLayerMax()) continue;
            }
            for (int z = 0; z < region.length && renderCount < maxBlocksToRender; z++) {
                for (int x = 0; x < region.width && renderCount < maxBlocksToRender; x++) {
                    BlockState state = region.getBlock(x, y, z);
                    if (state == null || state.isAir()) continue;
                    BlockPos worldPos = manager.schematicToWorld(x, y, z);
                    if (worldPos.distToLowCornerSqr(camPos.x, camPos.y, camPos.z) > maxDistSq) continue;
                    if (!camera.isInitialized()) continue;

                    BlockState actual = mc.level.getBlockState(worldPos);
                    boolean correct = state.equals(actual);
                    int color = correct ? 0x4400FF00 : 0x44FF0000;
                    renderGhostBlock(builder, poseStack.last(), worldPos, state, color, alpha);
                    renderCount++;
                }
            }
        }
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tesselator.end();
        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        renderNextBlockIndicator(poseStack, camera);
    }

    private void renderGhostBlock(BufferBuilder builder, PoseStack.Pose pose, BlockPos pos, BlockState state, int colorArgb, float alpha) {
        float r = ((colorArgb >> 16) & 0xFF) / 255f;
        float g = ((colorArgb >> 8) & 0xFF) / 255f;
        float b = (colorArgb & 0xFF) / 255f;
        float a = ((colorArgb >> 24) & 0xFF) / 255f * alpha;

        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float x2 = x + 1;
        float y2 = y + 1;
        float z2 = z + 1;

        Matrix4f mat = pose.pose();

        // Bottom face
        builder.addVertex(mat, x, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x, y, z2).setColor(r, g, b, a);
        // Top face
        builder.addVertex(mat, x, y2, z).setColor(r, g, b, a);
        builder.addVertex(mat, x, y2, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x, y2, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z).setColor(r, g, b, a);
        // North face
        builder.addVertex(mat, x, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x, y2, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z).setColor(r, g, b, a);
        builder.addVertex(mat, x, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y, z).setColor(r, g, b, a);
        // South face
        builder.addVertex(mat, x, y, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x, y2, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x, y, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        // West face
        builder.addVertex(mat, x, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x, y, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x, y2, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x, y2, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x, y2, z).setColor(r, g, b, a);
        // East face
        builder.addVertex(mat, x2, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y, z2).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z).setColor(r, g, b, a);
        builder.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
    }

    private void renderNextBlockIndicator(PoseStack poseStack, Camera camera) {
        BlockPos target = manager.getNextBlockTarget();
        if (target == null) return;
        Vec3 camPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        double time = System.currentTimeMillis() / 500.0;
        float flashAlpha = (float) (0.5 + 0.5 * Math.sin(time * Math.PI));
        float r = 1.0f, g = 1.0f, b = 0.0f;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f mat = poseStack.last().pose();
        float x = target.getX();
        float y = target.getY();
        float z = target.getZ();
        float x2 = x + 1;
        float y2 = y + 1;
        float z2 = z + 1;

        // Wireframe cube
        builder.addVertex(mat, x, y, z).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x2, y, z).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x2, y, z2).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x, y, z2).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x, y, z).setColor(r, g, b, flashAlpha);
        // Top
        builder.addVertex(mat, x, y2, z).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x2, y2, z).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x2, y2, z2).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x, y2, z2).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x, y2, z).setColor(r, g, b, flashAlpha);
        // Vertical lines
        builder.addVertex(mat, x, y, z).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x, y2, z).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x2, y, z).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x2, y2, z).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x2, y, z2).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x2, y2, z2).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x, y, z2).setColor(r, g, b, flashAlpha);
        builder.addVertex(mat, x, y2, z2).setColor(r, g, b, flashAlpha);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(3.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tesselator.end();
        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }
}