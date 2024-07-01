package com.corosus.watut.cloudRendering;

import com.corosus.watut.ParticleRegistry;
import com.corosus.watut.WatutMod;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
import com.corosus.watut.cloudRendering.threading.ThreadedCloudBuilder;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedVertexBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
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

                threadedCloudBuilder.setMultiBufferMode(true);
                threadedCloudBuilder.setCloudCount(100);

                this.generateClouds = false;

                boolean renderClouds = true;

                threadedCloudBuilder.start();
            }

            if (!threadedCloudBuilder.isRunning()) {
                if (threadedCloudBuilder.isWaitingToUploadData()) {

                    //clean up old buffers, tho ill probably switch to an alternating reusing method
                    //actually doesnt ThreadedVertexBuffer.unbind(); further down make this redundent? - seems to do different things
                    for (RenderableCloud renderableCloud : threadedCloudBuilder.getRenderableClouds()) {
                        if (renderableCloud.getVertexBuffer() != null) {
                            renderableCloud.getVertexBuffer().close();
                        }
                    }

                    threadedCloudBuilder.getRenderableClouds().clear();
                    threadedCloudBuilder.getRenderableClouds().addAll(threadedCloudBuilder.getRenderableCloudsToAdd());

                    for (RenderableCloud renderableCloud : threadedCloudBuilder.getRenderableClouds()) {
                        ThreadedVertexBuffer cloudBuffer = new ThreadedVertexBuffer(ThreadedVertexBuffer.Usage.STATIC);
                        renderableCloud.setVertexBuffer(cloudBuffer);
                        cloudBuffer.bind();
                        cloudBuffer.upload(renderableCloud.getRenderedBuffer());
                        ThreadedVertexBuffer.unbind();
                    }
                    threadedCloudBuilder.setWaitingToUploadData(false);
                }
            }

            /*if (WatutMod.cloudShader != null) {
                RenderSystem.setShader(() -> WatutMod.cloudShader);
            } else {
                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            }*/

            RenderSystem.setShader(() -> WatutMod.cloudShader);
            RenderSystem.setupShaderLights(WatutMod.cloudShader);
            //RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            RenderSystem.setShaderTexture(0, ParticleRegistry.idle.getSprite().atlasLocation());
            //FogRenderer.levelFogColor();
            p_254145_.pushPose();
            //p_254145_.scale(3.0F, 3.0F, 3.0F);
            //p_254145_.scale(10.0F, 10.0F, 10.0F);
            //p_254145_.translate(-f3, f4, -f5);
            p_254145_.translate(-p_253843_, -p_253663_, -p_253795_);

            float timeShort = (this.getTicks() % (20 * 30)) * 3F;

            p_254145_.translate(((timeShort + p_254364_)) * 0.03F, 0, 0);

            boolean renderClouds = true;

            if (renderClouds) {
                if (threadedCloudBuilder.isMultiBufferMode()) {
                    if (threadedCloudBuilder.getRenderableClouds().size() > 0) {
                        for (RenderableCloud cloudBuffer : threadedCloudBuilder.getRenderableClouds()) {
                            cloudBuffer.getVertexBuffer().bind();

                            RenderSystem.colorMask(true, true, true, true);

                            ShaderInstance shaderinstance = RenderSystem.getShader();
                            cloudBuffer.getVertexBuffer().drawWithShader(p_254145_.last().pose(), p_254537_, shaderinstance);

                            VertexBuffer.unbind();
                        }
                    }
                } else {
                    if (threadedCloudBuilder.getCloudBuffer() != null) {
                        threadedCloudBuilder.getCloudBuffer().bind();

                        RenderSystem.colorMask(true, true, true, true);

                        ShaderInstance shaderinstance = RenderSystem.getShader();
                        threadedCloudBuilder.getCloudBuffer().drawWithShader(p_254145_.last().pose(), p_254537_, shaderinstance);

                        VertexBuffer.unbind();
                    }
                }
            }

            p_254145_.popPose();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    private ThreadedBufferBuilder.RenderedBuffer buildClouds(ThreadedBufferBuilder p_234262_, double p_234263_, double p_234264_, double p_234265_, Vec3 p_234266_) {
        float f = 4.0F;
        float f1 = 0.00390625F;
        int i = 8;
        int j = 4;
        float f2 = 9.765625E-4F;
        float f3 = (float) Mth.floor(p_234263_) * 0.00390625F;
        float f4 = (float)Mth.floor(p_234265_) * 0.00390625F;
        float f5 = (float)p_234266_.x;
        float f6 = (float)p_234266_.y;
        float f7 = (float)p_234266_.z;
        float f8 = f5 * 0.9F;
        float f9 = f6 * 0.9F;
        float f10 = f7 * 0.9F;
        float f11 = f5 * 0.7F;
        float f12 = f6 * 0.7F;
        float f13 = f7 * 0.7F;
        float f14 = f5 * 0.8F;
        float f15 = f6 * 0.8F;
        float f16 = f7 * 0.8F;
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        p_234262_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float f17 = (float)Math.floor(p_234264_ / 4.0D) * 4.0F;
        for(int k = -3; k <= 4; ++k) {
            for(int l = -3; l <= 4; ++l) {
                float f18 = (float)(k * 8);
                float f19 = (float)(l * 8);
                if (f17 > -5.0F) {
                    p_234262_.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    p_234262_.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    p_234262_.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    p_234262_.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                }

                if (f17 <= 5.0F) {
                    p_234262_.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    p_234262_.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    p_234262_.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    p_234262_.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                }

                if (k > -1) {
                    for(int i1 = 0; i1 < 8; ++i1) {
                        p_234262_.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        p_234262_.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        p_234262_.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        p_234262_.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                    }
                }

                if (k <= 1) {
                    for(int j2 = 0; j2 < 8; ++j2) {
                        p_234262_.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        p_234262_.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        p_234262_.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        p_234262_.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                    }
                }

                if (l > -1) {
                    for(int k2 = 0; k2 < 8; ++k2) {
                        p_234262_.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        p_234262_.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        p_234262_.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        p_234262_.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                    }
                }

                if (l <= 1) {
                    for(int l2 = 0; l2 < 8; ++l2) {
                        p_234262_.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        p_234262_.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        p_234262_.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        p_234262_.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                    }
                }
            }
        }

        return p_234262_.end();
    }



    private void buildCloud1(ThreadedBufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, float scale) {
        //Vector3f cubePos = new Vector3f(0, 0, 0);

        float radius = 150;
        Vector3f cubePos = new Vector3f((float) (Math.random() * radius - Math.random() * radius),
                (float) (0),
                (float) (Math.random() * radius - Math.random() * radius));
        renderCube(bufferIn, cloudsX, cloudsY, cloudsZ, cloudsColor, cubePos, 1);

        int depth = 1;
        buildCloudNodeRecursive(bufferIn, cloudsX, cloudsY, cloudsZ, cloudsColor, cubePos, 5-depth, depth);
    }

    private void buildCloudNodeRecursive(ThreadedBufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, Vector3f parentCubePos, float scale, int depth) {
        if (depth < 5) {
            for (int i = 0; i < 3; i++) {
                float range = 3;
                float rangeY = 1;
                Vector3f dirRand = new Vector3f((float) (Math.random() * range - Math.random() * range),
                        (float) (Math.random() * rangeY - Math.random() * rangeY),
                        (float) (Math.random() * range - Math.random() * range));
                Vector3f cubePos = parentCubePos.add(new Vector3f(dirRand.x, dirRand.y, dirRand.z));
                renderCube(bufferIn, cloudsX, cloudsY, cloudsZ, cloudsColor, cubePos, 5-depth);

                buildCloudNodeRecursive(bufferIn, cloudsX, cloudsY, cloudsZ, cloudsColor, cubePos, scale, depth + 1);
            }
        }
    }



    private void renderCube(ThreadedBufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, Vector3f cubePos, float scale) {
        Random rand = rand2;

        Quaternionf q2 = new Quaternionf(0, 0, 0, 1);
        int range = 5;
        range = 180;
        /*Vector3f w = new Vector3f();
        w.rota*/
        /*q2.mul(Vector3f.XP.rotationDegrees(rand.nextInt(range)));
        q2.mul(Vector3f.YP.rotationDegrees(rand.nextInt(range)));
        //q2.mul(Vector3f.YP.rotationDegrees(rand.nextInt(45)));
        q2.mul(Vector3f.ZP.rotationDegrees(rand.nextInt(range)));*/



        boolean randRotate = false;

        float particleAlpha = 1F;
        //particleAlpha = (float) Math.random();

        for (Direction dir : Direction.values()) {
            Quaternionf quaternion = dir.getRotation();

            Vector3f[] avector3f3 = new Vector3f[]{
                    new Vector3f(-1.0F, 0.0F, -1.0F),
                    new Vector3f(-1.0F, 0.0F, 1.0F),
                    new Vector3f(1.0F, 0.0F, 1.0F),
                    new Vector3f(1.0F, 0.0F, -1.0F)};


            Vector3f normal = new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ());
            if (randRotate) normal.rotate(q2);
            float normalRange = 0.1F;
            //normal.mul(normalRange);
            //normal.add(1-normalRange, 1-normalRange, 1-normalRange);
            //float uh = 0.55F;
            //normal.add(uh, uh, uh);

            //normal = new Vector3f((float) Math.random(), (float) Math.random(), (float) Math.random());
            //normal = new Vector3f(1, 0, 0);

            for(int i = 0; i < 4; ++i) {
                Vector3f vector3f = avector3f3[i];
                vector3f.rotate(quaternion);
                vector3f.add((float) dir.getStepX(), (float) dir.getStepY(), (float) dir.getStepZ());
                if (randRotate) vector3f.rotate(q2);
                vector3f.mul(scale);
                //vector3f.add((float) cloudsX + 0.5F, (float) cloudsY, (float) cloudsZ + 0.5F);
                vector3f.add((float) 0 + 0.5F, (float) cloudsY, (float) 0 + 0.5F);
                vector3f.add((float) cubePos.x, (float) cubePos.y, (float) cubePos.z);
            }

            //TextureAtlasSprite sprite = ParticleRegistry.cloud_square;
            TextureAtlasSprite sprite = ParticleRegistry.cloud_square.getSprite();

            float f7 = sprite.getU0();
            float f8 = sprite.getU1();
            float f5 = sprite.getV0();
            float f6 = sprite.getV1();

            /*f7 = (float) Math.random();
            f8 = (float) Math.random();
            f5 = (float) Math.random();
            f6 = (float) Math.random();*/

            /*f7 = 0;
            f8 = 1;
            f5 = 0;
            f6 = 1;*/


            float particleRed = (float) cloudsColor.x;
            float particleGreen = (float) cloudsColor.y;
            float particleBlue = (float) cloudsColor.z;

            particleRed = (float) (0.7F + (Math.random() * 0.1F));
            /*particleGreen = (float) Math.random();
            particleBlue = (float) Math.random();*/
            particleGreen = (float) particleRed;
            particleBlue = (float) particleRed;

            if (mode_triangles && false) {
                bufferIn.vertex(avector3f3[0].x(), avector3f3[0].y(), avector3f3[0].z()).uv(f8, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
                bufferIn.vertex(avector3f3[3].x(), avector3f3[3].y(), avector3f3[3].z()).uv(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
                bufferIn.vertex(avector3f3[1].x(), avector3f3[1].y(), avector3f3[1].z()).uv(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();

                bufferIn.vertex(avector3f3[3].x(), avector3f3[3].y(), avector3f3[3].z()).uv(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
                bufferIn.vertex(avector3f3[1].x(), avector3f3[1].y(), avector3f3[1].z()).uv(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
                bufferIn.vertex(avector3f3[2].x(), avector3f3[2].y(), avector3f3[2].z()).uv(f7, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
            } else {
                bufferIn.vertex(avector3f3[0].x(), avector3f3[0].y(), avector3f3[0].z()).uv(f8, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
                bufferIn.vertex(avector3f3[1].x(), avector3f3[1].y(), avector3f3[1].z()).uv(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
                bufferIn.vertex(avector3f3[2].x(), avector3f3[2].y(), avector3f3[2].z()).uv(f7, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
                bufferIn.vertex(avector3f3[3].x(), avector3f3[3].y(), avector3f3[3].z()).uv(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
            }

        }

    }
}
