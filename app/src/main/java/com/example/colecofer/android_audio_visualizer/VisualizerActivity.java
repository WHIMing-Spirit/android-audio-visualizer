package com.example.colecofer.android_audio_visualizer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;

import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.ArrayDeque;

import static com.example.colecofer.android_audio_visualizer.Utility.getDBs;
import static com.example.colecofer.android_audio_visualizer.Utility.updateDecibelHistory;
import static com.loopj.android.http.AsyncHttpClient.log;

public class VisualizerActivity extends AppCompatActivity implements Visualizer.OnDataCaptureListener {

    private static final int REQUEST_PERMISSION = 101;
    private static final int REAL_BUCKET_INDEX = 5;
    private static final int IMAGINARY_BUCKET_INDEX = 6;
    private static final int MAX_FFT_ARRAY_SIZE = 1024;
    private final int SCREEN_VERTICAL_HEIGHT = 1024;

    private static int fftArraySize;
    private long previousUpdateTime;
    ArrayDeque<Float> decibelHistory;

    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private VisualizerSurfaceView surfaceView;
    private VisualizerRenderer visualizerRenderer;
    private Visualizer.OnDataCaptureListener captureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizer);
        //startTrackPlayback();  //Uncomment this line to start Spotify track playback
        initDecibelHistory();
        initVisualizer();
    }

    /**
     * Invokes track playback using the SpotifyPlayer in VisualizerModel
     */
    private void startTrackPlayback() {
        SpotifyPlayer player = VisualizerModel.getInstance().getPlayer();
        player.playUri(MainActivity.operationCallback, VisualizerModel.getInstance().getTrackURI(), 0, 0);
        log.d("test", "TrackID: " + VisualizerModel.getInstance().getTrackURI());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                //If Permission is granted then start the initialization
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initVisualizer();
                }
            }
        }
    }

    /**
     * Initializes the visualizer.
     * This function currently uses locally stored files because we are blocked
     * at getting the visualizer to listen to all audio output which is accomplished
     * when passing a 0 to the Visualizer constructor. This is necessary to use Spotify.
     */
    private void initVisualizer() {
        this.fftArraySize = Visualizer.getCaptureSizeRange()[1];

        /** This ends up being the max size we will use, it is actually half of this number that defines
         *  how many buckets we can have. So we have 512 "Frequency buckets" if this is 1024 to account
         *  for the real and imaginary parts of each bucket. There is a frequency range of 0-20000 HZ.
         *  This gives us a frequency granularity of 39.06 Hz, so our target will be the 3rd bucket for
         *  the real component, and 4th bucket for the imaginary component which covers 78.12-117.18 Hz
         */
        if (this.fftArraySize > MAX_FFT_ARRAY_SIZE) {
            this.fftArraySize = MAX_FFT_ARRAY_SIZE;
        }

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        surfaceView = new VisualizerSurfaceView(this);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        //Check if ES2 is supported on the device
        if (supportsEs2) {
            surfaceView.setEGLContextClientVersion(2);
            visualizerRenderer = new VisualizerRenderer();
            surfaceView.setRenderer(visualizerRenderer, displayMetrics.density, fftArraySize);
        } else {
            log.d("opengl", "Does not support ES2");
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }

        //Sets up the visualizer for local files
        mediaPlayer = MediaPlayer.create(this, R.raw.jazz);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        visualizer.setCaptureSize(fftArraySize);
        visualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), true, true);
        visualizer.setEnabled(true);

        this.previousUpdateTime = System.currentTimeMillis();

        setContentView(surfaceView);

    }

    private void initDecibelHistory() {
        this.decibelHistory = new ArrayDeque<>();
        for(int i = 0; i < SCREEN_VERTICAL_HEIGHT; ++i) {
            this.decibelHistory.addFirst(0.0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mediaPlayer.release();
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        /** Gives us the decibel level for the fft bucket we care about **/
        double currentDecibels = getDBs(fft[REAL_BUCKET_INDEX], fft[IMAGINARY_BUCKET_INDEX], this.fftArraySize);

        Pair<Long, Boolean> currentStatus = updateDecibelHistory(currentDecibels, this.decibelHistory, this.previousUpdateTime);

        if(currentStatus.second == true) {
            VisualizerModel.getInstance().currentVisualizer.updateVertices(this.decibelHistory);
        }
    }

}
