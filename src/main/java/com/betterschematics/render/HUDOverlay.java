package com.betterschematics.render;

import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

public class HUDOverlay {
    private final SchematicManager manager;

    public HUDOverlay(SchematicManager manager) { this.manager = manager; }

    public void render(GuiGraphics g) {
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int panelX = sw - 210;
        int panelY = 5;

        g.fill(panelX, panelY, sw - 5, sh - 5, 0xAA000000);

        g.drawString(mc.font, schematic.name, panelX + 5, panelY + 5, 0xFFFFAA00);
        panelY += 15;

        ProgressTracker pt = manager.getProgressTracker();
        double pc = pt.getPercentComplete();
        g.drawString(mc.font, String.format("Progress: %.1f%%", pc), panelX + 5, panelY + 2, 0xFFFFFFFF);
        int barW = 190;
        g.fill(panelX + 5, panelY + 14, panelX + 5 + barW, panelY + 24, 0xFF333333);
        int pw = (int) Math.clamp((long)(barW * pc / 100.0), 0, barW);
        g.fill(panelX + 5, panelY + 14, panelX + 5 + pw, panelY + 24, 0xFF00AA00);
        panelY += 28;

        BlockPos origin = manager.getPlacementOrigin();
        g.drawString(mc.font, "Origin: " + origin.getX() + ", " + origin.getY() + ", " + origin.getZ(), panelX + 5, panelY + 2, 0xFFFFAA00);
        panelY += 15;

        g.drawString(mc.font, "Layer: " + manager.getCurrentLayerMin() + " - " + manager.getCurrentLayerMax(), panelX + 5, panelY + 2, 0xFFAAFFFF);
        panelY += 15;

        var region = schematic.getMainRegion();
        if (region != null) {
            var size = region.size;
            g.drawString(mc.font, "Size: " + size.getX() + "x" + size.getY() + "x" + size.getZ(), panelX + 5, panelY + 2, 0xFFFFAA00);
            panelY += 15;
        }

        g.drawString(mc.font, "Rot: " + manager.getRotation() + "deg  MX: " + manager.isMirrorX() + "  MZ: " + manager.isMirrorZ(), panelX + 5, panelY + 2, 0xFF888888);
        panelY += 20;

        g.drawString(mc.font, "--- Materials needed ---", panelX + 5, panelY + 2, 0xFFAAFFFF);
        panelY += 15;

        String matList = manager.exportMaterialList();
        if (matList != null && !matList.isEmpty()) {
            String[] lines = matList.split("\n");
            int maxLines = (sh - panelY - 10) / 10;
            int count = 0;
            for (String line : lines) {
                if (count >= maxLines) break;
                if (line.trim().isEmpty()) continue;
                g.drawString(mc.font, line.trim(), panelX + 5, panelY, 0xFFAAAAAA);
                panelY += 10;
                count++;
            }
        }
    }
}
