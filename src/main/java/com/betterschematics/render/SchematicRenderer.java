package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
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

    public void render(Matrix4f poseMatrix, Matrix4f projMatrix, Camera camera, float partialTick) {
        if (!renderEnabled) return;
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        SchematicRegion region = data.getMainRegion();
        if (region == null) return;

        Vec3 camPos = camera.getPosition();
        Matrix4f mat = new Matrix4f(poseMatrix);
        mat.translate((float)-camPos.x, (float)-camPos.y, (float)-camPos.z);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = new BufferBuilder(65536);
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

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
                    renderWireframeBox(buf, mat, worldPos, c);
                    count++;
                }
            }
        }

        var mesh = buf.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
        RenderSystem.disableDepthTest();
    }

    private void renderWireframeBox(BufferBuilder buf, Matrix4f mat, BlockPos pos, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        float x2 = x + 1, y2 = y + 1, z2 = z + 1;

        addVertex(buf, mat, x, y, z, r, g, b, a);
        addVertex(buf, mat, x2, y, z, r, g, b, a);
        addVertex(buf, mat, x2, y, z, r, g, b, a);
        addVertex(buf, mat, x2, y, z2, r, g, b, a);
        addVertex(buf, mat, x2, y, z2, r, g, b, a);
        addVertex(buf, mat, x, y, z2, r, g, b, a);
        addVertex(buf, mat, x, y, z2, r, g, b, a);
        addVertex(buf, mat, x, y, z, r, g, b, a);
        addVertex(buf, mat, x, y2, z, r, g, b, a);
        addVertex(buf, mat, x2, y2, z, r, g, b, a);
        addVertex(buf, mat, x2, y2, z, r, g, b, a);
        addVertex(buf, mat, x2, y2, z2, r, g, b, a);
        addVertex(buf, mat, x2, y2, z2, r, g, b, a);
        addVertex(buf, mat, x, y2, z2, r, g, b, a);
        addVertex(buf, mat, x, y2, z2, r, g, b, a);
        addVertex(buf, mat, x, y2, z, r, g, b, a);
        addVertex(buf, mat, x, y, z, r, g, b, a);
        addVertex(buf, mat, x, y2, z, r, g, b, a);
        addVertex(buf, mat, x2, y, z, r, g, b, a);
        addVertex(buf, mat, x2, y2, z, r, g, b, a);
        addVertex(buf, mat, x2, y, z2, r, g, b, a);
        addVertex(buf, mat, x2, y2, z2, r, g, b, a);
        addVertex(buf, mat, x, y, z2, r, g, b, a);
        addVertex(buf, mat, x, y2, z2, r, g, b, a);
    }

    private void addVertex(BufferBuilder buf, Matrix4f mat, float x, float y, float z, float r, float g, float b, float a) {
        buf.addVertex(mat, x, y, z).setColor(r, g, b, a);
    }
}