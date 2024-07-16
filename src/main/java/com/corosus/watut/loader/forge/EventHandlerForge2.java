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
        int scale = 4;
        boolean inCloud = false;
        BlockPos playerPos = event.getCamera().getBlockPosition().multiply(scale);

        SkyChunk skyChunk = SkyChunkManager.instance().getSkyChunkFromBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        //TODO: this isnt good enough, very delayed often, we need 2 copies of SkyChunk, one thats always accurate to the active rendering vbo
        if (!skyChunk.isBeingBuilt()) {
            if (SkyChunkManager.instance().getPoint(playerPos.getX(), playerPos.getY(), playerPos.getZ()) != null) {
                inCloud = true;
            }

            inFogLastState = inCloud;
        } else {
            inCloud = inFogLastState;
        }

        if (inCloud) {
            SkyChunkManager.instance().getSkyChunkFromBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ()).setClientCameraInCloudInChunk(true);

            event.setNearPlaneDistance(0);
            event.setFarPlaneDistance(5);
            event.setCanceled(true);
        }



    }

    @SubscribeEvent
    public static void fogColor(ViewportEvent.ComputeFogColor event) {

        float brightness = 0.7F;
        /*event.setRed(brightness);
        event.setGreen(brightness);
        event.setBlue(brightness);*/

    }
}
