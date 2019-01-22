package com.example.colecofer.android_audio_visualizer;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import android.support.v7.graphics.Palette;
import android.graphics.Bitmap;

public class SpotifyClient {

    private final String BASE_URL = "https://api.spotify.com/v1";
    private final String FEATURES_URL = "/audio-features/";
    private final String SEARCH_URL = "/search/";
    private final String PLAYER_URL = "/me/player/";
    private final String TRACK_URL = "/tracks/";
    private final String ARTIST_URL = "/artists/";
    private final String ALBUM_URL = "";
    private final String COVER_ART_URL = "https://i.scdn.co/image/6a79792a433af1786048c1dac022f5ab33f093f0";
    private final String AUTH_URL = "https://accounts.spotify.com/api/token";

    //TODO: Change these to our personal information (this is a public repo...)
    private final String CLIENT_ID = "5f0eac9db12042cfa8b9fb95b0f3f4d8";
    private final String CLIENT_SECRET = "4f0d128f8f1b4776a530292cdef1dd45";

    private final static String SPOTIFY_TAG = "SPOTIFY";


    /**
     * Gets the authorization token given the clientid and clientsecret
     * @param callback
     */
    public void getAuthToken(final SpotifyRequestCallBack callback) {
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams body = new RequestParams();
        body.put("client_id", CLIENT_ID);
        body.put("client_secret", CLIENT_SECRET);
        body.put("grant_type", "client_credentials");

        client.post(AUTH_URL, body, new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                String access_token = parseFieldFromJSON(responseString, "access_token");
                callback.spotifyResponse(true, access_token);
//                callback.spotifyResponse(true, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.spotifyResponse(false, responseString);
            }

        });
    }

    /**
     * Recieves general information about a track given a track ID.
     * Specifically
     * @param trackID
     * @param authToken
     * @param callback
     */
    public void getTrackInfo(String trackID, String authToken, final SpotifyRequestCallBack callback) {
        String fullFeaturesURL = BASE_URL + TRACK_URL + trackID;

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Authorization", "Bearer " + authToken);

        client.get(fullFeaturesURL, new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.spotifyResponse(true, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.spotifyResponse(false, responseString);
            }

        });
    }

    /**
     * Gets the artist name given the Spotify ID
     * @param trackID The Spotify / Track ID
     * @param authToken The authentication token
     * @param callback Callback function to be implemented
     */
    public void getArtistName(String trackID, String authToken, final SpotifyRequestCallBack callback) {
        String fullArtistURL = BASE_URL + ARTIST_URL + trackID;

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Authorization", authToken);

        client.get(fullArtistURL, new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.spotifyResponse(true, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.spotifyResponse(false, responseString);
            }

        });
    }

    /**
     * Gets the album name given a Spotify albumID
     * @param albumID
     * @param authToken
     * @param callback
     */
    public void getAlbumName(String albumID, String authToken, final SpotifyRequestCallBack callback) {
        String fullArtistURL = BASE_URL + ALBUM_URL + albumID;

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Authorization", authToken);

        client.get(fullArtistURL, new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.spotifyResponse(true, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.spotifyResponse(false, responseString);
            }

        });
    }
    public void getAlbumArt(String albumID, String authToken, final SpotifyRequestCallBack callback) {
        String fullArtistURL = BASE_URL + ALBUM_URL + albumID;
        Bitmap AlbumArt = BitmapFactory.decodeFile("/LifeAfterDeath.bmp");
        Palette AlbumPallet = createPaletteSync(AlbumArt);
        int primary = AlbumPallet.getLightVibrantColor(0);
        int secondary = AlbumPallet.getDarkVibrantColor(0);

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Authorization", authToken);

        client.get(fullArtistURL, new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.spotifyResponse(true, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.spotifyResponse(false, responseString);
            }

        });
    }

    /**
     * Calls the Spotify features API end-point for the passed in trackID.
     * TODO: Remove hard coded auth token
     * @param trackID The ID for any given Spotify song
     * @param callback function that gets invoked after success or failure
     */
    public void getFeaturesFromTrackID(String trackID, String authToken, final SpotifyRequestCallBack callback) {
        String fullFeaturesURL = BASE_URL + FEATURES_URL + trackID;

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Authorization", "Bearer " + authToken);

        client.get(fullFeaturesURL, new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.spotifyResponse(true, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.spotifyResponse(false, responseString);
            }


            @Override
            public void onStart() {
                Log.d("HTTP", "Request is starting...");
            }

            @Override
            public void onRetry(int retryNo) {
                Log.d("HTTP", "Request is retrying...");
            }

        });
    }


    /**
     * Calls the Spotify search API end-point and returns the JSON response of search
     * queries through the callback function.
     * TODO: Remove hard coded auth token
     * @param query Name of the song
     * @param searchType A list of types to search for (e.g. "album, artist, playlist, track")
     * @param callback function that gets invoked after success or failure
     */
    public void searchSpotify(String query, String searchType, String authToken, final SpotifyRequestCallBack callback) {
        String fullSearchURL = BASE_URL + SEARCH_URL;

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Authorization", "Bearer " + authToken);

        RequestParams params = new RequestParams();
        params.put("q", query);
        params.put("type", searchType);

        client.get(fullSearchURL, params, new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d("HTTP", "Status Code: " + statusCode);
                callback.spotifyResponse(true, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.spotifyResponse(false, responseString);
            }

            @Override
            public void onStart() {
                Log.d("HTTP", "Request is starting...");
            }

            @Override
            public void onRetry(int retryNo) {
                Log.d("HTTP", "Request is retrying...");
            }
        });
    }


    /**
     * Converts a String in JSON format to a JSONObject
     * @param jsonStr The string in JSON format
     * @return The JSONObject, or null if there was an error
     */
    public static JSONObject convertStringToJSON(String jsonStr) {
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            return jsonObj;
        } catch (JSONException e) {
            Log.d(SPOTIFY_TAG, "Could not convert into JSONObject" + e.getMessage());
            return null;
        }
    }


    /**
     * Parses a json block and returns the passed in field
     * @param responseJSON The json block from the Spotify request
     * @return The desired field
     */
    public static String parseFieldFromJSON(String responseJSON, String field) {
        JSONObject json = convertStringToJSON(responseJSON);
        String value = "Error: Couldn't find field";
        try {
            value = json.getString(field);
        } catch (JSONException e) {
            Log.d("Spotify", "Error - Could not extract field from response" + e.getMessage());
        }
        return value;
    }
    // Generate palette synchronously and return it
    public Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }

    // Generate palette asynchronously and use it on a different
// thread using onGenerated()
//    public void createPaletteAsync(Bitmap bitmap) {
//        Palette.from(bitmap).generate(new PaletteAsyncListener() {
//            public void onGenerated(Palette p) {
//                // Use generated instance
//            }
//        });
//    }


}
