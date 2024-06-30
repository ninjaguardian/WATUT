package com.corosus.watut;

import com.mojang.blaze3d.vertex.VertexBuffer;

import java.util.ArrayList;
import java.util.List;

public class ThreadedCloudBuilder implements Runnable {

    private VertexBuffer cloudBuffer;
    private List<VertexBuffer> cloudBuffers = new ArrayList<>();
    private boolean multiBufferMode = false;
    private int cloudCount = 150;

    private boolean isRunning = false;

    public boolean isRunning() {
        return isRunning;
    }

    public VertexBuffer getCloudBuffer() {
        return cloudBuffer;
    }

    public void setCloudBuffer(VertexBuffer cloudBuffer) {
        this.cloudBuffer = cloudBuffer;
    }

    public List<VertexBuffer> getCloudBuffers() {
        return cloudBuffers;
    }

    public void setCloudBuffers(List<VertexBuffer> cloudBuffers) {
        this.cloudBuffers = cloudBuffers;
    }

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

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void run() {
        isRunning = true;
        doWork();
        isRunning = false;
    }

    public void doWork() {

    }
}
