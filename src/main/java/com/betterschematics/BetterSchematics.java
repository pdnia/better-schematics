package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.gui.SchematicScreen;
import com.betterschematics.render.HUDOverlay;
import com.betterschematics.render.SchematicRenderer;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
@Mod("betterschematics")
public class BetterSchematics {
    public static final String MODID = "betterschematics";

    private static BetterSchematics instance;
    private final SchematicManager schematicManager;
    private final SchematicRenderer schematicRenderer;
    private final HUDOverlay hudOverlay;

    public BetterSchematics() {
        instance = this;
        this.schematicManager = new SchematicManager();
        this.schematicRenderer = new SchematicRenderer();
        this.hudOverlay = new HUDOverlay(schematicManager);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerClientEvents();
        }
    }

    private void registerClientEvents() {
        // Key input event
        InputEvent.Key.BUS.addListener(this::onKeyInput);

        // Client tick event
        TickEvent.ClientTickEvent.PRE.BUS.addListener(event -> onClientTick());

        // Register key bindings
        RegisterKeyMappingsEvent.BUS.addListener(BetterSchematicsConfig::registerKeys);

        // Register HUD overlay layer
        AddGuiOverlayLayersEvent.BUS.addListener(event -> {
            var layers = event.getLayeredDraw();
            var modLayerName = Identifier.fromNamespaceAndPath(MODID, "better_schematics_overlay");
            layers.addAbove(
                Identifier.withDefaultNamespace("hotbar"),
                modLayerName,
                (ggx, dt) -> renderOverlay(ggx)
            );
        });

        // Register 3D world render for schematic outline
        RenderLevelStageEvent.BUS.addListener(event => {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTSÒ_TRANSLUCENT_BLOCKS) {
                schematicRenderer.renderSchematicOutline(
                    schematicManager,
                    event.getProjectionMatrix(),
                    event.getCamera().pose()
                );
            }
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
        // periodic tasks
    }

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
    public SchematicRenderer getSchematicRenderer() { return schematicRenderer; }
    public HUDOverlay getHudOverlay() { return hudOverlay; }
}