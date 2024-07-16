package com.corosus.watut.loader.forge;

import com.corosus.watut.ParticleRegistry;
import com.corosus.watut.WatutMod;
import com.corosus.watut.cloudRendering.ShaderInstanceCloud;
import com.corosus.watut.cloudRendering.SkyChunkManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = WatutMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandlerForge {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void guiRender(RenderGuiEvent.Post event) {
        WatutMod.getPlayerStatusManagerClient().onGuiRender();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onMouse(InputEvent.MouseButton.Post event) {
        WatutMod.getPlayerStatusManagerClient().onMouse(event.getAction() != 0);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onKey(InputEvent.Key event) {
        WatutMod.getPlayerStatusManagerClient().onKey();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onGameTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            WatutMod.getPlayerStatusManagerClient().tickGame();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (event.player.level().isClientSide()) {
                WatutMod.getPlayerStatusManagerClient().tickPlayer(event.player);
            } else {
                WatutMod.getPlayerStatusManagerServer().tickPlayer(event.player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        WatutMod.getPlayerStatusManagerServer().playerLoggedIn(event.getEntity());
    }


    public static void getRegisteredParticles(TextureStitchEvent.Post event) {
        ParticleRegistry.textureAtlasUpload(event.getAtlas());
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            System.out.println("register shaders");
            WatutMod.cloudShader = null;
            WatutMod.cloudShader = new ShaderInstanceCloud(event.getResourceProvider(), new ResourceLocation("watut:position_tex_color_normal_1"),
                    WatutMod.POSITION_TEX_COLOR_NORMAL_VEC3);
            event.registerShader(WatutMod.cloudShader, (shaderInstance -> {}));
        } catch (IOException e) {
            e.printStackTrace();
            //WatutMod.cloudShader = GameRenderer.getPositionTexColorNormalShader();
            //throw new RuntimeException(e);
        }

    }

    @SubscribeEvent
    public void fogRender(ViewportEvent.RenderFog event) {

        int scale = 1;
        boolean inCloud = false;
        BlockPos playerPos = event.getCamera().getBlockPosition().multiply(scale);
        if (SkyChunkManager.instance().getPoint(playerPos.getX(), playerPos.getY(), playerPos.getZ()) != null) {
            inCloud = true;
            //System.out.println(time + " is player in cloud: " + inCloud);
            event.setNearPlaneDistance(0);
            event.setFarPlaneDistance(1);
        }

    }

    @SubscribeEvent
    public void fogColor(ViewportEvent.ComputeFogColor event) {



    }
}
