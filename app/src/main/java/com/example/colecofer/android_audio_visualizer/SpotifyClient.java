package com.example.colecofer.android_audio_visualizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SpotifyClient {

    private final String BASE_URL = "https://api.spotify.com/v1";
    private final String FEATURES_URL = "/audio-features/";
    private final String SEARCH_URL = "/search/";
    private final String TRACK_URL = "/tracks/";
    private final String AUTH_URL = "https://accounts.spotify.com/api/token";

    //TODO: Change these to our personal information (this is a public repo...)
    private final String CLIENT_ID = "5f0eac9db12042cfa8b9fb95b0f3f4d8";
    private final String CLIENT_SECRET = "4f0d128f8f1b4776a530292cdef1dd45";

    private final static String SPOTIFY_TAG = "SPOTIFY";


    /**
     * Gets the authorization token given the clientid and clientsecret
     *
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
     *
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
     * Parse the artist name from the JSON response
     *
     * @param responseJSON JSON response from the track info endpoint
     * @return string representing the artist name
     */
    public static String getArtistName(String responseJSON) {
        JSONObject json = convertStringToJSON(responseJSON);
        String value = "Error: Couldn't find field";
        try {
            value = json.getJSONArray("artists").getJSONObject(0).getString("name");
        } catch (JSONException e) {
            Log.d("Spotify", "Error - Could not extract artist name from response" + e.getMessage());
        }
        return value;
    }


    /**
     * Parse the album name from the JSON response
     *
     * @param responseJSON JSON response from the track info endpoint
     * @return string representing the album name
     */
    public static String getAlbumName(String responseJSON) {
        JSONObject json = convertStringToJSON(responseJSON);
        String value = "Error: Couldn't find field";
        try {
            value = json.getString("name");
        } catch (JSONException e) {
            Log.d("Spotify", "Error - Could not extract album name from response" + e.getMessage());
        }
        return value;
    }


    public static String getArtUrl(String responseJSON) {
        JSONObject json = convertStringToJSON(responseJSON);
        String value = "Error: Couldn't find field";
        JSONArray imageArray;
        boolean found = false;
        try {
            int index = 0;
            int height;
            int width;

            while(found == false) {
                imageArray = json.getJSONObject("album").getJSONArray("images");
                height = Integer.parseInt(imageArray.getJSONObject(index).getString("height"));
                width = Integer.parseInt(imageArray.getJSONObject(index).getString("width"));

                // Ensure that image is no bigger than 640 x 640
                if (height <= 640 && width <= 640) {
                    found = true;
                    value = imageArray.getJSONObject(index).getString("url");
                }
                index++;
            }
            return value;

        } catch (JSONException e) {
            Log.d("Spotify", "Error - Could not extract album art url from response" + e.getMessage());
        }
        return value;
    }


    public static void getAlbumArt(String url, final BitmapRequestCallBack bitmapCallback) {
        AsyncHttpClient client = new AsyncHttpClient();
        Bitmap myBitmap;
        client.get(url, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                int offset = 0;
                bitmapCallback.bitmapResponse(true, BitmapFactory.decodeByteArray(binaryData, offset, binaryData.length));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                // TODO update to a default value
                bitmapCallback.bitmapResponse(true, null);

            }
        });
    }


    /**
     * Calls the Spotify features API end-point for the passed in trackID.
     * TODO: Remove hard coded auth token
     *
     * @param trackID  The ID for any given Spotify song
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


    public static float[][] getAlbumArtColors(Bitmap albumArt) {
        Palette AlbumPallet = createPaletteSync(albumArt);
        AlbumPallet.getSwatches();
        Palette.Swatch primarySwatch = AlbumPallet.getDominantSwatch();
        Palette.Swatch secondarySwatch = AlbumPallet.getVibrantSwatch();
        Palette.Swatch tertiarySwatch = AlbumPallet.getMutedSwatch();

        float colors[][] = new float[][]{{0,0,0},{0,0,0},{0,0,0}};

        for(int i = 0; i < 3; ++i) {
            if(primarySwatch != null)
                colors[0][i] = primarySwatch.getHsl()[i];
            else
                colors[0][i] = 0;
            if(secondarySwatch != null)
                colors[1][i] = secondarySwatch.getHsl()[i];
            else
                colors[1][i] = 0;
            if(tertiarySwatch != null)
                colors[2][i] = tertiarySwatch.getHsl()[i];
            else
                colors[2][i] = 0;

        }

        return colors;
    }
    public static Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }


    /**
     * Calls the Spotify search API end-point and returns the JSON response of search
     * queries through the callback function.
     * TODO: Remove hard coded auth token
     *
     * @param query      Name of the song
     * @param searchType A list of types to search for (e.g. "album, artist, playlist, track")
     * @param callback   function that gets invoked after success or failure
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
     *
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
     *
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

}
