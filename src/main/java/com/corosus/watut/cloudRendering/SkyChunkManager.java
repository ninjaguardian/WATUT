package com.corosus.watut.cloudRendering;

import net.minecraft.core.BlockPos;

import java.util.concurrent.ConcurrentHashMap;

public class SkyChunkManager {

    //TODO: consider making a full copy of this, would be the multi thread safe future proofed solution
    //theres scenarios where i need the old data while thread has nuked it to build new set
    //eg: checking if in cloud
    private ConcurrentHashMap<Long, SkyChunk> lookupSkyChunks = new ConcurrentHashMap<>();

    private static SkyChunkManager skyChunkManager;

    public static SkyChunkManager instance() {
        if (skyChunkManager == null) {
            skyChunkManager = new SkyChunkManager();
        }
        return skyChunkManager;
    }

    public SkyChunk getSkyChunk(int skyChunkPosX, int skyChunkPosY, int skyChunkPosZ) {
        long hash = BlockPos.asLong(skyChunkPosX, skyChunkPosY, skyChunkPosZ);
        if (!lookupSkyChunks.containsKey(hash)) {
            SkyChunk skyChunk = new SkyChunk(skyChunkPosX, skyChunkPosY, skyChunkPosZ);
            lookupSkyChunks.put(hash, skyChunk);
            return skyChunk;
        }
        return lookupSkyChunks.get(hash);
    }

    public SkyChunk getSkyChunkIfExists(int skyChunkPosX, int skyChunkPosY, int skyChunkPosZ) {
        long hash = BlockPos.asLong(skyChunkPosX, skyChunkPosY, skyChunkPosZ);
        return lookupSkyChunks.get(hash);
    }

    public SkyChunk getSkyChunkFromBlockPos(int blockPosX, int blockPosY, int blockPosZ) {
        return getSkyChunk(blockPosX / SkyChunk.size, blockPosY / SkyChunk.size, blockPosZ / SkyChunk.size);
    }

    public ConcurrentHashMap<Long, SkyChunk> getSkyChunks() {
        return lookupSkyChunks;
    }

    public SkyChunk.SkyChunkPoint getPoint(boolean mainThread, int blockPosX, int blockPosY, int blockPosZ) {
        SkyChunk skyChunk = getSkyChunkFromBlockPos(blockPosX, blockPosY, blockPosZ);
        return skyChunk.getPoint(mainThread, blockPosX & (SkyChunk.size - 1), blockPosY & (SkyChunk.size - 1), blockPosZ & (SkyChunk.size - 1));
    }

    public long addPoint(boolean mainThread, int blockPosX, int blockPosY, int blockPosZ) {
        SkyChunk skyChunk = getSkyChunkFromBlockPos(blockPosX, blockPosY, blockPosZ);
        return skyChunk.addPoint(mainThread, blockPosX & (SkyChunk.size - 1), blockPosY & (SkyChunk.size - 1), blockPosZ & (SkyChunk.size - 1));
    }
}
