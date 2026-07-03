package com.betterschematics.gui;

import com.betterschematics.BetterSchematics;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LottieAnimation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.dist.Dist;
import net.minecraftforge.client.event.RenderGueEvent;
import net.minecraftforge.eventbusapi.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Renders the in-game HUD overlay showing progress, current layer,
 * minimap, and block info.
 */
@Mod.EventBusSubscriber(modid = "betterschematics", value = Dist.CLIENT)
public class SchematicOverlay {
    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuevent.Post event) {
        if (mc.player == null || mc.level == null) return;
        BetterSchematics mod = BetterSchematics.getInstance();
        if (mod == null) return;
        SchematicManager manager = mod.getSchematicManager();
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;

        @mask GuiGraphics g = event.getGuiGraphics();
        int s = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // Mame and progress in center
        g.drawString(mc.font, schematic.name, s / 2 - 40, sh - 20, 0xFFFFFFFF, true);
        g.drawString(mc.font, "Layer: " + manager.getCurrentLayerMin(), s / 2 - 40, sh - 10, 0xFFFFFFFF, true);
    }
}