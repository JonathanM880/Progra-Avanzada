package com.progavanzada.figuras3d;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.progavanzada.figuras3d.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Sphere {

    // Buffers para vértices e índices de la esfera sólida
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;

    // Buffers para la malla (wireframe)
    private FloatBuffer wireVertexBuffer;
    private ShortBuffer wireIndexBuffer;

    // Conteos
    private int vertexCount;
    private int indexCount;
    private int wireIndexCount;

    private float[] modelMatrix = new float[16];

    private final int vertexStride = 3 * 4; // 3 coordenadas por vértice * 4 bytes por float
    private final int mProgram;             // ID del programa shader compilado
    public boolean showWireframe = false;   // Bandera para dibujar malla

    private float[] color= new float[]{0.5f, 0f, 0.5f, 1f};
    // Constructor: crea buffers y shaders
    public Sphere(float radius, int stacks, int slices) {
        float[] vertices = createSphereVertices(radius, stacks, slices); // vértices de la esfera
        short[] indices = createSphereIndices(stacks, slices);           // índices (triángulos)

        vertexCount = vertices.length / 3;
        indexCount = indices.length;

        vertexBuffer = createFloatBuffer(vertices); // carga vértices a GPU
        indexBuffer = createShortBuffer(indices);   // carga índices a GPU

        // Para wireframe se usa resolución fija (mejor visualización)
        float[] wireVertices = createSphereVertices(radius, 50, 50);
        short[] wireIndices = createWireIndices(50, 50);
        wireVertexBuffer = createFloatBuffer(wireVertices);
        wireIndexBuffer = createShortBuffer(wireIndices);
        wireIndexCount = wireIndices.length;

        //Para poder moverla
        Matrix.setIdentityM(modelMatrix, 0);

        // Compila los shaders y los une en un programa
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    // Crea un buffer flotante desde un array
    private FloatBuffer createFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4); // reserva memoria
        bb.order(ByteOrder.nativeOrder());                          // orden del sistema
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(data);
        buffer.position(0);
        return buffer;
    }

    // Igual para índices (shorts)
    private ShortBuffer createShortBuffer(short[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = bb.asShortBuffer();
        buffer.put(data);
        buffer.position(0);
        return buffer;
    }

    // Genera coordenadas de la esfera en 3D
    private float[] createSphereVertices(float radius, int stacks, int slices) {
        float[] vertices = new float[(stacks + 1) * (slices + 1) * 3];
        int index = 0;

        for (int i = 0; i <= stacks; i++) {
            double lat = Math.PI * i / stacks;    // ángulo de latitud
            double sinLat = Math.sin(lat);
            double cosLat = Math.cos(lat);

            for (int j = 0; j <= slices; j++) {
                double lon = 2 * Math.PI * j / slices; // ángulo de longitud
                double sinLon = Math.sin(lon);
                double cosLon = Math.cos(lon);

                float x = (float) (cosLon * sinLat);
                float y = (float) cosLat;
                float z = (float) (sinLon * sinLat);

                vertices[index++] = radius * x;  // posición en X
                vertices[index++] = radius * y;  // en Y
                vertices[index++] = radius * z;  // en Z
            }
        }
        return vertices;
    }

    // Genera los triángulos de la esfera
    private short[] createSphereIndices(int stacks, int slices) {
        short[] indices = new short[stacks * slices * 6];
        int index = 0;

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = (i * (slices + 1)) + j;
                int second = first + slices + 1;

                indices[index++] = (short) first;
                indices[index++] = (short) second;
                indices[index++] = (short) (first + 1);

                indices[index++] = (short) (first + 1);
                indices[index++] = (short) second;
                indices[index++] = (short) (second + 1);
            }
        }
        return indices;
    }

    // Genera líneas para el wireframe
    private short[] createWireIndices(int stacks, int slices) {
        short[] indices = new short[2 * stacks * (slices + 1) + 2 * slices * (stacks + 1)];
        int index = 0;

        // Líneas horizontales
        for (int i = 0; i <= stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = i * (slices + 1) + j;
                indices[index++] = (short) first;
                indices[index++] = (short) (first + 1);
            }
        }

        // Líneas verticales
        for (int j = 0; j <= slices; j++) {
            for (int i = 0; i < stacks; i++) {
                int first = i * (slices + 1) + j;
                indices[index++] = (short) first;
                indices[index++] = (short) (first + slices + 1);
            }
        }
        return indices;
    }

    // mueve
    public void move(float dx, float dy, float dz) {
        Matrix.translateM(modelMatrix, 0, dx, dy, dz);
    }


    // Método base: envía todo a la GPU
    public void draw(float[] vpMatrix) {
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);

        GLES20.glUseProgram(mProgram);

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        int colorHandle = GLES20.glGetUniformLocation(mProgram, "uColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);

        if (showWireframe) {
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, vertexStride, wireVertexBuffer);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
            GLES20.glUniform4fv(colorHandle, 1, new float[]{1f, 1f, 1f, 1f}, 0);
            GLES20.glLineWidth(2.5f);
            GLES20.glDrawElements(GLES20.GL_LINES, wireIndexCount, GLES20.GL_UNSIGNED_SHORT, wireIndexBuffer);
            GLES20.glDisableVertexAttribArray(positionHandle);
        }
    }


    // Shader de vértices: aplica la transformación MVP
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // Shader de fragmento: pinta con color uniforme
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 uColor;" +
                    "void main() {" +
                    "  gl_FragColor = uColor;" +
                    "}";

    public void setColor(float[] color) {
        this.color = color;
    }
}