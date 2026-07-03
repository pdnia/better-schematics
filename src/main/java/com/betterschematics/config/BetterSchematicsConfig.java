package com.betterschematics.config;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "betterschematics")
public class BetterSchematicsConfig {
    public static final KeyMapping openGuiKey = new KeyMapping("Key.openGui.BetterSchematics", GLFW.KEY_G, "Key.BetterSchematics.Group");
    public static final KeyMapping executePlaceKey = new KeyMapping("Key.place.BetterSchematics", GLFW.KEY_P, "Key.BetterSchematics.Group");
    public static final KeyMapping toggleRenderKey = new KeyMapping("Key.toggleRender.BetterSchematics", GLFW.KEY_R, "Key.BetterSchematics.Group");
    public static final KeyMapping layerUpKey = new KeyMapping("Key.layerUp.BetterSchematics", GLFW.KEY_UP, "Key.BetterSchematics.Group");
    public static final KeyMapping layerDownKey = new KeyMapping("Key.layerDown.BetterSchematics", GLFW.KEY_DOWN, "Key.BetterSchematics.Group");
    public static final ForgeConfigSpec SPEC = new ForgeConfigSpec.Builder().comment("Better Schematics Configuration").build();
    @SubscribeEvent public static void onKeyMappings(RegisterKeyMappingsEvent event) { event.register(openGuiKey); event.register(executePlaceKey); event.register(toggleRenderKey); event.register(layerUpKey); event.register(layerDownKey); }
}