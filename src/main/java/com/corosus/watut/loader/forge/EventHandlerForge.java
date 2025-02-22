package com.corosus.watut.loader.forge;

import com.corosus.watut.ParticleRegistry;
import com.corosus.watut.WatutMod;
import com.corosus.watut.command.CommandWatutReloadJSON;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WatutMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class EventHandlerForge {

    @SubscribeEvent
    
    public void registerCommandsClient(RegisterClientCommandsEvent event) {
        CommandWatutReloadJSON.register(event.getDispatcher());
    }

    @SubscribeEvent
    
    public void onMouse(InputEvent.MouseButton.Post event) {
        WatutMod.getPlayerStatusManagerClient().onMouse(event.getAction() != 0);
    }

    @SubscribeEvent
    
    public void onKey(InputEvent.Key event) {
        WatutMod.getPlayerStatusManagerClient().onKey();
    }

    @SubscribeEvent
    
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
}
