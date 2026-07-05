package com.betterschematics.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Nonextendings()
public class BetterSchematicsConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_MINIMAP;
    public static final ForgeConfigSpec.BooleanValue SHOW_MATERIAL_LIST;

    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("betterschematics", "keycategory"));

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

    /**
     * Registers key mappings. Called from mod constructor.
     */
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        openGuiKey = new KeyMapping("betterschematics.key.open_gui", GLFW.GLFW_KEY_M, CATEGORY);
        executePlaceKey = new KeyMapping("betterschematics.key.execute_place", GLFW.GLFW_KEY_G, CATEGORY);
        toggleRenderKey = new KeyMapping("betterschematics.key.toggle_render", GLFW.GLFW_KEY_R, CATEGORY);
        layerUpKey = new KeyMapping("betterschematics.key.layer_up", GLFW.GLFW_KEY_UP, CATEGORY);
        layerDownKey = new KeyMapping("betterschematics.key.layer_down", GLFW.GLFW_KEY_DOWN, CATEGORY);
        event.register(openGuiKey);
        event.register(executePlaceKey);
        event.register(toggleRenderKey);
        event.register(layerUpKey);
        event.register(layerDownKey);
    }
}