
package com.freeme.elementscenter.data;

import java.io.IOException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;

public class ECOnlinePlayer implements OnCompletionListener, OnPreparedListener {
    private MediaPlayer mPlayer;
    private View mRoot;
    private OnlinePlayerStatusListener mListener;
    private String mUrl = "";

    public interface OnlinePlayerStatusListener {
        public void onPlayPrepared(View root);

        public void onPlayCompletion(View root);
    }

    public ECOnlinePlayer() {
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlayerStatusListener(View root, OnlinePlayerStatusListener listener) {
        mRoot = root;
        mListener = listener;
    }

    public void playByUrl(String url) {
        try {
            mUrl = url;
            mPlayer.reset();
            mPlayer.setDataSource(url);
            mPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void reset(){
        mPlayer.reset();
    }

    public void pause() {
        mPlayer.pause();
    }

    public boolean isPlaying(String url) {
        boolean isPlaying = false;
        if (mUrl.equals(url) && mPlayer.isPlaying()) {
            isPlaying = true;
        }
        return isPlaying;
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
        if (mListener != null) {
            mListener.onPlayPrepared(mRoot);
        }
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (mListener != null) {
            mListener.onPlayCompletion(mRoot);
        }
    }
}
