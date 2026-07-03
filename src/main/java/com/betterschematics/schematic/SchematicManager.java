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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manages loaded schematics: loading, placement, rotation, layer filtering,
 * material counting, progress tracking.
 */
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
            SchematicData data = SchematicData.load(file);
            this.schematic = data;
            this.placementOrigin = new BlockPos(
                    (int) Minecraft.getInstance().player.getX(),
                    (int) Minecraft.getInstance().player.getY(),
                    (int) Minecraft.getInstance().player.getZ());
            this.progressTracker.setSchematic(data);
            BetterSchematics.LOGGER.info("Loaded schematic: " + data.name);
            return true;
        } catch (IOException e) {
            BetterSchematics.LOGGER.error("Failed to load schematic", e);
            return false;
        }
    }

    public void rotatePlacement(boolean clockwise) {
        setRotation(rotation + (clockwise ? 90 : -90));
    }

    public void toggleMirror(char axis) {
        switch (axis) {
            case 'x': mirrorX = !mirrorX; break;
            case 'z': mirrorZ = !mirrorZ; break;
        }
    }

    public void shiftLayerUp() {
        if (schematic == null) return;
        SchematicRegion r = schematic.getMainRegion();
        if (r != null) {
            currentLayerMin = Math.min(currentLayerMin + 1, r.getSize().getY() - 1);
            currentLayerMax = Math.min(currentLayerMax + 1, r.getSize().getY() - 1);
        }
    }

    public void shiftLayerDown() {
        if (schematic == null) return;
        currentLayerMin = Math.max(currentLayerMin - 1, 0);
        currentLayerMax = Math.max(currentLayerMax - 1, 0);
    }

    public void placeNextBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || schematic == null) return;
        Level world = mc.level;
        SchematicRegion region = schematic.getMainRegion();
        if (region == null) return;

        BlockPos size = region.getSize();
        for (int y = currentLayerMin; y <= currentLayerMax; y++) {
            for (int x = 0; x < size.getX(); x++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = region.getBlockState(local);
                    if (expected.isAir()) continue;
                    BlockPos worldPos = transformPos(local);
                    if (!expected.equals(world.getBlockState(worldPos))) {
                        nextBlockTarget = worldPos;
                        return;
                    }
                }
            }
        }
        nextBlockTarget = null;
    }

    public void placeSingleBlock(BlockPos worldPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || schematic == null) return;
        BlockPos local = inverseTransformPos(worldPos);
        SchematicRegion region = schematic.getMainRegion();
        if (region == null) return;
        BlockState expected = region.getBlockState(local);
        if (expected == null || expected.isAir()) return;

        Map<Block, Integer> needed = new HashMap<>();
        for (ItemStack s : mc.player.getInventory().items) {
            if (s.getItem() instanceof BlockItem) {
                Block b = ((BlockItem) s.getItem()).getBlock();
                needed.merge(b, s.getCount(), Integer::sum);
            }
        }
        Block block = expected.getBlock();
        Integer count = needed.get(block);
        if (count != null && count > 0) {
            mc.level.setBlock(worldPos, expected, 3);
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                ItemStack s = mc.player.getInventory().getItem(i);
                if (s.getItem() instanceof BlockItem && ((BlockItem) s.getItem()).getBlock() == block) {
                    s.shrink(1);
                    break;
                }
            }
        }
    }

    public BlockPos transformPos(BlockPos local) {
        BlockPos p = local;
        SchematicRegion r = schematic != null ? schematic.getMainRegion() : null;
        if (r != null) {
            if (mirrorZ) {
                p = new BlockPos(p.getX(), p.getY(), r.getSize().getZ() - 1 - p.getZ());
            }
            if (mirrorX) {
                p = new BlockPos(r.getSize().getX() - 1 - p.getX(), p.getY(), p.getZ());
            }
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
                        if (bs != null && !bs.isAir()) {
                            mr.merge(bs, 1L, Long::sum);
                        }
                    }
                }
            }
            for (Map.Entry<BlockState, Long> e : mr.entrySet()) {
                String key = e.getKey().getBlock().getName().getString();
                map.merge(key, e.getValue(), Long::sum);
            }
        }
        StringBuilder sb = new StringBuilder();
        map.entrySet().stream().sorted((a,b) => b.getValue().compareTo(a.getValue())).forEach(e -> sb.append(e.getKey()).append(" x ").append(e.getValue()).append("\n"));
        return sb.toString();
    }
}