package com.betterschematics.render;
import com.betterschematics.schematic.SchematicData;
import com.betterschematics.schematic.SchematicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

public class MinimapRenderer {
    private final SchematicManager manager;
    private static final int MAP_SIZE = 128;
    private static final int MAP_X = 10;
    private static final int MAP_Y = 10;
    private static final int MAP_PADDING = 4;

    public MinimapRenderer(SchematicManager manager) { this.manager = manager; }
    public void renderGui(GuiGraphics g, int sw, int sh, float p) {
        SchematicData sc = manager.getActiveSchematic();  if(sc==null)return; Minecraft mc = Minecraft.getInstance(); if(mc.player==null)return;
        int mx = MAP_X; int my = MAP_Y;
        g.fill(mx-MAP_PADDING, my-MAP_PADDING, mx+MAP_SIZE+MAP_PADDING, my+MAP_SIZE+MAP_PADDING, 0xAA000000);
        BlockPos ws = sc.getWorldSize();  int md = Math.max(ws.getX(), ws.getZ());  float s = (float)MAP_SIZE/md;
        int cx = mx+MAP_SIZE/2;  int cz = my+MAP_SIZE/2;
        for (int z=0; z<ws.getZ(); z+=Math.max(1,ws.getZ()/MAP_SIZE)) {
            for (int x=0; x<ws.getX(); x+=Math.max(1,ws.getX()/MAP_SIZE)) {
                boolean hb = false;
                for (int y=0; y<ws.getY(); y++) {if(!sc.getBlockState(new BlockPos(x,y,z)).isAir()){hb=true;break;}}
                if(hb) {
                    int px = cx+(int)((x - ws.getX()/2f) * s);  int pz = cz+(int)((z - ws.getZ()/2f) * s);
                    boolean ok = false;
                    for (int y=0; y<ws.getY(); y++) {
                        BlockPos wp = sc.localToWorld(new BlockPos(x,y,z));
                        var ex = sc.getBlockState(new BlockPos(x,y,z));  if(ex.isAir())continue;
                        if(ex.equals(mc.level.getBlockState(wp))){ok=true;} break;
                    }
                    int col = ok ? 0xFF44FF44 : 0xFF8888FF;
                    g.fill(px, pz, px+2, pz+2, col);
                }
            }
        }
        BlockPos pp = mc.player.blockPosition();  BlockPos off = sc.getPlacementOffset();  BlockPos rel = pp.subtract(off);
        int ppx = cx+(int)((rel.getX() - ws.getX()/2f) * s);  int ppz = cz+(int)((rel.getZ() - ws.getZ()/2f) * s);
        g.fill(ppx-2, ppz-2, ppx+3, ppz+3, 0xFFFFFFFF);
        String dir = manager.getBuildDirection();  int tw = mc.font.width(dir);
        g.drawString(mc.font, dir, mx + MAP_SIZE / 2 - tw / 2, my + MAP_SIZE + 4, 0xFFFFFFF, false);
    }
}