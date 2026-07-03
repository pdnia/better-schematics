package com.betterschematics.schematic;

import net.minecraft.core.BlockPos;

public class SchematicData {
    private final String name;
    private final BlockPos worldSize;
    private BlockPos placementOffset = BlockPos.ZERO;
    private int rotation = 0;
    private boolean mirrorX = false;

    public SchematicData(String name, BlockPos size) {
        this.name = name;
        this.worldSize = size;
    }

    public String getName() { return name; }
    public BlockPos getWorldSize() { return worldSize; }
    public BlockPos getPlacementOffset() { return placementOffset; }
    public void setPlacementOffset(BlockPos o) { placementOffset = o; }
    public int getRotation() { return rotation; }
    public void setRotation(int r) { rotation = r; }
    public boolean isMirrorX() { return mirrorX; }
    public void setMirrorX(boolean v) { mirrorX = v; }
    public BlockPos localToWorld(BlockPos local) { return local.offset(placementOffset); }
}