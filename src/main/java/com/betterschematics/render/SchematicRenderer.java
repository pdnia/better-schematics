package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import java.util.OptionalDouble;

public class SchematicRenderer {
    private final SchematicManager manager;
    private boolean renderEnabled = true;

    // outline render type
    private static final RenderType LINES_TYPE = RenderType.create(
        "betterschematics:lines",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        256,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .createCompositeState(false)
    );

    // ghost block render type – translucent with no culling
    private static final RenderType GHOST_TYPE = RenderType.create(
        "betterschematics:ghost",
        DefaultVertexFormat.BLOCK,
        VertexFormat.Mode.QUADS,
        2097152,
        true, // affectsOutline
        true, // enableSorting
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
            .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setOverlayState(RenderStateShard.OVERLAY)
            .createCompositeState(false)
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

        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(camera.rotation());
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();

        SchematicRegion region = data.getMainRegion();
        if (region == null) return;
        BlockPos size = region.size;

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockPos origin = manager.getPlacementOrigin();

        // === RENDER BOUNDING BOX ===
        BlockPos endPos = manager.transformPos(new BlockPos(size.getX()-1, size.getY()-1, size.getZ()-1));
        int minX = Math.min(origin.getX(), endPos.getX());
        int minY = Math.min(origin.getY(), endPos.getY());
        int minZ = Math.min(origin.getZ(), endPos.getZ());
        int maxX = Math.max(origin.getX(), endPos.getX())+1;
        int maxY = Math.max(origin.getY(), endPos.getY())+1;
        int maxZ = Math.max(origin.getZ(), endPos.getZ())+1;
        VertexConsumer outlineVc = buffers.getBuffer(LINES_TYPE);
        addWireframeBox(outlineVc, mat, minX, minY, minZ, maxX, maxY, maxZ, 1f, 1f, 0.5f, 0.5f);
        buffers.endBatch(LINES_TYPE);

        // === RENDER GHOST BLOCKS ===
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        SchematicGhostBufferSource ghostSource = new SchematicGhostBufferSource(
            buffers, poseStack, 0.35f, 0.5f, 1.0f, 0.45f
        );

        int renderedCount = 0;
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = region.getBlockState(local);
                    if (expected == null || expected.isAir()) continue;
                    BlockPos worldPos = manager.transformPos(local);
                    BlockState actual = mc.level.getBlockState(worldPos);
                    if (expected.equals(actual)) continue;

                    poseStack.pushPose();
                    poseStack.translate(worldPos.getX(), worldPos.getY(), worldPos.getZ());

                    blockRenderer.renderSingleBlock(
                        expected,
                        poseStack,
                        ghostSource,
                        LightTexture.FULL_BRIGHT,
                        LightTexture.FULL_BRIGHT_PACKED,
                        ModelData.EMPTY
                    );

                    poseStack.popPose();
                    renderedCount++;
                }
            }
        }
        ghostSource.flush();
    }

    // === GHOST MULTI-BUFFER SOURCE ===
    private static class SchematicGhostBufferSource implements MultiBufferSource {
        private final MultiBufferSource.BufferSource parent;
        private final PoseStack poseStack;
        private final float rMul, gMul, bMul, aMul;

        SchematicGhostBufferSource(MultiBufferSource.BufferSource parent, PoseStack poseStack,
                                   float rMul, float gMul, float bMul, float aMul) {
            this.parent = parent;
            this.poseStack = poseStack;
            this.rMul = rMul; this.gMul = gMul; this.bMul = bMul; this.aMul = aMul;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            VertexConsumer original = parent.getBuffer(GHOST_TYPE);
            return new TintingVertexConsumer(original, rMul, gMul, bMul, aMul);
        }

        void flush() {
            parent.endBatch(GHOST_TYPE);
        }
    }

    // === TINTING VERTEX CONSUMER ===
    private static class TintingVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float rMul, gMul, bMul, aMul;

        TintingVertexConsumer(VertexConsumer delegate, float rMul, float gMul, float bMul, float aMul) {
            this.delegate = delegate;
            this.rMul = rMul; this.gMul = gMul; this.bMul = bMul; this.aMul = aMul;
        }

        @Override
        public VertexConsumer addVertex(Matrix4f matrix, float x, float y, float z) {
            return delegate.addVertex(matrix, x, y, z);
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return delegate.setColor(
                (int)(r * rMul),
                (int)(g * gMul),
                (int)(b * bMul),
                (int)(a * aMul)
            );
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return delegate.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return delegate.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return delegate.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(org.joml.Matrix3f matrix, float x, float y, float z) {
            return delegate.setNormal(matrix, x, y, z);
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            delegate.defaultColor(
                (int)(r * rMul),
                (int)(g * gMul),
                (int)(b * bMul),
                (int)(a * aMul)
            );
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }

    // === WIREFRAME HELPERS ===
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
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
    }
}
