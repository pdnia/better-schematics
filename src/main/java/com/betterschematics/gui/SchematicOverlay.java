package com.betterschematics.gui;

import com.betterschematics.BetterSchematics;
import com.betterschematics.render.MinimapRenderer;
import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiLayerEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Renders the in-game HUD overlay showing progress, current layer, minimap, and block info.
 */
@Mod.EventBusSubscriber(modid = "betterschematics", value = Dist.CLIENT)
public class SchematicOverlay {

    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        if (mc.player == null || mc.level == null) return;
        
        if (event.getName() != VanillaGuiOverlay.CHAT_PANEL.id()) return;
        
        BetterSchematics mod = BetterSchematics.getInstance();
        if (mod == null) return;
        
        SchematicManager manager = mod.getSchematicManager();
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        renderProgressBar(guiGraphics, manager, screenWidth, screenHeight);
        renderBlockInfo(guiGraphics, manager, screenWidth, screenHeight);
        renderLayerInfo(guiGraphics, manager, screenWidth, screenHeight);
        
        mod.getRenderer().getMinimapRenderer().renderGui(guiGraphics, screenWidth, screenHeight, 
            event.getPartialTick().getGameTimeDeltaTicks());
    }

    private static void renderProgressBar(GuiGraphics guiGraphics, SchematicManager manager,
                                       int screenWidth, int screenHeight) {
        ProgressTracker tracker = manager.getProgressTracker();
        if (tracker == null) return;

        int barWidth = 200;
        int barHeight = 14;
        int barX = screenWidth / 2 - barWidth / 2;
        int barY = screenHeight - 8 - 30;

        String title = manager.getActiveSchematic().getName();
        String progress = tracker.getPercentString();
        String blockCount = tracker.getCorrectBlocks() + " / " + tracker.getTotalBlocks();
        
        guiGraphics.drawCenteredString(mc.font, title, screenWidth / 2, barY - 12, 0xFFFFFF);
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA000000);
        
        int fillWidth = (int)(barWidth * tracker.getPercentComplete() / 100.0);
        int color = tracker.getPercentComplete() >= 100 ? 0xFF00FF00 : 0xFF4488FF;
        guiGraphics.fill(barX + 1, barY + 1, barX + fillWidth - 1, barY + barHeight - 1, color);
        
        if (tracker.getWrongBlocks() > 0 && tracker.getTotalBlocks() > 0) {
            int wrongWidth = (int)(barWidth * tracker.getWrongBlocks() / (double) tracker.getTotalBlocks());
            int wrongStart = barX + fillWidth;
            guiGraphics.fill(wrongStart, barY + 1, wrongStart + wrongWidth, barY + barHeight - 1, 0x88FF4444);
        }
        
        String displayText = progress + "  |  " + blockCount;
        guiGraphics.drawCenteredString(mc.font, displayText, screenWidth / 2, barY + 3, 0xFFFFFF);
        
        if (tracker.getWrongBlocks() > 0) {
            String wrongText = tracker.getWrongBlocks() + " wrong";
            guiGraphics.drawString(mc.font, wrongText, barX + barWidth + 4, barY + 3, 0xFFFF4444);
        }
    }

    private static void renderBlockInfo(GuiGraphics guiGraphics, SchematicManager manager,
                                     int screenWidth, int screenHeight) {
        BlockPos next = manager.getNextBlockPos();
        if (next == null) return;
        
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;
        
        var expected = schematic.getBlockStateAtWorld(next);
        if (expected == null) return;
        
        String blockName = expected.getBlock().getName().getString();
        String posText = "Next: " + blockName + " @ " + next.getX() + ", " + next.getY() + ", " + next.getZ();
        
        int textX = screenWidth / 2 - mc.font.width(posText) / 2;
        int textY = screenHeight - 8 - 50;
        
        guiGraphics.fill(textX - 2, textY - 1, textX + mc.font.width(posText) + 2, 
                        textY + mc.font.lineHeight + 1, 0xAA000000);
        guiGraphics.drawString(mc.font, posText, textX, textY, 0xFFFFFF00);
    }

    private static void renderLayerInfo(GuiGraphics guiGraphics, SchematicManager manager,
                                       int screenWidth, int screenHeight) {
        if (!manager.isLayerModeEnabled()) return;
        
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;
        
        String layerText = "Layer " + (manager.getCurrentLayerMin() + 1) + 
                          " - " + (manager.getCurrentLayerMax() + 1) + 
                          " / " + schematic.getWorldSize().getY();
        
        int textX = screenWidth / 2 - mc.font.width(layerText) / 2;
        int textY = 5;
        
        guiGraphics.fill(textX - 4, textY,  textX + mc.font.width(layerText) + 4, 
                        textY + mc.font.lineHeight + 4, 0xCC000000);
        guiGraphics.drawString(mc.font, layerText, textX, textY + 2, 0xFF00FFFF);
    }
}