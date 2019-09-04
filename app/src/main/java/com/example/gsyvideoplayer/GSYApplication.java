package com.example.gsyvideoplayer;

import androidx.multidex.MultiDexApplication;

import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;


/**
 * Created by shuyu on 2016/11/11.
 */

public class GSYApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        GSYVideoType.enableMediaCodec();
        GSYVideoType.enableMediaCodecTexture();

//        ExoSourceManager.setSkipSSLChain(true);
//        PlayerFactory.setPlayManager(SystemPlayerManager.class);//系统模式
        PlayerFactory.setPlayManager(IjkPlayerManager.class);//ijk模式

        //CacheFactory.setCacheManager(ExoPlayerCacheManager.class);//exo缓存模式，支持m3u8，只支持exo
        //CacheFactory.setCacheManager(ProxyCacheManager.class);//代理缓存模式，支持所有模式，不支持m3u8等

        //GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        //GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);

//        GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
        GSYVideoType.setRenderType(GSYVideoType.TEXTURE);
        //GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);

        //IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);

        /*ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
            @Override
            public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
                Uri contentUri = Uri.parse(dataSource);
                int contentType = inferContentType(dataSource);
                switch (contentType) {
                    case C.TYPE_HLS:
                        return new HlsMediaSource.Factory(CustomSourceTag.getDataSourceFactory(GSYApplication.this.getApplicationContext(), preview)).createMediaSource(contentUri);
                }
                return null;
            }
        });*/

    }


}
