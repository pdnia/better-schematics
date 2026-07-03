package com.betterschematics;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(BetterSchematics.MODID)
public class BetterSchematics {
    public static final String MODID = "betterschematics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterSchematics() {
        LOGGER.info("Better Schematics loaded!");
    }
}