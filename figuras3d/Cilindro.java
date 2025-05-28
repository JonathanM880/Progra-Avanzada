package com.progavanzada.figuras3d;


import android.opengl.GLES20;
import android.opengl.Matrix;

import com.progavanzada.figuras3d.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Cilindro {
    private final FloatBuffer lateralVertexBuffer;
    private final FloatBuffer topBaseVertexBuffer;
    private final FloatBuffer bottomBaseVertexBuffer;

    private final int mProgram;
    private int positionHandle, mvpMatrixHandle, colorHandle;

    private final int lateralVertexCount;
    private final int baseVertexCount;

    private final int vertexStride = 3 * 4; // 3 coords * 4 bytes per float

    // Colors
    private final float[] lateralColor = {1.0f, 1.0f, 0.0f, 0.5f}; // Amarillo
    private final float[] topBaseColor = {0.0f, 0.0f, 1.0f, 1.0f}; // Azul
    private final float[] bottomBaseColor = {1.0f, 0.39f, 0.28f, 1.0f}; // Tomate

    private float[] modelMatrix = new float[16];
    private float[] currentTopBaseColor = topBaseColor.clone();
    private float[] currentBottomBaseColor = bottomBaseColor.clone();
    private float[] currentLateralColor = lateralColor.clone();
    private float height;
    private float radius;

    // Vertex Shader
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  gl_PointSize = 10.0;" +
                    "}";

    // Fragment Shader
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    public Cilindro(int slices, float radius, float height) {
        this.height = height;
        this.radius = radius;

        // Generar vértices
        float[] lateralVertices = crearLateralVertices(slices);
        lateralVertexCount = lateralVertices.length / 3;

        float[] topBaseVertices = crearBaseVertices(slices, height / 2);
        float[] bottomBaseVertices = crearBaseVertices(slices, -height / 2);
        baseVertexCount = topBaseVertices.length / 3;

        // Buffers
        lateralVertexBuffer = createFloatBuffer(lateralVertices);
        topBaseVertexBuffer = createFloatBuffer(topBaseVertices);
        bottomBaseVertexBuffer = createFloatBuffer(bottomBaseVertices);

        // Shaders
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        Matrix.setIdentityM(modelMatrix, 0); // posición por defecto en (0, 0, 0)
    }

    private FloatBuffer createFloatBuffer(float[] vertices) {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(vertices);
        fb.position(0);
        return fb;
    }

    private float[] crearLateralVertices(int slices) {
        float[] vertices = new float[slices * 6 * 3]; // 6 vértices por slice
        int index = 0;
        for (int i = 0; i < slices; i++) {
            double angle1 = 2 * Math.PI * i / slices;
            double angle2 = 2 * Math.PI * (i + 1) / slices;

            float x1 = (float) (radius * Math.cos(angle1));
            float z1 = (float) (radius * Math.sin(angle1));
            float x2 = (float) (radius * Math.cos(angle2));
            float z2 = (float) (radius * Math.sin(angle2));

            // Primer triángulo
            //Arriba izquierda: (x1, height/2, z1)
            vertices[index++] = x1; vertices[index++] = height / 2; vertices[index++] = z1;
            //Arriba derecha: (x2, height/2, z2)
            vertices[index++] = x2; vertices[index++] = height / 2; vertices[index++] = z2;
            //Abajo izquierda: (x1, -height/2, z1)
            vertices[index++] = x1; vertices[index++] = -height / 2; vertices[index++] = z1;

            // Segundo triángulo
            //Arriba derecha: (x2, height/2, z2)
            vertices[index++] = x2; vertices[index++] = height / 2; vertices[index++] = z2;
            //Abajo derecha: (x2, -height/2, z2)
            vertices[index++] = x2; vertices[index++] = -height / 2; vertices[index++] = z2;
            //Abajo izquierda: (x1, -height/2, z1)
            vertices[index++] = x1; vertices[index++] = -height / 2; vertices[index++] = z1;
        }
        return vertices;
    }

    private float[] crearBaseVertices(int slices, float y) {
        float[] vertices = new float[(slices + 2) * 3];
        int idx = 0;
        vertices[idx++] = 0f; vertices[idx++] = y; vertices[idx++] = 0f; // centro

        for (int i = 0; i <= slices; i++) {
            double angle = 2 * Math.PI * i / slices;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            vertices[idx++] = x;
            vertices[idx++] = y;
            vertices[idx++] = z;
        }
        return vertices;
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] finalMVP = new float[16];
        Matrix.multiplyMM(finalMVP, 0, mvpMatrix, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, finalMVP, 0);

        GLES20.glEnableVertexAttribArray(positionHandle);


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Base inferior
        bottomBaseVertexBuffer.position(0);
        GLES20.glUniform4fv(colorHandle, 1, currentBottomBaseColor, 0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
                vertexStride, bottomBaseVertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, baseVertexCount);

        // Lateral
        lateralVertexBuffer.position(0);
        GLES20.glUniform4fv(colorHandle, 1, currentLateralColor, 0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
                vertexStride, lateralVertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, lateralVertexCount);

        // Base superior
        topBaseVertexBuffer.position(0);
        GLES20.glUniform4fv(colorHandle, 1, currentTopBaseColor, 0);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
                vertexStride, topBaseVertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, baseVertexCount);

        GLES20.glDisable(GLES20.GL_BLEND); // Deshabilitar blending después de dibujar las caras
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);


        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void move(float dx, float dy, float dz) {
        Matrix.translateM(modelMatrix, 0, dx, dy, dz);
    }

    public void setColorPart(String part, float[] rgba) {
        if (rgba == null || rgba.length != 4) return;

        switch (part.toLowerCase()) {
            case "top":
                currentTopBaseColor = rgba.clone();
                break;
            case "bottom":
                currentBottomBaseColor = rgba.clone();
                break;
            case "lateral":
                currentLateralColor = rgba.clone();
                break;
        }
    }

    public void rotate(float angleX, float angleY, float angleZ) {
        float[] rotationMatrix = new float[16];
        Matrix.setIdentityM(rotationMatrix, 0);

        // Rotar en el eje X
        if (angleX != 0) {
            float[] temp = new float[16];
            Matrix.setRotateM(temp, 0, angleX, 1f, 0f, 0f);
            Matrix.multiplyMM(rotationMatrix, 0, temp, 0, rotationMatrix, 0);
        }

        // Rotar en el eje Y
        if (angleY != 0) {
            float[] temp = new float[16];
            Matrix.setRotateM(temp, 0, angleY, 0f, 1f, 0f);
            Matrix.multiplyMM(rotationMatrix, 0, temp, 0, rotationMatrix, 0);
        }

        // Rotar en el eje Z
        if (angleZ != 0) {
            float[] temp = new float[16];
            Matrix.setRotateM(temp, 0, angleZ, 0f, 0f, 1f);
            Matrix.multiplyMM(rotationMatrix, 0, temp, 0, rotationMatrix, 0);
        }

        // Aplicar la rotación acumulada a la matriz de modelo
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, modelMatrix, 0, rotationMatrix, 0);
        System.arraycopy(result, 0, modelMatrix, 0, 16);
    }

}