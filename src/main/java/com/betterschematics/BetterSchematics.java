package com.betterschematics;

import com.betterschematics.gui.SchematicOverlay;
import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.render.HUDOverlay;
import com.betterschematics.schematic.SchematicManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

 @Mod("betterschematics")
 public class BetterSchematics {
     public static final String MODID = "betterschematics";

     private static BetterSchematics instance;
     private final SchematicManager schematicManager;
     private final HUDOverlay hudOverlay;

     public BetterSchematics() {
         instance = this;
         this.schematicManager = new SchematicManager();
         this.hudOverlay = new HUDOverlay(schematicManager);
        // Manually register FORGE bus handlers
        MinecraftForge.EVENT_BUS.register(SchematicOverlay.class);
     }

     public static BetterSchematics getInstance() { return instance; }
     public SchematicManager getSchematicManager() { return schematicManager; }
     public HUDOverlay getHudOverlay() { return hudOverlay; }
 }