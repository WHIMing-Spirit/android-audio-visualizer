package com.example.colecofer.android_audio_visualizer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;


public class VisualizerSurfaceView extends GLSurfaceView {

    private static float amp = 0.000005f;
    private VisualizerRenderer renderer;
    private static float density;
    private static int captureSize;


    public VisualizerSurfaceView(Context context) {
        super(context);
    }

    public VisualizerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRenderer(VisualizerRenderer inputRenderer, float inputDensity, int captureSize) {
        this.density = inputDensity;
        this.renderer = inputRenderer;
        this.captureSize = captureSize;

        VisualizerModel.getInstance().renderer = inputRenderer;
        super.setRenderer(inputRenderer);
    }

    public void updateFft(byte[] fft) {
        int arraySize = captureSize/2;
        float[] fftRender = new float[arraySize*7];

        int j = 0;
        float plus = (float)1/(arraySize/16);
        float k = -1.0f;

        for(int i = 0; i < captureSize-1; i++){
            int amplify = (fft[i]*fft[i]) + (fft[i+1]*fft[i+1]);

            fftRender[j] = (float)amplify*amp;
            fftRender[j+1] = k;
            fftRender[j+2] = 0.0f;
            fftRender[j+3] = 1.0f;
            fftRender[j+4] = 0.0f;
            fftRender[j+5] = 0.0f;
            fftRender[j+6] = 1.0f;

            k+=plus;
            i++;
            j+=7;
        }

        renderer.newFftData(fftRender);
//        VisualizerModel.getInstance().currentVisualizer.updateFft(fft);
    }

}