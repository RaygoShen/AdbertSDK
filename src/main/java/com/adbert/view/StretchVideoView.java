package com.adbert.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.adbert.util.SDKUtil;

/**
 * Created by chihhan on 2017/7/6.
 */

public class StretchVideoView extends VideoView {

    private int mForceHeight = 0;
    private int mForceWidth = 0;

    public StretchVideoView(Context context) {
        super(context);
    }

    public StretchVideoView(Context context, int w, int h) {
        super(context);
        setDimensions(w, h);
    }

    public StretchVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mForceWidth == 0 || mForceHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else
            setMeasuredDimension(mForceWidth, mForceHeight);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    public void setUrl(String url) {
        this.setVideoURI(Uri.parse(url));
        this.requestFocus();
        this.setMediaController(null);
        this.setDrawingCacheEnabled(true);
        this.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                volumeClose();
                StretchVideoView.this.mediaPlayer = mediaPlayer;
                if (listener != null) {
                    listener.onPrepared(mediaPlayer);
                }
            }
        });
        this.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                if (listener != null) {
                    listener.OnError();
                }
                return true;
            }
        });
        this.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (listener != null) {
                    listener.OnCompletion();
                }
//                handler.post(runnable);
                handler.removeCallbacks(runnable);
            }
        });
    }

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (listener != null) {
                listener.onSeekChange();
            }
            handler.postDelayed(this, 1000);
        }
    };

    private VideoListener listener;

    public void setListener(VideoListener listener) {
        this.listener = listener;
    }

    public interface VideoListener {
        void onPrepared(MediaPlayer mp);

        void onSeekChange();

        void OnError();

        void OnCompletion();
    }

    private boolean isReady = false;

    @Override
    public void start() {
        super.start();
        isReady = true;
        handler.removeCallbacks(runnable);
        handler.post(runnable);
    }

    @Override
    public void pause() {
        if (this.isPlaying()) {
            super.pause();
        }
        handler.removeCallbacks(runnable);
    }

    public void destroy() {
        this.pause();
        this.stopPlayback();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    private boolean volume = false;

    public void volumeOpen() {
        volume = true;
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(1, 1);
            }
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
    }

    public void volumeClose() {
        volume = false;
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0, 0);
            }
        } catch (Exception e) {
//            SDKUtil.logException(e);
        }
    }

}
