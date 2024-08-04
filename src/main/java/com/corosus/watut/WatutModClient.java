package com.corosus.watut;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class WatutModClient {
    public static Player getPlayer()
    {
        return Minecraft.getInstance().player;
    }

}
