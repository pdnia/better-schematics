package com.betterschematics.gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public class SchematicScreen extends Screen {
    public SchematicScreen() { super(Component.literal("Better Schematics")); }
    @Override public boolean isPauseScreen() { return false; }
}