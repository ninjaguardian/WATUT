package com.corosus.watut.mixin.client;

import com.corosus.watut.WatutMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class RenderSkyOverride {

    @Redirect(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V"))
    public void renderClouds(LevelRenderer instance, PoseStack poseStack, Matrix4f l, float i1, double f1, double f2, double d0) {
        //CULog.dbg("renderClouds hook");
        //WatutMod.cloudRenderHandler.render(poseStack, l, i1, f1, f2, d0);
        WatutMod.cloudRenderHandler.renderClouds(poseStack, l, i1, f1, f2, d0);

        //vanilla
        //instance.renderClouds(poseStack, l, i1, f1, f2, d0);
    }
}