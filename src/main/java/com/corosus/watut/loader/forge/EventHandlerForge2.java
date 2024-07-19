package com.corosus.watut.loader.forge;

import com.corosus.watut.WatutMod;
import com.corosus.watut.cloudRendering.CloudRenderHandler;
import com.corosus.watut.cloudRendering.SkyChunk;
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
        //BlockPos playerPos = event.getCamera().getBlockPosition().multiply(scale);
        Vec3 vec = event.getCamera().getPosition();

        //TODO: the 0.5 is because my renderer is off by that, fix that then remove this
        //somehow its not a problem at scale 4 visually???

        float adj = 0.5F * scale;
        //adj = 0.0F * scale;
        BlockPos playerPos = new BlockPos(Mth.floor(vec.x + adj) / scale, ((Mth.floor(vec.y + adj))) / scale, Mth.floor(vec.z + adj) / scale);

        //SkyChunk skyChunk = SkyChunkManager.instance().getSkyChunkFromBlockPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

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
