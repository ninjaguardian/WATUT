package com.corosus.watut.cloudRendering.threading;

import com.corosus.coroutil.util.CULog;
import com.corosus.watut.*;
import com.corosus.watut.cloudRendering.Cloud;
import com.corosus.watut.cloudRendering.RenderableData;
import com.corosus.watut.cloudRendering.SkyChunk;
import com.corosus.watut.cloudRendering.SkyChunkManager;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;

public class ThreadedCloudBuilder {

    private Random rand = new Random();
    private Random rand2 = new Random();

    //private List<RenderableData> renderableData = new ArrayList<>();
    //private List<RenderableData> renderableCloudsToAdd = new ArrayList<>();
    //this set to false isnt supported anymore since adding more features like multithread
    private boolean multiBufferMode = true;
    private int cloudCount = 150;

    private int quadCount = 0;
    private int pointCount = 0;

    private long timeOffset = 0;

    private int sizeX = 30;
    private int sizeY = 20;
    private int sizeZ = 30;

    private int cloudsY = 140;

    private Cloud cloudShape = new Cloud(sizeX, sizeY, sizeZ);
    private Cloud cloudShape2 = new Cloud(sizeX, sizeY, sizeZ);
    private boolean cloudShapeNeedsPrecalc = true;
    private boolean cloudShape2NeedsPrecalc = true;

    private boolean isRunning = false;
    private boolean isWaitingToUploadData = false;

    private int gameTicksAtStart = 0;

    private ThreadedCloudBuilderJob threadedCloudBuilderJob;

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public synchronized boolean isWaitingToUploadData() {
        return isWaitingToUploadData;
    }

    public synchronized void setWaitingToUploadData(boolean waitingToUploadData) {
        isWaitingToUploadData = waitingToUploadData;
    }

    /*public List<RenderableData> getRenderableClouds() {
        return renderableData;
    }

    public void setRenderableClouds(List<RenderableData> renderableData) {
        this.renderableData = renderableData;
    }

    public List<RenderableData> getRenderableCloudsToAdd() {
        return renderableCloudsToAdd;
    }

    public void setRenderableCloudsToAdd(List<RenderableData> renderableCloudsToAdd) {
        this.renderableCloudsToAdd = renderableCloudsToAdd;
    }*/

    public boolean isMultiBufferMode() {
        return multiBufferMode;
    }

    public void setMultiBufferMode(boolean multiBufferMode) {
        this.multiBufferMode = multiBufferMode;
    }

    public int getCloudCount() {
        return cloudCount;
    }

    public void setCloudCount(int cloudCount) {
        this.cloudCount = cloudCount;
    }

    public int getSizeX() {
        return sizeX;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public void setSizeZ(int sizeZ) {
        this.sizeZ = sizeZ;
    }

    public int getCloudsY() {
        return cloudsY;
    }

    public void setCloudsY(int cloudsY) {
        this.cloudsY = cloudsY;
    }

    public synchronized void start() {

        this.gameTicksAtStart = getTicks();
        threadedCloudBuilderJob = new ThreadedCloudBuilderJob(this);
        setRunning(true);
        threadedCloudBuilderJob.start();
        //super.start();
    }

    public void doWork() {
        pointCount = 0;
        quadCount = 0;

        //unused
        Vec3 vec3 = new Vec3(0, 0, 0);

        if (multiBufferMode) {
            //TODO: REUSE THE BUFFERS, STOP MAKING NEW ONES
            ////renderableClouds.clear();
            //renderableCloudsToAdd.clear();

            rand = new Random(5);

            ThreadedBufferBuilder bufferbuilder = WatutMod.threadedBufferBuilder;
            float scale = 4;
            scale = 1;
            timeOffset = this.getTicks();

            //clear out old skychunk data
            for (SkyChunk skyChunk : SkyChunkManager.instance().getSkyChunks().values()) {
                skyChunk.getPoints().clear();
            }

            //FIRST WE ITERATE CLOUDS TO PUT INTO SKYCHUNK DATA

            for (int ii = 0; ii < cloudCount; ii++) {

                if (rand.nextFloat() <= 0.5F) continue;

                //VertexBuffer cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                int offsetXZ = (int) (20 * scale) / 2;
                //offsetXZ = 0;

                //BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.renderVBO(bufferbuilder, 0 - pos.x, d3, 0 - pos.z, vec3, 0.5F);
                //renderableCloud.setRenderedBuffer(this.renderVBO(bufferbuilder, -offsetXZ, 140, -offsetXZ, vec3, scale));
                RenderableData renderableData = null;
                this.generateCloud(bufferbuilder, 0, cloudsY, 0, vec3, scale, renderableData, ii);
                //BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = ;
                //BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.renderVBO(bufferbuilder, 0 - pos.x, 0, 0 - pos.z, vec3, 0.5F);
                //cloudBuffer.bind();
                //cloudBuffer.upload(bufferbuilder$renderedbuffer);

                //VertexBuffer.unbind();
            }

            //THEN WE RENDER THE SKYCHUNKS INTO VBOS

            for (SkyChunk skyChunk : SkyChunkManager.instance().getSkyChunks().values()) {
                skyChunk.getRenderableData().setVbo(renderSkyChunkVBO(bufferbuilder, skyChunk, 0, cloudsY, 0, vec3, scale));
            }

            //System.out.println("skychunk count: " + SkyChunkManager.instance().getSkyChunks().size());


        }/* else {
            ThreadedBufferBuilder bufferbuilder = ThreadedTesselator.getInstance().getBuilder();
            if (this.cloudBuffer != null) {
                this.cloudBuffer.close();
            }

            this.cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            //BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.buildClouds(bufferbuilder, d2, d3, d4, vec3);

            //from the cloud grid size
            float scale = 3;
            int offsetXZ = (int) (20 * scale) / 2;

            //BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.renderVBO(bufferbuilder, 0 - pos.x, d3, 0 - pos.z, vec3, 0.5F);
            timeOffset = this.getTicks();
            rand = new Random(5);
            ThreadedBufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.renderVBO(bufferbuilder, -offsetXZ, 140, -offsetXZ, vec3, scale);
            //BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.renderVBO(bufferbuilder, 0 - pos.x, 0, 0 - pos.z, vec3, 0.5F);
            this.cloudBuffer.bind();
            this.cloudBuffer.upload(bufferbuilder$renderedbuffer);
            VertexBuffer.unbind();
        }*/

        CULog.log("total clouds point count: " + pointCount);
        CULog.log("total clouds quad count: " + quadCount);
        CULog.log("total vbos count: " + SkyChunkManager.instance().getSkyChunks().size());
    }

    private ThreadedBufferBuilder.RenderedBuffer renderSkyChunkVBO(ThreadedBufferBuilder bufferIn, SkyChunk skyChunk, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, float scale) {

        bufferIn.begin(VertexFormat.Mode.QUADS, WatutMod.POSITION_TEX_COLOR_NORMAL_VEC3);

        //TODO: implement hasData or simply remove all data points in skychunk before each new vbo build
        if (skyChunk.hasData() || true) {
            for (SkyChunk.SkyChunkPoint entry : skyChunk.getPoints().values()) {
                List<Direction> listRenderables = entry.getRenderableSides();
                float dist = entry.calculateNormalizedDistanceToOutside();
                entry.setNormalizedDistanceToOutside(dist);
                renderCloudCube(bufferIn, cloudsX, cloudsY, cloudsZ, cloudsColor,
                        new Vector3f((skyChunk.getX() * SkyChunk.size) + entry.getX(), (skyChunk.getY() * SkyChunk.size) + entry.getY(), (skyChunk.getZ() * SkyChunk.size) + entry.getZ())
                        , scale, listRenderables, entry);
            }
        }

        return bufferIn.end();
    }

    private int getTicks() {
        return gameTicksAtStart;
    }

    private ClientLevel getLevelVolatile() {
        return Minecraft.getInstance().level;
    }

    private int getTicksVolatile() {
        return (int) getLevelVolatile().getGameTime();
    }

    private void generateCloud(ThreadedBufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, float scale, RenderableData renderableData, int cloudIndex) {
        //RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        //bufferIn.begin(VertexFormat.Mode.QUADS, WatutMod.POSITION_TEX_COLOR_NORMAL_VEC3);

        //float timeShortAdj2 = (this.getTicks()) * 2F;

        if (this.getTicks() % (20 * 30) == 0 && false) {
            cloudShapeNeedsPrecalc = true;
        }

        if (cloudShapeNeedsPrecalc) {
            CULog.log("performing one time cloud shape precalc");
            cloudShapeNeedsPrecalc = false;

            cloudShape = new Cloud(sizeX, sizeY, sizeZ);

            for (int x = 0; x < cloudShape.getSizeX(); x++) {
                for (int y = 0; y < cloudShape.getSizeY(); y++) {
                    for (int z = 0; z < cloudShape.getSizeZ(); z++) {

                        /*if ((x == cloudShape.getSizeX() / 2 && y == cloudShape.getSizeY() / 4 * 3 && z > 3 && z < cloudShape.getSizeZ() - 3)
                                || (x == cloudShape.getSizeX() / 2 && z == cloudShape.getSizeZ() / 2 && y > 3 && y < cloudShape.getSizeY() - 3)) {
                            //forceShapeAdj = 1;
                            float noiseThreshAdj = 1;
                            cloudShape.addPoint(x, y, z, noiseThreshAdj);

                        }*/

                        float xzRatio = Mth.clamp(1F - ((float)y / (float)(cloudShape.getSizeY() - 30)), 0.3F, 0.7F);

                        if ((x > (xzRatio * cloudShape.getSizeX()) && x < cloudShape.getSizeX() - (xzRatio * cloudShape.getSizeX()))
                                && (z > (xzRatio * cloudShape.getSizeZ()) && z < cloudShape.getSizeZ() - (xzRatio * cloudShape.getSizeZ()))) {

                            float noiseThreshAdj = 1;
                            cloudShape.addPoint(x, y, z, noiseThreshAdj);
                        }

                        /*if ((x == cloudShape.getSizeX() / 2 && y == cloudShape.getSizeY() / 4 * 3 && z > 3 && z < cloudShape.getSizeZ() - 3)
                                || (x == cloudShape.getSizeX() / 2 && z == cloudShape.getSizeZ() / 2 && y > 3 && y < cloudShape.getSizeY() - 3)) {
                            //forceShapeAdj = 1;
                            float noiseThreshAdj = 1;
                            cloudShape.addPoint(x, y, z, noiseThreshAdj);

                        }*/

                        /*float noiseThreshAdj = 1;
                        cloudShape.addPoint(x, y, z, noiseThreshAdj);*/

                    }
                }
            }

            cloudShape2 = new Cloud(sizeX, sizeY, sizeZ);

            for (int x = 0; x < cloudShape2.getSizeX(); x++) {
                for (int y = 0; y < cloudShape2.getSizeY(); y++) {
                    for (int z = 0; z < cloudShape2.getSizeZ(); z++) {

                        if ((x == cloudShape2.getSizeX() / 2 && y == cloudShape2.getSizeY() / 4 * 1 && z > 3 && z < cloudShape2.getSizeZ() - 3)
                                || (z == cloudShape2.getSizeZ() / 2 && y == cloudShape2.getSizeY() / 4 * 1 && x > 3 && x < cloudShape2.getSizeX() - 3)) {
                            //forceShapeAdj = 1;
                            float noiseThreshAdj = 1;
                            cloudShape2.addPoint(x, y, z, noiseThreshAdj);

                        }

                        /*if ((x == 0 || x == cloudShape2.getSizeX() - 1) && (z == 0 || z == cloudShape2.getSizeZ() - 1)) {
                            //forceShapeAdj = 1;
                            float noiseThreshAdj = 1;
                            cloudShape2.addPoint(x, y, z, noiseThreshAdj);
                        }*/

                        /*float noiseThreshAdj = 1;
                        cloudShape2.addPoint(x, y, z, noiseThreshAdj);*/

                    }
                }
            }

            //how far from a point in the shape to have no threshold adjustment at all
            int maxDistToZeroAdjust = 6;

            buildShapeThresholds(cloudShape, maxDistToZeroAdjust);
            buildShapeThresholds(cloudShape2, maxDistToZeroAdjust);

            CULog.log("finished one time cloud shape precalc");
        }

        buildCloud(bufferIn, cloudsX, cloudsY, cloudsZ, cloudsColor, scale, cloudShape2, renderableData, cloudIndex);
    }

    private void buildShapeThresholds(Cloud cloudShape, int maxDistToZeroAdjust) {
        //TODO: should only ever run once (in dev lifetime), as it should be stored/baked to data for use at runtime
        for (int x = 0; x < cloudShape.getSizeX(); x++) {
            for (int y = 0; y < cloudShape.getSizeY(); y++) {
                for (int z = 0; z < cloudShape.getSizeZ(); z++) {

                    float maxDist = 99999;

                    for (int xt = -maxDistToZeroAdjust; xt <= maxDistToZeroAdjust; xt++) {
                        for (int yt = -maxDistToZeroAdjust; yt <= maxDistToZeroAdjust; yt++) {
                            for (int zt = -maxDistToZeroAdjust; zt <= maxDistToZeroAdjust; zt++) {

                                int checkX = x + xt;
                                int checkY = y + yt;
                                int checkZ = z + zt;
                                if (xt > 0) {
                                    int what = 0;
                                }
                                //CULog.dbg(checkX + " - " + checkY + " - " + checkZ);
                                //System.out.println();
                                if (checkX < 0 || checkX >= cloudShape.getSizeX()) continue;
                                if (checkY < 0 || checkY >= cloudShape.getSizeY()) continue;
                                if (checkZ < 0 || checkZ >= cloudShape.getSizeZ()) continue;

                                Cloud.CloudPoint cloudPointAvoidChanging = cloudShape.getPoint(x, y, z);
                                if (cloudPointAvoidChanging == null || cloudPointAvoidChanging.getShapeAdjustThreshold() != 1) {
                                    Cloud.CloudPoint cloudPointCheck = cloudShape.getPoint(checkX, checkY, checkZ);

                                    //TODO: for now assuming threshold of 1 = pure shape point, not a distance adjusted one, maybe change this behavior, flag them correctly
                                    if (cloudPointCheck != null && cloudPointCheck.getShapeAdjustThreshold() == 1) {

                                        float dist = Vector3f.distance(x, y, z, checkX, checkY, checkZ);

                                        if (dist <= maxDist && dist <= maxDistToZeroAdjust) {
                                            maxDist = dist;
                                            float distFraction = Mth.clamp(1 - (dist / maxDistToZeroAdjust), 0, 0.99F);
                                            //distFraction = 0.5F;
                                            //distFraction = rand.nextFloat() * 0.5F;
                                            //Cloud.CloudPoint cloudPoint = cloudShape.getPoint(checkX, checkY, checkZ);
                                            cloudShape.addPoint(x, y, z, distFraction);
                                        }

                                    }
                                }

                            }
                        }
                    }

                }
            }
        }
    }

    private void buildCloud(ThreadedBufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, float scale, Cloud cloudShapes, RenderableData renderableData, int cloudIndex) {
        //Vector3f cubePos = new Vector3f(0, 0, 0);



        int columns = (int) Math.sqrt(cloudCount);
        int rows = cloudCount / columns;

        columns = 20;
        rows = 20;

        int xOffset = cloudIndex % columns;
        int zOffset = cloudIndex / columns;

        xOffset = xOffset * sizeX;
        zOffset = zOffset * sizeZ;

        float radius = 350;
        float radiusYCube = 30;
        //Random rand = new Random();
        Vector3f cubePos = new Vector3f((float) (rand.nextFloat() * radius - rand.nextFloat() * radius),
                (float) (rand.nextFloat() * radiusYCube - rand.nextFloat() * radiusYCube),
                (float) (rand.nextFloat() * radius - rand.nextFloat() * radius));

        //cubePos.mul(scale * 2);

        cubePos = new Vector3f(xOffset, 0, zOffset);

        Vector3f vec = new Vector3f(cubePos);
        //Random rand4 = new Random();
        vec.add((float) cloudsX, (float) cloudsY, (float) cloudsZ);
        //TODO: skychunk relocate me
        //renderableCloud.setLightningPos(vec);

        //cubePos = new Vector3f(0, 0, 0);

        //int radius = 40;
        //System.out.println("NEW CLOUD");
        Cloud cloud = new Cloud(sizeX, sizeY, sizeZ);
        cloud.setCloudShape(cloudShape);
        //cloud.addPoint(0, 0, 1);
        //cloud.addPoint(0, 0, 1);

        PerlinNoise perlinNoise = PerlinNoiseHelper.get().getPerlinNoise();
        if (Minecraft.getInstance().level == null) return;
        long time = (long) (Minecraft.getInstance().level.getGameTime() * 0.1F);
        time = (long) (Minecraft.getInstance().level.getGameTime() * 0.2F);
        //time = (long) (Minecraft.getInstance().level.getGameTime() * 0.8F);
        time += (cloudIndex * 25);
        time = 0;

        /*for (int x = 0; x <= cloud.getSizeX(); x++) {
            for (int y = 0; y <= cloud.getSizeY(); y++) {
                for (int z = 0; z <= cloud.getSizeZ(); z++) {

                }
            }
        }*/

        float timeShortAdj1 = (time) * 1F;
        float timeShortAdj2 = (time) * 1F;
        float timeShortAdj3 = (time) * 5F;

        int radiusY = 10;
        float maxDistDiag = 34;
        for (int x = 0; x < cloud.getSizeX(); x++) {
            for (int y = 0; y < cloud.getSizeY(); y++) {
                for (int z = 0; z < cloud.getSizeZ(); z++) {
                    float distFromCenterXZ = Vector3f.distance(cloud.getSizeX()/2, 0, cloud.getSizeZ()/2, x, 0, z);
                    float distFromCenterY = Vector3f.distance(0, cloud.getSizeY()/2, 0, 0, y, 0);
                    float vecXZ = distFromCenterXZ / (cloud.getSizeX() - 3);
                    float vecY = distFromCenterY / (cloud.getSizeY() - 3);
                    //require more strict threshold as it gets further from center of cloud
                    //float noiseThreshAdj = (vecXZ + vecY) / 2F * 0.9F;
                    float noiseThreshAdj = (vecXZ + vecY) / 2F * 1.8F;
                    //float noiseThreshAdj = (vecXZ + vecY) / 2F * 1.2F;
                    //float noiseThreshAdj = 0.5F;//(vecXZ + vecY) / 2F * 1.2F;
                    //float noiseThreshAdj = (vecXZ + vecY) / 2F * 0.8F;
                    int indexX = (int) Math.floor(x + cubePos.x);
                    int indexY = (int) Math.floor(y + cubePos.y);
                    int indexZ = (int) Math.floor(z + cubePos.z);

                    //TEMP
                    //noiseThreshAdj = 0.8F;
                    //noiseThreshAdj = 0.3F;

                    float uh = (float) ((Math.sin(timeShortAdj3 * 0.005F) + 1)) * 0.1F;
                    float uh2 = (float) ((-Math.sin(timeShortAdj3 * 0.005F)));

                    //double forceShapeAdj = 0;
                    //forced shape testing, a cross shape ( t )
                    float shapeAdjThreshold = 0F;
                    float more = 0.8F;
                    Cloud.CloudPoint cloudPoint = cloudShape.getPoint(x, y, z);
                    if (cloudPoint != null) {
                        //noiseThreshAdj -= (Math.sin(timeShortAdj2 * 0.01F) * 0.5F * cloudPoint.getShapeAdjustThreshold());
                        shapeAdjThreshold -= cloudPoint.getShapeAdjustThreshold() * uh * more;
                        //noiseThreshAdj = 0;
                    }
                    Cloud.CloudPoint cloudPoint2 = cloudShape2.getPoint(x, y, z);
                    if (cloudPoint2 != null) {
                        //noiseThreshAdj -= (Math.sin(timeShortAdj2 * 0.01F) * 0.5F * cloudPoint.getShapeAdjustThreshold());
                        shapeAdjThreshold -= cloudPoint2.getShapeAdjustThreshold() * uh2 * more;
                        //noiseThreshAdj = 0;
                    }
                    noiseThreshAdj += shapeAdjThreshold;
                    if ((x == cloud.getSizeX() / 2 && y == cloud.getSizeY() / 4 * 3)
                            || (x == cloud.getSizeX() / 2 && z == cloud.getSizeZ() / 2)) {
                        //forceShapeAdj = 1;
                        //noiseThreshAdj -= (0.4 * Math.sin(timeShortAdj2 * 0.01F) * 1.2F);
                    }

                    float mixedThreshold = shapeAdjThreshold;

                    //noiseThreshAdj -= 0.2F;

                    //noiseThreshAdj += Math.sin(timeShortAdj1 * 0.015F) * 0.15F;
                    noiseThreshAdj += Math.sin(timeShortAdj1 * 0.015F) * 0.22F;
                    //noiseThreshAdj += Math.sin(timeShortAdj1 * 0.015F) * 0.55F;

                    double scaleP = 10;
                    double noiseVal = perlinNoise.getValue(((indexX) * scaleP) + time, ((indexY) * scaleP) + time,((indexZ) * scaleP) + time)/* + 0.2F*/;
                    //noiseVal = 0.2F;
                    if (Math.abs(noiseVal) > 0.0 + noiseThreshAdj) {
                        //SkyChunkManager.instance().
                        //cloud.addPoint(x, y, z);
                        SkyChunkManager.instance().addPoint(indexX, indexY, indexZ);
                    }
                    /*if (vec < 0.80) {
                        cloud.addPoint(x, y, z);
                    }*/
                }
            }
        }

        /*for (Map.Entry<Long, Cloud.CloudPoint> entry : cloud.getLookupCloudPoints().entrySet()) {
            List<Direction> listRenderables = entry.getValue().getRenderableSides();
            float dist = entry.getValue().calculateNormalizedDistanceToOutside();
            entry.getValue().setNormalizedDistanceToOutside(dist);
            renderCloudCube(bufferIn, cloudsX, cloudsY, cloudsZ, cloudsColor,
                    new Vector3f(cubePos.x + entry.getValue().getX(), cubePos.y + entry.getValue().getY(), cubePos.z + entry.getValue().getZ())
                    , scale, listRenderables, entry.getValue(), 0, renderableCloud);
        }*/
    }

    private void renderCloudCube(ThreadedBufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, Vector3f cubePos, float scale, List<Direction> directions, SkyChunk.SkyChunkPoint cloudPoint) {
        //Random rand = rand2;

        pointCount++;

        Quaternionf q2 = new Quaternionf(0, 0, 0, 1);
        int range = 5;
        range = 180;
        /*Vector3f w = new Vector3f();
        w.rota*/
        /*q2.mul(Vector3f.XP.rotationDegrees(rand.nextInt(range)));
        q2.mul(Vector3f.YP.rotationDegrees(rand.nextInt(range)));
        //q2.mul(Vector3f.YP.rotationDegrees(rand.nextInt(45)));
        q2.mul(Vector3f.ZP.rotationDegrees(rand.nextInt(range)));*/



        Random rand2 = new Random((long) (cubePos.x + cubePos.z));

        boolean randRotate = false;

        float particleAlpha = 1F;
        //particleAlpha = (float) Math.random();

        float threshold = 0;
        //TODO: BAD CODE NOW since skychunk change, redesign/relocate
        Cloud.CloudPoint cloudShapePoint = cloudShape2.getPoint(cloudPoint.getX(), cloudPoint.getY(), cloudPoint.getZ());
        if (cloudShapePoint != null) {
            threshold = cloudShapePoint.getShapeAdjustThreshold();
        } else {
            threshold = rand2.nextFloat();
            threshold = rand2.nextFloat();
            threshold = (rand2.nextFloat() * 0.5F);
            threshold = 0.5F;

        }

        for (Direction dir : directions) {
            Quaternionf quaternion = dir.getRotation();

            Vector3f[] avector3f3 = new Vector3f[]{
                    new Vector3f(-1.0F, 0.0F, -1.0F),
                    new Vector3f(-1.0F, 0.0F, 1.0F),
                    new Vector3f(1.0F, 0.0F, 1.0F),
                    new Vector3f(1.0F, 0.0F, -1.0F)};

            Vector3f[] avector3f32 = new Vector3f[]{
                    new Vector3f(-1.0F, 0.0F, -1.0F),
                    new Vector3f(-1.0F, 0.0F, 1.0F),
                    new Vector3f(1.0F, 0.0F, 1.0F),
                    new Vector3f(1.0F, 0.0F, -1.0F)};

            Vector3f normal = new Vector3f(dir.getNormal().getX(), dir.getNormal().getY(), dir.getNormal().getZ());
            //normal = new Vector3f(0, 0, 0);
            if (randRotate) normal.rotate(q2);
            float normalRange = 0.1F;
            //normal.mul(normalRange);
            //normal.add(1-normalRange, 1-normalRange, 1-normalRange);
            //float uh = 0.55F;
            //normal.add(uh, uh, uh);

            //normal = new Vector3f((float) Math.random(), (float) Math.random(), (float) Math.random());
            //normal = new Vector3f(1, 0, 0);

            for(int i = 0; i < 4; ++i) {
                Vector3f vector3f = avector3f3[i];
                Vector3f vector3f2 = avector3f32[i];
                vector3f.rotate(quaternion);
                vector3f2.rotate(quaternion);
                vector3f.add((float) dir.getStepX(), (float) dir.getStepY(), (float) dir.getStepZ());
                if (randRotate) vector3f.rotate(q2);
                //vector3f.mul(scale * 2);
                vector3f.mul(scale / 2F);
                //vector3f.add((float) cloudsX + 0.5F, (float) cloudsY, (float) cloudsZ + 0.5F);
                vector3f.add((float) cloudsX + 0.0F, (float) cloudsY, (float) cloudsZ + 0.0F);
                //vector3f.add((float) 0 + 0.5F, (float) cloudsY, (float) 0 + 0.5F);
                //vector3f.add((float) cubePos.x, (float) cubePos.y, (float) cubePos.z);
                //vector3f.add((float) cubePos.x, (float) cubePos.y, (float) cubePos.z);
                vector3f.add((float) cubePos.x * scale, (float) cubePos.y * scale, (float) cubePos.z * scale);
            }

            //TextureAtlasSprite sprite = ParticleRegistry.cloud_square;
            TextureAtlasSprite sprite = ParticleRegistry.cloud_square.getSprite();

            float f7 = sprite.getU0();
            float f8 = sprite.getU1();
            float f5 = sprite.getV0();
            float f6 = sprite.getV1();

            /*f7 = (float) Math.random();
            f8 = (float) Math.random();
            f5 = (float) Math.random();
            f6 = (float) Math.random();*/

            /*f7 = 0;
            f8 = 1;
            f5 = 0;
            f6 = 1;*/


            float particleRed = (float) cloudsColor.x;
            float particleGreen = (float) cloudsColor.y;
            float particleBlue = (float) cloudsColor.z;

            particleRed = 1F;
            //particleRed = (float) (0.85F + (rand2.nextFloat() * 0.08F));
            /*particleGreen = (float) Math.random();
            particleBlue = (float) Math.random();*/
            particleGreen = threshold;
            //particleBlue = (float) particleRed - threshold;
            particleBlue = 0;

            /*particleGreen = particleRed;
            particleBlue = particleRed;*/

            threshold = 0.7F + (threshold * 0.2F);

            threshold = 0.93F;
            threshold = 0.83F;
            threshold = 0.9F;
            //threshold = 1F;
            //threshold = 0.8F;
            //threshold = 0.2F;

            float colorShift = 0.82F;
            colorShift = 0.99F;
            particleRed = threshold * colorShift;
            particleGreen = threshold * colorShift;
            particleBlue = threshold;


            float impact1 = 0.1F;
            //float impact1 = 0.0F;
            float heightFract = (1F - impact1) + ((cubePos.y / (float)(sizeY)) * impact1);
            //float heightFract = 1F;//1.5F + ((cubePos.y / (float)(sizeY)) * 0.5F);
            particleRed *= heightFract;
            particleGreen *= heightFract;
            particleBlue *= heightFract;

            //float impact2 = 0.1F;
            //float impact2 = 0.15F;
            //float impact2 = 0.0F;
            //moved to shader, keep this 1
            //float impact2 = 0.15F;
            float impact2 = 1F;
            float distToOutsideAdj = (1F - impact2) + cloudPoint.getNormalizedDistanceToOutside() * impact2;//Mth.clamp(1F - cloudPoint.getNormalizedDistanceToOutside(), 0, 1);
            //distToOutsideAdj = cloudPoint.getNormalizedDistanceToOutside();
            if (distToOutsideAdj == -1F) {
                //System.out.println("???????");
            }

            /*particleRed *= distToOutsideAdj;
            particleGreen *= distToOutsideAdj;
            particleBlue *= distToOutsideAdj;*/

            particleRed = Math.min(1F, particleRed);
            particleGreen = Math.min(1F, particleGreen);
            particleBlue = Math.min(1F, particleBlue);

            float maxDistFromCloudClass = 4F;
            float distToOutsideHalfBlockAdj = (0.5F / maxDistFromCloudClass) * impact2;
            //if (distToOutsideAdj != 0) {
                avector3f32[0].y *= distToOutsideHalfBlockAdj;
                avector3f32[1].y *= distToOutsideHalfBlockAdj;
                avector3f32[2].y *= distToOutsideHalfBlockAdj;
                avector3f32[3].y *= distToOutsideHalfBlockAdj;
            //}

            bufferIn.vertex(avector3f3[0].x(), avector3f3[0].y(), avector3f3[0].z()).uv(f8, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z())
                    .vertex(distToOutsideAdj, avector3f32[0].y(), distToOutsideHalfBlockAdj).endVertex();
            bufferIn.vertex(avector3f3[1].x(), avector3f3[1].y(), avector3f3[1].z()).uv(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z())
                    .vertex(distToOutsideAdj, avector3f32[1].y(), distToOutsideHalfBlockAdj).endVertex();
            bufferIn.vertex(avector3f3[2].x(), avector3f3[2].y(), avector3f3[2].z()).uv(f7, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z())
                    .vertex(distToOutsideAdj, avector3f32[2].y(), distToOutsideHalfBlockAdj).endVertex();
            bufferIn.vertex(avector3f3[3].x(), avector3f3[3].y(), avector3f3[3].z()).uv(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(normal.x(), normal.y(), normal.z())
                    .vertex(distToOutsideAdj, avector3f32[3].y(), distToOutsideHalfBlockAdj).endVertex();
            quadCount++;

        }

    }
}
