package com.betterschematics.mixin;

import com.betterschematics.BetterSchematics;
import com.betterschematics.render.HUDOverlay;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderGui(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        try {
            BetterSchematics bs = BetterSchematics.getInstance();
            if (bs != null) {
                HUDOverlay hud = bs.getHudOverlay();
                if (hud != null) {
                    hud.render(guiGraphics);
                }
            }
        } catch (Exception ignored) {}
    }
}
