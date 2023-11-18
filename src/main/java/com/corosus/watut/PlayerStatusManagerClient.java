package com.corosus.watut;

import com.corosus.watut.particle.ParticleAnimated;
import com.corosus.watut.particle.ParticleStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.particle.Particle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatusManagerClient extends PlayerStatusManager {

    private PlayerStatus selfPlayerStatus = new PlayerStatus(PlayerStatus.PlayerGuiState.NONE);
    public HashMap<UUID, PlayerStatus> lookupPlayerToStatusPrev = new HashMap<>();
    //public HashMap<UUID, ParticleAnimated> lookupPlayerToParticle = new HashMap<>();
    private long typingIdleTimeout = 60;

    public void tickPlayerClient(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player.getUUID().equals(player.getUUID())) {
            tickLocalPlayerClient(player);

            //temp for singpleplayer testing
            //tickOtherPlayerClient(player);
        } else {
            tickOtherPlayerClient(player);
        }
    }

    public void tickLocalPlayerClient(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ChatScreen) {
            ChatScreen chat = (ChatScreen) mc.screen;
            //Watut.dbg("chat: " + chat.input.getValue());
            if (checkIfTyping(chat.input.getValue(), player)) {
                sendStatus(PlayerStatus.PlayerGuiState.CHAT_TYPING);
            } else {
                sendStatus(PlayerStatus.PlayerGuiState.CHAT_OPEN);
            }
        } else if (mc.screen instanceof InventoryScreen) {
            sendStatus(PlayerStatus.PlayerGuiState.INVENTORY);
        } else if (mc.screen instanceof CraftingScreen) {
            sendStatus(PlayerStatus.PlayerGuiState.CRAFTING);
        } else if (mc.screen != null) {
            sendStatus(PlayerStatus.PlayerGuiState.MISC);
        } else if (mc.screen == null) {
            sendStatus(PlayerStatus.PlayerGuiState.NONE);
        }

        if (mc.screen != null && mc.level.getGameTime() % 1 == 0) {
            double xPercent = (mc.mouseHandler.xpos() / mc.getWindow().getScreenWidth()) - 0.5;
            double yPercent = (mc.mouseHandler.ypos() / mc.getWindow().getScreenHeight()) - 0.5;
            sendMouse((float) xPercent, (float) yPercent);
        }
    }

    public boolean checkIfTyping(String input, Player player) {
        PlayerStatus status = getStatus(player);
        if (input.length() > 0) {
            if (!input.startsWith("/")) {
                if (!input.equals(status.getLastTypeString())) {
                    status.setLastTypeString(input);
                    status.setLastTypeTime(player.level().getGameTime());
                }
            }
        } else {
            status.setLastTypeString(input);
            return false;
        }
        if (status.getLastTypeTime() + typingIdleTimeout >= player.level().getGameTime()) {
            return true;
        }
        return false;
    }

    public String getTypingPlayers() {
        Minecraft mc = Minecraft.getInstance();
        String str = "";

        for (Map.Entry<UUID, PlayerStatus> entry : lookupPlayerToStatus.entrySet()) {
            if (entry.getValue().getPlayerGuiState() == PlayerStatus.PlayerGuiState.CHAT_TYPING) {
                str += mc.getConnection().getPlayerInfo(entry.getKey()).getProfile().getName() + ", ";
            }
        }
        String anim = "";
        int animRate = 6;
        long time = mc.level.getGameTime() % (animRate*4);
        while (time > animRate) {
            time -= animRate;
            anim += ".";
        }

        if (str.length() > 2) {
            str = str.substring(0, str.length() - 2) + " is typing" + anim;
        }
        return str;
    }

    public void tickOtherPlayerClient(Player player) {
        Vec3 pos = player.position();
        PlayerStatus playerStatus = getStatus(player);
        PlayerStatus playerStatusPrev = getStatusPrev(player);
        if (playerStatus.getPlayerGuiState() != playerStatusPrev.getPlayerGuiState() || lookupPlayerToStatus.get(player.getUUID()).getParticle() == null) {
            //System.out.println(playerStatus.getPlayerGuiState() + " vs " + playerStatusPrev.getPlayerGuiState());
            if (lookupPlayerToStatus.get(player.getUUID()).getParticle() != null) {
                //Watut.dbg("remove particle");
                lookupPlayerToStatus.get(player.getUUID()).getParticle().remove();
                lookupPlayerToStatus.get(player.getUUID()).setParticle(null);
            }
            Particle particle = null;
            Vec3 posParticle = getParticlePosition(player);
            if (this.getStatus(player).getPlayerGuiState() == PlayerStatus.PlayerGuiState.CHAT_OPEN) {
                particle = new ParticleAnimated((ClientLevel) player.level(), posParticle.x, posParticle.y, posParticle.z, ParticleRegistry.chat_idle_set);
                //particle = new ParticleStatic((ClientLevel) player.level(), posParticle.x, posParticle.y, posParticle.z, ParticleRegistry.crafting);
            } else if (this.getStatus(player).getPlayerGuiState() == PlayerStatus.PlayerGuiState.CHAT_TYPING) {
                particle = new ParticleAnimated((ClientLevel) player.level(), posParticle.x, posParticle.y, posParticle.z, ParticleRegistry.chat_typing_set);
            } else if (this.getStatus(player).getPlayerGuiState() == PlayerStatus.PlayerGuiState.INVENTORY) {
                particle = new ParticleStatic((ClientLevel) player.level(), posParticle.x, posParticle.y, posParticle.z, ParticleRegistry.inventory);
            } else if (this.getStatus(player).getPlayerGuiState() == PlayerStatus.PlayerGuiState.CRAFTING) {
                particle = new ParticleStatic((ClientLevel) player.level(), posParticle.x, posParticle.y, posParticle.z, ParticleRegistry.crafting);
            } else if (this.getStatus(player).getPlayerGuiState() == PlayerStatus.PlayerGuiState.MISC) {
                particle = new ParticleStatic((ClientLevel) player.level(), posParticle.x, posParticle.y, posParticle.z, ParticleRegistry.inventory);
            }
            if (particle != null) {
                lookupPlayerToStatus.get(player.getUUID()).setParticle(particle);
                Minecraft.getInstance().particleEngine.add(particle);
            }
        } else {
            if (lookupPlayerToStatus.get(player.getUUID()).getParticle() != null) {
                Particle particle = lookupPlayerToStatus.get(player.getUUID()).getParticle();
                if (!particle.isAlive()) {
                    lookupPlayerToStatus.get(player.getUUID()).getParticle().remove();
                    lookupPlayerToStatus.get(player.getUUID()).setParticle(null);
                } else {
                    updateParticle(player, particle);
                    Vec3 posParticle = getParticlePosition(player);
                    particle.setPos(posParticle.x, posParticle.y, posParticle.z);
                    particle.setParticleSpeed(0, 0, 0);
                    if (particle instanceof ParticleStatic) {
                        ((ParticleStatic) particle).setQuadSize((float) (0.3F + Math.sin((player.level().getGameTime() / 10F) % 360) * 0.01F));
                        ((ParticleStatic) particle).rotationYaw++;
                        ((ParticleStatic) particle).prevRotationYaw++;
                    }

                }
            }
        }
        setStatusPrev(player, playerStatus.getPlayerGuiState());
    }

    public void setupRotationsHook(EntityModel model, Entity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (model instanceof PlayerModel && pEntity instanceof Player) {
            PlayerModel playerModel = (PlayerModel) model;
            Player player = (Player) pEntity;
            Minecraft mc = Minecraft.getInstance();
            PlayerStatus playerStatus = getStatus(player);
            if (playerStatus.getPlayerGuiState() == PlayerStatus.PlayerGuiState.CHAT_TYPING) {
                float typeAngle = (float) (45 + (Math.toRadians(Math.sin((pAgeInTicks / 1F) % 360) * 15)));
                float typeAngle2 = (float) (45 + (Math.toRadians(-Math.sin((pAgeInTicks / 1F) % 360) * 15)));
                playerModel.rightArm.xRot -= typeAngle;
                playerModel.rightSleeve.xRot -= typeAngle;
                playerModel.leftArm.xRot -= typeAngle2;
                playerModel.leftSleeve.xRot -= typeAngle2;

                double tiltIn = Math.toRadians(20);
                playerModel.rightArm.yRot -= tiltIn;
                playerModel.rightSleeve.yRot -= tiltIn;
                playerModel.leftArm.yRot += tiltIn;
                playerModel.leftSleeve.yRot += tiltIn;

                playerModel.head.xRot = (float) Math.toRadians(25);
                playerModel.hat.xRot = (float) Math.toRadians(25);

            } else if (playerStatus.getPlayerGuiState() == PlayerStatus.PlayerGuiState.CHAT_OPEN) {

            } else if (playerStatus.getPlayerGuiState() != PlayerStatus.PlayerGuiState.NONE) {
                double xPercent = playerStatus.getScreenPosPercentX();
                double yPercent = playerStatus.getScreenPosPercentY();
                double x = Math.toRadians(90) - Math.toRadians(22.5) - yPercent;
                double y = -Math.toRadians(15) + xPercent;
                //y = 0;
                playerModel.rightArm.yRot += y;
                playerModel.rightSleeve.yRot += y;
                playerModel.rightArm.xRot -= x;
                playerModel.rightSleeve.xRot -= x;
                playerModel.head.xRot = (float) Math.toRadians(15);
                playerModel.hat.xRot = (float) Math.toRadians(15);
                //playerModel.body.yRot = playerModel.head.yRot;
            }
        }
    }

    public Vec3 getParticlePosition(Player player) {
        Vec3 pos = player.position();
        float distFromFace = 0.75F;
        Vec3 lookVec = player.getLookAngle().scale(distFromFace);
        return new Vec3(pos.x + lookVec.x, pos.y + 1D, pos.z + lookVec.z);
    }

    public void updateParticle(Player player, Particle particle) {
        Vec3 pos = player.position();
        float distFromFace = 1.5F;
        Vec3 lookVec = player.getLookAngle().scale(distFromFace);
    }

    public PlayerStatus getStatusPrev(Player player) {
        checkPrev(player.getUUID());
        return lookupPlayerToStatusPrev.get(player.getUUID());
    }

    public void setStatusPrev(Player player, PlayerStatus.PlayerGuiState statusType) {
        checkPrev(player.getUUID());
        setStatusPrev(player.getUUID(), statusType);
    }

    public void setStatusPrev(UUID uuid, PlayerStatus.PlayerGuiState statusType) {
        checkPrev(uuid);
        lookupPlayerToStatusPrev.get(uuid).setPlayerGuiState(statusType);
    }

    public void checkPrev(UUID uuid) {
        if (!lookupPlayerToStatusPrev.containsKey(uuid)) {
            lookupPlayerToStatusPrev.put(uuid, new PlayerStatus(PlayerStatus.PlayerGuiState.NONE));
        }
    }

    public void sendStatus(PlayerStatus.PlayerGuiState playerStatus) {
        if (selfPlayerStatus.getPlayerGuiState() != playerStatus) {
            CompoundTag data = new CompoundTag();
            data.putString(WatutNetworking.NBTPacketCommand, WatutNetworking.NBTPacketCommandUpdateStatusPlayer);
            data.putInt(WatutNetworking.NBTDataPlayerStatus, playerStatus.ordinal());
            WatutNetworking.HANDLER.sendTo(new PacketNBTFromClient(data), Minecraft.getInstance().player.connection.getConnection(), NetworkDirection.PLAY_TO_SERVER);
        }
        selfPlayerStatus.setPlayerGuiState(playerStatus);
    }

    public void sendMouse(float x, float y) {
        if (selfPlayerStatus.getScreenPosPercentX() != x || selfPlayerStatus.getScreenPosPercentY() != y) {
            CompoundTag data = new CompoundTag();
            data.putString(WatutNetworking.NBTPacketCommand, WatutNetworking.NBTPacketCommandUpdateMousePlayer);
            data.putFloat(WatutNetworking.NBTDataPlayerMouseX, x);
            data.putFloat(WatutNetworking.NBTDataPlayerMouseY, y);

            WatutNetworking.HANDLER.sendTo(new PacketNBTFromClient(data), Minecraft.getInstance().player.connection.getConnection(), NetworkDirection.PLAY_TO_SERVER);
        }
        selfPlayerStatus.setScreenPosPercentX(x);
        selfPlayerStatus.setScreenPosPercentY(y);
    }

    public void receiveStatus(UUID uuid, PlayerStatus.PlayerGuiState playerStatus) {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
        //Watut.dbg("got status on client: " + playerStatus + " for player name: " + playerInfo.getProfile().getName());
        setGuiStatus(uuid, playerStatus);
    }

    public void receiveMouse(UUID uuid, float x, float y) {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
        //Watut.dbg("got moud on client: " + playerStatus + " for player name: " + playerInfo.getProfile().getName());
        //Watut.dbg("x: " + x);
        setMouse(uuid, x, y);
    }

}
