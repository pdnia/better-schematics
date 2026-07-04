package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

public class MinimapRenderer {
    private final SchematicManager manager;
    private static final int MAP_COLOR = 0x8D333333;

    public MinimapRenderer(SchematicManager manager) { this.manager = manager; }

    public void renderGui(GuiGraphics ctx, int sw, int sh, float partialTick) {
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;
        SchematicRegion region = schematic.getMainRegion();
        if (region == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int mapSize = 100;
        drawMinimap(ctx, sw - 110, 10, mapSize, region);
        drawPlayerDot(ctx, sw - 110, 10, mapSize, mc);
    }

    private void drawMinimap(GuiGraphics c, int x, int y, int s, SchematicRegion r) {
        BlockPos sz = r.size;
        float scale = Math.min((float)s / Math.max(sz.getX(), sz.getZ()), 1.0f);
        int rx = (int)(sz.getX() * scale);
        int rz = (int)(sz.getZ() * scale);
        int cx = x + (s - rx) / 2;
        int cy = y + (s - rz) / 2;
        c.fill(cx, cy, cx + rx, cy + rz, MAP_COLOR);
        c.drawString(Minecraft.getInstance().font, "S", cx + rx / 2 - 5, cy - 8, 0xFFFFFFFF);
    }

    private void drawPlayerDot(GuiGraphics c, int x, int y, int s, Minecraft mc) {
        BlockPos playerPos = mc.player.blockPosition();
        BlockPos worldPos = manager.inverseTransformPos(playerPos);
        if (worldPos != null) {
            c.fill(x + s / 2 - 2, y + s / 2 - 2, x + s / 2 + 2, y + s / 2 + 2, 0xffFF0000);
        }
    }
}
