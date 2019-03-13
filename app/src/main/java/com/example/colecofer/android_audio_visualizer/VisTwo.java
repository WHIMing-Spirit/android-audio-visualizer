package com.example.colecofer.android_audio_visualizer;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

import static com.example.colecofer.android_audio_visualizer.Constants.CIRCLE_COUNT;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_CIRCLE_FRACTAL_STRENGTH;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_DB_LEVEL;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_MATRIX;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_SCREEN_RATIO;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_TIME;
import static com.example.colecofer.android_audio_visualizer.VisualizerActivity.decibelHistory;


public class VisTwo extends VisualizerBase {

    private GLDot dot;
    private Utility util;
    private long visTwoStartTime;
    private int circleFractalStrength[] = new int[CIRCLE_COUNT];
    private int circleFractalStrengthHandle;

    /**
     *
     * @param context
     */
     VisTwo(Context context) {
        this.visNum = 2;
        this.util = new Utility(context);
        this.dot = new GLDot();

        this.vertexShader = util.getStringFromGLSL(R.raw.vistwovertex);
        this.fragmentShader = util.getStringFromGLSL(R.raw.vistwofragment);

        visTwoStartTime = System.currentTimeMillis();

    }

    /**
     * Initialization of handles during onSurfaceCreated in VisualizerRenderer
     */
    void initOnSurfaceCreated(int positionHandle, int colorHandle, int programHandle) {
        this.positionHandle = positionHandle;
        this.colorHandle = colorHandle;
        this.currentDecibelLevelHandle = GLES20.glGetUniformLocation(programHandle, GLSL_DB_LEVEL);
        this.timeHandle = GLES20.glGetUniformLocation(programHandle, GLSL_TIME);
        this.matrixHandle = GLES20.glGetUniformLocation(programHandle, GLSL_MATRIX);
        this.screenRatioHandle = GLES20.glGetUniformLocation(programHandle, GLSL_SCREEN_RATIO);
        this.circleFractalStrengthHandle = GLES20.glGetUniformLocation(programHandle, GLSL_CIRCLE_FRACTAL_STRENGTH);
    }

    // This function updates an array that maintains the fractal strength values for circle groups.
    // Circle groups are calculated in shader land based on distance from the midpoint
    // 0 means no effect, higher numbers are more pronounced
    void updateCircleFractalStrength() {
        Float[] decibelHistoryArray = decibelHistory.toArray(new Float[0]);
        double averageDecibel = (decibelHistoryArray[0] + decibelHistoryArray[1] + decibelHistoryArray[2] + decibelHistoryArray[3] + decibelHistoryArray[4]) / 5.0;
        if(averageDecibel > 2.){
            circleFractalStrength[0] = 4;
        } else {
            if (circleFractalStrength[0] != 0)
                circleFractalStrength[0] -= 1;
        }

        for(int i = CIRCLE_COUNT - 1; i >= 1; i--){
            if(circleFractalStrength[i-1] == 0 && circleFractalStrength[i] != 0) {
                circleFractalStrength[i] -= 1;
            } else {
                circleFractalStrength[i] = circleFractalStrength[i - 1];
            }
        }
    }


    @Override
    public void updateVertices() {

    }

    @Override
    public void draw(float[] mvpMatrix) {
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        updateCircleFractalStrength();

        // Setting the screen ratio of device to keep v2 circle shape
        GLES20.glUniform1f(screenRatioHandle, (float)height/width);

        // Pass along the circleFractalStrength information
        GLES20.glUniform1iv(circleFractalStrengthHandle, circleFractalStrength.length, circleFractalStrength, 0);

        this.dot.draw(this.positionHandle, this.colorHandle, this.timeHandle, this.currentDecibelLevelHandle, this.visTwoStartTime);
    }
}
