package com.example.colecofer.android_audio_visualizer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_DB_LEVEL;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_MATRIX;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_TIME;
import static com.example.colecofer.android_audio_visualizer.Constants.LEFT_DRAW_BOUNDARY;
import static com.example.colecofer.android_audio_visualizer.Constants.LINE_AMT_V3;
import static com.example.colecofer.android_audio_visualizer.Constants.RIGHT_DRAW_BOUNDARY;
import static com.example.colecofer.android_audio_visualizer.VisualizerActivity.decibelHistory;


/**
 * Class VisThree
 * This class extends VisualizerBase and overrides
 * updateVertices() and draw() methods so that openGL can
 * render it's contents.
 * */
public class VisThree extends VisualizerBase {

    private GLLineV3[] lines;  //Holds the lines to be displayed
    private float lineOffSet = (RIGHT_DRAW_BOUNDARY * 2 + 0.06f) / (LINE_AMT_V3 - 1); //We want to display lines from -.99 to .99 (.99+.99=1.98)
    private Utility util;

    private final float[] matrix = new float[16];   // the matrix for calculating transformation

    private long visThreeStartTime;

    private int[] shouldDrawFractalOnLine = new int[LINE_AMT_V3];
    protected int shouldDrawFractalHandle;

    /**
     * Constructor
     */
    public VisThree(Context context) {
        this.visNum = 3;
        this.lines = new GLLineV3[LINE_AMT_V3];

        float k = LEFT_DRAW_BOUNDARY;

        for(int i = 0; i < LINE_AMT_V3; ++i) {
            lines[i] = new GLLineV3(k);
            k += lineOffSet;
        }

        // for shader
        util = new Utility(context);

        this.vertexShader = util.getStringFromGLSL(R.raw.visthreevertex);
        this.fragmentShader = util.getStringFromGLSL(R.raw.visthreefragment);

        this.visThreeStartTime = System.currentTimeMillis();
    }

    /**
     * Initialization of handles during onSurfaceCreated in VisualizerRenderer
     */
    public void initOnSurfaceCreated(int positionHandle, int colorHandle, int programHandle) {
        this.positionHandle = positionHandle;
        this.colorHandle = colorHandle;
        this.currentDecibelLevelHandle = GLES20.glGetUniformLocation(programHandle, GLSL_DB_LEVEL);
        this.timeHandle = GLES20.glGetUniformLocation(programHandle, GLSL_TIME);
        this.matrixHandle = GLES20.glGetUniformLocation(programHandle, GLSL_MATRIX);
        this.shouldDrawFractalHandle = GLES20.glGetUniformLocation(programHandle, "should_Morph_To_Fractal");
    }

    @Override
    public void updateVertices() {
        for(int i = 0; i < LINE_AMT_V3; i++){
            lines[i].updateVertices();
        }
        updateFractalLineArray();
    }

    /**
     * This function updates the array that determines whether each line should be morphed to the fractal field
     * This runs once per draw cycle
     */
    public void updateFractalLineArray(){
        if(decibelHistory.peek() > .7f){
            shouldDrawFractalOnLine[LINE_AMT_V3 - 1] = 3;
        } else {
            if (shouldDrawFractalOnLine[LINE_AMT_V3 - 1] != 0)
                shouldDrawFractalOnLine[LINE_AMT_V3 - 1] -= 1;
        }

        for(int i = 0; i < LINE_AMT_V3 - 1; i++){
            if(shouldDrawFractalOnLine[i+1] == 0 && shouldDrawFractalOnLine[i] != 0) {
                shouldDrawFractalOnLine[i] -= 1;
            } else {
                shouldDrawFractalOnLine[i] = shouldDrawFractalOnLine[i + 1];
            }
        }
    }

    /**
     * Calls line's draw call
     */
    @Override
    public void draw(float[] mvpMatrix) {

        // ---------- bottom left -----------
        Matrix.setIdentityM(matrix,0);                                   // clean matrix buffer
        Matrix.scaleM(matrix, 0, 0.5f, 0.5f, 1.0f);             // scale vertex
        Matrix.translateM(matrix,0,-0.96f,-1.0f,0);            // move vertex to location

        Matrix.multiplyMM(mvpMatrix, 0, matrix, 0, matrix, 0);  // apply final effect

        GLES20.glUniformMatrix4fv(this.matrixHandle, 1, false, matrix, 0);

        //Go through each line and draw them
        for(int i = 0; i < LINE_AMT_V3; ++i) {
            lines[i].draw(this.positionHandle, this.colorHandle, this.timeHandle, this.visThreeStartTime, this.shouldDrawFractalHandle, this.shouldDrawFractalOnLine[i]);
        }

        // ---------- bottom right -----------
        Matrix.setIdentityM(matrix,0);                                   // clean matrix buffer
        Matrix.scaleM(matrix, 0, -1.0f, 1.0f, 1.0f);            // flip vertex
        Matrix.scaleM(matrix, 0, 0.5f, 0.5f, 1.0f);             // scale vertex
        Matrix.translateM(matrix,0,-0.96f,-1.0f,0);            // move vertex to location

        Matrix.multiplyMM(mvpMatrix, 0, matrix, 0, matrix, 0);  // apply final effect

        GLES20.glUniformMatrix4fv(this.matrixHandle, 1, false, matrix, 0);

        //Go through each line and draw them
        for(int i = 0; i < LINE_AMT_V3; ++i) {
            lines[i].draw(this.positionHandle, this.colorHandle, this.timeHandle, this.visThreeStartTime, this.shouldDrawFractalHandle, this.shouldDrawFractalOnLine[i]);
        }

        // ---------- top left -----------
        Matrix.setIdentityM(matrix,0);                                   // clean matrix buffer
        Matrix.scaleM(matrix, 0, 1.0f, -1.0f, 1.0f);            // flip vertex
        Matrix.scaleM(matrix, 0, 0.5f, 0.5f, 1.0f);             // scale vertex
        Matrix.translateM(matrix,0,-0.96f,-1.0f,0);             // move vertex to location

        Matrix.multiplyMM(mvpMatrix, 0, matrix, 0, matrix, 0);  // apply final effect

        GLES20.glUniformMatrix4fv(this.matrixHandle, 1, false, matrix, 0);

        //Go through each line and draw them
        for(int i = 0; i < LINE_AMT_V3; ++i) {
            lines[i].draw(this.positionHandle, this.colorHandle, this.timeHandle, this.visThreeStartTime, this.shouldDrawFractalHandle, this.shouldDrawFractalOnLine[i]);
        }

        // ---------- top right -----------
        Matrix.setIdentityM(matrix,0);                                   // clean matrix buffer
        Matrix.scaleM(matrix, 0, -1.0f, -1.0f, 1.0f);           // flip vertex
        Matrix.scaleM(matrix, 0, 0.5f, 0.5f, 1.0f);             // scale vertex
        Matrix.translateM(matrix,0,-0.96f,-1.0f,0);            // move vertex to location

        Matrix.multiplyMM(mvpMatrix, 0, matrix, 0, matrix, 0);  // apply final effect

        GLES20.glUniformMatrix4fv(this.matrixHandle, 1, false, matrix, 0);

        //Go through each line and draw them
        for(int i = 0; i < LINE_AMT_V3; ++i) {
            lines[i].draw(this.positionHandle, this.colorHandle, this.timeHandle, this.visThreeStartTime, this.shouldDrawFractalHandle, this.shouldDrawFractalOnLine[i]);
        }
    }
}
