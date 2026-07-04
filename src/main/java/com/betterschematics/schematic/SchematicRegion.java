package com.betterschematics.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class SchematicRegion {
    public final String name;
    public final BlockPos position;
    public final BlockPos size;
    public final BlockState[] palette;
    public final long[] blockData;
    public final ListTag tileEntities;
    public final ListTag entities;

    public SchematicRegion(String name, BlockPos position, BlockPos size, BlockState[] palette, long[] blockData, ListTag tileEntities, ListTag entities) {
        this.name = name; this.position = position; this.size = size;
        this.palette = palette; this.blockData = blockData;
        this.tileEntities = tileEntities; this.entities = entities;
    }

    public long getNonAirBlocks() {
        long c = 0;
        for (long l : blockData) if (l != 0) c++;
        return c;
    }

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
        CompoundTag posTag = tag.getCompound("Position").orElse(new CompoundTag());
        BlockPos position = new BlockPos(posTag.getInt("x").orElse(0), posTag.getInt("y").orElse(0), posTag.getInt("z").orElse(0));
        CompoundTag sizeTag = tag.getCompound("Size").orElse(new CompoundTag());
        BlockPos size = new BlockPos(sizeTag.getInt("x").orElse(0), sizeTag.getInt("y").orElse(0), sizeTag.getInt("z").orElse(0));
        ListTag palTag = tag.getList("BlockStatePalette").orElse(new ListTag());
        BlockState[] palette = new BlockState[palTag.size()];
        for (int i = 0; i < palette.length; i++) palette[i] = parseBlockState(palTag.getCompound(i).orElse(new CompoundTag()));
        long[] packed = tag.getLongArray("BlockStates").orElse(new long[0]);
        int total = size.getX() * size.getY() * size.getZ();
        long[] bd = new long[total];
        int bits = Math.max(2, 64 - Integer.numberOfLeadingZeros(Math.max(0, palette.length - 1)));
        int baseIdx = 0;
        for (int i = 0; i < bd.length; i++) {
            if (i != 0 && (i % 64) == 0) baseIdx++;
            int offset = (i % 64) * bits;
            bd[i] = (packed[baseIdx] >> offset) & ((1L << bits) - 1);
        }
        ListTag tl = tag.contains("TileEntities") ? tag.getList("TileEntities").orElse(new ListTag()) : new ListTag();
        ListTag el = tag.contains("Entities") ? tag.getList("Entities").orElse(new ListTag()) : new ListTag();
        return new SchematicRegion(name, position, size, palette, bd, tl, el);
    }

    private static BlockState parseBlockState(CompoundTag tag) {
        String n = tag.getString("Name").orElse("");
        Identifier rl = Identifier.tryParse(n);
        if (rl == null) return Blocks.AIR.defaultBlockState();
        Block block = BuiltInRegistries.BLOCK.getValue(rl);
        if (block == null) return Blocks.AIR.defaultBlockState();
        BlockState s = block.defaultBlockState();
        if (tag.contains("Properties")) {
            CompoundTag props = tag.getCompound("Properties").orElse(new CompoundTag());
            for (String key : props.keySet()) {
                Property<?> prop = s.getBlock().getStateDefinition().getProperty(key);
                if (prop != null) {
                    String val = props.getString(key).orElse("");
                    s = setPropertyValue(s, prop, val);
                }
            }
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState setPropertyValue(BlockState s, Property<T> q, String v) {
        return q.getValue(v).map(x -> s.setValue(q, x)).orElse(s);
    }
}