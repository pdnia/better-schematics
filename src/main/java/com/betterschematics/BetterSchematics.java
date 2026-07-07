package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.gui.SchematicScreen;
import com.betterschematics.render.HUDOverlay;
import com.betterschematics.render.SchematicRenderer;
import com.betterschematics.schematic.SchematicManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
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

    public BetterSchematics() {
        instance = this;
        this.schematicManager = new SchematicManager();
        this.renderer = new SchematicRenderer(schematicManager);
        this.hudOverlay = new HUDOverlay(schematicManager);

        RegisterKeyMappingsEvent.BUS.addListener(BetterSchematicsConfig::registerKeys);
        InputEvent.Key.BUS.addListener(this::onKeyInput);
        TickEvent.RenderTickEvent.Pre.BUS.addListener(this::onRenderTick);
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

    private void onRenderTick(TickEvent.RenderTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        renderer.render(mc.gameRenderer.getMainCamera());
    }
}