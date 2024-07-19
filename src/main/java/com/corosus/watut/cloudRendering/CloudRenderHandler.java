package com.corosus.watut.cloudRendering;

import com.corosus.coroutil.util.CULog;
import com.corosus.watut.ParticleRegistry;
import com.corosus.watut.WatutMod;
import com.corosus.watut.cloudRendering.threading.ThreadedCloudBuilder;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedVertexBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class CloudRenderHandler {

    private Random rand = new Random();

    private boolean mode_triangles = true;
    public Frustum cullingFrustum;
    private boolean generateClouds = true;
    private ThreadedCloudBuilder threadedCloudBuilder = new ThreadedCloudBuilder();

    private ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }

    private int getTicks() {
        return (int) getLevel().getGameTime();
    }

    public ThreadedCloudBuilder getThreadedCloudBuilder() {
        return threadedCloudBuilder;
    }

    public void renderClouds(PoseStack p_254145_, Matrix4f p_254537_, float p_254364_, double p_253843_, double p_253663_, double p_253795_) {
        //if (true) return;
        if (WatutMod.cloudShader == null) return;
        if (getLevel().effects().renderClouds(getLevel(), getTicks(), p_254364_, p_254145_, p_253843_, p_253663_, p_253795_, p_254537_))
            return;

        RenderSystem.enableCull();
        //RenderSystem.disableCull();
        //RenderSystem.enableBlend();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        //RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(true);

        if (!threadedCloudBuilder.isRunning() && !threadedCloudBuilder.isWaitingToUploadData()) {
            if (getTicks() % (20 * 5) == 0) {
                generateClouds = true;
            }
        }

        if (this.generateClouds) {

            //System.out.println("gen clouds " + getTicks());

            threadedCloudBuilder.setMultiBufferMode(true);
            threadedCloudBuilder.setCloudCount(20 * 20);
            threadedCloudBuilder.setCloudCount(10 * 10);
            //threadedCloudBuilder.setCloudCount(5 * 5);
            threadedCloudBuilder.setCloudCount(1);

            threadedCloudBuilder.setSizeX(40);
            threadedCloudBuilder.setSizeY(30);
            threadedCloudBuilder.setSizeZ(40);

            int scale = 4;
            threadedCloudBuilder.setCloudsY(200 / scale);

            //initSkyChunksForGrid();

            this.generateClouds = false;

            boolean renderClouds = true;

            threadedCloudBuilder.start();
        }

        if (!threadedCloudBuilder.isRunning()) {
            if (threadedCloudBuilder.isWaitingToUploadData()) {

                for (SkyChunk skyChunk : SkyChunkManager.instance().getSkyChunks().values()) {

                    RenderableData renderableData = skyChunk.getRenderableData();

                    //TODO: for now we might not need this, but to fix the thread conflict from using upload, it might be needed
                    //renderableData.swapBuffers();
                    renderableData.getActiveRenderingVertexBuffer().bind();
                    renderableData.getActiveRenderingVertexBuffer().upload(renderableData.getVbo());
                    skyChunk.setInitialized(true);
                    //skyChunk.setCameraPosForRender(skyChunk.getCameraPosDuringBuild());
                    skyChunk.pushNewOffThreadDataToMainThread();
                    skyChunk.setBeingBuilt(false);
                    ThreadedVertexBuffer.unbind();

                }

                //System.out.println("=============== upload done");
                threadedCloudBuilder.setWaitingToUploadData(false);
            }
        }

            /*if (WatutMod.cloudShader != null) {
                RenderSystem.setShader(() -> WatutMod.cloudShader);
            } else {
                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            }*/

        RenderSystem.setShader(() -> WatutMod.cloudShader);
        //RenderSystem.setupShaderLights(WatutMod.cloudShader);
        long time = getTicks();
        float speed = 0.05F;
        float x = (float) -Math.sin(time * speed);
        float z = (float) Math.cos(time * speed);

        if (WatutMod.cloudShader.LIGHT0_DIRECTION2 != null) {

            //WatutMod.cloudShader.LIGHT0_DIRECTION2.set(new Vector3f(x, 1, z));
            //WatutMod.cloudShader.LIGHT1_DIRECTION2.set(new Vector3f(0, x, 0));
                /*WatutMod.cloudShader.LIGHT0_DIRECTION2.set(new Vector3f(1, 0, 1));
                WatutMod.cloudShader.LIGHT1_DIRECTION2.set(new Vector3f(x, 0, z));*/

            WatutMod.cloudShader.LIGHT0_DIRECTION2.set(new Vector3f(1, 0, 1));
            WatutMod.cloudShader.LIGHT1_DIRECTION2.set(new Vector3f(1, 0, 1));
        }
        //RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
        RenderSystem.setShaderTexture(0, ParticleRegistry.idle.getSprite().atlasLocation());
        //FogRenderer.levelFogColor();
        p_254145_.pushPose();
        //p_254145_.scale(3.0F, 3.0F, 3.0F);
        //p_254145_.scale(10.0F, 10.0F, 10.0F);
        //p_254145_.translate(-f3, f4, -f5);

        //TODO: remove use
        //p_254145_.translate(-p_253843_, -p_253663_, -p_253795_);

        float timeShort = (this.getTicks() % (20 * 30)) * 3F;

        //p_254145_.translate(((timeShort + p_254364_)) * 0.03F, 0, 0);

        boolean renderClouds = true;

        if (renderClouds) {
            if (threadedCloudBuilder.isMultiBufferMode()) {
                //System.out.println("render start");
                    /*if (threadedCloudBuilder.getRenderableClouds().size() > 0) {
                        Random rand3 = new Random();
                        for (RenderableData cloudBuffer : threadedCloudBuilder.getRenderableClouds()) {
                            cloudBuffer.getActiveRenderingVertexBuffer().bind();

                            RenderSystem.colorMask(true, true, true, true);

                            if (rand3.nextFloat() > 0.993F && false) {
                                Vector3f vec = new Vector3f(cloudBuffer.getLightningPos());
                                vec.add(rand3.nextFloat() * threadedCloudBuilder.getSizeX(), rand3.nextFloat() * threadedCloudBuilder.getSizeY(), rand3.nextFloat() * threadedCloudBuilder.getSizeZ());
                                WatutMod.cloudShader.LIGHTNING_POS.set(vec);
                            } else {
                                WatutMod.cloudShader.LIGHTNING_POS.set(new Vector3f(0, -999, 0));
                            }


                            ShaderInstance shaderinstance = RenderSystem.getShader();
                            cloudBuffer.getActiveRenderingVertexBuffer().drawWithShader(p_254145_.last().pose(), p_254537_, shaderinstance);

                            VertexBuffer.unbind();
                        }
                    }*/

                Random rand3 = new Random();

                for (SkyChunk skyChunk : SkyChunkManager.instance().getSkyChunks().values()) {
                    RenderableData renderableData = skyChunk.getRenderableData();

                    Vec3 vecCamVBO = skyChunk.getCameraPosForRender();
                    //Vec3 vecCam = Minecraft.getInstance().cameraEntity.position();
                    Vec3 vecCamLive = new Vec3(p_253843_, p_253663_, p_253795_);
                    WatutMod.cloudShader.VBO_RENDER_POS.set(new Vector3f((float)(vecCamVBO.x - vecCamLive.x), (float) (vecCamVBO.y - vecCamLive.y), (float) (vecCamVBO.z - vecCamLive.z)));

                    if (skyChunk.isInitialized()) {
                        renderableData.getActiveRenderingVertexBuffer().bind();

                        RenderSystem.colorMask(true, true, true, true);
                        if (skyChunk.isClientCameraInCloudInChunk()) {
                            skyChunk.setClientCameraInCloudInChunk(false);
                            RenderSystem.disableCull();
                        }

                        if (rand3.nextFloat() > 0.993F && false) {
                            Vector3f vec = new Vector3f(renderableData.getLightningPos());
                            //TODO: skychunk changes, needs to be reworked, lightning could bleed into another chunk, for now just contain within chunk
                            //vec.add(rand3.nextFloat() * threadedCloudBuilder.getSizeX(), rand3.nextFloat() * threadedCloudBuilder.getSizeY()/* + rand3.nextFloat(80)*/, rand3.nextFloat() * threadedCloudBuilder.getSizeZ());
                            vec.add(rand3.nextFloat() * SkyChunk.size, rand3.nextFloat() * SkyChunk.size, rand3.nextFloat() * SkyChunk.size);
                            WatutMod.cloudShader.LIGHTNING_POS.set(vec);
                        } else {
                            WatutMod.cloudShader.LIGHTNING_POS.set(new Vector3f(0, -999, 0));
                        }

                        ShaderInstance shaderinstance = RenderSystem.getShader();
                        renderableData.getActiveRenderingVertexBuffer().drawWithShader(p_254145_.last().pose(), p_254537_, shaderinstance);
                        VertexBuffer.unbind();

                        RenderSystem.enableCull();
                    }
                }

                //System.out.println("render finish");
            }/* else {
                    if (threadedCloudBuilder.getCloudBuffer() != null) {
                        threadedCloudBuilder.getCloudBuffer().bind();

                        RenderSystem.colorMask(true, true, true, true);

                        ShaderInstance shaderinstance = RenderSystem.getShader();
                        threadedCloudBuilder.getCloudBuffer().drawWithShader(p_254145_.last().pose(), p_254537_, shaderinstance);

                        VertexBuffer.unbind();
                    }
                }*/
        }

            /*if (time % 20 == 0) {
                boolean inCloud = false;
                if (Minecraft.getInstance().player != null) {
                    Player player = Minecraft.getInstance().player;
                    int scale = 1;
                    BlockPos playerPos = player.blockPosition().multiply(scale);
                    if (SkyChunkManager.instance().getPoint(playerPos.getX(), playerPos.getY(), playerPos.getZ()) != null) {
                        inCloud = true;
                        System.out.println(time + " is player in cloud: " + inCloud);
                    }

                }

            }*/


        p_254145_.popPose();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    //init skychunks so buffers are made on render thread
    //TODO: find a more elegant solution maybe?
    /*public void initSkyChunksForGrid() {
        int columns = 20;
        int startX = 0;
        int startY = threadedCloudBuilder.getCloudsY();
        int startZ = 0;
        int sizeX = columns * threadedCloudBuilder.getSizeX();
        int sizeY = threadedCloudBuilder.getSizeY();
        int sizeZ = columns * threadedCloudBuilder.getSizeZ();
        for (int x = startX; x <= startX + sizeX; x++) {
            for (int z = startZ; z <= startZ + sizeZ; z++) {
                for (int y = startY; y <= startY + sizeY; y++) {
                    SkyChunkManager.instance().getSkyChunk(x >> 4, y >> 4, z >> 4);
                }
            }
        }
    }*/
}
