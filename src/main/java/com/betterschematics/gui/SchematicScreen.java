package com.betterschematics.gui;

import com.betterschematics.BetterSchematics;
import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.io.File;

public class SchematicScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SchematicManager manager;

    public SchematicScreen() {
        super(Component.literal("Better Schematics"));
        this.manager = BetterSchematics.getInstance().getSchematicManager();
    }

    @Override
    protected void init() {
        LOGGER.info("[BetterSchematics] init() called!");
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Load .litematic"), btn -> {
            LOGGER.info("[BetterSchematics] Load button clicked!");
            File f = new File("schematics/schematic.litematic");
            LOGGER.info("[BetterSchematics] Looking for file: {}", f.getAbsolutePath());
            if (f.exists()) {
                LOGGER.info("[BetterSchematics] File found, loading...");
                manager.loadSchematic(f);
            } else {
                LOGGER.warn("[BetterSchematics] File NOT found at: {}", f.getAbsolutePath());
            }
        }).bounds(cx - 50, cy + 20, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Rotate CW"), btn -> {
            LOGGER.info("[BetterSchematics] Rotate CW clicked");
            manager.rotatePlacement(true);
        }).bounds(cx - 80, cy + 50, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Rotate CCW"), btn -> {
            LOGGER.info("[BetterSchematics] Rotate CCW clicked");
            manager.rotatePlacement(false);
        }).bounds(cx + 5, cy + 50, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Mirror X"), btn -> {
            LOGGER.info("[BetterSchematics] Mirror X clicked");
            manager.toggleMirror('x');
        }).bounds(cx - 80, cy + 80, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Mirror Z"), btn -> {
            LOGGER.info("[BetterSchematics] Mirror Z clicked");
            manager.toggleMirror('z');
        }).bounds(cx + 5, cy + 80, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Layer On/Off"), btn -> {
            LOGGER.info("[BetterSchematics] Layer On/Off clicked");
            manager.setLayerMode(!manager.isLayerMode());
        }).bounds(cx - 40, cy + 110, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Materials"), btn -> {
            LOGGER.info("[BetterSchematics] Materials clicked");
            String m = manager.exportMaterialList();
            LOGGER.info("[BetterSchematics] Materials:\n{}", m);
        }).bounds(cx - 40, cy + 140, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Resume Game"), btn -> {
            LOGGER.info("[BetterSchematics] Resume Game clicked");
            this.onClose();
        }).bounds(cx - 40, cy + 180, 80, 20).build());

        LOGGER.info("[BetterSchematics] init() done, {} children", children().size());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, Component.literal("Better Schematics v0.3"), this.width / 2, 10, 0xFFFFFFFF);
        if (manager.hasSchematic()) {
            SchematicData d = manager.getActiveSchematic();
            ProgressTracker pt = manager.getProgressTracker();
            g.drawString(this.font, "Name: " + d.name, 5, 30, 0xFFFFFFFF);
            g.drawString(this.font, "Progress: " + String.format("%.1f%%", pt.getPercentComplete()), 5, 45, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }
}
