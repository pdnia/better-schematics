package com.betterschematics.gui;

import com.betterschematics.BetterSchematics;
import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.io.File;

public class SchematicScreen extends Screen {
    private final SchematicManager manager;

    public SchematicScreen() {
        super(Component.literal("Better Schematics"));
        this.manager = BetterSchematics.getInstance().getSchematicManager();
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Load .litematic"), btn -> {
            File f = new File("schematics/schematic.litematic");
            if (f.exists()) manager.loadSchematic(f);
        }).bounds(cx - 50, cy + 20, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Rotate CW"), btn -> manager.rotatePlacement(true))
            .bounds(cx - 80, cy + 50, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Rotate CCW"), btn -> manager.rotatePlacement(false))
            .bounds(cx + 5, cy + 50, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Mirror X"), btn -> manager.toggleMirror('x'))
            .bounds(cx - 80, cy + 80, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Mirror Z"), btn -> manager.toggleMirror('z'))
            .bounds(cx + 5, cy + 80, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Layer On/Off"), btn -> manager.setLayerMode(!manager.isLayerMode()))
            .bounds(cx - 40, cy + 110, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Materials"), btn -> {
            String m = manager.exportMaterialList();
            BetterSchematics.LOGGER.info(m);
        }).bounds(cx - 40, cy + 140, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Resume Game"), btn -> this.onClose())
            .bounds(cx - 40, cy + 180, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, Component.literal("Better Schematics v0.3"), this.width / 2, 10, 0xFFFFFFFF);
        if (manager.hasSchematic()) {
            SchematicData d = manager.getActiveSchematic();
            ProgressTracker pt = manager.getProgressTracker();
            g.drawString(this.font, "Name: " + d.name, 5, 30, 0xFFFFFFFF);
            g.drawString(this.font, "Progress: " + String.format("%.1f%%", pt.getPercentComplete()), 5, 45, 0xFFFFFFFF);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }
}
