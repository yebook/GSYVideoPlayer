package com.example.gsyvideoplayer;

import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gsyvideoplayer.video.MultiSampleVideo;
import com.example.gsyvideoplayer.video.manager.CustomManager;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class ListMultiVideoActivity extends AppCompatActivity {

    MultiSampleVideo video_item_player_1;
    ImageView video_item_player_3;
    //    String url = "file://sdcard/single/bbb.mp4";
//    String url = "file://sdcard/single/ccc.mp4";
//    String url3 = "file://sdcard/single/ddd.mp4";
//    String url4 = "file://sdcard/single/eee.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video);
        initView1();
    }


    int playPosition;

    private void initView1() {
        playPosition = new Random().nextInt(10);
        video_item_player_3 = (ImageView) findViewById(R.id.video_item_player_3);
        video_item_player_1 = (MultiSampleVideo) findViewById(R.id.video_item_player_1);
        startPlauVide(url);
    }

    private void playNextVideo() {
        startPlauVide(url);
    }

    private static final String TAG = "123456";
    String url = "file://sdcard/single/aaa.mp4";

    private void startPlauVide(String playUrl) {
        //多个播放时必须在setUpLazy、setUp和getGSYVideoManager()等前面设置
        video_item_player_1.setPlayTag(TAG);
        video_item_player_1.setPlayPosition(playPosition);
        video_item_player_1.setUp(playUrl, false, "");
        video_item_player_1.setMediaIjkVoiceNum(0.9f);
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
                        video_item_player_3.setVisibility(View.VISIBLE);
                        video_item_player_3.setImageBitmap(bitmap);
                        handlerIjk.sendEmptyMessageDelayed(DISSMISS_IMAGE_SHOW, 1500);
                    }
                });
                playNextVideo();
            }


            @Override
            public void onCompletion() {

            }

            @Override
            public void onError(int what, int extra) {

            }
        });
    }


    private static final int DISSMISS_IMAGE_SHOW = 21325;
    private Handler handlerIjk = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DISSMISS_IMAGE_SHOW:
                    video_item_player_3.setVisibility(View.GONE);
                    break;
            }
        }
    };


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
