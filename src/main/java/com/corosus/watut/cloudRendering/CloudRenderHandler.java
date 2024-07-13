package com.corosus.watut.cloudRendering;

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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class CloudRenderHandler {

    private boolean cloudsNeedUpdate = true;
    //private VertexBuffer cloudVBO;

    private int sphereIndex = 0;

    private Random rand = new Random();
    private Random rand2 = new Random();

    private boolean mode_triangles = true;
    public Frustum cullingFrustum;
    private int prevCloudX;
    private int prevCloudY;
    private int prevCloudZ;
    private CloudStatus prevCloudsType;
    private Vec3 prevCloudColor;
    private boolean generateClouds = true;
    private ThreadedCloudBuilder threadedCloudBuilder = new ThreadedCloudBuilder();

    private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");

    public void render(int ticks, float partialTicks, PoseStack matrixStackIn, ClientLevel world, Minecraft mc, double viewEntityX, double viewEntityY, double viewEntityZ) {
        //dont use until interface is fixed to add projection matrix
        //this.render(ticks, partialTicks, matrixStackIn, null, world, mc, viewEntityX, viewEntityY, viewEntityZ);
    }
    //PoseStack matrixStackIn, Matrix4f projectionMatrix, float p_172957_, double p_172958_, double viewEntityY, double p_172960_

    //public void render(int ticks, float partialTicks, PoseStack matrixStackIn, Matrix4f projectionMatrix, ClientLevel world, Minecraft mc, double viewEntityX, double viewEntityY, double viewEntityZ) {


    private ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }

    private int getTicks() {
        return (int) getLevel().getGameTime();
    }

    public void renderClouds(PoseStack p_254145_, Matrix4f p_254537_, float p_254364_, double p_253843_, double p_253663_, double p_253795_) {
        //if (true) return;
        if (WatutMod.cloudShader == null) return;
        if (getLevel().effects().renderClouds(getLevel(), getTicks(), p_254364_, p_254145_, p_253843_, p_253663_, p_253795_, p_254537_))
            return;
        float f = this.getLevel().effects().getCloudHeight();
        if (!Float.isNaN(f)) {
            RenderSystem.enableCull();
            //RenderSystem.disableCull();
            //RenderSystem.enableBlend();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            //RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(true);
            float f1 = 12.0F;
            float f2 = 4.0F;
            double d0 = 2.0E-4D;
            double d1 = (double)(((float)this.getTicks() + p_254364_) * 0.03F);
            double d2 = (p_253843_ + d1) / 12.0D;
            double d3 = (double)(f - (float)p_253663_ + 0.33F);
            double d4 = p_253795_ / 12.0D + (double)0.33F;
            d2 -= (double)(Mth.floor(d2 / 2048.0D) * 2048);
            d4 -= (double)(Mth.floor(d4 / 2048.0D) * 2048);
            float f3 = (float)(d2 - (double)Mth.floor(d2));
            float f4 = (float)(d3 / 4.0D - (double)Mth.floor(d3 / 4.0D)) * 4.0F;
            float f5 = (float)(d4 - (double)Mth.floor(d4));
            Vec3 vec3 = this.getLevel().getCloudColor(p_254364_);
            int i = (int)Math.floor(d2);
            int j = (int)Math.floor(d3 / 4.0D);
            int k = (int)Math.floor(d4);
            if (i != this.prevCloudX || j != this.prevCloudY || k != this.prevCloudZ || Minecraft.getInstance().options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4D) {
                this.prevCloudX = i;
                this.prevCloudY = j;
                this.prevCloudZ = k;
                this.prevCloudColor = vec3;
                this.prevCloudsType = Minecraft.getInstance().options.getCloudsType();
                //this.generateClouds = true;
            }



            if (!threadedCloudBuilder.isRunning() && !threadedCloudBuilder.isWaitingToUploadData()) {
                //if (getTicks() % (20 * 5) == 0) {
                    generateClouds = true;
                //}
            }

            Vec3 pos = Minecraft.getInstance().cameraEntity.position();

            if (this.generateClouds) {

                //System.out.println("gen clouds " + getTicks());

                threadedCloudBuilder.setMultiBufferMode(true);
                threadedCloudBuilder.setCloudCount(20 * 20);

                threadedCloudBuilder.setSizeX(40);
                threadedCloudBuilder.setSizeY(30);
                threadedCloudBuilder.setSizeZ(40);

                //initSkyChunksForGrid();

                this.generateClouds = false;

                boolean renderClouds = true;

                threadedCloudBuilder.start();
            }

            if (!threadedCloudBuilder.isRunning()) {
                if (threadedCloudBuilder.isWaitingToUploadData()) {

                    //clean up old buffers, tho ill probably switch to an alternating reusing method
                    //actually doesnt ThreadedVertexBuffer.unbind(); further down make this redundent? - seems to do different things
                    /*for (RenderableCloud renderableCloud : threadedCloudBuilder.getRenderableClouds()) {
                        if (renderableCloud.getVertexBuffer() != null) {
                            renderableCloud.getVertexBuffer().close();
                        }
                    }*/



                    /*threadedCloudBuilder.getRenderableClouds().clear();
                    threadedCloudBuilder.getRenderableClouds().addAll(threadedCloudBuilder.getRenderableCloudsToAdd());*/

                    //System.out.println("============= upload start");
                    /*for (RenderableData renderableData : threadedCloudBuilder.getRenderableClouds()) {
                        ThreadedVertexBuffer cloudBuffer = renderableData.getActiveRenderingVertexBuffer();
                        if (renderableData.getActiveRenderingVertexBuffer() == null) {
                            cloudBuffer = new ThreadedVertexBuffer(ThreadedVertexBuffer.Usage.STATIC);
                            renderableData.setVertexBuffer(cloudBuffer);
                        }
                        cloudBuffer.bind();
                        cloudBuffer.upload(renderableData.getVbo());
                        ThreadedVertexBuffer.unbind();
                    }*/

                    for (SkyChunk skyChunk : SkyChunkManager.instance().getSkyChunks().values()) {
                        RenderableData renderableData = skyChunk.getRenderableData();

                        //TODO: for now we might not need this, but to fix the thread conflict from using upload, it might be needed
                        //renderableData.swapBuffers();
                        renderableData.getActiveRenderingVertexBuffer().bind();
                        renderableData.getActiveRenderingVertexBuffer().upload(renderableData.getVbo());
                        skyChunk.setHasData(true);
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
            p_254145_.translate(-p_253843_, -p_253663_, -p_253795_);

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

                        if (skyChunk.hasData()) {
                            renderableData.getActiveRenderingVertexBuffer().bind();

                            RenderSystem.colorMask(true, true, true, true);

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

            p_254145_.popPose();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
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
