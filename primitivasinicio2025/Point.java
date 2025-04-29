package com.progavanzada.primitivasinicio2025;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

//clase de la primitiva
// Opengl trabaja con identificadores (ids)
public class Point {
    private final FloatBuffer vertexBuffer; //buffer de memoria que guarda coordenadas de vertices
    private final int mProgram; //combinar vertex shader con fragment shader
    private int positionHandle; //pasarle los datos de posición del vértice al shader.
    private int colorHandle; //almacena id del color
    static final int COORDS_POR_VERTEX = 3; //El número de coordenadas por vértice (x, y, z).
    static float pointCoord[] = {0.0f,0.0f,0.0f}; //coordenadas del punto (entre -1 y 1)
    private final int vertexCount = pointCoord.length/COORDS_POR_VERTEX; //cuenta vertices que vamos a dibujar (el punto tiene un vertice)
    private final int vertexStride = COORDS_POR_VERTEX*4; //determinar el tamaño de memoria de cada vertice || el programa sabe que cada 12 bytes tenemos un vertice
    // siempre debe dar 12
    float color[] = {1.0f,0.0f,0.0f,1.0f}; //color del punto || se puede hacer de otra forma

    public Point(){
        // siempre necesarias
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(pointCoord.length*4);
        byteBuffer.order(ByteOrder.nativeOrder()); //sirve para establecer el orden de bytes (endianness) del buffer
        vertexBuffer = byteBuffer.asFloatBuffer(); //convierte a floatBuffer para trabajar con floats en lugar de bytes
        vertexBuffer.put(pointCoord); //Copia los valores del arreglo pointCoord al FloatBuffer
        vertexBuffer.position(0); //para que empiece desde la posicion 0 del arreglo

        int vertexShader = loadShaders(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShaders(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram,vertexShader); //agrega el vertex a la inicicializacion del programa
        GLES20.glAttachShader(mProgram,fragmentShader);
        GLES20.glLinkProgram(mProgram); //para compilar programa
    }

    public void draw() {
        GLES20.glUseProgram(mProgram);

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition"); // identificador de la posicion
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttribPointer(
                positionHandle, COORDS_POR_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(
                colorHandle, 1, //numero de vectores que se va a enviar
                color, 0); //donde se empiezan a leer los valores (indice arreglo)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle); //es como limpiar
    }

    private final String vertexShaderCode =
            "attribute vec4 vPosition;"+ //es como una variable vec4 es como un tipo de dato y vPosition siempre debe llamarse asi
            "void main(){"+
            "gl_Position = vPosition;"+
            "gl_PointSize = 20.0;"+ //tamaño punto, pixeles
            "}"; //ATTRIBUTES

    private final String fragmentShaderCode=
            "precision mediump float;"+
            "uniform vec4 vColor;"+
            "void main(){"+
            "gl_FragColor =vColor; "+
            "}"; //UNIFORMS

    private int loadShaders(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

}