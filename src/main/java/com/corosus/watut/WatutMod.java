package com.corosus.watut;

import com.corosus.modconfig.CoroConfigRegistry;
import com.corosus.watut.cloudRendering.CloudRenderHandler;
import com.corosus.watut.cloudRendering.ShaderInstanceCloud;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilderPersistentStorage;
import com.corosus.watut.config.ConfigClient;
import com.corosus.watut.config.ConfigCommon;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.players.PlayerList;

public abstract class WatutMod
{

    // Define mod id in a common place for everything to reference
    public static final String MODID = "watut";

    private static PlayerStatusManagerClient playerStatusManagerClient = null;
    private static PlayerStatusManagerServer playerStatusManagerServer = null;

    private static WatutMod instance;

    public static CloudRenderHandler cloudRenderHandler = new CloudRenderHandler();

    public static ShaderInstanceCloud cloudShader;

    public static ThreadedBufferBuilder threadedBufferBuilder = new ThreadedBufferBuilder(2097152);
    //public static ThreadedBufferBuilderPersistentStorage threadedBufferBuilderPersistent = new ThreadedBufferBuilderPersistentStorage(2097152 * 10);
    public static ThreadedBufferBuilderPersistentStorage threadedBufferBuilderPersistent = new ThreadedBufferBuilderPersistentStorage(2097152);

    public static final VertexFormat POSITION_TEX_COLOR_NORMAL_VEC3 = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", DefaultVertexFormat.ELEMENT_POSITION).put("UV0", DefaultVertexFormat.ELEMENT_UV0).put("Color", DefaultVertexFormat.ELEMENT_COLOR).put("Normal", DefaultVertexFormat.ELEMENT_NORMAL).put("Position2", DefaultVertexFormat.ELEMENT_POSITION).put("Padding", DefaultVertexFormat.ELEMENT_PADDING).build());

    public static WatutMod instance() {
        return instance;
    }

    public static PlayerStatusManagerClient getPlayerStatusManagerClient() {
        if (playerStatusManagerClient == null) playerStatusManagerClient = new PlayerStatusManagerClient();
        return playerStatusManagerClient;
    }

    public static PlayerStatusManagerServer getPlayerStatusManagerServer() {
        if (playerStatusManagerServer == null) playerStatusManagerServer = new PlayerStatusManagerServer();
        return playerStatusManagerServer;
    }

    public WatutMod() {
        instance = this;
        CoroConfigRegistry.instance().addConfigFile(MODID, new ConfigCommon());
        CoroConfigRegistry.instance().addConfigFile(MODID, new ConfigClient());
    }

    public abstract PlayerList getPlayerList();

    public static void dbg(Object obj) {
        //System.out.println("" + obj);
    }
}
