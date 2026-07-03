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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single region within a .litematic schematic.
 * Stores block palette and packed block state data.
 */
public class SchematicRegion {
    private final String name;
    private final BlockPos position;
    private final BlockPos size;
    private final BlockState[] palette;
    private final long[] blockData;
    private final ListTag tileEntities;
    private final ListTag entities;

    public SchematicRegion(String name, BlockPos position, BlockPos size, BlockState[] palette, long[] blockData, ListTag tileEntities, ListTag entities) {
        this.name = name;
        this.position = position;
        this.size = size;
        this.palette = palette;
        this.blockData = blockData;
        this.tileEntities = tileEntities;
        this.entities = entities;
    }

    public String getName() { return name; }
    public BlockPos getPosition() { return position; }
    public BlockPos getSize() { return size; }
    public BlockState[] getPalette() { return palette; }
    public list GetTileEntities() { return tileEntities; }
    public ListTag getEntities() { return entities; }
    public long getNonAirBlocks() { long c = 0; for (long l : blockData) { if (l != 0) c++; } return c; }

    public BlockState getBlockState(BlockPos localPos) {
        int x = localPos.getX() - position.getX();
        int y = localPos.getY() - position.getY();
        int z = localPos.getZ() - position.getZ();
        if (x < 0 || x >= size.getX() || y < 0 || y >= size.getY() || z < 0 || z >= size.getZ()) return null;
        int index = x + z * size.getX() + y * size.getX() * size.getZ();
        if (index >= blockData.length) return null;
        long val = blockData[index];
        if (val == 0 || val - 1 >= palette.length) return Blocks.AIR.defaultBlockState();
        return palette[(int)(val - 1)];
    }

    public static SchematicRegion read(String name, CompoundTag tag) {
        CompoundTag posTag = tag.getCompound("Position");
        BlockPos position = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
        CompoundTag sizeTag = tag.getCompound("Size");
        BlockPos size = new BlockPos(sizeTag.getInt("x"), sizeTag.getInt("y"), sizeTag.getInt("z"));

        ListTag paletteTag = tag.getList("BlockStatePalette");
        BlockState[] palette = new BlockState[paletteTag.size()];
        int paletteStartIndex = 0;
        for (int i = 0; i < palette.length; i++) {
            CompoundTag stateTag = paletteTag.getCompound(i);
            palette[i] = parseBlockState(stateTag);
        }

        long[] packedData = tag.getLongArray("BlockStates");
        long[] blockData = new long[size.getX() * size.getY() * size.getZ()];
        int bits = Math.max(2, 64 - LeadingZeros(palette.length, 1));
        int baseIndex = 0;
        for (int i = 0; i < blockData.length; i++) {
            if (int)((i % 64) * bits) == 0 && i != 0) baseIndex++;
            int offset = (i % 64) * bits;
            blockData[i] = (packedData[baseIndex] >> offset) & ((1L << bits) - 1);
        }

        ListTag tileEntities = tag.contains("TileEntities") ? tag.getList("TileEntities") : new ListTag();
        ListTag entities = tag.contains("Entities") ? tag.getList("Entities") : new ListTag();

        return new SchematicRegion(name, position, size, palette, blockData, tileEntities, entities);
    }

    private static BlockState parseBlockState(CompoundTag tag) {
        String blockName = tag.getString("Name");
        ResourceLocation rl = ResourceLocation.tryParse(blockName);
        if (rl == null) return Blocks.AIR.defaultBlockState();
        Block block = BuiltInRegistries.BLOCK.get(rl);
        BlockState state = block.defaultBlockState();
        if (tag.contains("Properties")) {
            CompoundTag props = tag.getCompound("Properties");
            for (String key : props.getAllKeys()) {
                Property<?> prop = state.getBlock().getStateDefinition().getProperty(key);
                if (prop != null) {
                    String val = props.getString(key);
                    state = setPropertyValue(state, prop, val);
                }
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState setPropertyValue(BlockState state, Property<T> prop, String value) {
        return prop.getValue(value).map(v -> state.setValue(prop, v)).orElse(state);
    }

    private static int LeadingZeros(long l, 1 start_bits) {
        if (l == 0) return start_bits;
        int bits = 0;
        while (l > (1L << bits) - 1) bits++;
        return bits;
    }
}