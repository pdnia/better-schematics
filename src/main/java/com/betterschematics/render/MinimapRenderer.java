package com.betterschematics.render;

import com.betterschematics.BetterSchematics;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders a small minimap HUD showing the schematic layout from top-down view,
 * with player position, build direction, and progress.
 */
public class MinimapRenderer {
    private final SchematicManager manager;
    
    private static final int MAP_SIZE = 128;
    private static final int MAP_X = 10;
    private static final int MAP_Y = 10;
    private static final int MAP_PADDING = 4;

    public MinimapRenderer(SchematicManager manager) {
        this.manager = manager;
    }

    public void render(PoseStack poseStack, float partialTick) {
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
    }

    public void renderGui(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTick) {
        SchematicData schematic = manager.getActiveSchematic();
        if (schematic == null) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        int mapX = MAP_X;
        int mapY = MAP_Y;
        
        guiGraphics.fill(mapX - MAP_PADDING, mapY - MAP_PADDING, 
                        mapX + MAP_SIZE + MAP_PADDING, mapY + MAP_SIZE + MAP_PADDING, 
                        0xAA000000);
        
        BlockPos worldSize = schematic.getWorldSize();
        int maxDim = Math.max(worldSize.getX(), worldSize.getZ());
        float scale = (float) MAP_SIZE / maxDim;
        
        int centerX = mapX + MAP_SIZE / 2;
        int centerZ = mapY + MAP_SIZE / 2;
        
        for (int z = 0; z < worldSize.getZ(); z += Math.max(1, worldSize.getZ() / MAP_SIZE)) {
            for (int x = 0; x < worldSize.getX(); x += Math.max(1, worldSize.getX() / MAP_SIZE)) {
                boolean hasBlock = false;
                for (int y = 0; y < worldSize.getY(); y++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    if (!schematic.getBlockState(checkPos).isAir()) {
                        hasBlock = true;
                        break;
                    }
                }
                
                if (hasBlock) {
                    int pixelX = centerX + (int)((x - worldSize.getX() / 2f) * scale);
                    int pixelZ = centerZ + (int)((z - worldSize.getZ() / 2f) * scale);
                    
                    boolean isCorrect = false;
                    boolean isWrong = false;
                    
                    for (int y = 0; y < worldSize.getY(); y++) {
                        BlockPos wp = schematic.localToWorld(new BlockPos(x, y, z));
                        var expected = schematic.getBlockState(new BlockPos(x, y, z));
                        if (expected.isAir()) continue;
                        var actual = mc.level.getBlockState(wp);
                        if (expected.equals(actual)) {
                            isCorrect = true;
                        } else {
                            isWrong = true;
                        }
                        break;
                    }
                    
                    int color;
                    if (isWrong) color = 0xFFFF4444;
                    else if (isCorrect) color = 0xFF44FF44;
                    else color = 0xFF8888FF;
                    
                    guiGraphics.fill(pixelX, pixelZ, pixelX + 2, pixelZ + 2, color);
                }
            }
        }
        
        BlockPos playerPos = mc.player.blockPosition();
        BlockPos offset = schematic.getPlacementOffset();
        BlockPos relPos = playerPos.subtract(offset);
        
        int playerPixelX = centerX + (int)((relPos.getX() - worldSize.getX() / 2f) * scale);
        int playerPixelZ = centerZ + (int)((relPos.getZ() - worldSize.getZ() / 2f) * scale);
        
        guiGraphics.fill(playerPixelX - 2, playerPixelZ - 2, 
                        playerPixelX + 3, playerPixelZ + 3, 
                        0xFFFFFFFF);
        
        String direction = manager.getBuildDirection();
        guiGraphics.drawString(mc.font, direction, screenWidth - mapX - MAP_SIZE - 40, mapY + MAP_SIZE / 2,- mc.font.lineHeight / 2,0xFFFFFFFF);
    }
}