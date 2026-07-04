package com.betterschematics.gui;

import com.betterschematics.BetterSchematics;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "betterschematics", value = Dist.CLIENT)
public class SchematicOverlay {
    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if (mc.player == null || mc.level == null) return;
        BetterSchematics mod = BetterSchematics.getInstance();
        if (mod == null) return;
        SchematicManager manager = mod.getSchematicManager();
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;

        GuiGraphics g = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        g.drawString(mc.font, schematic.name, sw / 2 - 40, sh - 20, 0xFFFFFFFF);
        g.drawString(mc.font, "Layer: " + manager.getCurrentLayerMin(), sw / 2 - 40, sh - 10, 0xFFFFFFFF);
    }
}