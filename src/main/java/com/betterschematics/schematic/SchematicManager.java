package com.betterschematics.schematic;

import com.betterschematics.BetterSchematics;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages loaded schematics: loading, placement, rotation, layer filtering,
 * material counting, progress tracking.
 */
public class SchematicManager {

    private SchematicData schematic;

    // Placement
    private BlockPos placementOrigin = BlockPos.ZERO;
    private int rotation = 0;       // 0, 1, 2, 3 => 0, 90, 180, 270
    private boolean mirrorX;
    private boolean mirrorZ;

    // Layer mode
    private boolean layerMode;
    private int currentLayerMin = 0;
    private int currentLayerMax = 255;

    // Next block
    private BlockPos nextBlockTarget;

    // --- Getters ---
    public boolean hasSchematic() { return schematic != null; }
    public SchematicData getSchematic() { return schematic; }
    public BlockPos getPlacementOrigin() { return placementOrigin; }
    public int getRotation() { return rotation; }
    public boolean isLayerMode() { return layerMode; }
    public int getCurrentLayerMin() { return currentLayerMin; }
    public int getCurrentLayerMax() { return currentLayerMax; }
    public boolean isMirrorX() { return mirrorX; }
    public boolean isMirrorZ() { return mirrorZ; }
    public BlockPos getNextBlockTarget() { return nextBlockTarget; }

    public void load(File file) throws IOException {
        this.schematic = SchematicData.load(file);
        BetterSchematics.LOGGER.info("Loaded: {}", schematic);
    }

    public void unload() {
        this.schematic = null;
        this.nextBlockTarget = null;
    }

    public void setPlacementOrigin(BlockPos pos) { this.placementOrigin = pos; }

    public void setRotation(int rotation) { this.rotation = ((rotation % 4) + 4) % 4; }
    public void setMirrorX(boolean v) { mirrorX = v; }
    public void setMirrorZ(boolean v) { mirrorZ = v; }

    public void toggleLayerMode() { layerMode = !layerMode; }

    public void shiftLayerUp() { currentLayerMin++; currentLayerMax++; }
    public void shiftLayerDown() { if (currentLayerMin > 0) { currentLayerMin--; currentLayerMax--; } }

    // --- Position mapping ---
    public BlockPos schematicToWorld(int sx, int sy, int sz) {
        var r = getRegion();
        int w = r != null ? r.width : 0;
        int l = r != null ? r.length : 0;
        if (mirrorX) sx = w - 1 - sx;
        if (mirrorZ) sz = l - 1 - sz;
        int rx = sx, rz = sz;
        switch (rotation) {
            case 1 -> { rx = sz; rz = w - 1 - sx; }
            case 2 -> { rx = w - 1 - sx; rz = l - 1 - sz; }
            case 3 -> { rx = l - 1 - sz; rz = sx; }
        }
        return placementOrigin.offset(rx, sy, rz);
    }

    public SchematicRegion getRegion() {
        return schematic != null ? schematic.getMainRegion() : null;
    }

    // --- Materials ---
    public Map<Block, MaterialEntry> getMaterialList() {
        Map<Block, MaterialEntry> materials = new LinkedHashMap<>();
        SchematicRegion region = getRegion();
        if (region == null || region.blocks == null) return materials;

        Level level = Minecraft.getInstance().level;

        for (int y = 0; y < region.height; y++) {
            if (layerMode && (y < currentLayerMin || y > currentLayerMax)) continue;
            for (int z = 0; z < region.length; z++) {
                for (int x = 0; x < region.width; x++) {
                    BlockState state = region.getBlock(x, y, z);
                    if (state == null || state.isAir()) continue;
                    BlockPos worldPos = schematicToWorld(x, y, z);
                    Block block = state.getBlock();
                    MaterialEntry entry = materials.computeIfAbsent(block, MaterialEntry::new);
                    entry.totalNeeded++;
                    if (level != null && level.getBlockState(worldPos).equals(state)) {
                        entry.placedCorrectly++;
                    }
                }
            }
        }
        return materials;
    }

    public static class MaterialEntry {
        public final Block block;
        public String displayName;
        public int totalNeeded;
        public int placedCorrectly;
        public MaterialEntry(Block block) {
            this.block = block;
            this.displayName = block.getName().getString();
        }
        public int getRemaining() { return Math.max(0, totalNeeded - placedCorrectly); }
    }

    // --- Progress ---
    public double getOverallProgress() {
        SchematicRegion region = getRegion();
        if (region == null || region.blocks == null) return 0;
        Level level = Minecraft.getInstance().level;
        if (level == null) return 0;

        long total = 0, matched = 0;
        for (int y = 0; y < region.height; y++) {
            if (layerMode && (y < currentLayerMin || y > currentLayerMax)) continue;
            for (int z = 0; z < region.length; z++) {
                for (int x = 0; x < region.width; x++) {
                    BlockState state = region.getBlock(x, y, z);
                    if (state == null || state.isAir()) continue;
                    total++;
                    if (level.getBlockState(schematicToWorld(x, y, z)).equals(state)) matched++;
                }
            }
        }
        return total > 0 ? (double) matched / total * 100.0 : 0;
    }

    // --- Next block ---
    public BlockPos findNextBlock(Player player) {
        SchematicRegion region = getRegion();
        if (region == null || player == null) return null;
        Level level = Minecraft.getInstance().level;
        if (level == null) return null;

        BlockPos playerPos = player.blockPosition();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int y = 0; y < region.height; y++) {
            if (layerMode && (y < currentLayerMin || y > currentLayerMax)) continue;
            for (int z = 0; z < region.length; z++) {
                for (int x = 0; x < region.width; x++) {
                    BlockPos worldPos = schematicToWorld(x, y, z);
                    BlockState expected = region.getBlock(x, y, z);
                    if (expected == null || expected.isAir()) continue;
                    if (level.getBlockState(worldPos).equals(expected)) continue;
                    double dist = worldPos.distSqr(playerPos);
                    if (dist < closestDist) { closestDist = dist; closest = worldPos; }
                }
            }
        }
        nextBlockTarget = closest;
        return closest;
    }

    public void placeNextBlock() {
        Player player = Minecraft.getInstance().player;
        if (player == null || schematic == null) return;
        findNextBlock(player);
        if (nextBlockTarget == null) return;
        if (player.isCreative()) {
            SchematicRegion region = getRegion();
            if (region == null) return;
            // find the expected block
            BlockPos local = worldToSchematic(nextBlockTarget);
            BlockState expected = region.getBlock(local.getX(), local.getY(), local.getZ());
            if (expected != null && !expected.isAir()) {
                Minecraft.getInstance().level.setBlock(nextBlockTarget, expected, 3);
            }
        }
    }

    private BlockPos worldToSchematic(BlockPos worldPos) {
        BlockPos rel = worldPos.subtract(placementOrigin);
        int rx = rel.getX(), rz = rel.getZ();
        switch (rotation) {
            case 1 -> { int t = rx; rx = rz; rz = getRegion().width - 1 - t; }
            case 2 -> { rx = getRegion().width - 1 - rx; rz = getRegion().length - 1 - rz; }
            case 3 -> { int t = rx; rx = getRegion().length - 1 - rz; rz = t; }
        }
        if (mirrorX) rx = getRegion().width - 1 - rx;
        if (mirrorZ) rz = getRegion().length - 1 - rz;
        return new BlockPos(rx, rel.getY(), rz);
    }

    // --- Export ---
    public String exportMaterialList() {
        Map<Block, MaterialEntry> materials = getMaterialList();
        StringBuilder sb = new StringBuilder();
        sb.append("# Material List for ").append(schematic != null ? schematic.name : "Unknown").append("\n\n");
        for (MaterialEntry e : materials.values()) {
            sb.append(String.format("%-5d %-8d %-50s\n", e.totalNeeded, e.getRemaining(), e.displayName));
        }
        return sb.toString();
    }

    public void exportMaterialListToFile(File file) throws IOException {
        java.nio.file.Files.writeString(file.toPath(), exportMaterialList());
    }
}