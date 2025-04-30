package com.progavanzada.primitivasinicio2025;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

//se encarga de hacer el dibujo
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Point point;
    private Line line;
    private Line line1;
    private Line line2;

    private final float[] coordLine = { //solo dos coordenadas
            -0.5f, 0.5f,
            0.5f, 0.5f};
    private final float[] coordLine2 = { //solo dos coordenadas
            -0.3f, 0.3f,
            0.2f,-0.2f};
    private final float[] coordLine3 = { //solo dos coordenadas
            -0.4f, 0.3f,
            0.2f,-0.1f};
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);
        point = new Point();
        line = new Line(coordLine);
        line1 = new Line(coordLine2);
        line2 = new Line(coordLine3);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20. glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        point.draw();
        line.draw();
        line1.draw();
        line2.draw();
    }
}
