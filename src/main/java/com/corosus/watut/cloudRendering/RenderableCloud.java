package com.corosus.watut.cloudRendering;

import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedBufferBuilder;
import com.corosus.watut.cloudRendering.threading.vanillaThreaded.ThreadedVertexBuffer;

public class RenderableCloud {

    private ThreadedVertexBuffer vertexBuffer;
    private ThreadedBufferBuilder.RenderedBuffer renderedBuffer;

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
}
