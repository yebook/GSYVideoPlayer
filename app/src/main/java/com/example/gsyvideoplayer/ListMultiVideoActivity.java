package com.example.gsyvideoplayer;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gsyvideoplayer.video.MultiSampleVideo;
import com.example.gsyvideoplayer.video.manager.CustomManager;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.GsyMediaBackListener;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class ListMultiVideoActivity extends AppCompatActivity {

    MultiSampleVideo video_item_player_1;
    MultiSampleVideo video_item_player_2;
//    String url = "file://sdcard/etv/task/single/aaa.mp4";
    String url = "file://sdcard/etv/task/single/bbb.mp4";
    String url2 = "file://sdcard/etv/task/single/ccc.mp4";
    String url3 = "file://sdcard/etv/task/single/ddd.mp4";
    String url4 = "file://sdcard/etv/task/single/eee.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video);

        initView1();
        initView2();
    }

    private static final String TAG = "123456";

    private void initView1() {
        video_item_player_1 = (MultiSampleVideo) findViewById(R.id.video_item_player_1);
        //多个播放时必须在setUpLazy、setUp和getGSYVideoManager()等前面设置
        video_item_player_1.setPlayTag(TAG);
        video_item_player_1.setPlayPosition(1);
        video_item_player_1.setUp(url, null, "");
        video_item_player_1.startPlayLogic();

        video_item_player_1.setPlayStatuesListener(new GsyMediaBackListener() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onAutoCompletion(StandardGSYVideoPlayer standardGSYVideoPlayer) {
                video_item_player_1.taskShotPic(new GSYVideoShotListener() {
                    @Override
                    public void getBitmap(Bitmap bitmap) {

                    }
                });
                video_item_player_1.setPlayTag(TAG);
                video_item_player_1.setPlayPosition(1);
                video_item_player_1.setUp(url, null, "");
                video_item_player_1.startPlayLogic();
            }


            @Override
            public void onCompletion() {

            }

            @Override
            public void onError(int what, int extra) {

            }
        });
    }

    private void initView2() {
        video_item_player_2 = (MultiSampleVideo) findViewById(R.id.video_item_player_2);
        //多个播放时必须在setUpLazy、setUp和getGSYVideoManager()等前面设置
        video_item_player_2.setPlayTag("132");
        video_item_player_2.setPlayPosition(2);
        video_item_player_2.setUp(url, null, "");
        video_item_player_2.startPlayLogic();

        video_item_player_2.setPlayStatuesListener(new GsyMediaBackListener() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onAutoCompletion(StandardGSYVideoPlayer standardGSYVideoPlayer) {
                video_item_player_2.setPlayTag("132");
                video_item_player_2.setPlayPosition(2);
                video_item_player_2.setUp(url, null, "");
                video_item_player_2.startPlayLogic();
            }


            @Override
            public void onCompletion() {

            }

            @Override
            public void onError(int what, int extra) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CustomManager.onPauseAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomManager.onResumeAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CustomManager.clearAllVideo();
    }

}
