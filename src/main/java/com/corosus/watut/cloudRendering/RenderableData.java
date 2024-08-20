package com.corosus.watut.cloudRendering;

import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilderPersistentStorage;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedVertexBuffer;
import org.joml.Vector3f;

public class RenderableData {

    //alternating buffers for off thread building
    private ThreadedVertexBuffer vertexBufferA;
    //private ThreadedVertexBuffer vertexBufferB;

    //skychunk VBO data
    private ThreadedBufferBuilderPersistentStorage.RenderedBuffer vbo;

    //data for uniform
    private Vector3f lightningPos;

    //buffer swapper, each renderable data object could be in different states for this unless we sync them as new one are created
    private boolean bufferAActive = true;

    public RenderableData() {

    }

    public void initBuffersIfNeeded() {
        if (vertexBufferA == null) vertexBufferA = new ThreadedVertexBuffer(ThreadedVertexBuffer.Usage.STATIC);
        //if (vertexBufferB == null) vertexBufferB = new ThreadedVertexBuffer(ThreadedVertexBuffer.Usage.STATIC);
    }

    public ThreadedVertexBuffer getActiveRenderingVertexBuffer() {
        initBuffersIfNeeded();
        return vertexBufferA;
    }/*

    public ThreadedVertexBuffer getOffthreadBuildingVertexBuffer() {
        initBuffersIfNeeded();
        return bufferAActive ? vertexBufferB : vertexBufferA;
    }*/

    public void swapBuffers() {
        bufferAActive = !bufferAActive;
    }

    public ThreadedBufferBuilderPersistentStorage.RenderedBuffer getVbo() {
        return vbo;
    }

    public void setVbo(ThreadedBufferBuilderPersistentStorage.RenderedBuffer vbo) {
        this.vbo = vbo;
    }

    public Vector3f getLightningPos() {
        return lightningPos;
    }

    public void setLightningPos(Vector3f lightningPos) {
        this.lightningPos = lightningPos;
    }
}
