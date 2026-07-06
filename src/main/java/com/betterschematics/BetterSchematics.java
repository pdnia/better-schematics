package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.gui.SchematicScreen;
import com.betterschematics.render.HUDOverlay;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
        TickEvent.ClientTickEvent.Pre.BUS.addListener(event -> onClientTick());
        RegisterKeyMappingsEvent.BUS.addListener(BetterSchematicsConfig::registerKeys);

        AddGuiOverlayLayersEvent.BUS.addListener(event -> {
            var layers = event.getLayeredDraw();
            var modLayerName = ResourceLocation.fromNamespaceAndPath(MODID, "better_schematics_overlay");
            layers.addAbove(
                ResourceLocation.withDefaultNamespace("hotbar"),
                modLayerName,
                (ggx, dt) -> renderOverlay(ggx)
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

    private void onClientTick() { }

    private void renderOverlay(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        SchematicData schematic = schematicManager.getActiveSchematic();
        if (schematic == null) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        g.drawString(mc.font, "BetterSchematics", sw / 2 - 40, 10, 0xFFFFFFFF);
        g.drawString(mc.font, "Schematic: " + schematic.name, sw / 2 - 40, 20, 0xFFFFFFFF);
        g.drawString(mc.font, "Layer: " + schematicManager.getCurrentLayerMin(), sw / 2 - 40, 30, 0xFFFFFFFF);
    }

    public static BetterSchematics getInstance() { return instance; }
    public SchematicManager getSchematicManager() { return schematicManager; }
    public HUDOverlay getHudOverlay() { return hudOverlay; }
}