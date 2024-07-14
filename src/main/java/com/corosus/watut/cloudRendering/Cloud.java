package com.corosus.watut.cloudRendering;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cloud {

    private int sizeX = 20;
    private int sizeY = 20;
    private int sizeZ = 20;
    private HashMap<Long, CloudPoint> lookupCloudPoints = new HashMap<>();
    //for influencing a rough predetermined shape
    private Cloud cloudShape;

    public Cloud(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public void initCloudShape() {
        this.cloudShape = new Cloud(sizeX, sizeY, sizeZ);
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public Cloud getCloudShape() {
        return cloudShape;
    }

    public void setCloudShape(Cloud cloudShape) {
        this.cloudShape = cloudShape;
    }

    public HashMap<Long, CloudPoint> getLookupCloudPoints() {
        return lookupCloudPoints;
    }

    public CloudPoint getPoint(int x, int y, int z) {
        long hash = BlockPos.asLong(x, y, z);
        return lookupCloudPoints.get(hash);
    }

    public void addPoint(int x, int y, int z, float shapeAdjustThreshold) {
        long hash = this.addPoint(x, y, z);
        lookupCloudPoints.get(hash).setShapeAdjustThreshold(shapeAdjustThreshold);
    }

    public long addPoint(int x, int y, int z) {
        long hash = BlockPos.asLong(x, y, z);
        if (lookupCloudPoints.containsKey(hash)) {
            /*System.out.println("ERROR: entry exists already " + "x=" + x +
                    ", y=" + y +
                    ", z=" + z + " - " + Objects.hash(x, y, z));*/
            return hash;
        }
        CloudPoint cloudPoint = new CloudPoint(x, y, z, this);
        lookupCloudPoints.put(hash, cloudPoint);
        //System.out.println("added " + cloudPoint.toString() + " - " + Objects.hash(x, y, z));
        return hash;
    }

    public class CloudPoint {
        private int x;
        private int y;
        private int z;
        //if point has a part/side of its voxel to render
        private boolean isVisible = false;
        private Cloud cloud;
        private float shapeAdjustThreshold = 0;
        private float normalizedDistanceToOutside = 1F;

        public CloudPoint(int x, int y, int z, Cloud cloud) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.cloud = cloud;
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
            CloudPoint that = (CloudPoint) o;
            return x == that.x && y == that.y && z == that.z;
        }

        @Override
        public String toString() {
            return "CloudPoint{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }

}
