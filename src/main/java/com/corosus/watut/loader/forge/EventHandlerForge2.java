package com.corosus.watut.loader.forge;

import com.corosus.watut.WatutMod;
import com.corosus.watut.cloudRendering.SkyChunk;
import com.corosus.watut.cloudRendering.SkyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WatutMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandlerForge2 {

    public static boolean inFogLastState = false;

    @SubscribeEvent
    public static void fogRender(ViewportEvent.RenderFog event) {

        //TODO: not being applied correctly, the way the grid adjust to scale most likely is wrong
        int scale = 1;
        boolean inCloud = false;
        BlockPos playerPos = event.getCamera().getBlockPosition().multiply(scale);

        //SkyChunk skyChunk = SkyChunkManager.instance().getSkyChunkFromBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        //TODO: this isnt good enough, very delayed often, we need 2 copies of SkyChunk, one thats always accurate to the active rendering vbo
        /*if (!skyChunk.isBeingBuilt()) {
            inFogLastState = inCloud;
        } else {
            inCloud = inFogLastState;
        }*/

        if (SkyChunkManager.instance().getPoint(true, playerPos.getX(), playerPos.getY(), playerPos.getZ()) != null) {
            inCloud = true;
            inFogLastState = true;
        } else {
            inFogLastState = false;
        }

        if (inCloud) {
            SkyChunkManager.instance().getSkyChunkFromBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ()).setClientCameraInCloudInChunk(true);

            event.setNearPlaneDistance(0);
            event.setFarPlaneDistance(2);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void fogColor(ViewportEvent.ComputeFogColor event) {

        if (inFogLastState) {
            float brightness = 0.7F;
            event.setRed(brightness);
            event.setGreen(brightness);
            event.setBlue(brightness);
        }

    }
}
