package com.corosus.watut.cloudRendering;

import net.minecraft.core.BlockPos;

import java.util.HashMap;

public class SkyChunkManager {

    private HashMap<Long, SkyChunk> lookupSkyChunks = new HashMap<>();

    private static SkyChunkManager skyChunkManager;

    public static SkyChunkManager instance() {
        if (skyChunkManager == null) {
            skyChunkManager = new SkyChunkManager();
        }
        return skyChunkManager;
    }

    //TODO: should be chunk coords?
    public SkyChunk getSkyChunk(int skyChunkPosX, int skyChunkPosY, int skyChunkPosZ) {
        long hash = BlockPos.asLong(skyChunkPosX, skyChunkPosY, skyChunkPosZ);
        if (!lookupSkyChunks.containsKey(hash)) {
            SkyChunk skyChunk = new SkyChunk(skyChunkPosX, skyChunkPosY, skyChunkPosZ);
            lookupSkyChunks.put(hash, skyChunk);
            return skyChunk;
        }
        return lookupSkyChunks.get(hash);
    }

    public HashMap<Long, SkyChunk> getSkyChunks() {
        return lookupSkyChunks;
    }

    public long addPoint(int blockPosX, int blockPosY, int blockPosZ) {
        SkyChunk skyChunk = getSkyChunk(blockPosX >> 4, blockPosY >> 4, blockPosZ >> 4);
        return skyChunk.addPoint(blockPosX & (SkyChunk.size - 1), blockPosY & (SkyChunk.size - 1), blockPosZ & (SkyChunk.size - 1));
    }
}
