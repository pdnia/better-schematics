package com.betterschematics.schematic;

import com.betterschematics.BetterSchematics;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;

/**
 * Manages schematic loading, placement, layer mode, and auto-build logic.
 */
public class SchematicManager {
    private SchematicData activeSchematic;
    private ProgressTracker progressTracker;
    private boolean layerModeEnabled = false;
    private int currentLayerMin = 0;
    private int currentLayerMax = 0;
    private int layerRange = 1;
    private boolean autoBuild = false;
    private BlockPos targetBlockPos;
    private String buildDirection = "E";

    public SchematicManager() { activeSchematic = null; }

    public void loadSchematic(File file) {
SchematicData sd = new SchematicData(); 
if (sd.loadFromFile(file)) { this.activeSchematic = sd; progressTracker = new ProgressTracker(sd); buildDirection = sd.getBuildDirection(); placeAtPlayer(); BetterSchematics.LOGGER.info("Loaded schematic: " + sd.getName()); }
}

    public void placeAtPlayer() {
        Minecraft mc = Minecraft.getInstance(); if (mc.player == null || activeSchematic == null) return;
        BlockPos playerPos = mc.player.blockPosition();
        activeSchematic.setPlacementOffset(new BlockPos(playerPos.getX(), playerPos.getY() - 1, playerPos.getZ() - 1));
    }

    public void movePlacement(int dx, int dy, int dz) { if (activeSchematic != null) activeSchematic.setPlacementOffset(activeSchematic.getPlacementOffset().offset(dx, dy, dz)); }
    public void rotatePlacement(boolean clockwise) { if (activeSchematic != null) activeSchematic.rotate(clockwise); }
    public void mirrorPlacement(char axis) {
if (activeSchematic != null) { switch (axis) { case 'x' -> activeSchematic.setMirrorX(!activeSchematic.isMirrorX()); case 'z' -> activeSchematic.setMirrorZ(!activeSchematic.isMirrorZ()); } }
}

    public void placeNextBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || activeSchematic == null) return;
        BlockPos next = findNextMissingBlock();
        if (next == null) { BetterSchematics.LOGGER.info("No missing blocks!"); return; }
        targetBlockPos = next;
        if (progressTracker != null) progressTracker.recalculate();
    }

    private BlockPos findNextMissingBlock() {
        Minecraft mc = Minecraft.getInstance(); if (mc.level == null || activeSchematic == null) return null;
        BlockPos worldSize = activeSchematic.getWorldSize();
        int yStart = layerModeEnabled ? currentLayerMin : 0;
        int yEnd = layerModeEnabled ? currentLayerMax : worldSize.getY() - 1;
        for (int y = yStart; y <= yEnd; y++) {
            for (int z = 0; z < worldSize.getZ(); z++) {
                for (int x = 0; x < worldSize.getX(); x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = activeSchematic.getBlockState(local);
                    if (expected.isAir()) continue;
                    BlockPos worldPos = activeSchematic.localToWorld(local);
                    BlockState actual = mc.level.getBlockState(worldPos);
                    if (!expected.equals(actual)) return worldPos;
                }
            }
        }
        return null;
    }

    public void shiftLayerUp() { if (layerModeEnabled) { currentLayerMin += layerRange; currentLayerMax += layerRange; currentLayerMin = Math.min(currentLayerMin, activeSchematic.getWorldSize().getY() - 1); currentLayerMax = Math.min(currentLayerMax, activeSchematic.getWorldSize().getY() - 1); } }
    public void shiftLayerDown() { if (layerModeEnabled) { currentLayerMin -= layerRange; currentLayerMax -= layerRange; currentLayerMin = Math.max(currentLayerMin, 0); currentLayerMax = Math.max(currentLayerMax, 0); } }

    public SchematicData getActiveSchematic() { return activeSchematic; }
    public ProgressTracker getProgressTracker() { return progressTracker; }
    public void syncProgress() { if (progressTracker != null) progressTracker.recalculate(); }
    public boolean isLayerModeEnabled() { return layerModeEnabled; }
    public void setLayerModeEnabled(boolean v) { layerModeEnabled = v; }
    public int getCurrentLayerMin() { return currentLayerMin; }
    public int getCurrentLayerMax() { return currentLayerMax; }
    public void setLayerRange(int r) { layerRange = r; }
    public void setCurrentLayer(int l) { currentLayerMin = l; currentLayerMax = Math.min(l + layerRange - 1, activeSchematic != null ? activeSchematic.getWorldSize().getY() - 1 : 0); }
    public String getBuildDirection() { return buildDirection; }
    public BlockPos getNextBlockPos() { return targetBlockPos; }
}