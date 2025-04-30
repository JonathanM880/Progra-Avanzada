package com.progavanzada.primitivasinicio2025;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Line {
    private final FloatBuffer vertexBuffer; //buffer de memoria que guarda coordenadas de vertices
    private final int mProgram; //combinar vertex shader con fragment shader (para ejecutar)
    private int positionHandle; //pasarle los datos de posición del vértice al shader.
    private int colorHandle; //almacena id del color
    private final float[] coordLine = { //solo dos coordenadas
            -0.9f, 0.9f,
            0.9f,-0.9f};
    float[] color = {1.0f,0.0f,0.0f,1.0f};

    public Line(float[] coordLine) {
        // siempre necesarias
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(coordLine.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder()); //sirve para establecer el orden de bytes (endianness) del buffer
        vertexBuffer = byteBuffer.asFloatBuffer(); //convierte a floatBuffer para trabajar con floats en lugar de bytes
        vertexBuffer.put(coordLine); //Copia los valores del arreglo pointCoord al FloatBuffer
        vertexBuffer.position(0); //para que empiece desde la posicion 0 del arreglo

        int vertexShader = loadShaders(GLES20.GL_VERTEX_SHADER, vertexShaderCode); //generar id vertex
        int fragmentShader = loadShaders(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode); //generar id fragment

        mProgram = GLES20.glCreateProgram(); //crea el programa
        GLES20.glAttachShader(mProgram,vertexShader); //agrega el vertex a la inicicializacion del programa
        GLES20.glAttachShader(mProgram,fragmentShader);
        GLES20.glLinkProgram(mProgram); //para compilar programa

    }

    public void draw(){
        GLES20.glUseProgram(mProgram);

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttribPointer(
                positionHandle, 2, //dimensiones, tamaño vertices
                GLES20.GL_FLOAT, false,
                0, vertexBuffer); //puedo ponerle 0 al stride si no lo declaro arriba

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(
                colorHandle, 1, //numero de vectores que se va a enviar
                color, 0); //donde se empiezan a leer los valores (indice arreglo)
                      //Que vamos a dibujar||desde donde||vertices
        GLES20.glLineWidth(10.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }


    private final String vertexShaderCode =
            "attribute vec4 vPosition;"+ //es como una variable vec4 es como un tipo de dato y vPosition siempre debe llamarse asi
                    "void main(){"+
                    "gl_Position = vPosition;"+
                    "}"; //ATTRIBUTES

    private final String fragmentShaderCode=
            "precision mediump float;"+
                    "uniform vec4 vColor;"+
                    "void main(){"+
                    "gl_FragColor =vColor; "+
                    "}"; //UNIFORMS

    private int loadShaders(int type, String shaderCode){ //para cargar los shaders
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
