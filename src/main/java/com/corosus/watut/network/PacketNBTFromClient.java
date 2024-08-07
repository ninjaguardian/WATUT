package com.corosus.watut.network;

import com.corosus.watut.WatutMod;
import com.corosus.watut.WatutNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketNBTFromClient(CompoundTag nbt) implements PacketBase
{
	public static final CustomPacketPayload.Type<PacketNBTFromClient> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(WatutMod.MODID, "nbt_server"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketNBTFromClient> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG, PacketNBTFromClient::nbt,
			PacketNBTFromClient::new);

	public PacketNBTFromClient(RegistryFriendlyByteBuf buf)
	{
		this(buf.readNbt());
	}


	public void write(FriendlyByteBuf buf)
	{
		buf.writeNbt(nbt);
	}


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

	/*@Override
	public ResourceLocation id() {
		return WatutMod.PACKET_ID_NBT_FROM_SERVER;
	}*/

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
