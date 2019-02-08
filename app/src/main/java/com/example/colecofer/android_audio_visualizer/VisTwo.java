package com.example.colecofer.android_audio_visualizer;

import android.opengl.GLES20;
import java.nio.FloatBuffer;
import static com.example.colecofer.android_audio_visualizer.Constants.COLOR_DATA_SIZE;
import static com.example.colecofer.android_audio_visualizer.Constants.COLOR_OFFSET;
import static com.example.colecofer.android_audio_visualizer.Constants.POSITION_DATA_SIZE;
import static com.example.colecofer.android_audio_visualizer.Constants.POSITION_OFFSET;
import static com.example.colecofer.android_audio_visualizer.Constants.VIS2_STRIDE_BYTES;
import static com.example.colecofer.android_audio_visualizer.VisualizerActivity.decibelHistory;

public class VisTwo extends VisualizerBase {

    private GLDot dot;

    public VisTwo() {
        // create a layer with 600 * 600 dots
        dot = new GLDot(60, 60);

        this.vertexShader =
                "uniform mat4 u_MVPMatrix;" +		        // A constant representing the combined model/view/projection matrix.
                "attribute vec4 a_Position;\n" + 	        // Per-vertex position information we will pass in.
                "attribute vec4 a_Color;\n" +		        // Per-vertex color information we will pass in.
                "uniform float a_DB_Level;\n" +             // The current decibel level to be used by the shader.
                "varying vec4 v_Color;\n" +                 // This will be passed into the fragment shader.
                "void main()\n" +           		        // The entry point for our vertex shader.
                "{\n" +
                "   v_Color = a_Color;\n" +	    	        // Pass the color through to the fragment shader.
                "   gl_Position = a_Position;\n" + 	        // gl_Position is a special variable used to store the final position.
                "   gl_PointSize = 30.0 * a_DB_Level;\n" +  // Will vary the pixel size from 0.25px-1.25px (eventually)
                "}\n";

        this.fragmentShader =
                "precision mediump float;\n"	+	// Set the default precision to medium. We don't need as high of a
                "varying vec4 v_Color;\n" +         // This is the color from the vertex shader interpolated across the
                "void main()\n"	+	                // The entry point for our fragment shader.
                "{\n" +
                "   gl_FragColor = v_Color;\n"	+	// Pass the color directly through the pipeline.
                "}\n";
    }


    @Override
    public void updateVertices() {

    }

    @Override
    public void updateVertices(float[] newVertices) {

    }

    // TODO We may want to consider moving the "drawDot" logic into this function, it seems to be serving no real purpose
    @Override
    public void draw() {
        drawDot(dot.draw(), dot.count());
    }

    private void drawDot(FloatBuffer dotVertexData, int count) {
        /** Updates the position of individual dots for our screen rendering in the OpenGL pipeline */
        dotVertexData.position(POSITION_OFFSET);
        GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VIS2_STRIDE_BYTES, dotVertexData);
        GLES20.glEnableVertexAttribArray(positionHandle);

        /** Updates the color information for the dots rendered to the screen in the OpenGL pipeline */
        dotVertexData.position(COLOR_OFFSET);
        GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, VIS2_STRIDE_BYTES, dotVertexData);
        GLES20.glEnableVertexAttribArray(colorHandle);

        /** Updates the size of the dots using the most current decibel level, i.e. the first element of the decibel history */
        GLES20.glUniform1f(currentDecibelLevelHandle, decibelHistory.peekFirst());

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, count);
    }
}
