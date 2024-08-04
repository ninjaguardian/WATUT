package com.corosus.watut.network;

import com.corosus.watut.WatutMod;
import com.corosus.watut.loader.fabric.WatutNetworkingFabric;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacketNBTRecord(CompoundTag data) implements CustomPacketPayload {
	public static final Type<PacketNBTRecord> TYPE = new Type<>(WatutNetworkingFabric.NBT_PACKET_ID);

	public static final StreamCodec<RegistryFriendlyByteBuf, PacketNBTRecord> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG, PacketNBTRecord::data,
			PacketNBTRecord::new);


	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}