package com.corosus.watut.network;

import com.corosus.watut.WatutMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketNBTFromClient(CompoundTag nbt) implements PacketBase
{
	public PacketNBTFromClient(FriendlyByteBuf buf)
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
			if (player != null) {
				WatutMod.getPlayerStatusManagerServer().receiveAny(player, nbt);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public ResourceLocation id() {
		return WatutMod.PACKET_ID_NBT_FROM_SERVER;
	}
}
