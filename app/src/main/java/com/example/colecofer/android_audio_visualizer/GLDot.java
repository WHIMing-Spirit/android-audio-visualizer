package com.example.colecofer.android_audio_visualizer;

import android.graphics.Color;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import static com.example.colecofer.android_audio_visualizer.Constants.DOT_COUNT;
import static com.example.colecofer.android_audio_visualizer.Constants.DOT_HEIGHT;
import static com.example.colecofer.android_audio_visualizer.Constants.DOT_WIDTH;

public class GLDot {

    private FloatBuffer dotVerticesBuffer;
    private Random random = new Random();
    private OpenSimplexNoise noise = new OpenSimplexNoise(random.nextLong());

    public GLDot() {

        float[] vertices = new float[DOT_COUNT * 7];

        int index = 0;

        int visTwoIndex = 1;
        int visColor = VisualizerModel.getInstance().getColor(visTwoIndex);

        for(int i = 0; i < DOT_HEIGHT; i++) {

            for(int j = 0; j < DOT_WIDTH; j++) {
                vertices[index*7+0] = (float)(-1.0 + 2.0 /(DOT_HEIGHT + 1)*(1+i));
                vertices[index*7+1] = (float)(-1.0 + 2.0 /(DOT_WIDTH + 1)*(1+j));
                vertices[index*7+2] = 0.0f;
                vertices[index*7+3] = (float) (Color.red(visColor) * 0.01);
                vertices[index*7+4] = (float) (Color.green(visColor) * 0.01);
                vertices[index*7+5] = (float) (Color.blue(visColor) * 0.01);
                vertices[index*7+6] = 1.0f;

                index++;
            }
        }

        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        dotVerticesBuffer = vertexByteBuffer.asFloatBuffer();
        dotVerticesBuffer.put(vertices).position(0);
    }

    FloatBuffer draw() {

//        float[] vertices = new float[DOT_COUNT * 7];
//
//        int index = 0;
//
//        for(int y = 0; y < DOT_HEIGHT; y++) {
//            for(int x = 0; x < DOT_WIDTH; x++) {
//                double nx = (double)x/(double)DOT_WIDTH - 0.5, ny = (double)y/(double)DOT_HEIGHT - 0.5;
//
//                vertices[index*7+0] = (float)(-1.0 + 2.0 /(DOT_HEIGHT + 1)*(1+y));
//                vertices[index*7+1] = (float)(-1.0 + 2.0 /(DOT_WIDTH + 1)*(1+x));
//                vertices[index*7+2] = 0.0f;
//                vertices[index*7+3] = 1.0f;
//                vertices[index*7+4] = 1.0f;
//                vertices[index*7+5] = 0.0f;
//                vertices[index*7+6] = (float) Math.abs(noise.eval(nx, ny));
////                vertices[index*7+6] = 0.5f;
//
//
//                index++;
//            }
//        }
//
//        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
//        vertexByteBuffer.order(ByteOrder.nativeOrder());
//        dotVerticesBuffer = vertexByteBuffer.asFloatBuffer();
//        dotVerticesBuffer.put(vertices).position(0);

        return this.dotVerticesBuffer;
    }
}
