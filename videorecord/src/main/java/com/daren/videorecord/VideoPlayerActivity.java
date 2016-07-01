package com.daren.videorecord;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.StringUtils;

import java.io.File;



/**
 * 通用单独播放界面
 *
 * @author tangjun
 */
public class VideoPlayerActivity extends VRBaseActivity implements SurfaceVideoView.OnPlayStateListener, OnErrorListener, OnPreparedListener, OnClickListener, OnCompletionListener, OnInfoListener, Chronometer.OnChronometerTickListener {

    /**
     * 播放控件
     */
    private SurfaceVideoView mVideoView;
    /**
     * 暂停按钮
     */
    private View mPlayerStatus;
//	private View mLoading;

    /**
     * 播放路径
     */
    private String mPath;
    /**
     * 视频截图路径
     */
    private String mCoverPath;

    /**
     * 是否需要回复播放
     */
    private boolean mNeedResume;

    private LoadingCircleView mLoadingCircleView;
    private Chronometer mChronometer;
    private int mMiss = 30;//默认30秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 防止锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mPath = getIntent().getStringExtra(VConstant.RECORD_VIDEO_PATH);

        mCoverPath = getIntent().getStringExtra(VConstant.RECORD_VIDEO_CAPTURE);
        if (StringUtils.isEmpty(mPath)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_video_player);

        mLoadingCircleView = (LoadingCircleView) findViewById(R.id.loadingView);
        mLoadingCircleView.setProgerss(1, true);
        mVideoView = (SurfaceVideoView) findViewById(R.id.videoview);
        mPlayerStatus = findViewById(R.id.play_status);
//		mLoading = findViewById(R.id.loading);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setOnChronometerTickListener(this);

        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnPlayStateListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnClickListener(this);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnCompletionListener(this);

        mVideoView.getLayoutParams().height = DeviceUtils.getScreenWidth(this);

        findViewById(R.id.root).setOnClickListener(this);
        if (mPath.startsWith("http")) {
            downloadVideo(mPath);
        } else {
            mVideoView.setVideoPath(mPath);
        }
    }

    private void downloadVideo(String remoteUrl) {
        String localPath = VCamera.getVideoCachePath() + VideoDownloadUtils.getFilename(remoteUrl);
        File file = new File(localPath);
        if (file.exists()){
            mLoadingCircleView.setVisibility(View.GONE);
            mVideoView.setVideoPath(localPath);
            return;
        }
        VideoDownloadUtils.downloadVideo(remoteUrl, localPath, new VideoDownloadUtils.DownloadCallback() {
            @Override
            public void onProgress(final int progress) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mLoadingCircleView.setProgerss(progress, true);
                    }
                });
            }

            @Override
            public void onComplete(final String localPath) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mVideoView.setVideoPath(localPath);
//						mNeedResume = true;
//						mVideoView.start();
                    }
                });
            }

            @Override
            public void onError() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mLoadingCircleView.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoView != null && mNeedResume) {
            mNeedResume = false;
            if (mVideoView.isRelease())
                mVideoView.reOpen();
            else
                mVideoView.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                mNeedResume = true;
                mVideoView.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mVideoView != null) {
            mVideoView.release();
            mVideoView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMiss = mVideoView.getDuration() / 1000;
        mChronometer.start();

        mVideoView.setVolume(SurfaceVideoView.getSystemVolumn(this));
        mVideoView.start();
//		new Handler().postDelayed(new Runnable() {
//
//			@SuppressWarnings("deprecation")
//			@Override
//			public void run() {
//				if (DeviceUtils.hasJellyBean()) {
//					mVideoView.setBackground(null);
//				} else {
//					mVideoView.setBackgroundDrawable(null);
//				}
//			}
//		}, 300);
        mLoadingCircleView.setVisibility(View.GONE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {//跟随系统音量走
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                mVideoView.dispatchKeyEvent(this, event);
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        mPlayerStatus.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (!isFinishing()) {
            //播放失败
        }
        finish();
        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            finish();

        } else if (i == R.id.videoview) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
                mChronometer.stop();
            } else {
                mVideoView.start();
                mChronometer.start();
            }

        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!isFinishing())
            mVideoView.reOpen();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                //音频和视频数据不正确
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (!isFinishing())
                    mVideoView.pause();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (!isFinishing())
                    mVideoView.start();
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                if (DeviceUtils.hasJellyBean()) {
                    mVideoView.setBackground(null);
                } else {
                    mVideoView.setBackgroundDrawable(null);
                }
                break;
        }
        return false;
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        mChronometer.setText(formatMiss());
    }


    public String formatMiss() {
//		String hh = miss / 3600 > 9 ? miss / 3600 + "" : "0" + miss / 3600;
        String mm = (mMiss % 3600) / 60 > 9 ? (mMiss % 3600) / 60 + "" : "0" + (mMiss % 3600) / 60;
        String ss = (mMiss % 3600) % 60 > 9 ? (mMiss % 3600) % 60 + "" : "0" + (mMiss % 3600) % 60;
        mMiss--;
        return  mm + ":" + ss;
    }
}
