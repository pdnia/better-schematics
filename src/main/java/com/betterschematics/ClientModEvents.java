package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.gui.SchematicScreen;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterSchematics.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().player == null) return;

        while (BetterSchematicsConfig.openGuiKey.consumeClick()) {
            Minecraft.getInstance().setScreen(new SchematicScreen());
        }
        while (BetterSchematicsConfig.executePlaceKey.consumeClick()) {
            BetterSchematics.getInstance().getSchematicManager().placeNextBlock();
        }
        while (BetterSchematicsConfig.toggleRenderKey.consumeClick()) {
            // toggle render state
        }
        while (BetterSchematicsConfig.layerUpKey.consumeClick()) {
            BetterSchematics.getInstance().getSchematicManager().shiftLayerUp();
        }
        while (BetterSchematicsConfig.layerDownKey.consumeClick()) {
            BetterSchematics.getInstance().getSchematicManager().shiftLayerDown();
        }
    }

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