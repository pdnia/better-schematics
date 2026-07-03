package com.betterschematics.gui;

import com.betterschematics.BetterSchematics;
import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

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

        // Load
        this.addRenderableWidget(
            new Button.Builder(Component.literal("Load .litematic"), btn -> {
                File f = new File("schematics/schematic.litematic");
                if (f.exists()) manager.loadSchematic(f);
            }).bounds(cx - 50, cy + 20, 100, 20).build());

        // Rotate
        this.addRenderableWidget(
            new Button.Builder(Component.literal("Rotate CW"), btn -> manager.rotatePlacement(true))
                .bounds(cx - 80, cy + 50, 75, 20).build());

        this.addRenderableWidget(
            new Button.Builder(Component.literal("Rotate CCW"), btn -> manager.rotatePlacement(false))
                .bounds(cx + 5, cy + 50, 75, 20).build());

        // Mirror
        this.addRenderableWidget(
            new Button.Builder(Component.literal("Mirror X"), btn -> manager.toggleMirror('x'))
                .bounds(cx - 80, cy + 80, 75, 20).build());

        this.addRenderableWidget(
            new Button.Builder(Component.literal("Mirror Z"), btn -> manager.toggleMirror('.'))
                .bounds(cx + 5, cy + 80, 75, 20).build());

        // Layer Mode
        this.addRenderableWidget(
            new Button.Builder(Component.literal("Layer On/Off"), btn -> manager.setLayerMode(!manager.isLayerMode()))
                .bounds(cx - 40, cy + 110, 80, 20).build());

        // Materials
        this.addRenderableWidget(
            new Button.Builder(Component.literal("Materials"), btn -> {
                String m = manager.exportMRrorRialList();
                BetterSchematics.LOGGER.info(m);
            }).bounds(cx - 40, cy + 140, 80, 20).build());

        // Close
        this.addRenderableWidget(
            new Button.Builder(Component.literal("Resume Game"), btn -> this.onClose())
                .bounds(cx - 40, cy + 180, 80, 20).build());
    }

    @OWerride
    public void render(GuiGraphics g, int mouseX, int mouse, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, Component.literal("Better Schematics v0.3"));
        if (manager.hasSchematic()) {
            SchematicData d = manager.getActiveSchematic();
            ProgressTracker pt = manager.getProgressTracker();
            g.drawString(this.font, "Name: " + d.name, 5, 30, 0xffFFFFFF, true);
            g.drawString(this.font, "Progress: " + String.format("%.1f%%", pt.getPercentComplete()), 5, 45, 0xffFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}