package com.progavanzada.figuras3d;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.progavanzada.figuras3d.Cilindro;
import com.progavanzada.figuras3d.Cono;
import com.progavanzada.figuras3d.Sphere;
import com.progavanzada.figuras3d.Torus;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mVPMatrix = new float[16];


    // cuerpo
    private Cilindro cuerpoGuisante;
    //pies
    private Sphere piesGuisante1, piesGuisante2, piesGuisante3, piesGuisante4;
    //cabeza
    private  Sphere cabezaGuisante;
    private Cilindro ramita1;
    private Cono ramita2;
    //ojos
    private Sphere ojoIzq, ojoDer, pupilaIzq, pupilaDer;
    //boca
    private Torus bocaGuisante;
    //bala
    private Sphere guisante;




    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //cuerpo
        cuerpoGuisante = new Cilindro(40,0.05f, 0.5f);
        cuerpoGuisante.move(-0.45f,0,0.5f);
        cuerpoGuisante.setColorPart("lateral", new float[]{0.0f,1.0f,0.0f,1.0f});
        cuerpoGuisante.setColorPart("top",new float[]{0.0f,0.9f,0.0f,1.0f});
        //pies
        piesGuisante1 = new Sphere(0.08f, 30, 30);
        piesGuisante1.move(-0.5f,-0.25f,0.55f);
        piesGuisante1.setColor(new float[]{0.0f,0.83f,0.0f,1.0f});

        piesGuisante2 = new Sphere(0.08f, 30, 30);
        piesGuisante2.move(-0.4f,-0.25f,0.55f);
        piesGuisante2.setColor(new float[]{0.0f,0.83f,0.0f,1.0f});

        piesGuisante3 = new Sphere(0.08f, 30, 30);
        piesGuisante3.move(-0.5f,-0.25f,0.4f);
        piesGuisante3.setColor(new float[]{0.0f,0.83f,0.0f,1.0f});

        piesGuisante4 = new Sphere(0.08f, 30, 30);
        piesGuisante4.move(-0.4f,-0.25f,0.4f);
        piesGuisante4.setColor(new float[]{0.0f,0.83f,0.0f,1.0f});
        //cabeza
        cabezaGuisante = new Sphere(0.15f, 30,30);
        cabezaGuisante.move(-0.45f,0.25f,0.5f);
        cabezaGuisante.setColor(new float[]{0.0f,1.0f,0.0f,1.0f});

        ramita1 = new Cilindro(30,0.03f,0.03f);
        ramita1.move(-0.45f,0.3f,0.34f);
        ramita1.setColorPart("lateral", new float[]{0.0f,0.9f,0.0f,1.0f});
        ramita1.setColorPart("top", new float[]{0.0f,0.9f,0.0f,1.0f});
        ramita1.rotate(90,0,0);

        ramita2 = new Cono(0.04f,0.04f,30);
        ramita2.move(-0.45f,0.3f,0.34f);
        ramita2.setColorPart("base", new float[]{0.0f,0.85f,0.0f,1.0f});
        ramita2.setColorPart("lateral", new float[]{0.0f,0.85f,0.0f,1.0f});
        ramita2.rotate(90,0,0);
        //ojos
        ojoIzq = new Sphere(0.05f,30,30);
        ojoIzq.move(-0.51f,0.29f,0.62f);
        ojoIzq.setColor(new float[]{0.0f,0.0f,0.0f,1.0f});

        ojoDer = new Sphere(0.05f,30,30);
        ojoDer.move(-0.39f,0.29f,0.62f);
        ojoDer.setColor(new float[]{0.0f,0.0f,0.0f,1.0f});

        pupilaIzq = new Sphere(0.02f,30,30);
        pupilaIzq.move(-0.5f,0.3f,0.62f);
        pupilaIzq.setColor(new float[]{1.0f,1.0f,1.0f,1.0f});

        pupilaDer = new Sphere(0.02f,30,30);
        pupilaDer.move(-0.4f,0.3f,0.62f);
        pupilaDer.setColor(new float[]{1.0f,1.0f,1.0f,1.0f});
        //boca
        bocaGuisante = new Torus(30,0.08f,0.04f);
        bocaGuisante.setColor(new float[]{0.0f,0.91f,0.0f,1.0f});
        bocaGuisante.move(-0.45f,0.20f,0.65f);
        //bala
        guisante = new Sphere(0.1f,30,30);
        guisante.setColor(new float[]{0.0f,1.0f,0.0f,1.0f});
        guisante.move(-0.45f,0.20f,1.0f);



    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST); // ¡Habilita el test de profundidad!

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(
                mViewMatrix, 0,
                2.0f, 1.0f, 2.5f,   // Cámara al frente del objeto
                0.0f, 0.5f, 0.0f,   // Punto que está mirando (el objeto)
                0.0f, 1.0f, 0.0f    // Vector "up" (hacia arriba)
        );

        Matrix.multiplyMM(mVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //cuerpo
        cuerpoGuisante.draw(mVPMatrix);
        //pies
        piesGuisante1.draw(mVPMatrix);
        piesGuisante2.draw(mVPMatrix);
        piesGuisante3.draw(mVPMatrix);
        piesGuisante4.draw(mVPMatrix);
        //cabeza
        cabezaGuisante.draw(mVPMatrix);
        ramita1.draw(mVPMatrix);
        ramita2.draw(mVPMatrix);
        //ojos
        ojoIzq.draw(mVPMatrix);
        ojoDer.draw(mVPMatrix);
        pupilaIzq.draw(mVPMatrix);
        pupilaDer.draw(mVPMatrix);
        //boca
        bocaGuisante.draw(mVPMatrix);
        //bala
        guisante.draw(mVPMatrix);



    }
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
