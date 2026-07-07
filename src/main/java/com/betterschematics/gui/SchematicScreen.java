package com.betterschematics.gui;

import com.betterschematics.BetterSchematics;
import com.betterschematics.schematic.ProgressTracker;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SchematicManager manager;
    private final List<File> schematicFiles = new ArrayList<>();
    private static final int MAX_FILES_SHOWN = 8;

    public SchematicScreen() {
        super(Component.literal("Better Schematics"));
        this.manager = BetterSchematics.getInstance().getSchematicManager();
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;

        scanSchematicFiles();

        int yStart = cy - 60;
        if (!schematicFiles.isEmpty()) {
            int maxShow = Math.min(schematicFiles.size(), MAX_FILES_SHOWN);
            for (int i = 0; i < maxShow; i++) {
                final File file = schematicFiles.get(i);
                String displayName = file.getName();
                if (displayName.length() > 24) {
                    displayName = displayName.substring(0, 22) + "..";
                }
                this.addRenderableWidget(Button.builder(Component.literal(displayName), btn -> {
                    manager.loadSchematic(file);
                }).bounds(cx - 120, yStart + i * 22, 240, 20).build());
            }
        }

        int controlY = cy + 20;
        this.addRenderableWidget(Button.builder(Component.literal("Rotate CW"), btn -> {
            manager.rotatePlacement(true);
        }).bounds(cx - 80, controlY + 40, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Rotate CCW"), btn -> {
            manager.rotatePlacement(false);
        }).bounds(cx + 5, controlY + 40, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Mirror X"), btn -> {
            manager.toggleMirror('x');
        }).bounds(cx - 80, controlY + 70, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Mirror Z"), btn -> {
            manager.toggleMirror('z');
        }).bounds(cx + 5, controlY + 70, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Layer On/Off"), btn -> {
            manager.setLayerMode(!manager.isLayerMode());
        }).bounds(cx - 40, controlY + 100, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Materials"), btn -> {
            String m = manager.exportMaterialList();
            LOGGER.info("[BetterSchematics] Materials:\n{}", m);
            // Show in chat too
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                for (String line : m.split("\\n")) {
                    if (!line.isBlank()) {
                        mc.player.displayClientMessage(Component.literal(line), false);
                    }
                }
            }
        }).bounds(cx - 40, controlY + 130, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Resume Game"), btn -> {
            this.onClose();
        }).bounds(cx - 40, controlY + 170, 80, 20).build());
    }

    private void scanSchematicFiles() {
        schematicFiles.clear();
        Minecraft mc = Minecraft.getInstance();
        File schematicsDir = new File(mc.gameDirectory, "schematics");
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
        }
        if (schematicsDir.isDirectory()) {
            File[] files = schematicsDir.listFiles((dir, name) -> name.endsWith(".litematic"));
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        schematicFiles.add(f);
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, Component.literal("Better Schematics v0.3"), this.width / 2, 10, 0xFFFFFFFF);

        if (schematicFiles.isEmpty()) {
            int cx = this.width / 2;
            int cy = this.height / 2;
            g.drawCenteredString(this.font, Component.literal("No .litematic files found!"), cx, cy - 50, 0xFFFF5555);
            Minecraft mc = Minecraft.getInstance();
            File sd = new File(mc.gameDirectory, "schematics");
            g.drawCenteredString(this.font, Component.literal("Place files in: " + sd.getAbsolutePath()), cx, cy - 35, 0xFFAAAAAA);
        }

        if (manager.hasSchematic()) {
            SchematicData d = manager.getActiveSchematic();
            ProgressTracker pt = manager.getProgressTracker();
            g.drawString(this.font, "Loaded: " + d.name, 5, this.height - 35, 0xFFFFFFFF);
            g.drawString(this.font, "Progress: " + String.format("%.1f%%", pt.getPercentComplete()), 5, this.height - 20, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }
}