package com.betterschematics.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))))) {
            root = NbtIo.read(dis);
        }
        SchematicData data = new SchematicData();
        data.minecraftDataVersion = root.getInt("MinecraftDataVersion");
        if (root.contains("Metadata")) {
            CompoundTag meta = root.getCompound("Metadata");
            data.name = meta.getString("Name");
            data.author = meta.getString("Author");
        }
        if (root.contains("Regions")) {
            CompoundTag regionsTag = root.getCompound("Regions");
            for (String key : regionsTag.getAllKeys()) {
                CompoundTag regionTag = regionsTag.getCompound(key);
                SchematicRegion region = SchematicRegion.read(key, regionTag);
                data.regions.add(region);
            }
        }
        data.totalBlocks = root.getLong("TotalBlocks");
        return data;
    }
}