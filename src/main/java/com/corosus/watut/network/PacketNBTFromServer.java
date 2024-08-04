package com.corosus.watut.network;

import com.corosus.coroutil.config.ConfigCoroUtil;
import com.corosus.coroutil.util.CULog;
import com.corosus.watut.WatutMod;
import com.corosus.watut.WatutNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record PacketNBTFromServer(CompoundTag nbt) implements PacketBase
{
	public PacketNBTFromServer(FriendlyByteBuf buf)
	{
		this(buf.readNbt());
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeNbt(nbt);
	}

	@Override
	public void handle(Player player)
	{

		try {
			UUID uuid = UUID.fromString(nbt.getString(WatutNetworking.NBTDataPlayerUUID));
			WatutMod.getPlayerStatusManagerClient().receiveAny(uuid, nbt);
		} catch (Exception ex) {
			CULog.dbg("WATUT ERROR: packet with invalid uuid sent from server");
			CULog.dbg("full nbt data: " + nbt);
			if (ConfigCoroUtil.useLoggingDebug) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public ResourceLocation id() {
		return WatutMod.PACKET_ID_NBT_FROM_SERVER;
	}
}
