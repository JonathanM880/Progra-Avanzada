package com.progavanzada.figuras3d;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class Torus {
    private static final int COORDS_FOR_VERTEX = 3;
    private static final int COORDS_PER_VERTEX = 3;
    private final FloatBuffer vertexBuffer;
    private final ShortBuffer shortBuffer;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int mProgram;

    private int positionHandle;
    private int colorHandle;
    private int uMVPMatrixHandle;
    private int vertexCount;
    private float[] coords;

    private float[] modelMatrix = new float[16];
    private float[] currentColor = new float[]{c(246), c(149), c(80), 1.0f}; // por defecto

    static float c(int v) { return v / 255f; }
    private short[] drawOrder;

    public void createTorus(int nOfSquares, float R, float r) {
        List<Float> torusCoords = new ArrayList<>();
        List<Short> drawList = new ArrayList<>();
        float angle = (float) (2 * Math.PI);
        float angleStep = angle / nOfSquares;

        for (int i = 0; i <= nOfSquares; i++) {
            float φ = angleStep * i;
            for (int j = 0; j <= nOfSquares; j++) {
                float θ = angleStep * j;
                float x = (float) ((R + r * Math.cos(φ)) * Math.cos(θ));
                float y = (float) ((R + r * Math.cos(φ)) * Math.sin(θ));
                float z = (float) (r * Math.sin(φ));
                torusCoords.add(x);
                torusCoords.add(y);
                torusCoords.add(z);
            }
        }




        int verticesPerRow = nOfSquares + 1;
        for (int i = 0; i < nOfSquares; i++) {
            for (int j = 0; j < nOfSquares; j++) {
                short topLeft = (short) (i * verticesPerRow + j);
                short topRight = (short) (topLeft + 1);
                short bottomLeft = (short) ((i + 1) * verticesPerRow + j);
                short bottomRight = (short) (bottomLeft + 1);

                drawList.add(topLeft);
                drawList.add(bottomLeft);
                drawList.add(topRight);

                drawList.add(topRight);
                drawList.add(bottomLeft);
                drawList.add(bottomRight);
            }
        }

        // Guardar los datos en arrays
        float[] coords = new float[torusCoords.size()];
        for (int i = 0; i < torusCoords.size(); i++) {
            coords[i] = torusCoords.get(i);
        }

        drawOrder = new short[drawList.size()];
        for (int i = 0; i < drawList.size(); i++) {
            drawOrder[i] = drawList.get(i);
        }

        this.vertexCount = drawOrder.length;
        this.coords = coords;
    }

    public Torus(int nOfSquares, float R, float r) {
        createTorus(nOfSquares, R, r);

        ByteBuffer vb = ByteBuffer.allocateDirect(coords.length * 4).order(ByteOrder.nativeOrder());
        vertexBuffer = vb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer db = ByteBuffer.allocateDirect(drawOrder.length * 2).order(ByteOrder.nativeOrder());
        shortBuffer = db.asShortBuffer();
        shortBuffer.put(drawOrder);
        shortBuffer.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        Matrix.setIdentityM(modelMatrix, 0); // posicionar en (0, 0, 0)
    }

    public void move(float dx, float dy, float dz) {
        Matrix.translateM(modelMatrix, 0, dx, dy, dz);
    }

    public void setColor(float[] rgba) {
        if (rgba != null && rgba.length == 4) {
            currentColor = rgba.clone();
        }
    }

    public void draw(float[] vpMatrix) {
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);

        GLES20.glUseProgram(mProgram);

        positionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_FOR_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, currentColor, 0);

        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexCount, GLES20.GL_UNSIGNED_SHORT, shortBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public static int loadShader(int type, String glShaderCode)
    {
        int shader= GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, glShaderCode);
        GLES20.glCompileShader(shader);
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Error compilando shader: " + error);
        }

        return shader;

    }
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 aPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * aPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
}