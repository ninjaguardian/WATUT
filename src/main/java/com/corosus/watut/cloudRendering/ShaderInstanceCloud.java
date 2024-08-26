package com.corosus.watut.cloudRendering;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import javax.annotation.Nullable;
import java.io.IOException;

public class ShaderInstanceCloud extends ShaderInstance {

    @Nullable
    public final Uniform LIGHTNING_POS;
    @Nullable
    public final Uniform VBO_RENDER_POS;
    @Nullable
    public final Uniform LIGHT0_DIRECTION2;
    @Nullable
    public final Uniform LIGHT1_DIRECTION2;
    @Nullable
    public final Uniform CLOUD_COLOR;

    public ShaderInstanceCloud(ResourceProvider p_173336_, ResourceLocation shaderLocation, VertexFormat p_173338_) throws IOException {
        super(p_173336_, shaderLocation, p_173338_);
        this.LIGHTNING_POS = this.getUniform("Lightning_Pos");
        this.VBO_RENDER_POS = this.getUniform("VBO_Render_Pos");
        this.LIGHT0_DIRECTION2 = this.getUniform("Light0_Direction2");
        this.LIGHT1_DIRECTION2 = this.getUniform("Light0_Direction2");
        this.CLOUD_COLOR = this.getUniform("CloudColor");
    }
}
