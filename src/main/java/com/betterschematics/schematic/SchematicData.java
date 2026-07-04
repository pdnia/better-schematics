package com.betterschematics.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchematicData {
    public String name = "Unknown";
    public String author = "Unknown";
    public int minecraftDataVersion;
    public long totalBlocks;
    public List<SchematicRegion> regions = new ArrayList<>();

    public SchematicRegion getMainRegion() {
        return regions.isEmpty() ? null : regions.get(0);
    }

    public static SchematicData load(File file) throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        CompoundTag root = NbtIo.readCompressed(dis, NbtAccounter.unlimitedHeap());
        dis.close();
        SchematicData data = new SchematicData();
        data.minecraftDataVersion = root.getInt("MinecraftDataVersion").orElse(0);
        if (root.contains("Metadata")) {
            CompoundTag meta = root.getCompound("Metadata").orElse(new CompoundTag());
            data.name = meta.getString("Name").orElse("Unknown");
            data.author = meta.getString("Author").orElse("Unknown");
        }
        if (root.contains("Regions")) {
            CompoundTag regionsTag = root.getCompound("Regions").orElse(new CompoundTag());
            for (String key : regionsTag.getAllKeys()) {
                CompoundTag regionTag = regionsTag.getCompound(key).orElse(null);
                if (regionTag != null) {
                    data.regions.add(SchematicRegion.read(key, regionTag));
                }
            }
        }
        data.totalBlocks = root.getLong("TotalBlocks").orElse(0L);
        return data;
    }
}
