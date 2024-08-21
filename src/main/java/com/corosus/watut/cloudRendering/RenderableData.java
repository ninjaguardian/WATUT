package com.corosus.watut.cloudRendering;

import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedVertexBuffer;
import org.joml.Vector3f;

public class RenderableData {

    //alternating buffers for off thread building
    private ThreadedVertexBuffer vertexBufferA;
    private ThreadedVertexBuffer vertexBufferAddedPoints;
    private ThreadedVertexBuffer vertexBufferRemovedPoints;
    //private ThreadedVertexBuffer vertexBufferB;

    //skychunk VBO data
    private ThreadedBufferBuilder.RenderedBuffer vbo;
    private ThreadedBufferBuilder.RenderedBuffer vboAddedPoints;
    private ThreadedBufferBuilder.RenderedBuffer vboRemovedPoints;

    //data for uniform
    private Vector3f lightningPos;

    //buffer swapper, each renderable data object could be in different states for this unless we sync them as new one are created
    private boolean bufferAActive = true;

    public RenderableData() {

    }

    public void initBuffersIfNeeded() {
        if (vertexBufferA == null) vertexBufferA = new ThreadedVertexBuffer(ThreadedVertexBuffer.Usage.STATIC);
        if (vertexBufferAddedPoints == null) vertexBufferAddedPoints = new ThreadedVertexBuffer(ThreadedVertexBuffer.Usage.STATIC);
        if (vertexBufferRemovedPoints == null) vertexBufferRemovedPoints = new ThreadedVertexBuffer(ThreadedVertexBuffer.Usage.STATIC);
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

    public ThreadedBufferBuilder.RenderedBuffer getVbo() {
        return vbo;
    }

    public void setVbo(ThreadedBufferBuilder.RenderedBuffer vbo) {
        this.vbo = vbo;
    }

    public ThreadedBufferBuilder.RenderedBuffer getVboAddedPoints() {
        return vboAddedPoints;
    }

    public void setVboAddedPoints(ThreadedBufferBuilder.RenderedBuffer vboAddedPoints) {
        this.vboAddedPoints = vboAddedPoints;
    }

    public ThreadedBufferBuilder.RenderedBuffer getVboRemovedPoints() {
        return vboRemovedPoints;
    }

    public void setVboRemovedPoints(ThreadedBufferBuilder.RenderedBuffer vboRemovedPoints) {
        this.vboRemovedPoints = vboRemovedPoints;
    }

    public ThreadedVertexBuffer getVertexBufferAddedPoints() {
        return vertexBufferAddedPoints;
    }

    public ThreadedVertexBuffer getVertexBufferRemovedPoints() {
        return vertexBufferRemovedPoints;
    }

    public Vector3f getLightningPos() {
        return lightningPos;
    }

    public void setLightningPos(Vector3f lightningPos) {
        this.lightningPos = lightningPos;
    }
}
