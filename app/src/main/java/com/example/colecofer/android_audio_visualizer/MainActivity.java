package com.example.colecofer.android_audio_visualizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import static com.loopj.android.http.AsyncHttpClient.log;

public class MainActivity extends AppCompatActivity implements Player.NotificationCallback, ConnectionStateCallback {

    private final String TAG = MainActivity.class.getSimpleName();

    //TODO: This is Spotify's test account because I don't want to hard code ours into a public repository...
    private static final String CLIENT_ID = "089d841ccc194c10a77afad9e1c11d54";
    private static final String REDIRECT_URI = "testschema://callback";
    private static final String TRACK_BASE_URI = "spotify:track:";
    private static final String HYPNOTIZE_TRACK_URI = "spotify:track:7KwZNVEaqikRSBSpyhXK2j";

    //Used to verify that we've been redirected back from Spotify after authenticating in browser
    private static final int REQUEST_CODE = 1337;

    //Permission scopes for authentication
    private static final String[] SCOPES = new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"};

    private SpotifyPlayer player;
    private PlaybackState currentPlaybackState;
    private BroadcastReceiver networkStateReceiver;
    private Metadata metadata;
    private String authToken;
    private String webApiAuthToken;
    private SpotifyClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        redirectToBrowserForLogin();
        client = new SpotifyClient();
        client.getAuthToken(new SpotifyRequestCallBack() {
            @Override
            public void spotifyResponse(boolean success, String response) {
                log("Response: " + response);
                webApiAuthToken = response;

            }
        });

    }


    /**
     * Redirects the user to the Spotify login page in their local browser
     */
    private void redirectToBrowserForLogin() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(SCOPES)
                .build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }


    /**
     * Initalizes the user interface including listeners
     */
    public void initUI() {

        //Play button
        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Make sure the user has successfully logged into the player before starting the song
                if (isLoggedIn()) {
                    EditText trackEditText = findViewById(R.id.trackEditText);
                    String trackURI = TRACK_BASE_URI + trackEditText.getText().toString();
                    VisualizerModel.getInstance().setTrackURI(trackURI);

                    //Prepare player and transition to visualizer activity
                    VisualizerModel.getInstance().setPlayer(player);
                    Intent visualizerActivityIntent = new Intent(MainActivity.this, VisualizerActivity.class);
                    startActivity(visualizerActivityIntent);
                } else {
                    log("Error: User was not successfully logged into Spotify.");
                }
            }
        });


        //Search Image Button
        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPlaybackState = player.getPlaybackState();

                EditText trackEditText = findViewById(R.id.trackEditText);
                final String trackString = trackEditText.getText().toString();

                //TODO: Check here if the trackID is valid or not. (I think by simply getting a 200 on response should suffice)

                // Get track info
                client.getTrackInfo(trackString, webApiAuthToken, new SpotifyRequestCallBack() {
                    @Override
                    public void spotifyResponse(boolean success, String response) {
                        log("Track info:");
                        log(response);
                    }
                });

//                //Get the Artist Name
//                client.getArtistName(trackString, authToken, new SpotifyRequestCallBack() {
//                    @Override
//                    public void spotifyResponse(boolean success, String response) {
//                        log("Get Artist Name status: " + success);
//                        if (success == true) {
//                            String artistName = SpotifyClient.parseFieldFromJSON(response, "name");
//                            log("Parsed Artist Name: " + artistName);
//                            TextView artistNameText = findViewById(R.id.artistNameTextView);
//                            artistNameText.setText("Artist: " + artistName);
//                        }
//                    }
//                });
//
//                //Get the Album Name
//                client.getAlbumName(trackString, authToken, new SpotifyRequestCallBack() {
//                    @Override
//                    public void spotifyResponse(boolean success, String response) {
//                        log("Get Album Name status: " + success);
//                        log(response);
//                        if (success == true) {
//                            String albumName = SpotifyClient.parseFieldFromJSON(response, "name");
//                            log("Parsed Album Name: " + albumName);
//                            TextView albumText = findViewById(R.id.albumNameTextView);
//                            albumText.setText("Album: " + albumName);
//                        }
//                    }
//                });

            }
        });

    }


    private void setCoverArt() {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;
                // Auth flow returned an error
                case ERROR:
                    log("Auth error: " + response.getError());
                    break;
                // Most likely auth flow was cancelled by the user
                default:
                    log("Auth result: " + response.getType());
            }
        }
    }

    /**
     * Called when authentication was successful, and then initializes the SpotifyPlayer
     *
     * @param authResponse The response from a successful authentication
     */
    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        log("Successfully obtained the authentication token");
        authToken = "Bearer " + authResponse.getAccessToken();
        if (player == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);
            player = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {

                @Override
                public void onInitialized(SpotifyPlayer player) {
                    log("Player has been initialized");
                    player.setConnectivityStatus(operationCallback, getNetworkConnectivity(MainActivity.this));
                    player.addNotificationCallback(MainActivity.this);
                    player.addConnectionStateCallback(MainActivity.this);

                    //TODO: Do we need to update the view here?
                }

                @Override
                public void onError(Throwable error) {
                    log("Error in initialization: " + error.getMessage());
                }
            });
        } else {
            player.login(authResponse.getAccessToken());
        }
    }


    /**
     * Check if there is a user logged into the player
     *
     * @return True if there is someone logged in.
     */
    public boolean isLoggedIn() {
        return player != null && player.isLoggedIn();
    }


    /**
     * Registering for connectivity changes in Android does not actually deliver them to
     * us in the delivered intent.
     *
     * @param context Android context
     * @return Connectivity state to be passed to the SDK
     */
    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }


    //Callback functions for playback events
    public static final Player.OperationCallback operationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            log.d("MAIN_ACTIVITY", "Callback: Success!");
        }

        @Override
        public void onError(Error error) {
            log.d("MAIN_ACTIVITY", "Callback ERROR:" + error);
        }
    };


    @Override
    public void onLoggedIn() {
        log("Login complete");
    }

    @Override
    public void onLoggedOut() {
        log("Logout complete");
    }

    @Override
    public void onLoginFailed(Error error) {
        log("On login failed: " + error);
    }

    @Override
    public void onTemporaryError() {
        log("Temporary error occured.");
    }

    @Override
    public void onConnectionMessage(String s) {
        log("On connection message: " + s);
    }

    @Override
    public void onPlaybackError(Error error) {
        log("onPlaybackError: " + error);
    }


    /**
     * This method is invoked whenever a playback event occurs (e.g. play / pause)
     *
     * @param playerEvent The event that occured
     */
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        currentPlaybackState = player.getPlaybackState();
        metadata = player.getMetadata();
        //updateView();
    }

    /**
     * Removes the need to specify the TAG each time you log.
     *
     * @param message The message to log
     */
    public void log(String message) {
        Log.d(TAG, message);
    }

}
