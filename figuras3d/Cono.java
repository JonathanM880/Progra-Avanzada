package com.progavanzada.figuras3d;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.progavanzada.figuras3d.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Cono {
    private static final int COORDS_PER_VERTEX = 3;
    private FloatBuffer baseBuffer, sideBuffer;
    private int baseVertexCount, sideVertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private float[] baseColor = {0.0f, 0.4f, 0.0f, 1f}; // Verde oscuro para la base
    private float[] color = {0.2f, 0.8f, 0.2f, 1f};     // Verde claro para los lados   // rojo para los lados

    private float[] modelMatrix = new float[16];
    private float[] currentBaseColor = baseColor.clone();
    private float[] currentSideColor = color.clone();

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final int mProgram;
    // private float[] color = {0.8f, 0.3f, 0.1f, 1.0f};

    public Cono(float radius, float height, int nSegments) {
        // --- BASE ---
        float[] baseVertices = new float[(nSegments + 2) * 3];
        // Centro de la base
        baseVertices[0] = 0f;
        baseVertices[1] = 0f;
        baseVertices[2] = 0f;
        // Vértices del círculo
        for (int i = 0; i <= nSegments; i++) {
            double angle = 2 * Math.PI * i / nSegments;
            baseVertices[(i + 1) * 3] = (float) (radius * Math.cos(angle));
            baseVertices[(i + 1) * 3 + 1] = 0f;
            baseVertices[(i + 1) * 3 + 2] = (float) (radius * Math.sin(angle));
        }
        baseVertexCount = nSegments + 2;
        ByteBuffer bb = ByteBuffer.allocateDirect(baseVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        baseBuffer = bb.asFloatBuffer();
        baseBuffer.put(baseVertices);
        baseBuffer.position(0);

        // --- LATERALES ---
        float[] sideVertices = new float[nSegments * 3 * 3]; // nSegments triángulos, 3 vértices cada uno
        float[] tip = {0f, height, 0f};
        for (int i = 0; i < nSegments; i++) {
            // Punta
            sideVertices[i * 9] = tip[0];
            sideVertices[i * 9 + 1] = tip[1];
            sideVertices[i * 9 + 2] = tip[2];
            // Vértice actual de la base
            sideVertices[i * 9 + 3] = baseVertices[(i + 1) * 3];
            sideVertices[i * 9 + 4] = baseVertices[(i + 1) * 3 + 1];
            sideVertices[i * 9 + 5] = baseVertices[(i + 1) * 3 + 2];
            // Vértice siguiente de la base
            sideVertices[i * 9 + 6] = baseVertices[(i + 2) * 3];
            sideVertices[i * 9 + 7] = baseVertices[(i + 2) * 3 + 1];
            sideVertices[i * 9 + 8] = baseVertices[(i + 2) * 3 + 2];
        }
        sideVertexCount = nSegments * 3;
        ByteBuffer sb = ByteBuffer.allocateDirect(sideVertices.length * 4);
        sb.order(ByteOrder.nativeOrder());
        sideBuffer = sb.asFloatBuffer();
        sideBuffer.put(sideVertices);
        sideBuffer.position(0);

        // Shaders
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        Matrix.setIdentityM(modelMatrix, 0); // Por defecto, sin transformación
    }

    public void move(float dx, float dy, float dz) {
        Matrix.translateM(modelMatrix, 0, dx, dy, dz);
    }

    public void setColorPart(String part, float[] rgba) {
        if (rgba == null || rgba.length != 4) return;

        switch (part.toLowerCase()) {
            case "base":
                currentBaseColor = rgba.clone();
                break;
            case "side":
            case "lateral":
                currentSideColor = rgba.clone();
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

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] finalMVP = new float[16];
        Matrix.multiplyMM(finalMVP, 0, mvpMatrix, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, finalMVP, 0);
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Dibuja la base con color distinto
        GLES20.glVertexAttribPointer(
                positionHandle,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                baseBuffer
        );

        GLES20.glUniform4fv(colorHandle, 1, currentBaseColor, 0); // Color de la base
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, baseVertexCount);

        // Dibuja los lados
        GLES20.glVertexAttribPointer(
                positionHandle,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                sideBuffer
        );
        GLES20.glUniform4fv(colorHandle, 1, currentSideColor, 0); // Color de los lados
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, sideVertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }


}
