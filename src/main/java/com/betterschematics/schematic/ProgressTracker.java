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
    private SchematicData schematic;
    private long totalBlocks = 0;
    private long placedBlocks = 0;
    private long correctBlocks = 0;
    private long wrongBlocks = 0;
    private double percentComplete = 0;

    public ProgressTracker() {}

    public void setSchematic(SchematicData schematic) { this.schematic = schematic; recalculate(); }
    public long getTotalBlocks() { return totalBlocks; }
    public long getPlacedBlocks() { return placedBlocks; }
    public long getCorrectBlocks() { return correctBlocks; }
    public long getWrongBlocks() { return wrongBlocks; }
    public double getPercentComplete() { return percentComplete; }

    public void recalculate() {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if (world == null || schematic == null) { reset(); return; }

        totalBlocks = 0; placedBlocks = 0; correctBlocks = 0; wrongBlocks = 0;
        SchematicRegion region = schematic.getMainRegion();
        if (region == null) return;

        BlockPos size = region.getSize();
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos local = new BlockPos(x, y, z);
                    BlockState expected = region.getBlockState(local);
                    if (expected == null || expected.isAir()) continue;
                    totalBlocks++;
                    BlockState actual = world.getBlockState(local);
                    if (actual.isAir()) continue;
                    placedBlocks++;
                    if (expected.equals(actual)) correctBlocks++; else wrongBlocks++;
                }
            }
        }
        percentComplete = totalBlocks > 0 ? (100.0 * placedBlocks / totalBlocks) : 100.0;
    }

    private void reset() { totalBlocks = 0; placedBlocks = 0; correctBlocks = 0; wrongBlocks = 0; percentComplete = 0; }
}