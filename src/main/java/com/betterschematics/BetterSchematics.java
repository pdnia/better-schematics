package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.gui.SchematicScreen;
import com.betterschematics.render.SchematicRenderer;
import com.betterschematics.schematic.SchematicManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(BetterSchematics.MODID)
public class BetterSchematics {
    public static final String MODID = "betterschematics";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static BetterSchematics instance;
    private final SchematicManager schematicManager;
    private final SchematicRenderer renderer;

    public BetterSchematics() {
        instance = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BetterSchematicsConfig.SPEC);
        this.schematicManager = new SchematicManager();
        this.renderer = new SchematicRenderer(schematicManager);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Better Schematics v0.3.0 loaded!");
    }

    public static BetterSchematics getInstance() { return instance; }
    public SchematicManager getSchematicManager() { return schematicManager; }
    public SchematicRenderer getRenderer() { return renderer; }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
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

    @SubscribeEvent
    public void onRenderWorld(net.minecraftforge.client.event.RenderLevelStageEvent event) {
        if (event.getStage() == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCACY_TRANSLUCANTBLOCKS) {
            renderer.render(event.getPoseStack(), event.getCamera(), event.getPartialTick());
        }
    }
}