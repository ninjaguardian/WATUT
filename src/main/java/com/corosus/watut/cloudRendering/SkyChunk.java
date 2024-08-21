package com.corosus.watut.cloudRendering;

import com.corosus.coroutil.util.CULog;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//cubic sky chunk
public class SkyChunk {

    //its position in the world like a chunk coord, not a blockpos coord
    private final int x;
    private final int y;
    private final int z;

    //public static int size = 128;
    public static int size = 32;

    //TODO: we could replace this with what chunks use, look around use to turn x y z into efficient storage by index of CrudeIncrementalIntIdentityHashBiMap and PalletedContainer.Strategy
    private HashMap<Long, SkyChunkPoint> lookupPointsMainThread = new HashMap<>();
    private HashMap<Long, SkyChunkPoint> lookupPointsOffThread = new HashMap<>();

    //tells main thread that it can be safely used
    private boolean isInitialized = false;
    //private boolean beingBuilt = false;
    private long lastBuildTime = 0;
    private int rebuildFrequency = 20*5;
    private boolean isWaitingToUploadData = false;

    private boolean clientCameraInCloudForSkyChunk = false;

    private RenderableData renderableData;

    private Vec3 cameraPosDuringBuild = Vec3.ZERO;
    private Vec3 cameraPosForRender = Vec3.ZERO;

    //brought over from ThreadedCloudBuilder
    private int gameTicksAtStart = 0;

    public SkyChunk(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        //CULog.log("SkyChunk at " + );
        renderableData = new RenderableData();
    }

    public RenderableData getRenderableData() {
        return renderableData;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }/*

    public boolean isBeingBuilt() {
        return beingBuilt;
    }

    public void setBeingBuilt(boolean beingBuilt) {
        this.beingBuilt = beingBuilt;
    }*/

    public boolean needsBuild() {
        if (Minecraft.getInstance().level == null) return false;
        //return lastBuildTime + rebuildFrequency < Minecraft.getInstance().level.getGameTime();

        rebuildFrequency = 20*5;
        rebuildFrequency = 20*2;
        //rebuildFrequency = 1;

        //keep all skychunks updating in sync, or immediately if new
        //if its 2 updates behind (because it just came into range, force a new build
        //if (lastBuildTime + (rebuildFrequency * 2) < Minecraft.getInstance().level.getGameTime()) return true;
        //if (lastBuildTime == 0 || lastBuildTime + (rebuildFrequency * 1) < Minecraft.getInstance().level.getGameTime()) return true;
        //this isnt reliable at all
        return lastBuildTime == 0 || Minecraft.getInstance().level.getGameTime() % rebuildFrequency == 0;
        //return false;
    }

    public synchronized boolean isWaitingToUploadData() {
        return isWaitingToUploadData;
    }

    public synchronized void setWaitingToUploadData(boolean waitingToUploadData) {
        isWaitingToUploadData = waitingToUploadData;
    }

    public long getLastBuildTime() {
        return lastBuildTime;
    }

    public void setLastBuildTime(long lastBuildTime) {
        this.lastBuildTime = lastBuildTime;
    }

    public Vec3 getCameraPosDuringBuild() {
        return cameraPosDuringBuild;
    }

    public void setCameraPosDuringBuild(Vec3 cameraPosDuringBuild) {
        this.cameraPosDuringBuild = cameraPosDuringBuild;
    }

    public Vec3 getCameraPosForRender() {
        return cameraPosForRender;
    }

    public void setCameraPosForRender(Vec3 cameraPosForRender) {
        this.cameraPosForRender = cameraPosForRender;
    }

    public HashMap<Long, SkyChunkPoint> getPointsMainThread() {
        return lookupPointsMainThread;
    }

    public HashMap<Long, SkyChunkPoint> getPointsOffThread() {
        return lookupPointsOffThread;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        this.isInitialized = initialized;
    }

    public boolean isClientCameraInCloudForSkyChunk() {
        return clientCameraInCloudForSkyChunk;
    }

    public static BlockPos worldPosToChunkPos(double x, double y, double z) {
        return new BlockPos(Mth.floor(x / SkyChunk.size), Mth.floor(y / SkyChunk.size), Mth.floor(z / SkyChunk.size));
    }

    public BlockPos getWorldPos() {
        return new BlockPos(x * SkyChunk.size, y * SkyChunk.size, z * SkyChunk.size);
    }

    public void setClientCameraInCloudForSkyChunk(boolean clientCameraInCloudForSkyChunk) {
        this.clientCameraInCloudForSkyChunk = clientCameraInCloudForSkyChunk;
    }

    //uses internal pos values
    public SkyChunkPoint getPoint(boolean mainThread, int x, int y, int z) {
        long hash = BlockPos.asLong(x, y, z);
        return mainThread ? lookupPointsMainThread.get(hash) : lookupPointsOffThread.get(hash);
    }

    public long getLongHashCode() {
        return BlockPos.asLong(x, y, z);
    }

    public long addPoint(boolean mainThread, int x, int y, int z) {
        if (x < 0 || x >= SkyChunk.size || y < 0 || y >= SkyChunk.size || z < 0 || z >= SkyChunk.size) {
            CULog.err("x y z used outside of SkyChunk range!!! " + x + " " + y + " " + z);
        }
        long hash = BlockPos.asLong(x, y, z);
        if (mainThread) {
            if (!lookupPointsMainThread.containsKey(hash)) {
                SkyChunkPoint cloudPoint = new SkyChunkPoint(lookupPointsMainThread, x, y, z);
                lookupPointsMainThread.put(hash, cloudPoint);
            }
        } else {
            if (!lookupPointsOffThread.containsKey(hash)) {
                SkyChunkPoint cloudPoint = new SkyChunkPoint(lookupPointsOffThread, x, y, z);
                lookupPointsOffThread.put(hash, cloudPoint);
            }
        }
        return hash;
    }

    public void pushNewOffThreadDataToMainThread() {
        setCameraPosForRender(getCameraPosDuringBuild());
        lookupPointsMainThread.clear();
        //lookupPointsMainThread.putAll(lookupPointsOffThread);
    }

    public class SkyChunkPoint {
        private int x;
        private int y;
        private int z;
        //if point has a part/side of its voxel to render
        private boolean isVisible = false;
        private float shapeAdjustThreshold = 0;
        private float normalizedDistanceToOutside = 1F;

        //private SkyChunk skyChunk;
        private HashMap<Long, SkyChunkPoint> lookupPoints;

        public SkyChunkPoint(HashMap<Long, SkyChunkPoint> lookupPoints, int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.lookupPoints = lookupPoints;
        }

        public float getShapeAdjustThreshold() {
            return shapeAdjustThreshold;
        }

        public void setShapeAdjustThreshold(float shapeAdjustThreshold) {
            this.shapeAdjustThreshold = shapeAdjustThreshold;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public float getNormalizedDistanceToOutside() {
            return normalizedDistanceToOutside;
        }

        public void setNormalizedDistanceToOutside(float normalizedDistanceToOutside) {
            this.normalizedDistanceToOutside = normalizedDistanceToOutside;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SkyChunkPoint that = (SkyChunkPoint) o;
            return x == that.x && y == that.y && z == that.z;
        }/*

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }*/

        @Override
        public String toString() {
            return "SkyChunkPoint{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }

        public List<Direction> getRenderableSides() {
            List<Direction> listRenderables = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                int xCheck = x + dir.getStepX();
                int yCheck = y + dir.getStepY();
                int zCheck = z + dir.getStepZ();
                long hash = BlockPos.asLong(xCheck, yCheck, zCheck);
                //TODO: skychunk change, this will cause wasted faces on areas we cant see along skychunk borders, should be fixable by peeking
                if (xCheck >= 0 && xCheck <= SkyChunk.size &&
                        yCheck >= 0 && yCheck <= SkyChunk.size &&
                        zCheck >= 0 && zCheck <= SkyChunk.size) {
                    if (!lookupPoints.containsKey(hash)) {
                        listRenderables.add(dir);
                    }
                } else {
                    if (!lookupPoints.containsKey(hash)) {
                        listRenderables.add(dir);
                    }
                }
            }
            isVisible = listRenderables.size() > 0;
            return listRenderables;
        }

        public float calculateNormalizedDistanceToOutside() {
            if (!isVisible) return 1F;
            //just 1 axis for now to test creative idea
            //float maxDist = 4;
            float maxDist = 4;
            float maxLookAhead = 15;
            maxLookAhead = 3;
            float curDist = 0;
            for (int xx = 0; xx < maxDist + 1; xx++) {
                int xCheck = x + 0;
                int yCheck = y + xx;
                int zCheck = z + 0;
                long hash = BlockPos.asLong(xCheck, yCheck, zCheck);
                if (xCheck >= 0 && xCheck <= SkyChunk.size &&
                        yCheck >= 0 && yCheck <= SkyChunk.size &&
                        zCheck >= 0 && zCheck <= SkyChunk.size) {
                    if (!lookupPoints.containsKey(hash)) {
                        boolean stillClear = true;
                        //if we want spots below an upper portion of the cloud to appear dark, as if blocked by the sun
                        //for bigger gaps this might not be ideal
                        for (int xxx = 0; xxx <= maxLookAhead; xxx++) {
                            int yyCheck = yCheck + xxx;
                            long hash2 = BlockPos.asLong(xCheck, yyCheck, zCheck);
                            if (lookupPoints.containsKey(hash2)) {
                                stillClear = false;
                                break;
                            }
                        }
                        if (stillClear) {
                            if (xx < maxDist) {
                                float dist = Vector3f.distance(x, y, z, xCheck, yCheck, zCheck);
                                return Math.min(1F, (dist / maxDist));
                            } else {
                                return 0.9999F;
                            }
                        }
                    }
                } else {
                    return 1F;
                }
            }
            return 1F;
        }
    }

}
