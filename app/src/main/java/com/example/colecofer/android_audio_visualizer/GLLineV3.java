package com.example.colecofer.android_audio_visualizer;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.example.colecofer.android_audio_visualizer.Constants.AMPLIFIER;
import static com.example.colecofer.android_audio_visualizer.Constants.BYTES_PER_FLOAT;
import static com.example.colecofer.android_audio_visualizer.Constants.COLOR_DATA_SIZE;
import static com.example.colecofer.android_audio_visualizer.Constants.COLOR_OFFSET;
import static com.example.colecofer.android_audio_visualizer.Constants.LEFT_DRAW_BOUNDARY;
import static com.example.colecofer.android_audio_visualizer.Constants.PIXEL;
import static com.example.colecofer.android_audio_visualizer.Constants.POSITION_DATA_SIZE;
import static com.example.colecofer.android_audio_visualizer.Constants.POSITION_OFFSET;
import static com.example.colecofer.android_audio_visualizer.Constants.RIGHT_DRAW_BOUNDARY;
import static com.example.colecofer.android_audio_visualizer.Constants.SCREEN_VERTICAL_HEIGHT;
import static com.example.colecofer.android_audio_visualizer.Constants.SCREEN_VERTICAL_HEIGHT_V3;
import static com.example.colecofer.android_audio_visualizer.Constants.VERTEX_AMOUNT;
import static com.example.colecofer.android_audio_visualizer.Constants.VIS1_ARRAY_SIZE;
import static com.example.colecofer.android_audio_visualizer.Constants.VIS1_STRIDE_BYTES;
import static com.example.colecofer.android_audio_visualizer.Constants.VIS1_VERTEX_COUNT;
import static com.example.colecofer.android_audio_visualizer.Constants.VIS3_ARRAY_SIZE;
import static com.example.colecofer.android_audio_visualizer.Constants.VIS3_VERTEX_COUNT;
import static com.example.colecofer.android_audio_visualizer.VisualizerActivity.decibelHistory;

public class GLLineV3 {

    private FloatBuffer lineVerticesBuffer;
    private float[] vertices;
    private float leftSide;
    private float rightSide;

    /**
     * Constructor
     * @param xPosition: Current line's base position
     */
    public GLLineV3(float xPosition) {

        this.leftSide = xPosition;   // Current line's left side coord
        this.rightSide = leftSide + 0.005f;  // Current line's right side coord


        // Initialize the current line's base vertices
        createBaseLine();

        // Set up the FloatBuffer to draw before the onDataCapture kicks in
        lineVerticesBuffer = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        lineVerticesBuffer.put(vertices).position(0);
    }

    /**
     * Creating the base line vertices
     */
    public void createBaseLine(){

        this.vertices = new float[VIS3_ARRAY_SIZE];

        int vertexIndex = 0;
        float xAxis = -1.0f;
        float xOffset = (float) 2 / (SCREEN_VERTICAL_HEIGHT_V3 -1);

        for(int i = 0; i < VIS3_ARRAY_SIZE; i+=14){
            // Left side
            this.vertices[vertexIndex] = xAxis;
            this.vertices[vertexIndex+1] = this.leftSide;
            this.vertices[vertexIndex+2] = 0.0f;
            this.vertices[vertexIndex+3] = 0.9f;
            this.vertices[vertexIndex+4] = 0.1f;
            this.vertices[vertexIndex+5] = 0.0f;
            this.vertices[vertexIndex+6] = 1.0f;

            // Right side
            this.vertices[vertexIndex+7] = xAxis;
            this.vertices[vertexIndex+8] = this.rightSide;
            this.vertices[vertexIndex+9] = 0.0f;
            this.vertices[vertexIndex+10] = 0.9f;
            this.vertices[vertexIndex+11] = 0.1f;
            this.vertices[vertexIndex+12] = 0.0f;
            this.vertices[vertexIndex+13] = 1.0f;

            // Next y coord
            xAxis += xOffset;
            vertexIndex+= (VERTEX_AMOUNT*2);
        }
    }

    /**
     * Update the base line with decibel value
     */
    public void updateVertices() {
        // Change to object array to traverse
        Object[] decibelArray = decibelHistory.toArray();

        int leftOffset = 0;
        int rightOffset = (SCREEN_VERTICAL_HEIGHT_V3-1) * 14;

        // Only loop for the size of the decibel array size
        for(int i = 0; i < SCREEN_VERTICAL_HEIGHT_V3/2; i++){
            // Calculate the coordinates after the amplification
            // Left side needs to move in negative direction
            // Right side needs to move in positive direction
            // Amplification should be half for both sides because Amplification = left + right

            // V3 version
            float ampDataLeft = ((this.leftSide - (AMPLIFIER * PIXEL * (float) decibelArray[i])));
            float ampDataRight = ((this.rightSide + (AMPLIFIER * PIXEL * (float) decibelArray[i])*2));

//            this.vertices[leftOffset+1] = ampDataLeft;
//            this.vertices[rightOffset+1] = ampDataLeft;

            this.vertices[leftOffset+8] = ampDataRight;
            this.vertices[rightOffset+8] = ampDataRight;

            leftOffset += 14;
            rightOffset -= 14;
        }

        FloatBuffer fftInput = ByteBuffer.allocateDirect(this.vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fftInput.put(this.vertices).position(0);
        this.lineVerticesBuffer = fftInput;
    }

    /**
     * Returns a floatbuffer of values to be drawn. (with timeHandle)
     */
    public void draw(int positionHandle, int colorHandle, int timeHandle, long startTime) {
        /** Position Handle */
        this.lineVerticesBuffer.position(POSITION_OFFSET);
        GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, VIS1_STRIDE_BYTES, this.lineVerticesBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        /** Color Handle */
        this.lineVerticesBuffer.position(COLOR_OFFSET);
        GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, VIS1_STRIDE_BYTES, this.lineVerticesBuffer);
        GLES20.glEnableVertexAttribArray(colorHandle);

        /** Time Handle */
        GLES20.glUniform1f(timeHandle, (float)(System.currentTimeMillis() - startTime));

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VIS3_VERTEX_COUNT);


    }
}