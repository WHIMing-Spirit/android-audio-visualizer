package com.example.colecofer.android_audio_visualizer;

import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.util.Log;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class VisualizerModel {

    private final String MODEL_TAG = "MODEL_TAG";

    //Spotify data
    private SpotifyPlayer player;
    private PlaybackState currentPlaybackState;
    private BroadcastReceiver networkStateReceiver;
    private String trackURI;
    String trackName;
    String artistName;
    String albumName;
    public int colorMatrix[];

    //Visualizer / OpenGL instances
    private static final VisualizerModel visualizerModel = new VisualizerModel(); //VisualizerModel Singleton
    public static VisualizerRenderer renderer;         //TODO: Consider making these private
    public static VisualizerBase currentVisualizer;

    /**
     * Default Constructor
     */
    public VisualizerModel() {
        trackURI = "Not defined";
        trackName = "Not defined";
        artistName = "Not defined";
        albumName = "Not defined";
        colorMatrix = new int[] {0, 0, 0, 0};
    }


    /**
     * Allows access to the VisualizerModel Singleton outside of class scope.
     * @return The VisualizerModel singleton
     */
    public static VisualizerModel getInstance() { return visualizerModel; }

    public static VisualizerRenderer getRenderer() { return renderer; }

    public void setPlayer(SpotifyPlayer newPlayer) {
        this.player = newPlayer;
    }

    public SpotifyPlayer getPlayer() {
        return this.player;
    }

    public String getTrackURI() {
        return this.trackURI;
    }

    public void setTrackURI(String trackURI) {
        this.trackURI = trackURI;
    }

    public void setColors(int[] colors) {

        for(int i = 0; i < 3; ++i) {

            this.colorMatrix[i] = colors[i];
        }

        this.colorMatrix[3] = 1;

    }

    /**
     * Removes the need to specify the TAG each time you log.
     * @param message The message to log
     */
    public void log(String message) { Log.d(MODEL_TAG, message);}

    public static void initRenderer(VisualizerRenderer inputRenderer) {
        renderer = inputRenderer;
    }

}
