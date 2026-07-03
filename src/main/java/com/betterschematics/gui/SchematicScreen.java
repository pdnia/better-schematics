package com.betterschematics.gui;

import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.betterschematics.BetterSchematics;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.io.File;

/**
 * Main GUI screen for Better Schematics.
 */
public class SchematicScreen extends Screen {
    private final SchematicManager manager;

    public SchematicScreen() {
        super(GameNarrator.EMPTY);
        this.manager = BetterSchematics.getInstance().getSchematicManager();
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Load button
        this.addRenderableWidget(Button.builder(Component.literal("Load .litematic"), btn -> {
            File file = new File("schematics/schematic.litematic");
            if (file.exists()) {
                manager.loadSchematic(file);
            } else {
                BetterSchematics.LOGGER.warn("File not found: " + file.getAbsolutePath());
            }
        }).bounds(cx - 50, cy + 20, 100, 20).build());

        // Rotate buttons
        this.addRenderableWidget(Button.builder(Component.literal("Rotate CW", btn -> manager.rotatePlacement(true))
                .bounds(cx - 80, cy + 50, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Rotate CCW", btn -> manager.rotatePlacement(false))
                .bounds(cx + 5, cy + 50, 75, 20).build());

        // Mirror buttons
        this.addRenderableWidget(Button.builder(Component.literal("Mirror X", btn -> manager.toggleMirror('x'))
                .bounds(cx - 80, cy + 80, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Mirror Z", btn -> manager.toggleMirror('.'))
                .bounds(cx + 5, cy + 80, 75, 20).build());

        // Layer mode
        this.addRenderableWidget(Button.builder(Component.literal("Layer Mode OFF", btn -> manager.setLayerMode(!manager.isLayerMode()))
                .bounds(cx - 40, cy + 110, 80, 20).build());

        // Material list
        this.addRenderableWidget(Button.builder(Component.literal("Materials", btn -> {
            String mats = manager.exportMaterialList();
            BetterSchematics.LOGGER.info(mats);
        }).bounds(cx - 40, cy + 140, 80, 20).build());

        // Close
        this.addRenderableWidget(Button.builder(Component.literal("Resume Game", btn -> { this.onClose(); })
                .bounds(cx - 40, cy + 180, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font,
                Component.literal("Better Schematics v0.3"));
        if (manager.hasSchematic()) {
            SchematicData data = manager.getActiveSchematic();
            ProgressTracker pt = manager.getProgressTracker();
            graphics.drawString(this.font, "Name: " + data.name, 5, 30, 0xFFFFFFFF);
            graphics.drawString(this.font, "Progress: " + String.format("%.1f%%", pt.getPercentComplete()), 5, 45, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}