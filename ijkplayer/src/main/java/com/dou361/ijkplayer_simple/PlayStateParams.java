package com.dou361.ijkplayer_simple;

/**
 * ========================================
 * <p/>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p/>
 * 作 者：陈冠明
 * <p/>
 * 个人网站：http://www.dou361.com
 * <p/>
 * 版 本：1.0
 * <p/>
 * 创建日期：2016/5/26
 * <p/>
 * 描 述：
 * <p/>
 * <p/>
 * 修订历史：
 * <p/>
 * ========================================
 */
public class PlayStateParams {

    /**
     * 可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
     */
    public static final int fitparent = 0;
    /**
     * 可能会剪裁,等比例放大视频，直到填满View为止,超过View的部分作裁剪处理
     */
    public static final int fillparent = 1;
    /**
     * 将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
     */
    public static final int wrapcontent = 2;
    /**
     * 不剪裁,非等比例拉伸画面填满整个View
     */
    public static final int fitxy = 3;
    /**
     * 不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
     */
    public static final int f16_9 = 4;
    /**
     * 不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
     */
    public static final int f4_3 = 5;


    /**
     * 播放出错
     */
    public static final int STATE_ERROR = -1;
    /**
     * 空闲
     */
    public static final int STATE_IDLE = 0;
    /**
     * 准备中/加载中
     */
    public static final int STATE_PREPARING = 1;
    /**
     * 准备完成
     */
    public static final int STATE_PREPARED = 2;
    /**
     * 播放中
     */
    public static final int STATE_PLAYING = 3;
    /**
     * 暂停
     */
    public static final int STATE_PAUSED = 4;
    /**
     * 播放完成
     */
    public static final int STATE_COMPLETED = 5;

    public static final int PV_PLAYER__Auto = 0;
    public static final int PV_PLAYER__AndroidMediaPlayer = 1;
    public static final int PV_PLAYER__IjkMediaPlayer = 2;
    public static final int PV_PLAYER__IjkExoMediaPlayer = 3;

}
