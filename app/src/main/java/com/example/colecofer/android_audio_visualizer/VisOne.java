package com.example.colecofer.android_audio_visualizer;

import android.content.Context;
import android.opengl.GLES20;

import static com.example.colecofer.android_audio_visualizer.Constants.AMPLIFIER_V1;
import static com.example.colecofer.android_audio_visualizer.Constants.AMPLIFIER_V1_SKINNY;
import static com.example.colecofer.android_audio_visualizer.Constants.DEFAULT_LINE_SIZE_V1;
import static com.example.colecofer.android_audio_visualizer.Constants.DEFAULT_LINE_SIZE_V1_SKINNY;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_DB_LEVEL;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_TIME;
import static com.example.colecofer.android_audio_visualizer.Constants.LEFT_DRAW_BOUNDARY;
import static com.example.colecofer.android_audio_visualizer.Constants.LINE_AMT;
import static com.example.colecofer.android_audio_visualizer.Constants.LINE_AMT_SKINNY;
import static com.example.colecofer.android_audio_visualizer.Constants.RIGHT_DRAW_BOUNDARY;

/**
 * Class VisOne
 * This class extends VisualizerBase and overrides
 * updateVertices() and draw() methods so that openGL can
 * render it's contents.
 * */
public class VisOne extends VisualizerBase {

    private GLLine[] lines;  //Holds the lines to be displayed
    private float lineOffSet;
    private Utility util;
    private Long visOneStartTime;
    private int numberOfLines;

    /**
     * Constructor
     */
    public VisOne(Context context) {
        float defaultLineSize;
        float lineAmplifier;

        // Check the screen size and use correct lone constants accordingly, the if covers square and portrait orientation
        if(this.screenHeight == this.screenWidth || this.screenWidth - this.screenHeight < 100) {
            this.numberOfLines   = LINE_AMT_SKINNY;
            this.lineOffSet = (RIGHT_DRAW_BOUNDARY * 2) / (numberOfLines - 1); //We want to display lines from -.99 to .99 (.99+.99=1.98)
            defaultLineSize = DEFAULT_LINE_SIZE_V1_SKINNY;
            lineAmplifier   = AMPLIFIER_V1_SKINNY;
        } else {
            this.numberOfLines   = LINE_AMT;
            this.lineOffSet = (RIGHT_DRAW_BOUNDARY * 2) / (numberOfLines - 1); //We want to display lines from -.99 to .99 (.99+.99=1.98)
            defaultLineSize = DEFAULT_LINE_SIZE_V1;
            lineAmplifier   = AMPLIFIER_V1;
        }

        this.visNum = 1;
        this.lines = new GLLine[numberOfLines];

        float leftBoundary = LEFT_DRAW_BOUNDARY;

        for(int i = 0; i < numberOfLines; ++i) {
            this.lines[i] = new GLLine(leftBoundary, defaultLineSize, lineAmplifier);
            leftBoundary += this.lineOffSet;
        }

        // for shader
        util = new Utility(context);

        this.vertexShader = util.getStringFromGLSL(R.raw.visonevertex);
        this.fragmentShader = util.getStringFromGLSL(R.raw.visonefragment);

        this.visOneStartTime = System.currentTimeMillis();
    }

    /**
     * Initialization of handles during onSurfaceCreated in VisualizerRenderer
     */
    public void initOnSurfaceCreated(int positionHandle, int colorHandle, int programHandle) {
        this.positionHandle = positionHandle;
        this.colorHandle = colorHandle;
        this.currentDecibelLevelHandle = GLES20.glGetUniformLocation(programHandle, GLSL_DB_LEVEL);
        this.timeHandle = GLES20.glGetUniformLocation(programHandle, GLSL_TIME);
    }

    @Override
    public void updateVertices() {
        // The state of the lines should only be changed if it is the first line in question, the rest of the lines should just mirror
        // this state
        lines[0].updateVertices(true);
        for(int i = 1; i < this.numberOfLines; ++i){
            lines[i].updateVertices(false);
        }
    }

    /**
     * Calls line's draw call
     */
    @Override
    public void draw(float[] mvpMatrix) {
        //Go through each line and draw them
        for(int i = 0; i < this.numberOfLines; ++i) {
            lines[i].draw(this.positionHandle, this.colorHandle, this.visOneStartTime);
        }
    }
}
