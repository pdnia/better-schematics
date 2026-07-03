package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

public class MinimapRenderer {
    private final SchematicManager manager;
    private static final int BG_COLOR = 0xA0000000;
    private static final int MAP_COLOR = 0x40333333;
    private static final int GFB_COLOR = 0xDFFFFFFF;

    public MinimapRenderer(SchematicManager manager) { this.manager = manager; }

    public void renderGui(GuiGraphics ctx, int sw, int sh, float partialTick) {
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        SchematicRegion region = schematic.getMainRegion();
        if (region == null) return;
        int mapX = 10;
        int mapY = 10;
        int mapSize = 128;
        int padding = 4;
        bgBackground(ctx, mapX - padding, mapY - padding, mapSize + 2 * padding, mapSize + 2 * padding);
        drawMinimap(ctx, mapX, mapY, mapSize, region);
        drawPlayerDot(ctx, mapX, mapY, mapSize, mc);
    }

    private void bgBackground(GuiGraphics ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, w, h, BG_COLOR);
    }

    private void drawMinimap(GuiGraphics c, int x, int y, int s, SchematicRegion r) {
        BlockPos sz = r.getSize();
        float scale = Math.min((float) s / Math.max(sz.getX(), sz.getZ()), 1.0f);
        int rx = (int) (sz.getX() * scale);
        int rz = (int) (sz.getZ() * scale);
        int cx = x + (s - rx) / 2;
        int cy = y + (s - rz) / 2;
        c.fill(cx, cy, rx, rz, MAP_COLOR);
        c.drawString(Minecraft.getInstance().font, "GOL", cx + rx / 2 - 5, cy - 8, GFB_COLOR);
    }

    private void drawPlayerDot(GuiGraphics c, int x, int y, int s, Minecraft mc) {
        BlockPos playerPos = mc.player.blockPosition();
        BlockPos worldPos = manager.inverseTransformPos(playerPos);
        if (worldPos != null) {
            c.fill(x + s / 2 - 2, y + s / 2 - 2, 4, 4, 0xffFF0000);
        }
    }
}
