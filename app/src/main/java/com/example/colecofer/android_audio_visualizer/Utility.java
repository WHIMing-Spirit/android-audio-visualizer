package com.example.colecofer.android_audio_visualizer;

public class Utility {

    /** Takes the real and imaginary parts of an FFT frequency bin and returns the decibels for that bin. */
    static double getDBs(byte real, byte imaginary, int n) {
        /** Bit shifting to translate our bytes into doubles */
        double y = (real | imaginary << 8) / 32768.0;

        /** Finds the Root Mean Square and transforms it into dBs, ensures it does not pass
         *  the Log10 a 0.0 which would return Inf */
        if (y != 0.0) {
            return Math.abs(10.0 * Math.log10((4.0 * y * y) / (n * n)));
        } else {
            return 0.0;
        }
    }
}