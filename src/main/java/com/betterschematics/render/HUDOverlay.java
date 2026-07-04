package com.betterschematics.render;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class HUDOverlay {
    private final SchematicManager manager;

    public HUDOverlay(SchematicManager manager) { this.manager = manager; }

    public void render(GuiGraphics g, float partialTick) {
        if (!manager.hasSchematic() || Minecraft.getInstance().options.hideGui) return;
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        if (BetterSchematicsConfig.SHOW_MINIMAP.get()) renderMinimap(g, sw - 110, 10, 100, 100);
        renderProgressBar(g, sw / 2 - 100, 5, 200, 12);
        renderLayerInfo(g, 5, 5, 200, 20);
    }

    private void renderMinimap(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xA0000000);
        SchematicData d = manager.getActiveSchematic();
        if (d == null) return;
        g.fill(x, y, x + w, y + h, 0x80333333);
        g.drawString(Minecraft.getInstance().font, d.name, x + 5, y + 10, 0xFFFFFFFF);
    }

    private void renderProgressBar(GuiGraphics g, int x, int y, int w, int h) {
        ProgressTracker pt = manager.getProgressTracker();
        double pc = pt.getPercentComplete();
        g.fill(x, y, x + w, y + h, 0xA0000000);
        int pw = (int) Math.clamp((long)(w * pc / 100.0), 0, w - 2);
        g.fill(x + 1, y + 1, x + 1 + pw, y + h - 1, 0x8800FF00);
        g.drawString(Minecraft.getInstance().font, String.format("%.1f%%", pc), x + w / 2 - 10, y + -4, 0xFFFFFFFF);
    }

    private void renderLayerInfo(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + 22, 0xA0000000);
        g.drawString(Minecraft.getInstance().font,
            String.format("Layer: %d - %d", manager.getCurrentLayerMin(), manager.getCurrentLayerMax()),
            x + 5, y + 15, 0xFFFFFFFF);
    }
}