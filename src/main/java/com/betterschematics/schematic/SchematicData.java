package com.betterschematics.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Parsed .litematic schematic with regions and block data.
 */
public class SchematicData {
    public int minecraftDataVersion;
    public String name = "Unnamed";
    public String author = "Unknown";
    public long totalBlocks;
    public final List<SchematicRegion> regions = new ArrayList<>();

    public SchematicRegion getMainRegion() {
        return regions.isEmpty() ? null : regions.get(0);
    }

    public static SchematicData load(File file) throws IOException {
        CompoundTag root;
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(
                    new GZIPInputStream(new FileInputStream(file))))) {
            root = NbtIo.read(dis);
        }
        SchematicData data = new SchematicData();
        data.minecraftDataVersion = root.getInt("MinecraftDataVersion");
        
        if (root.contains("Metadata")) {
            CompoundTag meta = root.getCompound("Metadata");
            data.name = meta.getString("Name");
            data.author = meta.getString("Author");
            if (meta.contains("TotalBlocks")) data.totalBlocks = meta.getLong("TotalBlocks");
        }
        
        if (root.contains("Regions")) {
            CompoundTag regionsTag = root.getCompound("Regions");
            for (String regionName : regionsTag.getAllKeys()) {
                CompoundTag regionTag = regionsTag.getCompound(regionName);
                data.regions.add(SchematicRegion.read(regionName, regionTag));
            }
        }
        return data;
    }

    @Override
    public String toString() {
        return String.format("Schematic{name='%s', regions=%d}", name, regions.size());
    }
}

/**
 * A single region within a .litematic file.
 */
public class SchematicRegion {
    public String name;
    public int originX, originY, originZ;
    public int width, height, length;
    public long totalVolume;
    public long nonAirBlocks;

    /** Block states in YZX order: index = (y * length + z) * width + x */
    public BlockState[] blocks;

    public static SchematicRegion read(String name, CompoundTag tag) {
        SchematicRegion r = new SchematicRegion();
        r.name = name;
        
        CompoundTag pos = tag.getCompound("Position");
        r.originX = pos.getInt("x");
        r.originY = pos.getInt("y");
        r.originZ = pos.getInt("z");
        
        CompoundTag sz = tag.getCompound("Size");
        r.width = sz.getInt("x");
        r.height = sz.getInt("y");
        r.length = sz.getInt("z");
        
        r.totalVolume = (long) r.width * r.height * r.length;
        
        // Parse palette
        var paletteList = tag.getList("BlockStatePalette", 10);
        java.util.ArrayList<CompoundTag> palette = new java.util.ArrayList<>();
        for (int i = 0; i < paletteList.size(); i++) {
            palette.add(paletteList.getCompound(i));
        }
        
        // Unpack block states
        long[] packed = tag.getLongArray("BlockStates");
        int totalBlocks = r.width * r.height * r.length;
        r.blocks = unpackBlockStates(packed, totalBlocks, palette);
        
        long nonAir = 0;
        for (BlockState bs : r.blocks) {
            if (bs != null && !bs.isAir()) nonAir++;
        }
        r.nonAirBlocks = nonAir;
        
        return r;
    }

    private static BlockState[] unpackBlockStates(long[] packed, int totalBlocks, java.util.ArrayList<CompoundTag> palette) {
        if (totalBlocks == 0 || packed.length == 0) return new BlockState[0];
        
        int bitsPerBlock = Math.max(2, 32 - Integer.numberOfLeadingZeros(palette.size() - 1));
        long mask = (1L << bitsPerBlock) - 1;
        
        BlockState[] result = new BlockState[totalBlocks];
        
        for (int i = 0; i < totalBlocks; i++) {
            int bitIndex = i * bitsPerBlock;
            int arrayIndex = bitIndex / 64;
            int bitOffset = bitIndex % 64;
            
            long value;
            if (bitOffset + bitsPerBlock <= 64) {
                value = (packed[arrayIndex] >>> bitOffset) & mask;
            } else {
                int bitsFromFirst = 64 - bitOffset;
                int bitsFromSecond = bitsPerBlock - bitsFromFirst;
                long first = (packed[arrayIndex] >>> bitOffset) & ((1L << bitsFromFirst) - 1);
                long second = (arrayIndex + 1 < packed.length)
                        ? (packed[arrayIndex + 1] & ((1L << bitsFromSecond) - 1))
                        : 0;
                value = first | (second << bitsFromFirst);
            }
            
            int paletteIndex = (int) value;
            if (paletteIndex >= 0 && paletteIndex < palette.size()) {
                result[i] = parseBlockState(palette.get(paletteIndex));
            }
        }
        
        return result;
    }

    private static BlockState parseBlockState(CompoundTag tag) {
        if (tag == null || !tag.contains("Name")) return Blocks.AIR.defaultBlockState();
        String name = tag.getString("Name");
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(name);
        if (rl == null) return Blocks.AIR.defaultBlockState();
        net.minecraft.world.level.block.Block block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(rl);
        if (block == null) return Blocks.AIR.defaultBlockState();
        BlockState state = block.defaultBlockState();
        if (tag.contains("Properties")) {
            CompoundTag props = tag.getCompound("Properties");
            for (String key : props.getAllKeys()) {
                var property = block.getStateDefinition().getProperty(key);
                if (property != null) {
                    String val = props.getString(key);
                    java.util.Optional<?> opt = property.getValue(val);
                    if (opt.isPresent()) {
                        state = setProperty(state, property, opt.get());
                    }
                }
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState setProperty(BlockState state, net.minecraft.world.level.block.state.properties.Property<?> prop, Object value) {
        return state.setValue((net.minecraft.world.level.block.state.properties.Property<T>) prop, (T) value);
    }

    public BlockState getBlock(int x, int y, int z) {
        if (blocks == null) return null;
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= length) return null;
        int idx = (y * length + z) * width + x;
        return (idx >= 0 && idx < blocks.length) ? blocks[idx] : null;
    }
}