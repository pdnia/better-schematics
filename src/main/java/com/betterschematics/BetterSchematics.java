package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.gui.SchematicScreen;
import com.betterschematics.render.HUDOverlay;
import com.betterschematics.render.SchematicRenderer;
import com.betterschematics.schematic.SchematicManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod("betterschematics")
public class BetterSchematics {
    public static final String MODID = "betterschematics";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static BetterSchematics instance;
    private final SchematicManager schematicManager;
    private final SchematicRenderer renderer;
    private final HUDOverlay hudOverlay;

    public BetterSchematics(IEventBus modEventBus) {
        instance = this;
        this.schematicManager = new SchematicManager();
        this.renderer = new SchematicRenderer(schematicManager);
        this.hudOverlay = new HUDOverlay(schematicManager);

        // Register key mappings on mod event bus
        modEventBus.addListener(BetterSchematicsConfig::registerKeys);

        // Register forge event listeners
        InputEvent.Key.BUS.addListener(this::onKeyInput);
        TickEvent.RenderTickEvent.Post.BUS.addListener(this::onRenderTick);
    }

    public static BetterSchematics getInstance() { return instance; }
    public SchematicManager getSchematicManager() { return schematicManager; }
    public SchematicRenderer getRenderer() { return renderer; }
    public HUDOverlay getHudOverlay() { return hudOverlay; }

    private void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().player == null) return;
        while (BetterSchematicsConfig.openGuiKey.consumeClick()) {
            Minecraft.getInstance().setScreen(new SchematicScreen());
        }
        while (BetterSchematicsConfig.executePlaceKey.consumeClick()) {
            schematicManager.placeNextBlock();
        }
        while (BetterSchematicsConfig.toggleRenderKey.consumeClick()) {
            renderer.toggleRender();
        }
        while (BetterSchematicsConfig.layerUpKey.consumeClick()) {
            schematicManager.shiftLayerUp();
        }
        while (BetterSchematicsConfig.layerDownKey.consumeClick()) {
            schematicManager.shiftLayerDown();
        }
    }

    private void onRenderTick(TickEvent.RenderTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        renderer.render(camera, event.timer());
    }
}
