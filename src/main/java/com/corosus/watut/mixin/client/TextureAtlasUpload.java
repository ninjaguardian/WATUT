package com.corosus.watut.mixin.client;

import com.corosus.watut.ParticleRegistry;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.stream.Stream;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasUpload {

    @Inject(method = "reload", at = @At("TAIL"))
    private void upload(TextureAtlas.Preparations pPreparations, CallbackInfo info) {
        ParticleRegistry.textureAtlasUpload((TextureAtlas)(Object)this);
    }
}