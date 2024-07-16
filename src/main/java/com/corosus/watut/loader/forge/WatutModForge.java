package com.corosus.watut.loader.forge;

import com.corosus.watut.WatutMod;
import com.corosus.watut.cloudRendering.SkyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod(WatutModForge.MODID)
public class WatutModForge extends WatutMod {
	
    public WatutModForge() {
        super();
        new WatutNetworkingForge();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::gatherData);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandlerForge());
        MinecraftForge.EVENT_BUS.register(new EventHandlerForge2());
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(EventHandlerForge::getRegisteredParticles);
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        WatutNetworkingForge.register();
    }

    /**
     *
     * run runData for me
     *
     * @param event
     */
    private void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        if (event.includeServer()) {

        }
        if (event.includeClient()) {
            gatherClientData(event);
        }
    }

    /**
     *
     * run runData for me
     *
     * @param event
     */
    @OnlyIn(Dist.CLIENT)
    private void gatherClientData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        gen.addProvider(event.includeClient(), new ParticleDataGen(packOutput, existingFileHelper));
    }

    @Override
    public PlayerList getPlayerList() {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList();
    }
}
