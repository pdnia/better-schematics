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

public class SchematicManager {
    private SchematicData schematic;
    private BlockPos placementOrigin = BlockPos.ZERO;
    private int rotation = 0;
    private boolean mirrorX = false;
    private boolean mirrorZ = false;
    private boolean layerMode = false;
    private int currentLayerMin = 0;
    private int currentLayerMax = 255;
    private BlockPos nextBlockTarget;
    private final ProgressTracker progressTracker = new ProgressTracker();

    public SchematicData getActiveSchematic() { return schematic; }
    public boolean hasSchematic() { return schematic != null; }
    public BlockPos getPlacementOrigin() { return placementOrigin; }
    public void setPlacementOrigin(BlockPos o) { placementOrigin = o; }
    public int getRotation() { return rotation; }
    public void setRotation(int r) { rotation = ((r % 4 + 4) % 4) * 90; }
    public boolean isMirrorX() { return mirrorX; }
    public void setMirrorX(boolean v) { mirrorX = v; }
    public boolean isMirrorZ() { return mirrorZ; }
    public void setMirrorZ(boolean v) { mirrorZ = v; }
    public boolean isLayerMode() { return layerMode; }
    public void setLayerMode(boolean v) { layerMode = v; }
    public int getCurrentLayerMin() { return currentLayerMin; }
    public void setCurrentLayerMin(int l) { currentLayerMin = l; }
    public int getCurrentLayerMax() { return currentLayerMax; }
    public void setCurrentLayerMax(int l) { currentLayerMax = l; }
    public ProgressTracker getProgressTracker() { return progressTracker; }
    public BlockPos getNextBlockTarget() { return nextBlockTarget; }

    public boolean loadSchematic(File file) {
        try {
            schematic = SchematicData.load(file);
            placementOrigin = new BlockPos((int)Minecraft.getInstance().player.getX(), (int)Minecraft.getInstance().player.getY(), (int)Minecraft.getInstance().player.getZ());
            progressTracker.setSchematic(schematic);
            return true;
        } catch (IOException e) { return false; }
    }

    public void rotatePlacement(boolean clockwise) {
        rotation = clockwise ? (rotation + 90) % 360 : (rotation + 270) % 360;
    }

    public void toggleMirror(char axis) {
        if (axis == 'x') mirrorX = !mirrorX;
        else if (axis == 'z') mirrorZ = !mirrorZ;
    }

    public void shiftLayerUp() {
        if (schematic != null) {
            SchematicRegion r = schematic.getMainRegion();
            if (r != null) {
                currentLayerMin = Math.min(currentLayerMin + 1, r.getSize().getY() - 1);
                currentLayerMax = Math.min(currentLayerMax + 1, r.getSize().getY() - 1);
            }
        }
    }

    public void shiftLayerDown() {
        currentLayerMin = Math.max(currentLayerMin - 1, 0);
        currentLayerMax = Math.max(currentLayerMax - 1, 0);
    }

    public void placeNextBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || schematic == null) return;
        SchematicRegion r = schematic.getMainRegion();
        if (r == null) return;
        BlockPos s = r.getSize();
        for (int y = currentLayerMin; y <= currentLayerMax; y++) {
            for (int x = 0; x < s.getX(); x++) {
                for (int z = 0; z < s.getZ(); z++) {
                    BlockState expected = r.getBlockState(new BlockPos(x, y, z));
                    if (expected != null && !expected.isAir()) {
                        BlockPos wp = transformPos(new BlockPos(x, y, z));
                        if (!expected.equals(mc.level.getBlockState(wp))) {
                            nextBlockTarget = wp;
                            return;
                        }
                    }
                }
            }
        }
        nextBlockTarget = null;
    }

    public BlockPos transformPos(BlockPos local) {
        BlockPos p = local;
        SchematicRegion r = schematic != null ? schematic.getMainRegion() : null;
        if (r != null) {
            if (mirrorZ) p = new BlockPos(p.getX(), p.getY(), r.getSize().getZ() - 1 - p.getZ());
            if (mirrorX) p = new BlockPos(r.getSize().getX() - 1 - p.getX(), p.getY(), p.getZ());
            switch (rotation) {
                case 90:  p = new BlockPos(-p.getZ(), p.getY(), p.getX()); break;
                case 180: p = new BlockPos(-p.getX(), p.getY(), -p.getZ()); break;
                case 270: p = new BlockPos(p.getZ(), p.getY(), -p.getX()); break;
            }
        }
        return p.offset(placementOrigin);
    }

    public BlockPos inverseTransformPos(BlockPos world) {
        return world.subtract(placementOrigin);
    }

    public String exportMaterialList() {
        if (schematic == null) return "";
        Map<String, Long> map = new TreeMap<>();
        for (SchematicRegion r : schematic.regions) {
            Map<BlockState, Long> mr = new HashMap<>();
            BlockPos s = r.getSize();
            for (int x = 0; x < s.getX(); x++) {
                for (int y = 0; y < s.getY(); y++) {
                    for (int z = 0; z < s.getZ(); z++) {
                        BlockState bs = r.getBlockState(new BlockPos(x, y, z));
                        if (bs != null && !bs.isAir()) mr.merge(bs, 1L, Long::sum);
                    }
                }
            }
            for (Map.Entry<BlockState, Long> e : mr.entrySet()) {
                map.merge(e.getKey().getBlock().getDescription().getString(), e.getValue(), Long::sum);
            }
        }
        StringBuilder sb = new StringBuilder();
        map.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).forEach(e -> {
            sb.append(e.getKey()).append(" x ").append(e.getValue()).append("\n");
        });
        return sb.toString();
    }
}