package com.betterschematics.render;

import com.betterschematics.BetterSchematics;
import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/**
 * Renders the HUD overlay: minimap, progress bar, material list, layer info, next block indicator.
 * Called via RegisterGuiOverlaysEvent to render after the chat panel.
 */
public class HUDOverlay {

    private final SchematicManager manager;

    public HUDOverlay(SchematicManager manager) {
        this.manager = manager;
    }

    public void render(GuiGraphics graphics, float partialTick) {
        if (!manager.hasSchematic() || Minecraft.getInstance().options.hideGui) return;

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        if (BetterSchematicsConfig.SHOW_MINIMAP.get()) {
            renderMinimap(graphics, screenWidth - 110, 10, 100, 100);
        }

        renderProgressBar(graphics, screenWidth / 2 - 100, 5, 200, 12);

        if (BetterSchematicsConfig.SHOW_MATERIAL_LIST.get()) {
            renderMaterialList(graphics, 5, 30, 180);
        }

        if (manager.isLayerMode()) {
            renderLayerInfo(graphics, screenWidth - 110, 120);
        }

        BlockPos next = manager.getNextBlockTarget();
        if (next != null) {
            String text = "Next: " + next.getX() + ", " + next.getY() + ", " + next.getZ();
            graphics.drawCenteredString(Minecraft.getInstance().font, text, screenWidth / 2, screenHeight - 20, 0xFFFFAA00);
        }
    }

    private void renderMinimap(GuiGraphics graphics, int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        var data = manager.getSchematic();
        var region = data.getMainRegion();
        if (region == null) return;

        float scaleX = (float) w / Math.max(1, region.width);
        float scaleZ = (float) h / Math.max(1, region.length);
        float scale = Math.min(scaleX, scaleZ);
        
        int plotW = (int) (region.width * scale);
        int plotH = (int) (region.length * scale);
        int offsetX = x + (w - plotW) / 2;
        int offsetY = y + (h - plotH) / 2;

        graphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x88000000);
        graphics.fill(offsetX, offsetY, offsetX + plotW, offsetY + plotH, 0x44222222);

        for (int bz = 0; bz < region.length; bz++) {
            for (int bx = 0; bx < region.width; bx++) {
                int drawX = offsetX + (int) (bx * scale);
                int drawY = offsetY + (int) (bz * scale);
                int cellW = Math.max(1, (int) scale);
                int cellH = Math.max(1, (int) scale);

                boolean hasBlock = false;
                for (int by = 0; by < Math.min(region.height, 16); by++) {
                    var state = region.getBlock(bx, by, bz);
                    if (state != null && !state.isAir()) {
                        hasBlock = true;
                        break;
                    }
                }
                
                if (hasBlock) {
                    boolean allCorrect = true;
                    boolean anyPresent = false;
                    for (int by = 0; by < Math.min(region.height, 16); by++) {
                        var expected = region.getBlock(bx, by, bz);
                        if (expected == null || expected.isAir()) continue;
                        anyPresent = true;
                        BlockPos wp = manager.schematicToWorld(bx, by, bz);
                        if (!mc.level.getBlockState(wp).equals(expected)) {
                            allCorrect = false;
                            break;
                        }
                    }
                    int color = anyPresent ? (allCorrect ? 0xFF00FF00 : 0xFFFF0000) : 0xFF888888;
                    graphics.fill(drawX, drawY, drawX + cellW, drawY + cellH, color);
                }
            }
        }

        BlockPos pPos = player.blockPosition();
        BlockPos origin = manager.getPlacementOrigin();
        int pX = pPos.getX() - origin.getX();
        int pZ = pPos.getZ() - origin.getZ();
        
        int rx, rz;
        switch (manager.getRotation()) {
            case 1 -> { rx = region.width - 1 - pZ; rz = pX; }
            case 2 -> { rx = region.width - 1 - pX; rz = region.length - 1 - pZ; }
            case 3 -> { rx = pZ; rz = region.length - 1 - pX; }
            default -> { rx = pX; rz = pZ; }
        }
        if (manager.isMirrorX()) rx = region.width - 1 - rx;
        if (manager.isMirrorZ()) rz = region.length - 1 - rz;

        int dotX = offsetX + (int) (rx * scale);
        int dotY = offsetY + (int) (rz * scale);
        dotX = Mth.clamp(dotX, offsetX, offsetX + plotW - 2);
        dotY = Mth.clamp(dotY, offsetY, offsetY + plotH - 2);
        
        graphics.fill(dotX - 1, dotY - 1, dotX + 3, dotY + 3, 0xFFFFFFFF);
        graphics.fill(dotX, dotY, dotX + 2, dotY + 2, 0xFF00AAFF);

        graphics.drawString(mc.font, "Minimap", x + 2, y - 10, 0xFFFFFFFF);
    }

    private void renderProgressBar(GuiGraphics graphics, int x, int y, int w, int h) {
        double progress = manager.getOverallProgress();
        
        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF333333);
        graphics.fill(x, y, x + w, y + h, 0xFF000000);
        
        int fillW = (int) (w * progress / 100.0);
        int color = progress >= 100 ? 0xFF00AA00 : 0xFF00CC44;
        graphics.fill(x, y, x + fillW, y + h, color);
        
        String text = String.format("Progress: %.1f%%", progress);
        Minecraft mc = Minecraft.getInstance();
        graphics.drawCenteredString(mc.font, text, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    private void renderMaterialList(GuiGraphics graphics, int x, int y, int maxWidth) {
        Minecraft mc = Minecraft.getInstance();
        var materials = manager.getMaterialList();
        
        int lineY = y;
        int maxItems = 10;
        
        graphics.drawString(mc.font, "§lMaterial List:", x, lineY, 0xFFFFAA00);
        lineY += 12;
        
        var sorted = materials.values().stream()
                .filter(e -> e.totalNeeded > 0)
                .sorted((a, b) -> Integer.compare(b.getRemaining(), a.getRemaining()))
                .limit(maxItems)
                .toList();
        
        for (var entry : sorted) {
            int remaining = entry.getRemaining();
            int color = remaining == 0 ? 0xFF00FF00 : 0xFFCCCCCC;
            String line = String.format("%s: %d/%d", entry.displayName, entry.placedCorrectly, entry.totalNeeded);
            
            if (mc.font.width(line) > maxWidth) {
                line = mc.font.plainSubstrByWidth(line, maxWidth - 3) + "...";
            }
            
            graphics.drawString(mc.font, line, x, lineY, color);
            lineY += 10;
        }
        
        if (materials.size() > maxItems) {
            graphics.drawString(mc.font, "... +" + (materials.size() - maxItems) + " more", x, lineY, 0xFF888888);
        }
    }

    private void renderLayerInfo(GuiGraphics graphics, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        
        graphics.fill(x - 2, y - 2, x + 102, y + 40, 0x88000000);
        graphics.drawString(mc.font, "§eLayer Mode: ON", x, y, 0xFFFFFF00);
        graphics.drawString(mc.font, "Layer " + manager.getCurrentLayerMin() + "-" + manager.getCurrentLayerMax(), x, y + 12, 0xFFFFFFFF);
        graphics.drawString(mc.font, "Stralki gora/dol", x, y + 24, 0xFF888888);
    }
}