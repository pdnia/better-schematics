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

import java.util.*;

/**
 * Represents a single region within a .litematic schematic.
 * Stores block palette and packed block state data.
 */
public class RegionData {
    private final String name;
    private final BlockPos position;
    private final BlockPos size;
    private final BlockState[] palette;
    private final int[] blockData;
    
    private final ListTag tileEntities;
    private final ListTag entities;

    public RegionData(String name, BlockPos position, BlockPos size,
                      BlockState[] palette, int[] blockData,
                      ListTag tileEntities, ListTag entities) {
        this.name = name;
        this.position = position;
        this.size = size;
        this.palette = palette;
        this.blockData = blockData;
        this.tileEntities = tileEntities;
        this.entities = entities;
    }

    public static RegionData read(String name, CompoundTag tag) {
        CompoundTag posTag = tag.getCompound("Position");
        BlockPos position = new BlockPos(
            posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));

        CompoundTag sizeTag = tag.getCompound("Size");
        BlockPos size = new BlockPos(
            sizeTag.getInt("x"), sizeTag.getInt("y"), sizeTag.getInt("z"));

        ListTag paletteTag = tag.getList("BlockStatePalette", 10);
        BlockState[] palette = new BlockState[paletteTag.size()];
        for (int i = 0; i < paletteTag.size(); i++) {
            palette[i] = parseBlockState(paletteTag.getCompound(i));
        }

        long[] packedBlockStates = tag.getLongArray("BlockStates");
        int totalBlocks = size.getX() * size.getY() * size.getZ();
        int bitsPerBlock = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(palette.length - 1));
        int[] unpacked = unpackBlockStates(packedBlockStates, bitsPerBlock, totalBlocks);

        ListTag tileEntities = tag.getList("TileEntities", 10);
        ListTag entities = tag.getList("Entities", 10);

        return new RegionData(name, position, size, palette, unpacked, tileEntities, entities);
    }

    private static BlockState parseBlockState(CompoundTag tag) {
        String blockName = tag.getString("Name");
        ResourceLocation rl = ResourceLocation.tryParse(blockName);
        if (rl == null) return Blocks.AIR.defaultBlockState();
        
        Block block = BuiltInRegistries.BLOCK.get(rl);
        if (block == null) return Blocks.AIR.defaultBlockState();
        
        BlockState state = block.defaultBlockState();
        
        if (tag.contains("Properties")) {
            CompoundTag props = tag.getCompound("Properties");
            for (String key : props.getAllKeys()) {
                Property<?> property = block.getStateDefinition().getProperty(key);
                if (property != null) {
                    state = setPropertyValue(state, property, props.getString(key));
                }
            }
        }
        
        return state;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Comparable<T>> BlockState setPropertyValue(
            BlockState state, Property<T> property, String value) {
        return state.setValue(property, property.getValue(value)
            .orElse(property.getPossibleValues().iterator().next()));
    }

    private static int[] unpackBlockStates(long[] packed, int bitsPerBlock, int totalBlocks) {
        int[] result = new int[totalBlocks];
        int valuesPerLong = 64 / bitsPerBlock;
        long mask = (1L << bitsPerBlock) - 1;
        
        for (int i = 0; i < totalBlocks; i++) {
            int longIndex = i / valuesPerLong;
            int bitOffset = (i % valuesPerLong) * bitsPerBlock;
            if (longIndex < packed.length) {
                result[i] = (int) ((packed[longIndex] >>> bitOffset) & mask);
            }
        }
        
        return result;
    }

    public BlockState getBlockState(BlockPos localPos) {
        BlockPos relPos = localPos.subtract(position);
        int x = relPos.getX();
        int y = relPos.getY();
        int z = relPos.getZ();
        
        if (x < 0 || x >= size.getX() || 
            y < 0 || y >= size.getY() || 
            z < 0 || z >= size.getZ()) {
            return null;
        }
        
        int index = (y * size.getZ() + z) * size.getX() + x;
        if (index >= 0 && index < blockData.length) {
            int paletteIndex = blockData[index];
            if (paletteIndex >= 0 && paletteIndex < palette.length) {
                return palette[paletteIndex];
            }
        }
        
        return Blocks.AIR.defaultBlockState();
    }

    public void collectMaterials(Map<BlockState, Long> materials) {
        for (int i = 0; i < blockData.length; i++) {
            BlockState state = palette[blockData[i]];
            if (!state.isAir()) {
                BlockState defaultState = state.getBlock().defaultBlockState();
                materials.merge(defaultState, 1L, Long::sum);
            }
        }
    }

    public void forEachBlock(BlockVisitor visitor) {
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    int index = (y * size.getZ() + z) * size.getX() + x;
                    BlockState state = palette[blockData[index]];
                    BlockPos localPos = new BlockPos(
                        position.getX() + x,
                        position.getY() + y,
                        position.getZ() + z
                    );
                    visitor.visit(localPos, state);
                }
            }
        }
    }

    @FunctionalInterface
    public interface BlockVisitor {
        void visit(BlockPos localPos, BlockState state);
    }

    public String getName() { return name; }
    public BlockPos getPosition() { return position; }
    public BlockPos getSize() { return size; }
    public int getTotalBlocks() { return blockData.length; }
    public long getNonAirBlocks() {
        long count = 0;
        for (int i = 0; i < blockData.length; i++) {
            if (!palette[blockData[i]].isAir()) count++;
        }
        return count;
    }
}