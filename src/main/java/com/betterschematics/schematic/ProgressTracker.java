package com.betterschematics.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ProgressTracker {
    private SchematicData schematic;
    private long totalBlocks;
    private long placedBlocks;

    public void setSchematic(SchematicData s) {
        this.schematic = s;
        this.totalBlocks = 0;
        this.placedBlocks = 0;
        if (s != null) {
            SchematicRegion r = s.getMainRegion();
            if (r != null) totalBlocks = r.getNonAirBlocks();
        }
    }

    public double getPercentComplete() {
        if (totalBlocks == 0) return 0;
        return (placedBlocks * 100.0) / totalBlocks;
    }

    public void markPlaced(BlockPos pos) {
        placedBlocks = Math.min(placedBlocks + 1, totalBlocks);
    }
}
