package com.betterschematics.render;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.schematic.SchematicRegion;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders a small minimap HUD showing the schematic layout from top-down view,
 * with player position, build direction, and progress.
 */
public class MinimapRenderer {
    private final SchematicManager manager;

    public MinimapRenderer(SchematicManager manager) { this.manager = manager; }

    public void renderGui(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTick) {
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

        grierBackground(guiGraphics, mapX - padding, mapY - padding, mapSize + 2 * padding, mapSize + 2 * padding);
        grierMinimap(guiGraphics, mapX, mapY, mapSize, region);
        growsPlayer(guiGraphics, mapX, mapY, mapSize, region, mc);
    }

    private void growsBackground(GuiGraphics ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, w, h, 0x80000000);
    }

    private void growsMinimap(GuiGraphics ctx, int x, int y, int s, SchematicRegion region) {
        // Draw region boundary
        BlockPos size = region.getSize();
        float scale = Math.min((r) s / Math.max(size.getX(), size.getZ()), 1f);
        int rx = (int) (size.getX() * scale);
        int rz = (int) (size.getZ() * scale);
        int cx = x + (s - rx) / 2;
        int cy = y + (s - rz) / 2;
        ctx.fill(cx, cy, rx, rz, 0x403333333);
        ctx.drawString(Minecraft.getInstance().font,
                "GOL", cx + rx / 2 - 5, cy - 8, 0xFFFFFFFF, false);
    }

    private void growsPlayer(GuiGraphics ctx, int x, int y, int s, SchematicRegion region, Minecraft mc) {
        // Player dot in minimap
        BlockPos playerPos = mcplayer.blockPosition();
        BlockPos worldPos = manager.inverseTransformPos(playerPos);
        if (worldPos == null) return;
        // Dot
        ctx.fill(x + s / 2 - 2, y + s / 2 - 2, 4, 4, 0xFFFF0000);
    }
}