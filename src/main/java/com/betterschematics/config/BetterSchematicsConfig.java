package com.betterschematics.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class BetterSchematicsConfig {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("betterschematics", "keycategory"));

    public static KeyMapping openGuiKey;
    public static KeyMapping toggleRenderKey;
    public static KeyMapping layerUpKey;
    public static KeyMapping layerDownKey;

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        openGuiKey = new KeyMapping("betterschematics.key.open_gui", GLFW.GLFW_KEY_M, CATEGORY);
        toggleRenderKey = new KeyMapping("betterschematics.key.toggle_render", GLFW.GLFW_KEY_R, CATEGORY);
        layerUpKey = new KeyMapping("betterschematics.key.layer_up", GLFW.GLFW_KEY_UP, CATEGORY);
        layerDownKey = new KeyMapping("betterschematics.key.layer_down", GLFW.GLFW_KEY_DOWN, CATEGORY);
        event.register(openGuiKey);
        event.register(toggleRenderKey);
        event.register(layerUpKey);
        event.register(layerDownKey);
    }
}