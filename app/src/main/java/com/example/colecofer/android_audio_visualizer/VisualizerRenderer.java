package com.example.colecofer.android_audio_visualizer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VisualizerRenderer implements GLSurfaceView.Renderer {

    static int AUDIO_COUNT;
    static int VERTEX_COUNT = 5;

    private int positionHandle;
    private int colorHandle;

    private float[] lineVertices;
    private FloatBuffer lineVertexBuffer;
    private GLLine[] lines;                               //Holds the lines to be displayed
    private final int LINE_AMT = 20;                      //Number of lines to display on the screen
    private float lineOffSet = 1.98f/(LINE_AMT -1);       //We want to display lines from -.99 to .99 (.99+.99=1.98)


    static final int POSITION_DATA_SIZE = 3;
    static final int BYTES_PER_FLOAT = 4;
    static final int STRIDE_BYTES = 7 * BYTES_PER_FLOAT;
    static final int POSITION_OFFSET = 0;
    static final int COLOR_OFFSET = 3;
    static final int COLOR_DATA_SIZE = 4;


    public VisualizerRenderer(int captureSize){
        this.AUDIO_COUNT = captureSize;
        this.VERTEX_COUNT = this.AUDIO_COUNT / 7;     //It's 7 because we have x, y, z, r, g, b, a

        //These are the default lines that are displayed before any fft values have been updated
        this.lineVertices = new float[]{
                // X, Y, Z
                // R, G, B, A

                -1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                -0.5f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

        };

        lineVertexBuffer = ByteBuffer.allocateDirect(lineVertices.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        lineVertexBuffer.put(lineVertices).position(0);

        //Create 100 lines
        lines = new GLLine[LINE_AMT];
        float k = -0.99f;
        for(int i = 0; i < LINE_AMT; ++i) {
            lines[i] = new GLLine(k);
            k += lineOffSet;
        }

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        final String vertexShader =
                "uniform mat4 u_MVPMatrix;" +		// A constant representing the combined model/view/projection matrix.
                "attribute vec4 a_Position;\n" + 	// Per-vertex position information we will pass in.
                "attribute vec4 a_Color;\n" +		// Per-vertex color information we will pass in.
                "varying vec4 v_Color;\n" +		    // This will be passed into the fragment shader.
                "void main()\n" +           		// The entry point for our vertex shader.
                "{\n" +
                "   v_Color = a_Color;\n" +	    	// Pass the color through to the fragment shader.
                "   gl_Position = a_Position;\n" + 	// gl_Position is a special variable used to store the final position.
                "}\n";                              // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;\n"	+	// Set the default precision to medium. We don't need as high of a
                "varying vec4 v_Color;\n" +         // This is the color from the vertex shader interpolated across the
                "void main()\n"	+	                // The entry point for our fragment shader.
                "{\n" +
                "   gl_FragColor = v_Color;\n"	+	// Pass the color directly through the pipeline.
                "}\n";


        /* Vertex Shader Error Handling */
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0)
        {
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);
            GLES20.glCompileShader(vertexShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0)
        {
            throw new RuntimeException("Error creating vertex shader.");
        }


        /* Fragment Shader Error Handling */
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0)
        {
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
            GLES20.glCompileShader(fragmentShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0)
        {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the shaders to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind position and color attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        //Get the position and color attributes
        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        colorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void newFftData(float[] fft){
        for(int i = 0; i < LINE_AMT; ++i) {
            float[] fftInput = new float[fft.length];
            System.arraycopy(fft, 0, fftInput, 0, fft.length);
            lines[i].updateFft(fftInput);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        //Go through each line and draw them
        for(int i = 0; i < LINE_AMT; ++i) {
            drawLine(lines[i].draw());
        }
    }

    public void drawLine(FloatBuffer lineVertexData){
        lineVertexData.position(POSITION_OFFSET);
        GLES20.glVertexAttribPointer(positionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE_BYTES, lineVertexData);
        GLES20.glEnableVertexAttribArray(positionHandle);

        lineVertexData.position(COLOR_OFFSET);
        GLES20.glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE_BYTES, lineVertexData);
        GLES20.glEnableVertexAttribArray(colorHandle);

        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, VERTEX_COUNT);
    }
}