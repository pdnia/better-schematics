package com.betterschematics.schematic;

import com.betterschematics.BetterSchematics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtInputStream;
import net.minecraft.world.level.block.BlockState;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.GzIPInputStream;

/**
 * Loads and holds a .litematic schematic with transforms and query methods.
 */
public class SchematicData {
    private String name;
    private BlockPos enclosingSize;
    private List<RegionData> regions;
    private BlockPos placementOffset;
    private int rotation = 0;
    private boolean mirrorX = false;
    private boolean mirrorZ = false;

    public SchematicData() { regions = new ArrayList<>(); placementOffset = BlockPos.ZERO; }

    public boolean loadFromFile(File file) {
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(file))) {
            CompoundTag root = NbtInputStream.readCompound(in);
            CompoundTag meta = root.getCompound("Metadata");
            this.name = file.getName().replace(".litematic", "");
            CompoundTag enclosing = meta.getCompound("EnclosingSize");this.enclosingSize = new BlockPos(enclosing.getInt("x"), enclosing.getInt("y"), enclosing.getInt("z"));
            CompoundTag regions = meta.getCompound("Regions");
            for (String key : regions.getAllKeys()) { this.regions.add(RegionData.read(key, regions.getCompound(key))); }
            return true;
        } catch (IOException e) { BetterSchematics.LOGGER.error("Failed to load schematic", e); return false; }
    }

    public BlockPos localToWorld(BlockPos localPos) {
        BlockPos p = localPos;
        if (mirrorZ) p = new BlockPos(p.getX(), p.getY(), enclosingSize.getZ() - 1 - p.getZ());
        if (mirrorX) p = new BlockPos(enclosingSize.getX() - 1 - p.getX(), p.getY(), p.getZ());
        switch (rotation) { case 90  -> p = new BlockPos(-p.getZ(), p.getY(), p.getX()); break; case 180 -> p = new BlockPos(-p.getX(), p.getY(), -p.getZ()); break; case 270 -> p = new BlockPos(p.getZ(), p.getY(), -p.getX()); break; }
        return p.offset(placementOffset);
    }

    public BlockState getBlockState(BlockPos localPos) { for (RegionData r : regions) { BlockState s = r.getBlockState(localPos); if (s != null) return s; } return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(); }
    public BlockState getBlockStateAtWorld(BlockPos worldPos) { BlockPos local = worldPos.subtract(placementOffset); return getBlockState(local); }
    public BlockPos worldToLocal(BlockPos worldPos) { return worldPos.subtract(placementOffset); }

    public String exportMaterialList() {
        Map<String, Long> map = new TreeMap<>();
        for (RegionData r : regions) {
            Map<BlockState, Long> m = new HashMap<>();
            r.collectMaterials(m);
            for (Map.Entry<BlockState, Long> e : m.entrySet()) {
                String key = e.getKey().getBlock().getName().getString();
                map.merge(key, e.getValue(), Long::sum);
            }
        }
        StringBuilder sb = new StringBuilder();
        map.entrySet().stream().sorted((a,b)->b.getValue().compareTo(a.getValue())).forEach(e -> sb.append(e.getKey()).append(" x ").append(e.getValue()).append("\n"));
        return sb.toString();
    }

    public String getName() { return name; }
    public BlockPos getEnclosingSize() { return enclosingSize; }
    public BlockPos getWorldSize() { return enclosingSize; }
    public BlockPos getPlacementOffset() { return placementOffset; }
    public void setPlacementOffset(BlockPos o) { placementOffset = o; }
    public int getRotation() { return rotation; }
    public void setRotation(int r) { rotation = ((r) % 4 * 90 + 360) % 360; }
    public boolean isMirrorX() { return mirrorX; }
    public void setMirrorX(boolean v) { mirrorX = v; }
    public boolean isMirrorZ() { return mirrorZ; }
    public void setMirrorZ(boolean v) { mirrorZ = v; }
    public List<RegionData> getRegions() { return regions; }
    public long getTotalBlocks() { return regions.stream().mapToLong(RegionData::getNonAirBlocks).sum(); }
    public String getBuildDirection() {
        switch (rotation) {
            case 0:  return mirrorX ? "W" : "E";
            case 90:  return mirrorX ? "N" : "S";
            case 180: return mirrorX ? "E" : "W";
            default: return mirrorX ? "S" : "N";
        }
    }
    public void rotate(boolean clockwise) { rotation = ((rotation/90 + (clockwise ? 1 : 3)) % 4) * 90; }
}