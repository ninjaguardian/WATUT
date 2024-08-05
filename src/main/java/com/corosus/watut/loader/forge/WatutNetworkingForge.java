package com.corosus.watut.loader.forge;

import com.corosus.watut.WatutMod;
import com.corosus.watut.WatutModClient;
import com.corosus.watut.WatutNetworking;
import com.corosus.watut.network.PacketNBTFromClient;
import com.corosus.watut.network.PacketNBTFromServer;
import com.corosus.watut.network.PacketBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.*;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class WatutNetworkingForge extends WatutNetworking {

    public WatutNetworkingForge() {
        super();
    }

    public static SimpleChannel HANDLER = ChannelBuilder.named(new ResourceLocation(WatutMod.MODID, WatutMod.MODID + "_packets")).simpleChannel();;

    public static void register() {
        registerClientboundPacket(
                WatutMod.PACKET_ID_NBT_FROM_SERVER,
                0,
                PacketNBTFromServer.class,
                PacketNBTFromServer::write,
                PacketNBTFromServer::new,
                PacketNBTFromServer::handle
        );

        registerServerboundPacket(
                WatutMod.PACKET_ID_NBT_FROM_CLIENT,
                1,
                PacketNBTFromClient.class,
                PacketNBTFromClient::write,
                PacketNBTFromClient::new,
                PacketNBTFromClient::handle
        );
    }

    public static <T extends PacketBase> void registerServerboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, RegistryFriendlyByteBuf> writer, Function<RegistryFriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args) {
        BiConsumer<T, CustomPayloadEvent.Context> serverHandler = (packet, ctx) -> {
            if(ctx.isServerSide())
            {
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> {
                    handler.accept(packet, ctx.getSender());
                });
            }
        };

        HANDLER.messageBuilder(clazz, numericalId, NetworkDirection.PLAY_TO_SERVER)
                .encoder(writer)
                .decoder(reader)
                .consumerMainThread(serverHandler)
                .add();
    }

    public static <T extends PacketBase> void registerClientboundPacket(ResourceLocation id, int numericalId, Class<T> clazz, BiConsumer<T, RegistryFriendlyByteBuf> writer, Function<RegistryFriendlyByteBuf, T> reader, BiConsumer<T, Player> handler, Object... args)
    {
        BiConsumer<T, CustomPayloadEvent.Context> clientHandler = (packet, ctx) -> {
            if(ctx.isClientSide())
            {
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> {
                    handler.accept(packet, WatutModClient.getPlayer());
                });
            }
        };

        HANDLER.messageBuilder(clazz, numericalId, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(writer)
                .decoder(reader)
                .consumerMainThread(clientHandler)
                .add();
    }

    @Override
    public void clientSendToServer(CompoundTag data) {
        HANDLER.send(new PacketNBTFromClient(data), PacketDistributor.SERVER.noArg());
    }

    @Override
    public void serverSendToClientAll(CompoundTag data) {
        HANDLER.send(new PacketNBTFromServer(data), PacketDistributor.ALL.noArg());
    }

    @Override
    public void serverSendToClientPlayer(CompoundTag data, Player player) {
        HANDLER.send(new PacketNBTFromServer(data), PacketDistributor.PLAYER.with((ServerPlayer) player));
    }

    @Override
    public void serverSendToClientNear(CompoundTag data, Vec3 pos, double dist, Level level) {
        HANDLER.send(new PacketNBTFromServer(data), PacketDistributor.NEAR.with(new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, dist, level.dimension())));
    }
}

