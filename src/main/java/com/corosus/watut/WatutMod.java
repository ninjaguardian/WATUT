package com.corosus.watut;

import com.corosus.modconfig.CoroConfigRegistry;
import com.corosus.watut.cloudRendering.CloudRenderHandler;
import com.corosus.watut.config.ConfigClient;
import com.corosus.watut.config.ConfigCommon;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
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

    public static ShaderInstance cloudShader;

    public static ThreadedBufferBuilder threadedBufferBuilder = new ThreadedBufferBuilder(2097152);

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
