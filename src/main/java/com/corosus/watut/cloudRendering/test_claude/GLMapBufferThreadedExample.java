package com.corosus.watut.cloudRendering.test_claude;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.*;
import java.util.concurrent.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GLMapBufferThreadedExample {

    private long window;
    private long updateContext;
    private int vao;
    private int vbo;
    private int shaderProgram;
    private volatile boolean running = true;
    private ExecutorService executor;

    public void run() {
        init();
        loop();

        running = false;
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwDestroyWindow(updateContext);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(800, 600, "Threaded glMapBuffer Example", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // Create a shared context for the update thread
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        updateContext = glfwCreateWindow(1, 1, "", NULL, window);
        if (updateContext == NULL)
            throw new RuntimeException("Failed to create the update context");

        glfwSwapInterval(1);
        glfwShowWindow(window);

        createShaderProgram();
        createVertexBuffer();

        executor = Executors.newSingleThreadExecutor();
        executor.submit(this::updateBufferLoop);
    }

    private void createShaderProgram() {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader,
                "#version 330 core\n" +
                        "layout (location = 0) in vec2 aPos;\n" +
                        "void main() {\n" +
                        "    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);\n" +
                        "}");
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader,
                "#version 330 core\n" +
                        "out vec4 FragColor;\n" +
                        "void main() {\n" +
                        "    FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" +
                        "}");
        glCompileShader(fragmentShader);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private void createVertexBuffer() {
        float[] vertices = {
                -0.5f, -0.5f,
                0.5f, -0.5f,
                0.0f,  0.5f
        };

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_STATIC_DRAW);

        ByteBuffer mappedBuffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
        if (mappedBuffer != null) {
            mappedBuffer.asFloatBuffer().put(vertices);
            glUnmapBuffer(GL_ARRAY_BUFFER);
        }

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }

    private void updateBufferLoop() {
        glfwMakeContextCurrent(updateContext);
        GL.createCapabilities();

        while (running) {
            updateBuffer();
            try {
                Thread.sleep(16); // Roughly 60 fps
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        glfwMakeContextCurrent(NULL);
    }

    private void updateBuffer() {
        float time = (float) glfwGetTime();
        /*float[] vertices = {
            (float) Math.sin(time) * 0.5f, -0.5f,
            (float) Math.cos(time) * 0.5f, -0.5f,
            0.0f,  0.5f
        };*/

        float[] vertices = {
                -0.1f, -0.5f,
                0.5f, -0.5f,
                0.0f,  0.5f
        };

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        ByteBuffer mappedBuffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, vertices.length * Float.BYTES, 
                                                   GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        if (mappedBuffer != null) {
            mappedBuffer.asFloatBuffer().put(vertices);
            glUnmapBuffer(GL_ARRAY_BUFFER);
        }
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            glUseProgram(shaderProgram);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 3);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new GLMapBufferThreadedExample().run();
    }
}