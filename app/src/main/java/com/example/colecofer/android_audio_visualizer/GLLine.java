package com.example.colecofer.android_audio_visualizer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLLine {

    private FloatBuffer lineVerticesBuffer;
    private float[] vertices;
    private float xOffset;

    static final int BYTES_PER_FLOAT = 4;


    public GLLine(float xPosition) {
        this.xOffset = xPosition;

        this.vertices = new float[] {
                // X, Y, Z
                // R, G, B, A

                -1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                -0.5f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
        };

        lineVerticesBuffer = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        lineVerticesBuffer.put(vertices).position(0);
    }


    /**
     * Modifies the passed in fft which is just an amplification value
     * of the current line's x-axis.
     * Converts the array into a FloatBuffer for efficiency (can be used
     * in the GPU)
     * @param fft
     */
    public void updateFft(float[] fft) {
        int size = fft.length;
        for(int x = 0; x < size; x += 7) {
            fft[x] += xOffset;
        }

        //Puts the fft array into a FloatBuffer (drawable state for the GPU)
        FloatBuffer fftInput = ByteBuffer.allocateDirect(fft.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fftInput.put(fft).position(0);
        lineVerticesBuffer = fftInput;
    }


    /**
     * This method should be invoked when Visualizer One catches the pulsating flag.
     * Do a plus and minus (?) to get the range of the highlight.
     * Change the float array in the y-axis range by the colorCode.
     * Amplify the reaction by the pulseAmp variable to give it a larger amplification.
     * @param colorCode
     * @param yAxis
     */
    private void highlightPulse(float[] colorCode, float yAxis) {

    }


    /**
     * Draw the line given a set of vertices
     */
    public FloatBuffer draw() {
        return this.lineVerticesBuffer;
    }

}