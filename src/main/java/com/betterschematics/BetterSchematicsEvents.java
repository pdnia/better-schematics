package com.betterschematics;

import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

/*j
 * Event handlers registered manually via MinecraftForge.EVENT_BUS.register()
 * in BetterSchematics constructor.
 */
public class BetterSchematicsEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // periodic tasks
    }

    @SubscribeEvent
    public static void onRenderOverlay(CustomizeGuiOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
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