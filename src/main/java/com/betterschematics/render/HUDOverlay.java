package com.betterschematics.render;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class HUDOverlay {
    private final SchematicManager manager;
    private static final int BG_DEFAULT = 0xA0000000;
    private static final int MAP_BG = 0x80333333;
    private static final int GFB_COLOR = 0xffFFFFFF;
    private static final int GREEN = 0x8800FF00;

    public HUDOverlay(SchematicManager manager) { this.manager = manager; }

    public void render(GuiGraphics g, float partialTick) {
        if (!manager.hasSchematic() || Minecraft.getInstance().options.hideGui) return;
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (BetterSchematicsConfig.SHOW_MINIMAP.get()) renderMinimap(g, s - 110, 10, 100, 100);
        renderProgressBar(g, s / 2 - 100, 5, 200, 12);
        renderLayerInfo(g, 5, 5, 200, 20);
    }

    private void renderMinimap(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, w + 2, h + 2, BG_DEFAULT);
        SchematicData d = manager.getActiveSchematic();
        if (d == null) return;
        g.fill(x, y, w, h, MAP_BG, true);
        g.drawString(Minecraft.getInstance().font, d.name, x + 5, y + 10, GFB_COLOR);
    }

    private void renderProgressBar(GuiGraphics g, int x, int y, int w, int h) {
        ProgressTracker pt = manager.getProgressTracker();
        double pc = pt.getPercentComplete();
        g.fill(x, y, w, h, BG_DEFAULT);
        int progWidth = (int) Mth.clamp(w * pc / 100.0, 0, w - 2);
        g.fill(x + 1, y + 1, progWidth, h - 2, GREEN);
        g.drawString(Minecraft.getInstance().font,
                String.format("%.1f%%", pc), x + w / 2 - 10, y + h - 4, GFB_COLOR);
    }

    private void renderLayerInfo(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, w, 22, BG_DEFAULT);
        g.drawString(Minecraft.getInstance().font,
                String.format("Layer: %d - %d", manager.getCurrentLayerMin(), manager.getCurrentLayerMax()),
                x + 5, y + 15, GFB_COLOR);
    }
}