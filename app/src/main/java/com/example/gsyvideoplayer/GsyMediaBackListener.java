package com.example.gsyvideoplayer;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

public interface GsyMediaBackListener {

    void onPrepared();

    void onAutoCompletion(StandardGSYVideoPlayer standardGSYVideoPlayer);

    void onCompletion();

    void onError(int what, int extra);
}
