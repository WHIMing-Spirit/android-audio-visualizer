package com.example.colecofer.android_audio_visualizer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import static com.example.colecofer.android_audio_visualizer.Constants.BYTES_PER_FLOAT;
import static com.example.colecofer.android_audio_visualizer.Constants.LINE_AMT;

public class GLLine {

    private FloatBuffer lineVerticesBuffer;
    private float[] vertices;
    private float xOffset;



    public GLLine(float xPosition) {

        this.xOffset = xPosition;

        //These are the default lines that are displayed before any fft values have been updated
        //TODO: This needs to generate X number of lines in the correct locations
        if (VisualizerModel.getInstance().currentVisualizer instanceof VisThree) {
            // vertices for VisOne
            this.vertices = new float[] {
                    // X, Y, Z
                    // R, G, B, A

                    //Bottom point
                    -1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f, 1.0f,

                    //Top point
                    -0.5f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f, 1.0f,
            };
        } else {
            this.vertices = new float[14];

            // first point
            this.vertices[0] = -1.0f;
            this.vertices[1] = xPosition;
            this.vertices[2] = 0.0f;
            this.vertices[3] = 1.0f;
            this.vertices[4] = 0.0f;
            this.vertices[5] = 0.0f;
            this.vertices[6] = 1.0f;

            // second point
            this.vertices[7] = 1.0f;
            this.vertices[8] = xPosition;
            this.vertices[9] = 0.0f;
            this.vertices[10] = 1.0f;
            this.vertices[11] = 0.0f;
            this.vertices[12] = 0.0f;
            this.vertices[13] = 1.0f;
        }

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
     * Returns a floatbuffer of values to be drawn.
     */
    public FloatBuffer draw() {
        return this.lineVerticesBuffer;
    }

}