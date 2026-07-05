package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.gui.SchematicScreen;
import com.betterschematics.render.HUDOverlay;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.GuiOverlayEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;

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

        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerClientEvents();
        }
    }

    private void registerClientEvents() {
        InputEvent.Key.BUS.addListener(this::onKeyInput);
        TickEvent.ClientTickEvent.PRE.BUS.addListener(event -> onClientTick());
        RegisterKeyMappingsEvent.BUS.addListener(BetterSchematicsConfig::registerKeys);

        AddGuiOverlayLayersEvent.BUS.addListener(event -> {
            var layers = event.getLayeredDraw();
            var modLayerName = Identifier.fromNamespaceAndPath(MODID, "better_schematics_overlay");
            layers.addAbove(
                Identifier.withDefaultNamespace("hotbar"),
                modLayerName,
                (ggx, dt) -> hudOverlay.render(ggx, dt)
            );
        });
    }

    private void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (BetterSchematicsConfig.openGuiKey != null && BetterSchematicsConfig.openGuiKey.consumeClick()) {
            mc.setScreen(new SchematicScreen());
        }
        while (BetterSchematicsConfig.executePlaceKey != null && BetterSchematicsConfig.executePlaceKey.consumeClick()) {
            schematicManager.placeNextBlock();
        }
        while (BetterSchematicsConfig.layerUpKey != null && BetterSchematicsConfig.layerUpKey.consumeClick()) {
            schematicManager.shiftLayerUp();
        }
        while (BetterSchematicsConfig.layerDownKey != null && BetterSchematicsConfig.layerDownKey.consumeClick()) {
            schematicManager.shiftLayerDown();
        }
    }

    private void onClientTick() {
    }

    public static BetterSchematics getInstance() { return instance; }
    public SchematicManager getSchematicManager() { return schematicManager; }
    public HUDOverlay getHudOverlay() { return hudOverlay; }
}