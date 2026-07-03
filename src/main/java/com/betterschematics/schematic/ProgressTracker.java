package com.betterschematics.schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tracks build progress by comparing expected schematic blocks with the actual world.
 */
public class ProgressTracker {
    private final SchematicData schematic;
    private long totalBlocks;
    private long placedBlocks;
    private long correctBlocks;
    private long wrongBlocks;
    private double percentComplete;

    public ProgressTracker(SchematicData schematic) {
        this.schematic = schematic;
        recalculate();
    }

    public void recalculate() {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if (world == null || schematic == null) {
            totalBlocks = 0;
            placedBlocks = 0;
            correctBlocks = 0;
            wrongBlocks = 0;
            percentComplete = 0;
            return;
        }

        long total = 0;
        long correct = 0;
        
        BlockPos worldSize = schematic.getWorldSize();
        
        for (int y = 0; y < worldSize.getY(); y++) {
            for (int z = 0; z < worldSize.getZ(); z++) {
                for (int x = 0; x < worldSize.getX(); x++) {
                    BlockPos localPos = new BlockPos(x, y, z);
                    BlockState expected = schematic.getBlockState(localPos);
                    if (expected.isAir()) continue;
                    
                    total++;
                    BlockPos worldPos = schematic.localToWorld(localPos);
                    BlockState actual = world.getBlockState(worldPos);
                    if (expected.equals(actual)) {
                        correct++;
                    }
                }
            }
        }
        
        this.totalBlocks = total;
        this.correctBlocks = correct;
        this.wrongBlocks = total - correct;
        this.placedBlocks = correct;
        this.percentComplete = total > 0 ? (double) correct / total * 100.0 : 0;
    }

    public long getTotalBlocks() { return totalBlocks; }
    public long getPlacedBlocks() { return placedBlocks; }
    public long getCorrectBlocks() { return correctBlocks; }
    public long getWrongBlocks() { return wrongBlocks; }
    public double getPercentComplete() { return percentComplete; }
    public String getPercentString() {
        return String.format("%.1f%%", percentComplete);
    }
}