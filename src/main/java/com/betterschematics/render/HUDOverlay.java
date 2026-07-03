package com.betterschematics.render;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class HUDOverlay {
    private final SchematicManager manager;

    public HUDOverlay(SchematicManager manager) { this.manager = manager; }

    public void render(GuiGraphics g, float partialTick) {
        if (!manager.hasSchematic() || Minecraft.getInstance().options.hideGui) return;
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (BetterSchematicsConfig.SHOW_MINIMAP.get()) {
            renderMinimap(g, sw - 110, 10, 100, 100);
        }
        renderProgressBar(g, sw / 2 - 100, 5, 200, 12);
        renderLayerInfo(g, 5, 5, 200, 20);
    }

    private void renderMinimap(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, w + 2, h + 2, 0xA0000000);
        SchematicData data = manager.getActiveSchematic();
        if (data == null) return;
        g.fill(x, y, w, h, 0x80333333);
        g.drawString(Minecraft.getInstance().font, data.name, x + 5, y + 10, 0xFFFFFFFF, true);
    }

    private void renderProgressBar(GuiGraphics g, int x, int y, int w, int h) {
        ProgressTracker pt = manager.getProgressTracker();
        double pc = pt.getPercentComplete();
        g.fill(x, y, w, h, 0x1A0000000);
        g.fill(x + 1, y + 1, (int) Mth.clamp(w * pc / 100.0, 0, w - 2), h - 2, 0x8000FF000);
        g.drawString(Minecraft.getInstance().font,
                String.format("%.1f%%", pc), x + w / 2 - 10, y + h - 4, 0xFFFFFFFF, false);
    }

    private void renderLayerInfo(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, w, 22, 0x1A0000000);
        g.drawString(Minecraft.getInstance().font,
                String.format("Layer: %d - %d", manager.getCurrentLayerMin(), manager.getCurrentLayerMax()),
                x + 5, y + 15, 0xFFFFFFFF, false);
    }
}