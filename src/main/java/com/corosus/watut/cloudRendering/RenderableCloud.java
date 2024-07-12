package com.corosus.watut.cloudRendering;

import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedVertexBuffer;
import org.joml.Vector3f;

public class RenderableCloud {

    private ThreadedVertexBuffer vertexBuffer;
    private ThreadedBufferBuilder.RenderedBuffer renderedBuffer;
    private Vector3f lightningPos;

    public ThreadedVertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(ThreadedVertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public ThreadedBufferBuilder.RenderedBuffer getRenderedBuffer() {
        return renderedBuffer;
    }

    public void setRenderedBuffer(ThreadedBufferBuilder.RenderedBuffer renderedBuffer) {
        this.renderedBuffer = renderedBuffer;
    }

    public Vector3f getLightningPos() {
        return lightningPos;
    }

    public void setLightningPos(Vector3f lightningPos) {
        this.lightningPos = lightningPos;
    }
}
