package com.betterschematics;

import com.betterschematics.config.BetterSchematicsConfig;
import com.betterschematics.render.HUDOverlay;
import com.betterschematics.schematic.SchematicManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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

        // Register key mappings on the MOD event bus
        FMLJavaModLoadingContext.get().getModEventBus().register(BetterSchematicsConfig::registerKeys);
    }

    public static BetterSchematics getInstance() { return instance; }
    public SchematicManager getSchematicManager() { return schematicManager; }
    public HUDOverlay getHudOverlay() { return hudOverlay; }
}