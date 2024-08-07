package com.corosus.watut.mixin.client;

import com.corosus.watut.WatutMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiRender {

    /*@Inject(method = "render", at = @At("TAIL"))
    private void render(GuiGraphics pGuiGraphics, float pPartialTick, CallbackInfo info) {
        WatutMod.getPlayerStatusManagerClient().onGuiRender();
    }*/

    @Inject(method = "renderChat", at = @At("TAIL"))
    private void render(GuiGraphics p_329202_, DeltaTracker deltaTracker, CallbackInfo info) {
        WatutMod.getPlayerStatusManagerClient().onGuiRender();
    }
}