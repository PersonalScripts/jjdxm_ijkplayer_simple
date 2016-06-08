package com.dou361.ijkplayer_simple;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dou361.ijkplayer_simple.listener.OnControlPanelVisibilityChangeListener;
import com.dou361.ijkplayer_simple.listener.OnInfoListener;
import com.dou361.ijkplayer_simple.listener.OnShowThumbnailListener;
import com.dou361.ijkplayer_simple.utils.ResourceUtils;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * ========================================
 * <p>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p>
 * 作 者：陈冠明
 * <p>
 * 个人网站：http://www.dou361.com
 * <p>
 * 版 本：1.0
 * <p>
 * 创建日期：2016/4/14
 * <p>
 * 描 述：
 * <p>
 * <p>
 * 修订历史：
 * <p>
 * ========================================
 */
public class PlayerView {


    private static final String TAG = PlayerView.class.getSimpleName();
    /**
     * 依附的容器Activity
     */
    private final Activity mActivity;
    /**
     * 全局上下文
     */
    private final Context mContext;
    private final Query query;
    private final IjkVideoView videoView;
    /**
     * 第三方so是否支持
     */
    private boolean playerSupport;
    /**
     * 码流列表
     */
    private String[] streamArr;
    /**
     * 不同码流对应的视频地址列表
     */
    private String[] urlArr;
    /**
     * 是否是直播 true为直播false为点播
     */
    private boolean isLive;
    /**
     * 当前状态
     */
    private int status = PlayStateParams.STATE_IDLE;
    private static final int MESSAGE_SHOW_PROGRESS = 1;
    private static final int MESSAGE_SEEK_NEW_POSITION = 3;
    private static final int MESSAGE_HIDE_CENTER_BOX = 4;
    private static final int MESSAGE_RESTART_PLAY = 5;
    /**
     * 当前播放位置
     */
    private int currentPosition;
    /**
     * 总时长
     */
    private long duration;
    /**
     * 当前声音大小
     */
    private int volume;
    /**
     * 当前亮度大小
     */
    private float brightness;
    /**
     * 是否显示控制面板true为显示false为隐藏
     */
    private boolean isShowControlPanl;
    /**
     * 禁止触摸
     */
    private boolean forbidTouch;
    /**
     * 是否可以拖动进度条
     */
    private boolean isDragging;
    /**
     * 设备宽度
     */
    private final int screenWidthPixels;
    /**
     * 是否网络提示
     */
    private boolean isGNetWork = true;
    private final ImageView iv_trumb;
    private final RelativeLayout rl_bottom_bar;
    private final SeekBar seekBar;
    private final AudioManager audioManager;
    private final int mMaxVolume;
    private final OrientationEventListener orientationEventListener;
    private boolean portrait;
    private final int initHeight;
    /**
     * 当前播放地址
     */
    private String currentUrl;
    /**
     * 是否只有全屏
     */
    private boolean fullScreenOnly;

    private OnInfoListener onInfoListener = new OnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {

        }
    };

    private OnControlPanelVisibilityChangeListener onControlPanelVisibilityChangeListener = new OnControlPanelVisibilityChangeListener() {
        @Override
        public void change(boolean isShowing) {

        }
    };

    private OnShowThumbnailListener mOnShowThumbnailListener = new OnShowThumbnailListener() {
        @Override
        public void onShowThumbnail(ImageView ivThumbnail) {

        }
    };
    private boolean closePlay;


    /**
     * 自定义bar显示的位置
     */
    public enum BarLocation {
        /**
         * 显示顶部
         */
        showTop,
        /**
         * 显示底部
         */
        showBotton;
    }

    /**
     * 显示缩略图
     */
    public PlayerView showThumbnail(OnShowThumbnailListener onShowThumbnailListener) {
        this.mOnShowThumbnailListener = onShowThumbnailListener;
        if (mOnShowThumbnailListener != null && iv_trumb != null) {
            mOnShowThumbnailListener.onShowThumbnail(iv_trumb);
        }
        return this;
    }

    /**
     * 播放几种状态监听监听
     */
    public PlayerView onInfo(OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
        return this;
    }

    public PlayerView onControlPanelVisibilityChang(OnControlPanelVisibilityChangeListener listener) {
        this.onControlPanelVisibilityChangeListener = listener;
        return this;
    }

    /**
     * 百分比显示切换
     */
    public PlayerView toggleAspectRatio() {
        if (videoView != null) {
            videoView.toggleAspectRatio();
        }
        return this;
    }

    public PlayerView(Activity activity) {
        this.mActivity = activity;
        this.mContext = activity;
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            playerSupport = true;
        } catch (Throwable e) {
            Log.e(TAG, "loadLibraries error", e);
        }
        screenWidthPixels = activity.getResources().getDisplayMetrics().widthPixels;
        query = new Query(activity);

        videoView = (IjkVideoView) activity.findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "video_view"));
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                statusChange(what);
                onInfoListener.onInfo(what, extra);
                return true;
            }
        });
        iv_trumb = (ImageView) activity.findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "iv_trumb"));
        rl_bottom_bar = (RelativeLayout) activity.findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "rl_bottom_bar"));
        seekBar = (SeekBar) activity.findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_seekBar"));
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(mSeekListener);
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_play")).clicked(onClickListener);
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")).clicked(onClickListener);
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_netTie_icon")).clicked(onClickListener);
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fullscreen")).clicked(onClickListener);
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_finish")).clicked(onClickListener);
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_replay_icon")).clicked(onClickListener);


        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final GestureDetector gestureDetector = new GestureDetector(activity, new PlayerGestureListener());


        View liveBox = activity.findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_box"));
        if (liveBox != null) {
            liveBox.setClickable(true);
            liveBox.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (gestureDetector.onTouchEvent(motionEvent))
                        return true;
                    // 处理手势结束
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            endGesture();
                            break;
                    }
                    return false;
                }
            });
        }


        orientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation >= 0 && orientation <= 30 || orientation >= 330 || (orientation >= 150 && orientation <= 210)) {
                    //竖屏
                    if (portrait) {
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationEventListener.disable();
                    }
                } else if ((orientation >= 90 && orientation <= 120) || (orientation >= 240 && orientation <= 300)) {
                    if (!portrait) {
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationEventListener.disable();
                    }
                }
            }
        };
        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        portrait = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        initHeight = activity.findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_box")).getLayoutParams().height;
        hideAll();
        if (!playerSupport) {
            showStatus(mActivity.getResources().getString(ResourceUtils.getResourceIdByName(mContext, "string", "not_support")));
        } else {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "ll_bg")).visible();
        }
    }


    /**
     * 设置播放地址
     * 包括视频清晰度列表
     * 对应地址列表
     */
    public void setPlaySource(String[] streamArr, String[] urlArr) {
        this.streamArr = streamArr;
        this.urlArr = urlArr;
        if (urlArr == null || urlArr.length <= 0) {
            return;
        }
        switchStream(0);
    }

    /**
     * 设置播放地址
     * 单个视频地址时
     */
    public void setPlaySource(String stream, String url) {
        setPlaySource(new String[]{stream}, new String[]{url});
    }

    /**
     * 设置播放地址
     * 单个视频地址时
     */
    public void setPlaySource(String url) {
        setPlaySource("标清", url);
    }


    /**
     * 自动播放
     */
    public void autoPlay(String path) {
        setPlaySource(path);
        startPlay();
    }

    /**
     * 开始播放
     */
    public void startPlay() {
        if (isLive) {
            videoView.setVideoPath(currentUrl);
            videoView.seekTo(0);
        } else {
            if (currentPosition <= 0) {
                videoView.setVideoPath(currentUrl);
            }
        }
        hideAll();
        if (isGNetWork && (getNetworkType() == 4 || getNetworkType() == 5 || getNetworkType() == 6)) {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_netTie")).visible();
        } else {
            if (playerSupport) {
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_loading")).visible();
                videoView.start();
            } else {
                showStatus(mActivity.getResources().getString(ResourceUtils.getResourceIdByName(mContext, "string", "not_support")));
            }
        }
    }

    /**
     * 设置视频名称
     */
    public void setTitle(String title) {
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_title")).text(title);
    }

    /**
     * 选择要播放的流
     */
    public void switchStream(int index) {
        if (urlArr != null && urlArr.length > index) {
            currentUrl = urlArr[index];
            isLive();
            currentPosition = 0;
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        statusChange(PlayStateParams.STATE_PAUSED);
        if (!isLive) {
            currentPosition = videoView.getCurrentPosition();
        }
        videoView.pause();
    }

    /**
     * 暂停播放
     */
    public void stopPlay() {
        videoView.stopPlayback();
        closePlay = true;
        if (mHandler != null) {
            mHandler.removeMessages(MESSAGE_RESTART_PLAY);
        }
    }

    /**
     * 设置播放位置
     */
    public PlayerView seekTo(int playtime) {
        videoView.seekTo(playtime);
        return this;
    }

    /**
     * 获取当前播放位置
     */
    public int getCurrentPosition() {
        currentPosition = videoView.getCurrentPosition();
        return currentPosition;
    }

    /**
     * 获取视频播放总时长
     */
    public long getDuration() {
        duration = videoView.getDuration();
        return duration;
    }

    /**
     * 设置2/3/4/5G和WiFi网络类型提示，
     *
     * @param isGNetWork true为进行2/3/4/5G网络类型提示
     *                   false 不进行网络类型提示
     */
    public void setNetWorkTypeTie(boolean isGNetWork) {
        this.isGNetWork = isGNetWork;
    }

    /**
     * 设置最大观看时长
     *
     * @param isCharge    true为收费 false为免费即不做限制
     * @param maxPlaytime 最大能播放时长
     */
    public void setChargeTie(boolean isCharge, int maxPlaytime) {

    }

    /**
     * 设置播放区域拉伸类型
     */
    public void setScaleType(int showType) {
        videoView.setAspectRatio(showType);
    }

    /**
     * 是否仅仅为全屏
     */
    public void setFullScreenOnly(boolean isFull) {
        this.fullScreenOnly = isFull;
        tryFullScreen(fullScreenOnly);
        if (fullScreenOnly) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    /**
     * 当前播放的是否是直播
     */
    public boolean isLive() {
        isLive = currentUrl != null && currentUrl.startsWith("rtmp://") || currentUrl.endsWith(".m3u8");
        return isLive;
    }

    /**
     * 是否禁止触摸
     */
    public void forbidTouch(boolean forbidTouch) {
        this.forbidTouch = forbidTouch;
    }

    /**
     * 是否显示拓展栏
     */
    public void showExpandBar(boolean isShow, BarLocation location) {

    }


    /**
     * 隐藏菜单栏
     */
    public void hideMenu(boolean isHide) {
        if (isHide) {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_menu")).gone();
        } else {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_menu")).visible();
        }
    }

    /**
     * 显示操作面板
     */
    public void operatorPanl() {
        isShowControlPanl = !isShowControlPanl;
        if (isShowControlPanl) {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_top_box")).visible();
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "rl_bottom_bar")).visible();
            if (isLive) {
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_seekBar")).gone();
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_currentTime")).gone();
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_endTime")).gone();
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_stream")).gone();
            } else {
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_seekBar")).visible();
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_currentTime")).visible();
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_endTime")).visible();
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_stream")).visible();
            }
            if (!fullScreenOnly) {
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fullscreen")).visible();
            }
            onControlPanelVisibilityChangeListener.change(true);
            /**显示面板的时候再根据状态显示播放按钮*/
            if (status == PlayStateParams.STATE_PLAYING
                    || status == PlayStateParams.STATE_PREPARED
                    || status == PlayStateParams.STATE_IDLE
                    || status == PlayStateParams.STATE_PAUSED
                    || status == PlayStateParams.STATE_COMPLETED) {
                if (isLive) {
                    query.id(ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")).gone();
                } else {
                    query.id(ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")).visible();
                }
            } else {
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")).gone();
            }
            updatePausePlay();
            mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
            mAutoPlayRunnable.start();
        } else {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_top_box")).gone();
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "rl_bottom_bar")).gone();
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")).gone();
            mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
            onControlPanelVisibilityChangeListener.change(false);
            mAutoPlayRunnable.stop();
        }

    }

    public void toggleFullScreen() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        updateFullScreenButton();
    }


    /**==========================================Activity生命周期方法回调=============================*/
    /**
     * @Override protected void onPause() {
     * super.onPause();
     * if (player != null) {
     * player.onPause();
     * }
     * }
     */
    public void onPause() {
        if (status == PlayStateParams.STATE_PLAYING) {
            pausePlay();
        }
    }

    /**
     * @Override protected void onResume() {
     * super.onResume();
     * if (player != null) {
     * player.onResume();
     * }
     * }
     */
    public void onResume() {
        if (status == PlayStateParams.STATE_PLAYING) {
            if (isLive) {
                videoView.seekTo(0);
            } else {
                if (currentPosition > 0) {
                    videoView.seekTo(currentPosition);
                }
            }
            doPauseResume();
        }
    }

    /**
     * @Override protected void onDestroy() {
     * super.onDestroy();
     * if (player != null) {
     * player.onDestroy();
     * }
     * }
     */
    public void onDestroy() {
        orientationEventListener.disable();
        mHandler.removeMessages(MESSAGE_RESTART_PLAY);
        mHandler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
        videoView.stopPlayback();
    }

    /**
     * @Override public void onConfigurationChanged(Configuration newConfig) {
     * super.onConfigurationChanged(newConfig);
     * if (player != null) {
     * player.onConfigurationChanged(newConfig);
     * }
     * }
     */
    public void onConfigurationChanged(final Configuration newConfig) {
        portrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        doOnConfigurationChanged(portrait);
    }

    /**
     * @Override public void onBackPressed() {
     * if (player != null && player.onBackPressed()) {
     * return;
     * }
     * super.onBackPressed();
     * }
     */
    public boolean onBackPressed() {
        if (!fullScreenOnly && getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        }
        return false;
    }

    /**
     * ==========================================Activity生命周期方法回调=============================
     */


    /**
     * ==========================================内部方法=============================
     */
    private boolean instantSeeking;
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_replay")).gone();//移动时隐藏掉状态image
            int newPosition = (int) ((duration * progress * 1.0) / 1000);
            String time = generateTime(newPosition);
            if (instantSeeking) {
                videoView.seekTo(newPosition);
            }
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_currentTime")).text(time);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
            if (instantSeeking) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!instantSeeking) {
                videoView.seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));
            }
            mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            isDragging = false;
            mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
        }
    };

    private long newPosition;
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_HIDE_CENTER_BOX:
                    query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_volume_box")).gone();
                    query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_brightness_box")).gone();
                    query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fastForward_box")).gone();
                    break;
                case MESSAGE_SEEK_NEW_POSITION:
                    if (!isLive && newPosition >= 0) {
                        videoView.seekTo((int) newPosition);
                        newPosition = -1;
                    }
                    break;
                case MESSAGE_SHOW_PROGRESS:
                    long pos = setProgress();
                    if (!isDragging && isShowControlPanl) {
                        msg = obtainMessage(MESSAGE_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                        updatePausePlay();
                    }
                    break;
                case MESSAGE_RESTART_PLAY:
                    status = PlayStateParams.STATE_ERROR;
                    doPauseResume();
                    break;
            }
        }
    };


    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fullscreen")) {
                toggleFullScreen();
            } else if (v.getId() == ResourceUtils.getResourceIdByName(mContext, "id", "app_video_play") || v.getId() == ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")) {
                doPauseResume();
            } else if (v.getId() == ResourceUtils.getResourceIdByName(mContext, "id", "app_video_replay_icon")) {
                status = PlayStateParams.STATE_ERROR;
                doPauseResume();
            } else if (v.getId() == ResourceUtils.getResourceIdByName(mContext, "id", "app_video_finish")) {
                if (!fullScreenOnly && !portrait) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    mActivity.finish();
                }
            } else if (v.getId() == ResourceUtils.getResourceIdByName(mContext, "id", "app_video_netTie_icon")) {
                isGNetWork = false;
                doPauseResume();
            }
        }
    };


    /**
     * 判断当前网络类型-1为未知网络0为没有网络连接1网络断开或关闭2为以太网3为WiFi4为2G5为3G6为4G
     */
    private int getNetworkType() {
        ConnectivityManager connectMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            /** 没有任何网络 */
            return 0;
        }
        if (!networkInfo.isConnected()) {
            /** 网络断开或关闭 */
            return 1;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
            /** 以太网网络 */
            return 2;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            /** wifi网络，当激活时，默认情况下，所有的数据流量将使用此连接 */
            return 3;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            /** 移动数据连接,不能与连接共存,如果wifi打开，则自动关闭 */
            switch (networkInfo.getSubtype()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    /** 2G网络 */
                    return 4;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    /** 3G网络 */
                    return 5;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    /** 4G网络 */
                    return 6;
            }
        }
        /** 未知网络 */
        return -1;
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        volume = -1;
        brightness = -1f;
//        if (newPosition >= 0) {
//            mHandler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
//            mHandler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
//        }
        mHandler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
        mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);

    }

    private void statusChange(int newStatus) {
        status = newStatus;
        if (!isLive && newStatus == PlayStateParams.STATE_COMPLETED) {
            hideAll();
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_replay")).visible();

        } else if (newStatus == PlayStateParams.STATE_PREPARING) {
            hideAll();
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_loading")).visible();
        } else if (newStatus == PlayStateParams.STATE_PREPARED || newStatus == PlayStateParams.STATE_PLAYING || newStatus == PlayStateParams.STATE_PAUSED) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    /**延迟一秒隐藏*/
                    hideAll();
                    query.id(ResourceUtils.getResourceIdByName(mContext, "id", "ll_bg")).gone();
                }
            }, 1000);
        } else if (newStatus == -10000) {
            if (!(isGNetWork && (getNetworkType() == 4 || getNetworkType() == 5 || getNetworkType() == 6))) {
                hideAll();
                if (isLive) {
                    showStatus("获取不到直播源");
                    /**5秒尝试重连*/
                    if (!closePlay) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_RESTART_PLAY, 10000);
                    }
                } else {
                    showStatus(mActivity.getResources().getString(ResourceUtils.getResourceIdByName(mContext, "string", "small_problem")));
                }
            }

        } else if (newStatus == PlayStateParams.STATE_ERROR) {
            if (!(isGNetWork && (getNetworkType() == 4 || getNetworkType() == 5 || getNetworkType() == 6))) {
                hideAll();
                if (isLive) {
                    showStatus(mActivity.getResources().getString(ResourceUtils.getResourceIdByName(mContext, "string", "small_problem")));
                    /**5秒尝试重连*/if (!closePlay) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_RESTART_PLAY, 10000);
                    }
                } else {
                    showStatus(mActivity.getResources().getString(ResourceUtils.getResourceIdByName(mContext, "string", "small_problem")));
                }
            }

        }
    }


    private void hideAll() {
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_top_box")).gone();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "rl_bottom_bar")).gone();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_replay")).gone();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_netTie")).gone();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_loading")).gone();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")).gone();
        onControlPanelVisibilityChangeListener.change(false);
    }

    private void doOnConfigurationChanged(final boolean portrait) {
        if (videoView != null && !fullScreenOnly) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    tryFullScreen(!portrait);
                    if (portrait) {
                        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_box")).height(initHeight, false);
                    } else {
                        int heightPixels = mActivity.getResources().getDisplayMetrics().heightPixels;
                        int widthPixels = mActivity.getResources().getDisplayMetrics().widthPixels;
                        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_box")).height(Math.min(heightPixels, widthPixels), false);
                    }
                    updateFullScreenButton();
                }
            });
            orientationEventListener.enable();
        }
    }

    private void tryFullScreen(boolean fullScreen) {
        if (mActivity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) mActivity).getSupportActionBar();
            if (supportActionBar != null) {
                if (fullScreen) {
                    supportActionBar.hide();
                } else {
                    supportActionBar.show();
                }
            }
        }
        setFullScreen(fullScreen);
    }

    private void setFullScreen(boolean fullScreen) {
        if (mActivity != null) {
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                mActivity.getWindow().setAttributes(attrs);
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.getWindow().setAttributes(attrs);
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }

    }

    private void showStatus(String statusText) {
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_replay")).visible();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_status_text")).text(statusText);
    }

    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    private int getScreenOrientation() {
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }


    private long setProgress() {
        if (isDragging) {
            return 0;
        }
        long position = videoView.getCurrentPosition();
        long duration = videoView.getDuration();
        if (seekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
            int percent = videoView.getBufferPercentage();
            seekBar.setSecondaryProgress(percent * 10);
        }

        this.duration = duration;
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_currentTime")).text(generateTime(position));
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_endTime")).text(generateTime(this.duration));
        return position;
    }

    private void doPauseResume() {
        if (status == PlayStateParams.STATE_COMPLETED) {
            /**直播是没有进入的*/
            currentPosition = 0;
            videoView.seekTo(currentPosition);
            startPlay();
        } else if (videoView.isPlaying()) {
            if (isLive) {
                videoView.stopPlayback();
            } else {
                pausePlay();
            }
        } else {
            startPlay();
        }
        updatePausePlay();
    }

    /**
     * 更新播放、暂停和停止按钮
     */
    private void updatePausePlay() {
        if (videoView.isPlaying()) {
            if (isLive) {
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_play")).image(ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_stop_white_24dp"));
            } else {
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_play")).image(ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_icon_media_pause"));
                query.id(ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")).image(ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_icon_media_pause"));
            }
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_loading")).gone();
        } else {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_play")).image(ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_arrow_white_24dp"));
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "play_icon")).image(ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_circle_outline_white_36dp"));
        }
    }

    /**
     * 更新全屏和半屏按钮
     */
    private void updateFullScreenButton() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fullscreen")).image(ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_icon_fullscreen_shrink"));
        } else {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fullscreen")).image(ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_icon_fullscreen_stretch"));
        }
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }

        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        // 显示
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_volume_icon")).image(i == 0 ? ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_volume_off_white_36dp") : ResourceUtils.getResourceIdByName(mContext, "drawable", "simple_player_volume_up_white_36dp"));
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_brightness_box")).gone();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_volume_box")).visible();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_volume_box")).visible();
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_volume")).text(s).visible();
    }

    /**
     * 滑动改变进度
     *
     * @param percent
     */
    private void onProgressSlide(float percent) {
        long position = videoView.getCurrentPosition();
        long duration = videoView.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);


        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fastForward_box")).visible();
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fastForward")).text(text + "s");
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fastForward_target")).text(generateTime(newPosition) + "/");
            query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_fastForward_all")).text(generateTime(duration));
        }
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (brightness < 0) {
            brightness = mActivity.getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        Log.d(this.getClass().getSimpleName(), "brightness:" + brightness + ",percent:" + percent);
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_brightness_box")).visible();
        WindowManager.LayoutParams lpa = mActivity.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        query.id(ResourceUtils.getResourceIdByName(mContext, "id", "app_video_brightness")).text(((int) (lpa.screenBrightness * 100)) + "%");
        mActivity.getWindow().setAttributes(lpa);

    }

    /**
     * ==========================================内部方法=============================
     */

    private AutoPlayRunnable mAutoPlayRunnable = new AutoPlayRunnable();

    private class AutoPlayRunnable implements Runnable {
        private int AUTO_PLAY_INTERVAL = 5000;
        private boolean mShouldAutoPlay;

        /**
         * 五秒无操作，收起控制面板
         */
        public AutoPlayRunnable() {
            mShouldAutoPlay = false;
        }

        public void start() {
            if (!mShouldAutoPlay) {
                mShouldAutoPlay = true;
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, AUTO_PLAY_INTERVAL);
            }
        }

        public void stop() {
            if (mShouldAutoPlay) {
                mHandler.removeCallbacks(this);
                mShouldAutoPlay = false;
            }
        }

        @Override
        public void run() {
            if (mShouldAutoPlay) {
                mHandler.removeCallbacks(this);
                operatorPanl();
                stop();
            }
        }
    }


    class Query {
        private final Activity activity;
        private View view;

        public Query(Activity activity) {
            this.activity = activity;
        }

        public Query id(int id) {
            view = activity.findViewById(id);
            return this;
        }

        public Query image(int resId) {
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(resId);
            }
            return this;
        }

        public Query visible() {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            return this;
        }

        public Query gone() {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            return this;
        }

        public Query invisible() {
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
            return this;
        }

        public Query clicked(View.OnClickListener handler) {
            if (view != null) {
                view.setOnClickListener(handler);
            }
            return this;
        }

        public Query text(CharSequence text) {
            if (view != null && view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        public Query visibility(int visible) {
            if (view != null) {
                view.setVisibility(visible);
            }
            return this;
        }

        private void size(boolean width, int n, boolean dip) {

            if (view != null) {

                ViewGroup.LayoutParams lp = view.getLayoutParams();


                if (n > 0 && dip) {
                    n = dip2pixel(activity, n);
                }

                if (width) {
                    lp.width = n;
                } else {
                    lp.height = n;
                }

                view.setLayoutParams(lp);

            }

        }

        public void height(int height, boolean dip) {
            size(false, height, dip);
        }

        public int dip2pixel(Context context, float n) {
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
            return value;
        }

        public float pixel2dip(Context context, float n) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = n / (metrics.densityDpi / 160f);
            return dp;

        }
    }

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            /**视频视窗双击事件*/
            if (!forbidTouch) {
                toggleFullScreen();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);

        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!forbidTouch) {
                float mOldX = e1.getX(), mOldY = e1.getY();
                float deltaY = mOldY - e2.getY();
                float deltaX = mOldX - e2.getX();
                if (firstTouch) {
                    toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                    volumeControl = mOldX > screenWidthPixels * 0.5f;
                    firstTouch = false;
                }

                if (toSeek) {
                    if (!isLive) {
                        /**进度设置*/
                        onProgressSlide(-deltaX / videoView.getWidth());
                    }
                } else {
                    float percent = deltaY / videoView.getHeight();
                    if (volumeControl) {
                        /**声音设置*/
                        onVolumeSlide(percent);
                    } else {
                        /**亮度设置*/
                        onBrightnessSlide(percent);
                    }


                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            /**视频视窗单击事件*/
            if (!forbidTouch) {
                operatorPanl();
            }
            return true;
        }
    }

}
