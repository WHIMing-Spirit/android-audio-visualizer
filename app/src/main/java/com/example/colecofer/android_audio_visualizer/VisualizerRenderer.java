package com.example.colecofer.android_audio_visualizer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_COLOR_HANDLE;
import static com.example.colecofer.android_audio_visualizer.Constants.GLSL_POSITION_HANDLE;
import static com.example.colecofer.android_audio_visualizer.Constants.SHOULD_SWITCH_VIS;

public class VisualizerRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /** Locals to catch the index for glsl variables */
        int positionHandle;
        int colorHandle;

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        /* Vertex Shader Error Handling */
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0)
        {
            GLES20.glShaderSource(vertexShaderHandle, VisualizerModel.getInstance().currentVisualizer.getVertexShaderString());
            GLES20.glCompileShader(vertexShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                throw new RuntimeException("Could not compile vertex shader program...");
            }
        }

        /* Fragment Shader Error Handling */
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0)
        {
            GLES20.glShaderSource(fragmentShaderHandle, VisualizerModel.getInstance().currentVisualizer.getFragmentShaderString());
            GLES20.glCompileShader(fragmentShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                throw new RuntimeException("Could not compile fragment shader program...");
            }
        }

        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the shaders to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind position and color attributes
            GLES20.glBindAttribLocation(programHandle, 0, GLSL_POSITION_HANDLE);
            GLES20.glBindAttribLocation(programHandle, 1, GLSL_COLOR_HANDLE);

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] != GLES20.GL_TRUE)
            {
                GLES20.glDeleteProgram(programHandle);
                throw new RuntimeException("Could not link shader programs together...");
            }
        }

        //Get the position and color attributes
        positionHandle = GLES20.glGetAttribLocation(programHandle, GLSL_POSITION_HANDLE);
        colorHandle = GLES20.glGetAttribLocation(programHandle, GLSL_COLOR_HANDLE);

        //Initialize and handles to each specific visualizers
        VisualizerModel.getInstance().visOne.initOnSurfaceCreated(positionHandle, colorHandle);
        VisualizerModel.getInstance().visTwo.initOnSurfaceCreated(positionHandle, colorHandle, programHandle);
        VisualizerModel.getInstance().visThree.initOnSurfaceCreated(positionHandle, colorHandle);

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
    }

    //Was newFftData
//    public void updateVertices(float[] newVertices) {
//        VisualizerModel.getInstance().currentVisualizer.updateVertices(newVertices);
//
//    }

    @Override
    public void onDrawFrame(GL10 gl) {

        if (SHOULD_SWITCH_VIS == true) {
            VisualizerModel.getInstance().checkToSwitchVisualizer();
        }

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        VisualizerModel.getInstance().currentVisualizer.draw();
    }
}