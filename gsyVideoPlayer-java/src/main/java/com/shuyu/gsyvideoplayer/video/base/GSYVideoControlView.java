package com.shuyu.gsyvideoplayer.video.base;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;

/**
 * 播放UI的显示、控制层、手势处理等
 * Created by guoshuyu on 2017/8/2.
 */

public abstract class GSYVideoControlView extends GSYVideoView implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {


    //手指放下的位置
    protected int mDownPosition;

    //手势调节音量的大小
    protected int mGestureDownVolume;

    //手势偏差值
    protected int mThreshold = 80;

    //手动改变滑动的位置
    protected int mSeekTimePosition;

    //手动滑动的起始偏移位置
    protected int mSeekEndOffset;

    //退出全屏显示的案件图片
    protected int mShrinkImageRes = -1;

    //全屏显示的案件图片
    protected int mEnlargeImageRes = -1;

    //触摸显示后隐藏的时间
    protected int mDismissControlTime = 2500;

    //触摸的X
    protected float mDownX;

    //触摸的Y
    protected float mDownY;

    //移动的Y
    protected float mMoveY;

    //亮度
    protected float mBrightnessData = -1;

    //触摸滑动进度的比例系数
    protected float mSeekRatio = 1;
    //是否改变音量
    protected boolean mChangeVolume = false;

    //是否改变播放进度
    protected boolean mChangePosition = false;

    //触摸显示虚拟按键
    protected boolean mShowVKey = false;

    //是否改变亮度
    protected boolean mBrightness = false;

    //是否首次触摸
    protected boolean mFirstTouch = false;

    //是否隐藏虚拟按键
    protected boolean mHideKey = true;

    //是否需要显示流量提示
    protected boolean mNeedShowWifiTip = true;

    //是否支持非全屏滑动触摸有效
    protected boolean mIsTouchWiget = true;

    //是否支持全屏滑动触摸有效
    protected boolean mIsTouchWigetFull = true;
    //lazy的setup
    protected boolean mSetUpLazy = false;

    //seek touch
    protected boolean mHadSeekTouch = false;
    //title
    protected TextView mTitleTextView;

    //点击锁屏的回调
    protected LockClickListener mLockClickListener;

    protected GSYVideoProgressListener mGSYVideoProgressListener;

    public GSYVideoControlView(@NonNull Context context) {
        super(context);
    }

    public GSYVideoControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GSYVideoControlView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GSYVideoControlView(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    protected void init(Context context) {
        super.init(context);

        mTitleTextView = (TextView) findViewById(R.id.title);

        if (isInEditMode())
            return;

        if (mTextureViewContainer != null) {
            mTextureViewContainer.setOnClickListener(this);
            mTextureViewContainer.setOnTouchListener(this);
        }
        if (getActivityContext() != null) {
            mSeekEndOffset = CommonUtil.dip2px(getActivityContext(), 50);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Debuger.printfLog(GSYVideoControlView.this.hashCode() + "------------------------------ dismiss onDetachedFromWindow");
    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
    }

    /**
     * 设置播放显示状态
     *
     * @param state
     */
    @Override
    protected void setStateAndUi(int state) {
        mCurrentState = state;
        if ((state == CURRENT_STATE_NORMAL && isCurrentMediaListener())
                || state == CURRENT_STATE_AUTO_COMPLETE || state == CURRENT_STATE_ERROR) {
            mHadPrepared = false;
        }

        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL:
                if (isCurrentMediaListener()) {
                    Debuger.printfLog(GSYVideoControlView.this.hashCode() + "------------------------------ dismiss CURRENT_STATE_NORMAL");
                    getGSYVideoManager().releaseMediaPlayer();
                    releasePauseCover();
                    mBufferPoint = 0;
                    mSaveChangeViewTIme = 0;
                    if (mAudioManager != null) {
                        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                    }
                }
                releaseNetWorkState();
                break;
            case CURRENT_STATE_PREPAREING:
                break;
            case CURRENT_STATE_PLAYING:
                if (isCurrentMediaListener()) {
                    Debuger.printfLog(GSYVideoControlView.this.hashCode() + "------------------------------ CURRENT_STATE_PLAYING");
                }
                break;
            case CURRENT_STATE_PAUSE:
                Debuger.printfLog(GSYVideoControlView.this.hashCode() + "------------------------------ CURRENT_STATE_PAUSE");
                break;
            case CURRENT_STATE_ERROR:
                if (isCurrentMediaListener()) {
                    getGSYVideoManager().releaseMediaPlayer();
                }
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                Debuger.printfLog(GSYVideoControlView.this.hashCode() + "------------------------------ dismiss CURRENT_STATE_AUTO_COMPLETE");
                break;
        }
        resolveUIState(state);
    }


    @Override
    protected void setSmallVideoTextureView(View.OnTouchListener onTouchListener) {
        super.setSmallVideoTextureView(onTouchListener);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (mHideKey) {
            hideNavKey(mContext);
        } else if (i == R.id.surface_container && mCurrentState == CURRENT_STATE_ERROR) {
            if (mVideoAllCallBack != null) {
                Debuger.printfLog("onClickStartError");
                mVideoAllCallBack.onClickStartError(mOriginUrl, mTitle, this);
            }
            prepareVideo();
        } else if (i == R.id.surface_container) {
            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                Debuger.printfLog("onClickBlank");
                mVideoAllCallBack.onClickBlank(mOriginUrl, mTitle, GSYVideoControlView.this);
            }
        }
    }

    /**
     * 双击
     */
    protected GestureDetector gestureDetector = new GestureDetector(getContext().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            touchDoubleUp();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!mChangePosition && !mChangeVolume && !mBrightness) {
                onClickUiToggle();
            }
            return super.onSingleTapConfirmed(e);
        }
    });

    /**
     * 亮度、进度、音频
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int id = v.getId();
        float x = event.getX();
        float y = event.getY();

        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    touchSurfaceDown(x, y);

                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);

                    if ((mIsTouchWigetFull)
                            || (mIsTouchWiget)) {
                        if (!mChangePosition && !mChangeVolume && !mBrightness) {
                            touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
                        }
                    }
                    touchSurfaceMove(deltaX, deltaY, y);

                    break;
                case MotionEvent.ACTION_UP:
                    touchSurfaceUp();
                    Debuger.printfLog(GSYVideoControlView.this.hashCode() + "------------------------------ surface_container ACTION_UP");
                    //不要和隐藏虚拟按键后，滑出虚拟按键冲突
                    if (mHideKey && mShowVKey) {
                        return true;
                    }
                    break;
            }
            gestureDetector.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean setUp(String url, String title) {
        if (super.setUp(url, title)) {
            if (title != null && mTitleTextView != null) {
                mTitleTextView.setText(title);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHadSeekTouch = true;
    }

    /***
     * 拖动进度条
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            Debuger.printfLog("onClickSeekbar");
            mVideoAllCallBack.onClickSeekbar(mOriginUrl, mTitle, this);
        }
        if (getGSYVideoManager() != null && mHadPlay) {
            try {
                int time = seekBar.getProgress() * getDuration() / 100;
                getGSYVideoManager().seekTo(time);
            } catch (Exception e) {
                Debuger.printfWarning(e.toString());
            }
        }
        mHadSeekTouch = false;
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        if (mCurrentState != CURRENT_STATE_PREPAREING) return;
        Debuger.printfLog(GSYVideoControlView.this.hashCode() + "------------------------------ surface_container onPrepared");
    }


    @Override
    public void onBufferingUpdate(final int percent) {
        post(new Runnable() {
            @Override
            public void run() {
                if (mCurrentState != CURRENT_STATE_NORMAL && mCurrentState != CURRENT_STATE_PREPAREING) {
                    if (percent != 0) {
                        setTextAndProgress(percent);
                        mBufferPoint = percent;
                        Debuger.printfLog("Net speed: " + getNetSpeedText() + " percent " + percent);
                    }
                }
            }
        });
    }

    /**
     * 增对列表优化，在播放前的时候才进行setup
     */
    @Override
    protected void prepareVideo() {
        if (mSetUpLazy) {
            super.setUp(mOriginUrl,
                    mMapHeadData,
                    mTitle);
        }
        super.prepareVideo();
    }

    protected void touchSurfaceDown(float x, float y) {
        mDownX = x;
        mDownY = y;
        mMoveY = 0;
        mChangeVolume = false;
        mChangePosition = false;
        mShowVKey = false;
        mBrightness = false;
        mFirstTouch = true;
    }

    protected void touchSurfaceMove(float deltaX, float deltaY, float y) {
        int curWidth = 0;
        int curHeight = 0;
        if (getActivityContext() != null) {
            curWidth = CommonUtil.getCurrentScreenLand((Activity) getActivityContext()) ? mScreenHeight : mScreenWidth;
            curHeight = CommonUtil.getCurrentScreenLand((Activity) getActivityContext()) ? mScreenWidth : mScreenHeight;
        }
        if (mChangePosition) {
            int totalTimeDuration = getDuration();
            mSeekTimePosition = (int) (mDownPosition + (deltaX * totalTimeDuration / curWidth) / mSeekRatio);
            if (mSeekTimePosition > totalTimeDuration)
                mSeekTimePosition = totalTimeDuration;
            String seekTime = CommonUtil.stringForTime(mSeekTimePosition);
            String totalTime = CommonUtil.stringForTime(totalTimeDuration);
            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
        } else if (mChangeVolume) {
            deltaY = -deltaY;
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int deltaV = (int) (max * deltaY * 3 / curHeight);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
            int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / curHeight);

            showVolumeDialog(-deltaY, volumePercent);
        } else if (!mChangePosition && mBrightness) {
            if (Math.abs(deltaY) > mThreshold) {
                float percent = (-deltaY / curHeight);
                onBrightnessSlide(percent);
                mDownY = y;
            }
        }
    }

    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        int curWidth = 0;
        if (getActivityContext() != null) {
            curWidth = CommonUtil.getCurrentScreenLand((Activity) getActivityContext()) ? mScreenHeight : mScreenWidth;
        }
        if (absDeltaX > mThreshold || absDeltaY > mThreshold) {
            if (absDeltaX >= mThreshold) {
                //防止全屏虚拟按键
                int screenWidth = CommonUtil.getScreenWidth(getContext());
                if (Math.abs(screenWidth - mDownX) > mSeekEndOffset) {
                    mChangePosition = true;
                    mDownPosition = getCurrentPositionWhenPlaying();
                } else {
                    mShowVKey = true;
                }
            } else {
                int screenHeight = CommonUtil.getScreenHeight(getContext());
                boolean noEnd = Math.abs(screenHeight - mDownY) > mSeekEndOffset;
                if (mFirstTouch) {
                    mBrightness = (mDownX < curWidth * 0.5f) && noEnd;
                    mFirstTouch = false;
                }
                if (!mBrightness) {
                    mChangeVolume = noEnd;
                    mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                }
                mShowVKey = !noEnd;
            }
        }
    }


    protected void touchSurfaceUp() {
        if (mChangePosition) {
            int duration = getDuration();
            int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
        }
        dismissProgressDialog();
        dismissVolumeDialog();
        dismissBrightnessDialog();
        if (mChangePosition && getGSYVideoManager() != null && (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE)) {
            try {
                getGSYVideoManager().seekTo(mSeekTimePosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int duration = getDuration();
            int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                Debuger.printfLog("onTouchScreenSeekPosition");
                mVideoAllCallBack.onTouchScreenSeekPosition(mOriginUrl, mTitle, this);
            }
        } else if (mBrightness) {
            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                Debuger.printfLog("onTouchScreenSeekLight");
                mVideoAllCallBack.onTouchScreenSeekLight(mOriginUrl, mTitle, this);
            }
        } else if (mChangeVolume) {
            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                Debuger.printfLog("onTouchScreenSeekVolume");
                mVideoAllCallBack.onTouchScreenSeekVolume(mOriginUrl, mTitle, this);
            }
        }
    }

    /**
     * 双击暂停/播放
     * 如果不需要，重载为空方法即可
     */
    protected void touchDoubleUp() {
        if (!mHadPlay) {
            return;
        }
        clickStartIcon();
    }

    /**
     * 处理控制显示
     *
     * @param state
     */
    protected void resolveUIState(int state) {
        switch (state) {
            case CURRENT_STATE_NORMAL:
                changeUiToNormal();
                break;
            case CURRENT_STATE_PREPAREING:
                changeUiToPreparingShow();
                break;
            case CURRENT_STATE_PLAYING:
                changeUiToPlayingShow();
                break;
            case CURRENT_STATE_PAUSE:
                changeUiToPauseShow();
                break;
            case CURRENT_STATE_ERROR:
                changeUiToError();
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                changeUiToCompleteShow();
                break;
            case CURRENT_STATE_PLAYING_BUFFERING_START:
                changeUiToPlayingBufferingShow();
                break;
        }
    }


    /**
     * 播放按键点击
     */
    protected void clickStartIcon() {
        if (TextUtils.isEmpty(mUrl)) {
            Debuger.printfError("********" + getResources().getString(R.string.no_url));
            return;
        }
        if (mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR) {
            if (isShowNetConfirm()) {
                showWifiDialog();
                return;
            }
            startButtonLogic();
        } else if (mCurrentState == CURRENT_STATE_PLAYING) {
            try {
                onVideoPause();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setStateAndUi(CURRENT_STATE_PAUSE);
            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                Debuger.printfLog("onClickStop");
                mVideoAllCallBack.onClickStop(mOriginUrl, mTitle, this);
            }
        } else if (mCurrentState == CURRENT_STATE_PAUSE) {
            if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                Debuger.printfLog("onClickResume");
                mVideoAllCallBack.onClickResume(mOriginUrl, mTitle, this);
            }

            if (!mHadPlay && !mStartAfterPrepared) {
                startAfterPrepared();
            }

            try {
                getGSYVideoManager().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setStateAndUi(CURRENT_STATE_PLAYING);
        } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
            startButtonLogic();
        }
    }

    protected void setTextAndProgress(int secProgress) {
        int position = getCurrentPositionWhenPlaying();
        int duration = getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        setProgressAndTime(progress, secProgress, position, duration);
        setProgressAndTime(progress, secProgress, position, duration);
    }

    protected void setProgressAndTime(int progress, int secProgress, int currentTime, int totalTime) {

        if (mGSYVideoProgressListener != null && mCurrentState == CURRENT_STATE_PLAYING) {
            mGSYVideoProgressListener.onProgress(progress, secProgress, currentTime, totalTime);
        }

        if (mHadSeekTouch) {
            return;
        }
        if (getGSYVideoManager().getBufferedPercentage() > 0) {
            secProgress = getGSYVideoManager().getBufferedPercentage();
        }
        if (secProgress > 94) secProgress = 100;
    }

    protected void setViewShowState(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    protected void onBrightnessSlide(float percent) {
        mBrightnessData = ((Activity) (mContext)).getWindow().getAttributes().screenBrightness;
        if (mBrightnessData <= 0.00f) {
            mBrightnessData = 0.50f;
        } else if (mBrightnessData < 0.01f) {
            mBrightnessData = 0.01f;
        }
        WindowManager.LayoutParams lpa = ((Activity) (mContext)).getWindow().getAttributes();
        lpa.screenBrightness = mBrightnessData + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        showBrightnessDialog(lpa.screenBrightness);
        ((Activity) (mContext)).getWindow().setAttributes(lpa);
    }


    protected boolean isShowNetConfirm() {
        return !mOriginUrl.startsWith("file") && !mOriginUrl.startsWith("android.resource") && !CommonUtil.isWifiConnected(getContext())
                && mNeedShowWifiTip && !getGSYVideoManager().cachePreview(mContext.getApplicationContext(), mOriginUrl);
    }

    private class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE) {
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                setTextAndProgress(0);
                            }
                        }
                );
            }
        }
    }

    private class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mCurrentState != CURRENT_STATE_NORMAL
                    && mCurrentState != CURRENT_STATE_ERROR
                    && mCurrentState != CURRENT_STATE_AUTO_COMPLETE) {
                if (getActivityContext() != null) {
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    hideAllWidget();
                                    if (mHideKey && mShowVKey) {
                                        hideNavKey(mContext);
                                    }
                                }
                            }
                    );
                }
            }
        }
    }

    /************************* 继承之后可自定义ui与显示隐藏 *************************/

    protected abstract void showWifiDialog();

    protected abstract void showProgressDialog(float deltaX,
                                               String seekTime, int seekTimePosition,
                                               String totalTime, int totalTimeDuration);

    protected abstract void dismissProgressDialog();

    protected abstract void showVolumeDialog(float deltaY, int volumePercent);

    protected abstract void dismissVolumeDialog();

    protected abstract void showBrightnessDialog(float percent);

    protected abstract void dismissBrightnessDialog();

    protected abstract void onClickUiToggle();

    protected abstract void hideAllWidget();

    protected abstract void changeUiToNormal();

    protected abstract void changeUiToPreparingShow();

    protected abstract void changeUiToPlayingShow();

    protected abstract void changeUiToPauseShow();

    protected abstract void changeUiToError();

    protected abstract void changeUiToCompleteShow();

    protected abstract void changeUiToPlayingBufferingShow();


    /************************* 开放接口 *************************/


    /**
     * 在点击播放的时候才进行真正setup
     */
    public boolean setUpLazy(String url, Map<String, String> mapHeadData, String title) {
        mOriginUrl = url;
        mSetUpLazy = true;
        mTitle = title;
        mMapHeadData = mapHeadData;
        if (isCurrentMediaListener() &&
                (System.currentTimeMillis() - mSaveChangeViewTIme) < CHANGE_DELAY_TIME)
            return false;
        mUrl = "waiting";
        mCurrentState = CURRENT_STATE_NORMAL;
        return true;
    }

    /**
     * 初始化为正常状态
     */
    public void initUIState() {
        setStateAndUi(CURRENT_STATE_NORMAL);
    }


    /**
     * title
     */
    public TextView getTitleTextView() {
        return mTitleTextView;
    }

    public int getEnlargeImageRes() {
        if (mEnlargeImageRes == -1) {
            return R.drawable.video_enlarge;
        }
        return mEnlargeImageRes;
    }

    /**
     * 设置右下角 显示切换到全屏 的按键资源
     * 必须在setUp之前设置
     * 不设置使用默认
     */
    public void setEnlargeImageRes(int mEnlargeImageRes) {
        this.mEnlargeImageRes = mEnlargeImageRes;
    }

    public int getShrinkImageRes() {
        if (mShrinkImageRes == -1) {
            return R.drawable.video_shrink;
        }
        return mShrinkImageRes;
    }

    /**
     * 设置右下角 显示退出全屏 的按键资源
     * 必须在setUp之前设置
     * 不设置使用默认
     */
    public void setShrinkImageRes(int mShrinkImageRes) {
        this.mShrinkImageRes = mShrinkImageRes;
    }

    /**
     * 是否可以全屏滑动界面改变进度，声音等
     * 默认 true
     */
    public void setIsTouchWigetFull(boolean isTouchWigetFull) {
        this.mIsTouchWigetFull = isTouchWigetFull;
    }

    public boolean isHideKey() {
        return mHideKey;
    }

    /**
     * 全屏隐藏虚拟按键，默认打开
     */
    public void setHideKey(boolean hideKey) {
        this.mHideKey = hideKey;
    }

    public boolean isNeedShowWifiTip() {
        return mNeedShowWifiTip;
    }


    public boolean isTouchWiget() {
        return mIsTouchWiget;
    }

    /**
     * 是否可以滑动界面改变进度，声音等
     * 默认true
     */
    public void setIsTouchWiget(boolean isTouchWiget) {
        this.mIsTouchWiget = isTouchWiget;
    }

    public boolean isTouchWigetFull() {
        return mIsTouchWigetFull;
    }

    /**
     * 是否需要显示流量提示,默认true
     */
    public void setNeedShowWifiTip(boolean needShowWifiTip) {
        this.mNeedShowWifiTip = needShowWifiTip;
    }

    /**
     * 调整触摸滑动快进的比例
     *
     * @param seekRatio 滑动快进的比例，默认1。数值越大，滑动的产生的seek越小
     */
    public void setSeekRatio(float seekRatio) {
        if (seekRatio < 0) {
            return;
        }
        this.mSeekRatio = seekRatio;
    }

    public float getSeekRatio() {
        return mSeekRatio;
    }

    /**
     * 锁屏点击
     */
    public void setLockClickListener(LockClickListener lockClickListener) {
        this.mLockClickListener = lockClickListener;
    }

    /**
     * 设置触摸显示控制ui的消失时间
     *
     * @param dismissControlTime 毫秒，默认2500
     */
    public void setDismissControlTime(int dismissControlTime) {
        this.mDismissControlTime = dismissControlTime;
    }

    public int getDismissControlTime() {
        return mDismissControlTime;
    }

    /**
     * 进度回调
     */
    public void setGSYVideoProgressListener(GSYVideoProgressListener videoProgressListener) {
        this.mGSYVideoProgressListener = videoProgressListener;
    }
}
