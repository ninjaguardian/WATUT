package com.corosus.watut.network;

import com.corosus.watut.WatutNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PacketNBTRecord(CompoundTag data) implements CustomPacketPayload {
	public static final Type<PacketNBTRecord> TYPE = new Type<>(WatutNetworking.NBT_PACKET_ID);

	public static final StreamCodec<RegistryFriendlyByteBuf, PacketNBTRecord> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG, PacketNBTRecord::data,
			PacketNBTRecord::new);


	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}