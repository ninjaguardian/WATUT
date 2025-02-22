package com.corosus.watut.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class ParticleRotating extends TextureSheetParticle {

    public boolean useCustomRotation = true;
    public float prevRotationYaw;
    public float rotationYaw;
    public float prevRotationPitch;
    public float rotationPitch;
    public float prevRotationRoll;
    public float rotationRoll;

    //removes particle once hits 0, other things should reset this to keep it spawned
    public int despawnCountdown = 40;


    public static ParticleRenderType PARTICLE_SHEET_TRANSLUCENT_NO_FACE_CULL = new ParticleRenderType() {
        public BufferBuilder begin(Tesselator p_107455_, TextureManager p_107456_) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            //RenderSystem.bindTexture(ScreenCapturing.mainRenderTarget.getColorTextureId());
            //RenderSystem._setShaderTexture(0, ScreenCapturing.mainRenderTarget.getColorTextureId());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            return p_107455_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        /*public void end(Tesselator p_107458_) {
            p_107458_.end();
            RenderSystem.enableCull();
        }*/

        public String toString() {
            return "PARTICLE_SHEET_TRANSLUCENT_NO_FACE_CULL";
        }
    };

    @Override
    public void tick() {
        despawnCountdown--;
        if (despawnCountdown <= 0) {
            remove();
        }
    }

    public float getColorRed() {
        return rCol;
    }

    public float getColorGreen() {
        return gCol;
    }

    public float getColorBlue() {
        return bCol;
    }

    public void keepAlive() {
        despawnCountdown = 40;
    }

    public ParticleRotating(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    public void setQuadSize(float size) {
        this.quadSize = size;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public ParticleRenderType getRenderType() {
        return PARTICLE_SHEET_TRANSLUCENT_NO_FACE_CULL;
    }

    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 vec3 = pRenderInfo.getPosition();
        float f = (float)(Mth.lerp(pPartialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp(pPartialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp(pPartialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternion;
        if (useCustomRotation) {
            quaternion = new Quaternionf(0, 0, 0, 1);
            quaternion.mul(Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, this.prevRotationYaw, rotationYaw)));
            quaternion.mul(Axis.XP.rotationDegrees(Mth.lerp(pPartialTicks, this.prevRotationPitch, rotationPitch)));
            quaternion.mul(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, this.prevRotationRoll, rotationRoll)));
        } else {
            if (this.roll == 0.0F) {
                quaternion = pRenderInfo.rotation();
            } else {
                quaternion = new Quaternionf(pRenderInfo.rotation());
                quaternion.rotateZ(Mth.lerp(pPartialTicks, this.oRoll, this.roll));
            }
        }

        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(pPartialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        int j = this.getLightColor(pPartialTicks);
        pBuffer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        pBuffer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        pBuffer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
        pBuffer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(j);
    }

    public void setPosPrev(double pX, double pY, double pZ) {
        this.xo = pX;
        this.yo = pY;
        this.zo = pZ;
    }
}
