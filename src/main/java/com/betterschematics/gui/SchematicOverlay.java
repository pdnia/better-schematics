package com.betterschematics.gui;

import com.betterschematics.BetterSchematics;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = BetterSchematics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SchematicOverlay {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void onRenderOverlay(CustomizeGuiOverlayEvent event) {
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