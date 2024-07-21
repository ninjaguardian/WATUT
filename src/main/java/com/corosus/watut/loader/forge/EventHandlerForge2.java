package com.corosus.watut.loader.forge;

import com.corosus.watut.WatutMod;
import com.corosus.watut.cloudRendering.SkyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WatutMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandlerForge2 {

    public static boolean inFogLastState = false;

    @SubscribeEvent
    public static void fogRender(ViewportEvent.RenderFog event) {

        int scale = WatutMod.cloudRenderHandler.getThreadedCloudBuilder().getScale();
        boolean inCloud = false;
        Vec3 vec = event.getCamera().getPosition();
        BlockPos playerPos = new BlockPos(Mth.floor(vec.x / scale), ((Mth.floor(vec.y / scale))), Mth.floor(vec.z / scale));

        //SkyChunk skyChunk = SkyChunkManager.instance().getSkyChunkFromBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        if (SkyChunkManager.instance().getPoint(true, playerPos.getX(), playerPos.getY(), playerPos.getZ()) != null) {
            inCloud = true;
            inFogLastState = true;
        } else {
            inFogLastState = false;
        }

        if (inCloud) {
            SkyChunkManager.instance().getSkyChunkFromBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ()).setClientCameraInCloudForSkyChunk(true);

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
