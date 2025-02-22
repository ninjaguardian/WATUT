package com.corosus.watut;

import com.corosus.watut.client.screen.ScreenData;
import com.corosus.watut.math.Lerpables;
import net.minecraft.client.particle.Particle;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class PlayerStatus {

    public enum PlayerGuiState {

        NONE,
        CHAT_SCREEN,
        INVENTORY,
        CRAFTING,
        ESCAPE,
        EDIT_SIGN,
        EDIT_BOOK,
        CHEST,
        ENCHANTING_TABLE,
        ANVIL,
        BEACON,
        BREWING_STAND,
        DISPENSER,
        FURNACE,
        GRINDSTONE,
        HOPPER,
        HORSE,
        LOOM,
        VILLAGER,
        COMMAND_BLOCK,
        MISC;

        private static final Map<Integer, PlayerGuiState> lookup = new HashMap<>();
        private static final List<PlayerGuiState> listPointingGuis = new ArrayList<>();
        private static final List<PlayerGuiState> listTypingGuis = new ArrayList<>();
        private static final List<PlayerGuiState> listSoundMakerGuis = new ArrayList<>();

        static {
            for (PlayerGuiState e : EnumSet.allOf(PlayerGuiState.class)) {
                lookup.put(e.ordinal(), e);
                listPointingGuis.add(e);
                listSoundMakerGuis.add(e);
            }
            listPointingGuis.remove(NONE);
            listPointingGuis.remove(CHAT_SCREEN);
            listPointingGuis.remove(EDIT_BOOK);
            listPointingGuis.remove(EDIT_SIGN);
            listPointingGuis.remove(COMMAND_BLOCK);

            listTypingGuis.add(CHAT_SCREEN);
            listTypingGuis.add(EDIT_BOOK);
            listTypingGuis.add(EDIT_SIGN);
            listTypingGuis.add(COMMAND_BLOCK);

            listSoundMakerGuis.remove(NONE);
            listSoundMakerGuis.remove(CHAT_SCREEN);
            listSoundMakerGuis.remove(CHEST);
        }

        public static boolean isPointingGui(PlayerGuiState playerGuiState) {
            return listPointingGuis.contains(playerGuiState);
        }

        public static boolean isTypingGui(PlayerGuiState playerGuiState) {
            return listTypingGuis.contains(playerGuiState);
        }

        public static boolean canPreventIdleInGui(PlayerGuiState playerGuiState) {
            return listPointingGuis.contains(playerGuiState);
        }

        public static boolean isSoundMakerGui(PlayerGuiState playerGuiState) {
            return listSoundMakerGuis.contains(playerGuiState);
        }

        public static PlayerGuiState get(int intValue) {
            return lookup.get(intValue);
        }
    }

    public enum PlayerChatState {

        NONE,
        CHAT_FOCUSED,
        CHAT_TYPING;

        private static final Map<Integer, PlayerChatState> lookup = new HashMap<>();

        static {
            for (PlayerChatState e : EnumSet.allOf(PlayerChatState.class)) {
                lookup.put(e.ordinal(), e);
            }
        }

        public static PlayerChatState get(int intValue) {
            return lookup.get(intValue);
        }
    }

    //synced values
    private PlayerGuiState playerGuiState;
    private PlayerChatState playerChatState;
    private float typingAmplifier = 1F;
    private float screenPosPercentX = 0;
    private float screenPosPercentY = 0;
    private boolean isPressing = false;
    private int ticksSinceLastAction = 0;
    private int ticksToMarkPlayerIdleSyncedForClient = 20*60*5;

    //misc values used on either transmitting client or receiving client
    private Particle particle;
    private Particle particleIdle;
    private long lastTypeTime;
    private String lastTypeString = "";
    private boolean flagForRemoval = false;

    private long lastTypeTimeForAmp;
    private String lastTypeStringForAmp = "";
    private int lastTypeDiff;
    //so we can orient the particle to the bodys orientation
    //private ModelPart body;
    private Lerpables lerpTarget = new Lerpables();
    private Lerpables lerpPrev = new Lerpables();

    public float lerpTicks = 0;
    //for partial ticks
    public float lerpTicksPrev = 0;
    public float lerpTicksMax = 5;
    public float lastPartialTick = 0;

    public float yRotHeadWhileOverriding = 0;
    public float xRotHeadWhileOverriding = 0;
    public float yRotHeadBeforeOverriding = 0;
    public float xRotHeadBeforeOverriding = 0;

    private float typingAmplifierSmooth = 0.5F;

    private CompoundTag nbtCache = new CompoundTag();

    //private ScreenData screenData = new ScreenData();
    private PlayerGuiState lastScreenCaptured = PlayerGuiState.NONE;

    public PlayerStatus(PlayerGuiState playerGuiState) {
        this.playerGuiState = playerGuiState;
        //this.screenData.init();
    }

    public void tick() {
        this.lerpTicksPrev = lerpTicks;
        if (isLerping()) {
            this.lerpTicks++;
        }
    }

    public void setNewLerp(float ticks) {
        lerpTicksMax = ticks;
        lerpTicks = 0;
        lerpTicksPrev = 0;
    }

    public float getPartialLerp(float partialTick) {
        float lerpPrev = (lerpTicksPrev / lerpTicksMax);
        float lerp = (lerpTicks / lerpTicksMax);
        return Math.min(lerpPrev + ((lerp - lerpPrev) * partialTick), lerpTicksMax);
    }

    public void resetParticles() {
        if (particle != null) particle.remove();
        if (particleIdle != null) particleIdle.remove();
        particle = null;
        particleIdle = null;
    }

    public void reset() {
        resetParticles();
        WatutMod.dbg("remove trigger for " + this);
        ticksSinceLastAction = 0;
    }

    public boolean isLerping() {
        return this.lerpTicks < this.lerpTicksMax;
    }

    public PlayerGuiState getPlayerGuiState() {
        return playerGuiState;
    }

    public void setPlayerGuiState(PlayerGuiState playerGuiState) {
        this.playerGuiState = playerGuiState;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public long getLastTypeTime() {
        return lastTypeTime;
    }

    public void setLastTypeTime(long lastTypeTime) {
        this.lastTypeTime = lastTypeTime;
    }

    public String getLastTypeString() {
        return lastTypeString;
    }

    public void setLastTypeString(String lastTypeString) {
        this.lastTypeString = lastTypeString;
    }

    public float getScreenPosPercentX() {
        return screenPosPercentX;
    }

    public void setScreenPosPercentX(float screenPosPercentX) {
        this.screenPosPercentX = screenPosPercentX;
    }

    public float getScreenPosPercentY() {
        return screenPosPercentY;
    }

    public void setScreenPosPercentY(float screenPosPercentY) {
        this.screenPosPercentY = screenPosPercentY;
    }

    public Lerpables getLerpTarget() {
        return lerpTarget;
    }

    public void setLerpTarget(Lerpables lerpTarget) {
        this.lerpTarget = lerpTarget;
    }

    public Lerpables getLerpPrev() {
        return lerpPrev;
    }

    public void setLerpPrev(Lerpables lerpPrev) {
        this.lerpPrev = lerpPrev;
    }

    public boolean isPressing() {
        return isPressing;
    }

    public void setPressing(boolean pressing) {
        isPressing = pressing;
    }

    public float getTypingAmplifier() {
        return typingAmplifier;
    }

    public void setTypingAmplifier(float typingAmplifier) {
        this.typingAmplifier = typingAmplifier;
    }

    public int getLastTypeDiff() {
        return lastTypeDiff;
    }

    public void setLastTypeDiff(int lastTypeDiff) {
        this.lastTypeDiff = lastTypeDiff;
    }

    public long getLastTypeTimeForAmp() {
        return lastTypeTimeForAmp;
    }

    public void setLastTypeTimeForAmp(long lastTypeTimeForAmp) {
        this.lastTypeTimeForAmp = lastTypeTimeForAmp;
    }

    public String getLastTypeStringForAmp() {
        return lastTypeStringForAmp;
    }

    public void setLastTypeStringForAmp(String lastTypeStringForAmp) {
        this.lastTypeStringForAmp = lastTypeStringForAmp;
    }

    public int getTicksSinceLastAction() {
        return ticksSinceLastAction;
    }

    public void setTicksSinceLastAction(int ticksSinceLastAction) {
        this.ticksSinceLastAction = ticksSinceLastAction;
    }

    public float getTypingAmplifierSmooth() {
        return typingAmplifierSmooth;
    }

    public void setTypingAmplifierSmooth(float typingAmplifierSmooth) {
        this.typingAmplifierSmooth = typingAmplifierSmooth;
    }

    public boolean isFlagForRemoval() {
        return flagForRemoval;
    }

    public void setFlagForRemoval(boolean flagForRemoval) {
        this.flagForRemoval = flagForRemoval;
    }

    public Particle getParticleIdle() {
        return particleIdle;
    }

    public void setParticleIdle(Particle particleIdle) {
        this.particleIdle = particleIdle;
    }

    public boolean isIdle() {
        return ticksSinceLastAction > ticksToMarkPlayerIdleSyncedForClient;
    }

    public CompoundTag getNbtCache() {
        return nbtCache;
    }

    public void setNbtCache(CompoundTag nbtCache) {
        this.nbtCache = nbtCache;
    }

    public int getTicksToMarkPlayerIdleSyncedForClient() {
        return ticksToMarkPlayerIdleSyncedForClient;
    }

    public void setTicksToMarkPlayerIdleSyncedForClient(int ticksToMarkPlayerIdleSyncedForClient) {
        this.ticksToMarkPlayerIdleSyncedForClient = ticksToMarkPlayerIdleSyncedForClient;
    }

    public PlayerChatState getPlayerChatState() {
        return playerChatState;
    }

    public void setPlayerChatState(PlayerChatState playerChatState) {
        this.playerChatState = playerChatState;
    }

    /*public ScreenData getScreenData() {
        return screenData;
    }

    public void setScreenData(ScreenData screenData) {
        this.screenData = screenData;
    }*/

    public PlayerGuiState getLastScreenCaptured() {
        return lastScreenCaptured;
    }

    public void setLastScreenCaptured(PlayerGuiState lastScreenCaptured) {
        this.lastScreenCaptured = lastScreenCaptured;
    }
}
