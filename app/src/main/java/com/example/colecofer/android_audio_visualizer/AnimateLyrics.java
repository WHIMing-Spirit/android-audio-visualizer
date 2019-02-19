package com.example.colecofer.android_audio_visualizer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.colecofer.android_audio_visualizer.Constants.LYRICS_TEXT_SIZE;


/**
 * This class creates lyric animations using Spannable Strings.
 * These are useful because you can treat words individually,
 * where you can't with normal Strings.
 * The animation consists of multiple Spannable Strings kept
 * inside of a TextView.
 */
public class AnimateLyrics {
    static TextView lyricsTextView;
    static ViewGroup.MarginLayoutParams lyricsParams;

    private Typeface lyricsTypeface;
    private ArrayList<Pair<Integer, String[]>> lyricList;
    private int lyricAmt = 0;    //Amount of lyric segments to disaply to the screen
    private Spannable lyrics[];
    private int screenWidth;
    private int screenHeight;
    private int lyricIndex = 0;

    /**
     * Animate Lyrics Constructor
     * @param context Context for reference
     * @param screenWidth Screen width in pixels
     * @param screenHeight Screen height in pixels
     * @param lyricList ArrayList of lyrics with time stamps
     */
    public AnimateLyrics(Context context, int screenWidth, int screenHeight, ArrayList<Pair<Integer, String[]>> lyricList) {

        //Lyric Text Setup
        this.lyricsTypeface = ResourcesCompat.getFont(context, R.font.sofiaprobold);
        this.lyricsTextView = new TextView(context);
        this.lyricsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LYRICS_TEXT_SIZE);
        this.lyricsTextView.setTypeface(lyricsTypeface);
        this.lyricsTextView.setTextColor(Color.WHITE);
        this.lyricsParams = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.MATCH_PARENT);
        this.lyricsTextView.setLayoutParams(lyricsParams);
        this.lyricsTextView.setPadding(100, 800, 100, 300);

        //Lyric Containers
        this.lyricList = (ArrayList<Pair<Integer, String[]>>) lyricList.clone();
        this.lyricAmt = this.lyricList.size();
        this.lyricIndex = 0;

        //Screen dimensions
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * Displays the next set of lyrics according to the timestamps
     * TODO: Possibly take lyrics off screen if they sit around too long (like the end)
     */
    public void update() {

        float lyricDisplayTime = lyricList.get(this.lyricIndex).first - 300; //Displays 300 millis before the lyrics start
        float currTime = VisualizerActivity.mediaPlayer.getCurrentPosition();
        if (currTime >= lyricDisplayTime && this.lyricIndex < this.lyricAmt) {
            String[] lyricsToDisplay = lyricList.get(this.lyricIndex).second;
            this.displayLyrics(lyricsToDisplay);
            this.lyricIndex += 1;
        }

    }

    /**
     * Takes an array of lyrics and displays them to
     * the lyric animation TextView.
     * @param lyrics String array of lyrics (word by word)
     */
    public void displayLyrics(String[] lyrics) {
        int wordsAmt = lyrics.length;
        String lyricsToDisplay = "";
        for (int i = 0; i < wordsAmt; ++i) {
            lyricsToDisplay += lyrics[i] + " ";
        }
        this.lyricsTextView.setText(lyricsToDisplay);
    }

    //        //Setup the text and colors
//        Spannable word = new SpannableString("Hah, sika than your average\n");
//
//        //The first two value of the hex are opacity... So perhaps we could alter these to fade them in and out... ?
//        word.setSpan(new ForegroundColorSpan(0x50FFFFFF), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        lyricsTextView.setText(word);
//
//        Spannable word1 = new SpannableString("Poppa twist cabbage off instinct");
//        word1.setSpan(new ForegroundColorSpan(Color.RED), 0, word1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        lyricsTextView.append(word1);


}
