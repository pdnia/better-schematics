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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicScreen extends Screen {
    private final SchematicManager manager;
    private final List<File> schematicFiles = new ArrayList<>();

    public SchematicScreen() {
        super(Component.literal("Better Schematics"));
        this.manager = BetterSchematics.getInstance().getSchematicManager();
    }

    @Override
    protected void init() {
        super.init();
        scanSchematicFiles();
        int cx = this.width / 2;
        int filesX = 20;
        int filesY = 35;
        final int maxShow = Math.min(schematicFiles.size(), 10);
        for (int i = 0; i < maxShow; i++) {
            final File file = schematicFiles.get(i);
            String name = file.getName();
            if (name.length() > 28) name = name.substring(0, 26) + "..";
            final String dn = name;
            this.addRenderableWidget(
                Button.builder(Component.literal(dn), btn -> {
                    manager.loadSchematic(file);
                    this.onClose();
                }).bounds(filesX, filesY + i * 22, 300, 20).build());
        }

        int ctrlX = cx + 60;
        int ctrlY = 35;
        final int btnW = 85;
        final int btnL = 22;

        this.addRenderableWidget(Button.builder(Component.literal("Rotate CW"), btn -> manager.rotatePlacement(true)).bounds(ctrlX, ctrlY, btnW, btnL).build());
        this.addRenderableWidget(Button.builder(Component.literal("Rotate CCW"), btn -> manager.rotatePlacement(false)).bounds(ctrlX + btnW + 4, ctrlY, btnW, btnL).build());
        ctrlY += btnL + 4;
        this.addRenderableWidget(Button.builder(Component.literal("Mirror X"), btn -> manager.toggleMirror('x')).bounds(ctrlX, ctrlY, btnW, btnL).build());
        this.addRenderableWidget(Button.builder(Component.literal("Mirror Z"), btn -> manager.toggleMirror('z')).bounds(ctrlX + btnW + 4, ctrlY, btnW, btnL).build());
        ctrlY += btnL + 4;
        this.addRenderableWidget(Button.builder(Component.literal("Layer +"), btn -> manager.shiftLayerUp()).bounds(ctrlX, ctrlY, btnW, btnL).build());
        this.addRenderableWidget(Button.builder(Component.literal("Layer -"), btn -> manager.shiftLayerDown()).bounds(ctrlX + btnW + 4, ctrlY, btnW, btnL).build());
        ctrlY += btnL + 4;
        this.addRenderableWidget(Button.builder(Component.literal("Layer Mode"), btn -> manager.setLayerMode(!manager.isLayerMode())).bounds(ctrlX, ctrlY, btnW, btnL).build());
        this.addRenderableWidget(Button.builder(Component.literal("Toggle Render"), btn -> BetterSchematics.getInstance().getRenderer().toggleRender()).bounds(ctrlX + btnW + 4, ctrlY, btnW, btnL).build());
        ctrlY += btnL + 4;
        this.addRenderableWidget(Button.builder(Component.literal("Materials"), btn -> {
            String m = manager.exportMaterialList();
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && !m.isEmpty()) {
                for (String line : m.split("\n")) {
                    if (!line.isBlank()) mc.player.displayClientMessage(Component.literal(line), false);
                }
            }
        }).bounds(ctrlX, ctrlY, btnW, btnL).build());
        this.addRenderableWidget(Button.builder(Component.literal("Resume Game"), btn -> this.onClose()).bounds(ctrlX + btnW + 4, ctrlY, btnW, btnL).build());
    }

    private void scanSchematicFiles() {
        schematicFiles.clear();
        Minecraft mc = Minecraft.getInstance();
        File dir = new File(mc.gameDirectory, "schematics");
        if (!dir.exists()) dir.mkdirs();
        if (dir.isDirectory()) {
            File[] files = dir.listFiles((d, n) -> n.endsWith(".litematic"));
            if (files != null) for (File f : files) if (f.isFile()) schematicFiles.add(f);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);
        g.drawCenteredString(this.font, Component.literal("Better Schematics v0.3"), this.width / 2, 8, 0xFFFFFFFF);
        if (schematicFiles.isEmpty()) {
            g.drawCenteredString(this.font, Component.literal("No .litematic files in schematics/"), this.width / 2, 20, 0xFFFFFFFF);
        }
        if (manager.hasSchematic()) {
            SchematicData d = manager.getActiveSchematic();
            ProgressTracker tr = manager.getProgressTracker();
            g.drawString(this.font, "Loaded: " + d.name, 5, this.height - 35, 0xFFFFFFFF);
            g.drawString(this.font, "Progress: " + String.format("%.1f%%", tr.getPercentComplete()), 5, this.height - 20, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(null);
    }
}