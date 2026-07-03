package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.render.HUDOverlay;
import com.betterschematics.render.SchematicRenderer;
import com.betterschematics.schematic.SchematicManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.dist.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BetterSchematics.MODID)
public class BetterSchematics {
    public static final String MODID = "betterschematics";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static BetterSchematics instance;
    private final SchematicManager schematicManager;
    private final SchematicRenderer renderer;
    private final HUDOverlay hudOverlay;

    public BetterSchematics(FMLJavaModLoadingContext context) {
        instance = this;
        var modBusGroup = context.getModBusGroup();
        context.registerConfig(ModConfig.Type.CLIENT, BetterSchematicsConfig.SPEC);

        this.schematicManager = new SchematicManager();
        this.renderer = new SchematicRenderer(schematicManager);
        this.hudOverlay = new HUDOverlay(schematicManager);

        FMLClientSetupEvent.getBus(modBusGroup).addListener(event -> LOGGER.info("Better Schematics v0.3.0 loaded!"));
        MinecraftForge.EVENT_BUS.addListener(BetterSchematics::onKeyInput);
        MinecraftForge.EVENT_BUS.addListener(BetterSchematics::onRenderWorldLast);
    }

    public static BetterSchematics getInstance() { return instance; }
    public SchematicManager getSchematicManager() { return schematicManager; }
    public SchematicRenderer getRenderer() { return renderer; }
    public HUDOverlay getHudOverlay() { return hudOverlay; }

    private static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().player == null) return;
        while (BetterSchematicsConfig.openGuiKey.consumeClick()) {
            // TODO: Minecraft.getInstance().setScreen(new SchematicScreen());
        }
        while (BetterSchematicsConfig.executePlaceKey.consumeClick()) {
            getInstance().schematicManager.placeNextBlock();
        }
        while (BetterSchematicsConfig.toggleRenderKey.consumeClick()) {
            getInstance().renderer.toggleRender();
        }
        while (BetterSchematicsConfig.layerUpKey.consumeClick()) {
            getInstance().schematicManager.shiftLayerUp();
        }
        while (BetterSchematicsConfig.layerDownKey.consumeClick()) {
            getInstance().schematicManager.shiftLayerDown();
        }
    }

    private static void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT) {
            getInstance().renderer.render(event.getPoseStack(), event.getCamera(), event.getPartialTick());
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "betterschematics_hud",
                    (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
                        BetterSchematics mod = getInstance();
                        if (mod != null) {
                            mod.getHudOverlay().render(gui, partialTick.getRealtimeDeltaTicks());
                        }
                    });
        }
    }
}