package com.betterschematics.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class SchematicRegion {
    private final String name;
    private final BlockPos position;
    private final BlockPos size;
    private final BlockState[] palette;
    private final long[] blockData;
    private final ListTag tileEntities;
    private final ListTag entities;

    public SchematicRegion(String name, BlockPos position, BlockPos size, BlockState[] palette, long[] blockData, ListTag tileEntities, ListTag entities) {
        this.name = name; this.position = position; this.size = size;
        this.palette = palette; this.blockData = blockData;
        this.tileEntities = tileEntities; this.entities = entities;
    }

    public String getName() { return name; }
    public BlockPos getPosition() { return position; }
    public BlockPos getSize() { return size; }
    public BlockState[] getPalette() { return palette; }
    public ListTag getTileEntities() { return tileEntities; }
    public ListTag getEntities() { return entities; }
    public long getNonAirBlocks() { long c = 0; for (long l : blockData) { if (l != 0) c++; } return c; }

    public BlockState getBlockState(BlockPos localPos) {
        int x = localPos.getX() - position.getX();
        int y = localPos.getY() - position.getY();
        int z = localPos.getZ() - position.getZ();
        if (x < 0 || x >= size.getX() || y < 0 || y >= size.getY() || z < 0 || z >= size.getZ()) return null;
        int idx = x + size.getX() * (z + size.getZ() * y);
        if (idx >= blockData.length) return null;
        long val = blockData[idx];
        if (val == 0 || val - 1 >= palette.length) return Blocks.AIR.defaultBlockState();
        return palette[(int)(val - 1)];
    }

    public static SchematicRegion read(String name, CompoundTag tag) {
        BlockPos position = new BlockPos(tag.getCompound("Position").getInt("x"), tag.getCompound("Position").getInt("y"), tag.getCompound("Position").getInt("z"));
        BlockPos size = new BlockPos(tag.getCompound("Size").getInt("x"), tag.getCompound("Size").getInt("y"), tag.getCompound("Size").getInt("z"));
        ListTag palTag = tag.getList("BlockStatePalette", 10);
        BlockState[] palette = new BlockState[palTag.size()];
        for (int i = 0; i < palette.length; i++) palette[i] = parseBlockState(palTag.getCompound(i));
        long[] packed = tag.getLongArray("BlockStates");
        int total = size.getX() * size.getY() * size.getZ();
        long[] bd = new long[total];
        int bits = Math.max(2, 64 - Integer.numberOfLeadingZeros(palette.length - 1));
        int baseIdx = 0;
        for (int i = 0; i < bd.length; i++) {
            if (i != 0 && (i % 64) == 0) baseIdx++;
            int offset = (i % 64) * bits;
            bd[i] = (packed[baseIdx] >> offset) & ((1L << bits) - 1);
        }
        ListTag tl = tag.contains("TileEntities") ? tag.getList("TileEntities", 10) : new ListTag();
        ListTag el = tag.contains("Entities") ? tag.getList("Entities", 10) : new ListTag();
        return new SchematicRegion(name, position, size, palette, bd, tl, el);
    }

    private static BlockState parseBlockState(CompoundTag tag) {
        String n = tag.getString("Name");
        ResourceLocation rl = ResourceLocation.tryParse(n);
        if (rl == null) return Blocks.AIR.defaultBlockState();
        Block block = BuiltInRegistries.BLOCK.get(rl);
        BlockState s = block.defaultBlockState();
        if (tag.contains("Properties")) {
            CompoundTag props = tag.getCompound("Properties");
            for (String key : props.getAllKeys()) {
                Property<?> prop = s.getBlock().getStateDefinition().getProperty(key);
                if (prop != null) {
                    String val = props.getString(key);
                    s = setPropertyValue(s, prop, val);
                }
            }
        }
        return s;
    }

    private static <T extends Comparable<T>> BlockState setPropertyValue(BlockState s, Property<T> p, String v) {
        return p.getValue(v).map(x -> s.setValue(p, x)).orElse(s);
    }
}