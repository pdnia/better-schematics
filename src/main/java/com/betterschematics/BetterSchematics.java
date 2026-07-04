package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.gui.SchematicScreen;
import com.betterschematics.render.HudOverlay;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("betterschematics")
public class BetterSchematics {
    public static final String MODID = "betterschematics";

    private static BetterSchematics instance;
    private final SchematicManager schematicManager;
    private final HUDOverlay hudOverlay;

    public BetterSchematics() {
        instance = this;
        this.schematicManager = new SchematicManager();
        this.hudOverlay = new HUDOverlay(schematicManager);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static BetterSchematics getInstance() { return instance; }
    public SchematicManager getSchematicManager() { return schematicManager; }
    public HUDOverlay getHudOverlay() { return hudOverlay; }

    public void onKeyInput(net.minecraftforge.client.event.InputEvent.Key event) {
        if (Minecraft.getInstance().player == null) return;
        while (BetterSchematicsConfig.openGuiKey.consumeClick()) {
            Minecraft.getInstance().setScreen(new SchematicScreen());
        }
        while (BetterSchematicsConfig.executePlaceKey.consumeClick()) {
            schematicManager.placeNextBlock();
        }
        while (BetterSchematicsConfig.toggleRenderKey.consumeClick()) {
            // toggle render state
        }
        while (BetterSchematicsConfig.layerUpKey.consumeClick()) {
            schematicManager.shiftLayerUp();
        }
        while (BetterSchematicsConfig.layerDownKey.consumeClick()) {
            schematicManager.shiftLayerDown();
        }
    }

    public void onClientTick(TickEvent.ClientTickEvent event) {
        // periodic tasks
    }
}