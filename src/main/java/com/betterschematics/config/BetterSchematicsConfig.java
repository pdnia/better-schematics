package com.betterschematics.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "betterschematics", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BetterSchematicsConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_MINIMAP;
    public static final ForgeConfigSpec.BooleanValue SHOW_MATERIAL_LIST;

    public static KeyMapping openGuiKey;
    public static KeyMapping executePlaceKey;
    public static KeyMapping toggleRenderKey;
    public static KeyMapping layerUpKey;
    public static KeyMapping layerDownKey;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SHOW_MINIMAP = builder.comment("Show minimap overlay").define("showMinimap", true);
        SHOW_MATERIAL_LIST = builder.comment("Show material list").define("showMaterialList", true);
        SPEC = builder.build();
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        openGuiKey = new KeyMapping("betterschematics.key.open_gui", GLFW.GLFW_KEY_M, "key.categories.betterschematics");
        executePlaceKey = new KeyMapping("betterschematics.key.execute_place", GLFW.GLFW_KEY_G, "key.categories.betterschematics");
        toggleRenderKey = new KeyMapping("betterschematics.key.toggle_render", GLFW.GLFW_KEY_R, "key.categories.betterschematics");
        layerUpKey = new KeyMapping("betterschematics.key.layer_up", GLFW.GLFW_KEY_UP, "key.categories.betterschematics");
        layerDownKey = new KeyMapping("betterschematics.key.layer_down", GLFW.GLFW_KEY_DOWN, "key.categories.betterschematics");
        event.register(openGuiKey);
        event.register(executePlaceKey);
        event.register(toggleRenderKey);
        event.register(layerUpKey);
        event.register(layerDownKey);
    }
}
