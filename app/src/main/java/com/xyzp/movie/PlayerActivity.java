package com.xyzp.movie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.ui.PlayerView;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import bean.LightSensorUtils;
import bean.Video;


public class PlayerActivity extends Activity {
    private LightSensorUtils lightSensorUtils;
    private final Set<TrackGroup> audioSet=new HashSet<>();
    private final Set<TrackGroup> subtitleSet=new HashSet<>();
    private final Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private PlayerView videoView;
    private ExoPlayer player;
    private ImageView playButton,backButton,moreButton,nextButton,perfButton;
    private SeekBar seekBar;
    private TextView currentTimeTextView,totalTimeTextView;
    private RelativeLayout topController,bottomController;
    private SlideDialog slideDialog;
    private List<Video> videoList;
    private DefaultTrackSelector trackSelector;
    private boolean controllersVisible = true, tip = true, reminder= false, playSituation=true; //组件可视，提示，进度提醒，播放情况
    private int movieid=0,checkedItem = 2; // 倍速,默认选中 1x
    //服务于页面滑动
    private int x=0,y=0, operationType = 0; //xy初始坐标,operationType  0: 未知, 1: 改变亮度, 2: 改变音量, 3: 改变进度, 4: 下拉通知栏
    private long current = 0; // 初始进度
    private String url,moviepath;

    @Override
    @androidx.media3.common.util.UnstableApi
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //material 3取色，安卓12起
        DynamicColors.applyToActivityIfAvailable(this);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            setContentView(R.layout.activity_player);
        } else {
            //竖屏
            setContentView(R.layout.activity_player_vertical);
        }
        init();
        if(savedInstanceState != null) {
            reminder = savedInstanceState.getBoolean("reminder");
            playSituation = sharedPreferences.getBoolean("play_situation", false);
        }

        //获取传递的值
        Intent intent = getIntent();
        url=intent.getStringExtra("movie_url");
        moviepath=intent.getStringExtra("movie_path");
        int position = intent.getIntExtra("movie_position",0);
        movieid = intent.getIntExtra("movie_id",0);
        videoList= (List<Video>) intent.getSerializableExtra("movie_video_list");
        prepare(position,url,moviepath);
        
        if(playSituation) { //回后台前正在播放，第一次播放
            player.play();
            playButton.setImageResource(R.drawable.pause_fill1_wght400_grad0_opsz48);
        } else {
            player.pause();
            playButton.setImageResource(R.drawable.play_arrow_fill1_wght400_grad0_opsz48);
        }

        //提醒
        if (tip) {
            //监测光线传感器数据
            lightSensorUtils=new LightSensorUtils(this);
            lightSensorUtils.setLightListener(new LightSensorUtils.LightListener() {
                @Override
                public void getLight(float value) {
                    if (value<10 && !reminder) { //第两次reminder使用
                        Toast.makeText(PlayerActivity.this, "当前环境过于昏暗！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            reminder=true; //两次reminder使用完成，设为true
        }

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                // 每隔一秒执行一次
            }

            public void onFinish() {
                // 倒计时结束，执行代码
                toggleControllersVisibilityOff();
            }
        }.start();

        //播放列表更新监听事件
        player.addListener(new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                // 获取上一个媒体项目的播放位置
                long previousPosition = oldPosition.positionMs;
                //保存当前进度
                if (url==null&&moviepath==null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(String.valueOf(videoList.get(oldPosition.mediaItemIndex).getId()),previousPosition);
                    editor.apply();
                }
            }
            //当一个视频切换到另一个视频时触发
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                //更新UI
                updatetotalTime();

                //跳转进度
                long currentposition=sharedPreferences.getLong(String.valueOf(videoList.get(player.getCurrentMediaItemIndex()).getId()),0);
                if(currentposition!=0) {
                    player.seekTo(currentposition);
                    final View viewPos = findViewById(R.id.snack_location);
                    Snackbar.make(viewPos, "从头开始", Snackbar.LENGTH_LONG).setAction("确认", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            player.seekTo(0);
                        }
                    }).setActionTextColor(getColor(R.color.purple_200)).show();
                }
            }
        });

        //时间改变更新
        player.addListener(new Player.Listener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {
                updatetotalTime();
            }
        });

        //全屏手势
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) { //单击
                toggleControllersVisibility();
                return true;
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) { //双击
                if (player.isPlaying()) {
                    player.pause();
                    playButton.setImageResource(R.drawable.play_arrow_fill1_wght400_grad0_opsz48);
                } else {
                    player.play();
                    playButton.setImageResource(R.drawable.pause_fill1_wght400_grad0_opsz48);
                }
                return true;
            }
        });
        videoView.setOnTouchListener(new View.OnTouchListener() {
            private static final int THRESHOLD = 50;
            private static final int FACTOR = 10;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        slideDialog.dismiss();
                        if(operationType==3) player.seekTo(current);
                        operationType = 0;
                        break;
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        current = player.getCurrentPosition();
                        if(y<200) operationType=4;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(operationType==4) return true;
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        int disX = moveX - x;
                        int disY = moveY - y;

                        if (Math.abs(disX) < THRESHOLD && Math.abs(disY) < THRESHOLD) return true;

                        slideDialog.show();
                        if (operationType == 0) {
                            if (Math.abs(disX) > Math.abs(disY)) {
                                operationType = 3;
                            } else if (x < v.getWidth() / 2) {
                                operationType = 1;
                            } else {
                                operationType = 2;
                            }
                        }

                        slideDialog.SetVisible(operationType);
                        if (operationType == 1) {
                            // 左侧上滑改变亮度
                            changeBrightness(-disY);
                        } else if (operationType == 2) {
                            // 右侧上滑改变音量
                            changeVolume(-disY);
                        } else if (operationType == 3) {
                            current += (long) disX * FACTOR;
                            if (disX > 0) {
                                slideDialog.setSlideImage(R.drawable.fast_forward_fill1_wght400_grad0_opsz32);
                            } else if (disX < 0) {
                                slideDialog.setSlideImage(R.drawable.fast_rewind_fill1_wght400_grad0_opsz32);
                            }
                            if (current < 0) {
                                current = 0;
                            } else if (current > player.getDuration()) {
                                current = player.getDuration();
                            }
                            slideDialog.setSlideCurrentTime(formatTime((int) current));
                        }

                        x = moveX;
                        y = moveY;
                        break;
                }
                return true;
            }
        });

        // 设置 返回 按钮的点击监听器
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 设置 更多 按钮的点击监听器
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(PlayerActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.movie_menu, popupMenu.getMenu());
                // 设置菜单项的点击监听器
                MovieMenu movieMenu=new MovieMenu(PlayerActivity.this,player,checkedItem,videoList,audioSet,subtitleSet,trackSelector);
                popupMenu.setOnMenuItemClickListener(movieMenu);
                popupMenu.show();
            }
        });

        // 设置 Play 按钮的点击监听器
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                    playButton.setImageResource(R.drawable.play_arrow_fill1_wght400_grad0_opsz48);
                } else {
                    player.play();
                    playButton.setImageResource(R.drawable.pause_fill1_wght400_grad0_opsz48);
                }
            }
        });
        perfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.seekToPreviousMediaItem();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.seekToNextMediaItem();
            }
        });

        // 设置 SeekBar 的拖动监听器
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    /**
     * 改亮度
     */
    private void changeBrightness(int brightessValue) {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (lp.screenBrightness==0) { //获取真实值
            try {
                lp.screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS)/ 255.0f;
            } catch (Settings.SettingNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        lp.screenBrightness += brightessValue*0.001f;
        // 限制 lp.screenBrightness 的取值范围在 0 到 1 之间
        if (lp.screenBrightness < 0) {
            lp.screenBrightness = 0;
        } else if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        }
        window.setAttributes(lp);
        slideDialog.setSlideSeekBar((int) (lp.screenBrightness * 1000),1000);
    }

    /**
     * 改音量
     */
    private void changeVolume(int value) {
        // 获取音频管理器
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取当前音量
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxvolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 计算新的音量值
        volume += value*0.02;
        if(volume>=maxvolume) {
            volume=maxvolume;
        }
        else if(volume<=0){
            volume=0;
            slideDialog.SetVolumeOff();
        }
        // 设置新的音量值
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        slideDialog.setSlideSeekBar(volume*50,maxvolume*50);
    }

    @androidx.media3.common.util.UnstableApi
    public void prepare(int position,String url,String moviepath) {
        trackSelector = new DefaultTrackSelector(this);

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();

        videoView.setPlayer(player);

        //放入列表
        if(url!=null) {
            MediaItem mediaItem=MediaItem.fromUri(url);
            player.addMediaItem(mediaItem);
        } else if (moviepath!=null) {
            MediaItem mediaItem=MediaItem.fromUri(moviepath);
            player.addMediaItem(mediaItem);
        } else {
            List<MediaItem> mediaItems=new ArrayList<>();
            for(Video video:videoList) {
                MediaItem mediaItem=MediaItem.fromUri(video.getPath());
                mediaItems.add(mediaItem);
            }
            player.addMediaItems(mediaItems);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
        }

        //跳转进度
        long currentposition=sharedPreferences.getLong(String.valueOf(movieid),0);
        if(currentposition!=0) {
            player.seekTo(position,currentposition);
            if (!reminder) { //此处第一次调用reminder
                final View viewPos = findViewById(R.id.snack_location);
                Snackbar.make(viewPos, "从头开始", Snackbar.LENGTH_LONG).setAction("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        player.seekTo(0);
                    }
                }).setActionTextColor(getColor(R.color.purple_200)).show();
            }
        } else {
            player.seekTo(position,0);
        }

        //  准备播放
        player.prepare();

        //获取轨道信息
        player.addListener(new Player.Listener() {
            @Override
            public void onTracksChanged(@NonNull Tracks tracks) {
                Player.Listener.super.onTracksChanged(tracks);
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                        TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                        if (C.TRACK_TYPE_AUDIO == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
                            for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
                                TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
                                audioSet.add(trackGroup);
                            }
                        } else if (C.TRACK_TYPE_TEXT == mappedTrackInfo.getRendererType(i)) { //判断是否是字幕
                            for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
                                TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
                                subtitleSet.add(trackGroup);
                            }
                        }

                    }
                }
            }
        });
    }

    /**
     * 切换组件可视状态
     */
    private void toggleControllersVisibility() {
        if (controllersVisible) {
            topController.setVisibility(View.GONE);
            bottomController.setVisibility(View.GONE);
            controllersVisible = false;
        } else {
            topController.setVisibility(View.VISIBLE);
            bottomController.setVisibility(View.VISIBLE);
            controllersVisible = true;
            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    // 每隔一秒执行一次
                }

                public void onFinish() {
                    // 倒计时结束，执行代码
                    toggleControllersVisibilityOff();
                }
            }.start();
        }
    }
    private void toggleControllersVisibilityOff() {
        topController.setVisibility(View.GONE);
        bottomController.setVisibility(View.GONE);
        controllersVisible = false;
    }

    /**
     * 更新当前时长和SeekBar，并且设立handler以实时更新
     */
    private void updateSeekBar() {
        int currentPosition = (int) player.getCurrentPosition();
        seekBar.setProgress(currentPosition);
        currentTimeTextView.setText(formatTime(currentPosition));

        Runnable updater = new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
            }
        };
        handler.post(updater);
    }

    /**
     * 更新进度条总长和总时长
     */
    public void updatetotalTime() {
        long duration = player.getDuration();
        if (duration != C.TIME_UNSET) {
            seekBar.setMax((int) duration);
            totalTimeTextView.setText(formatTime((int) duration));
            updateSeekBar();
        }
    }

    /**
     * 规范化时长
     */
    private String formatTime(int timeInMilliseconds) {
        int seconds = timeInMilliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    /**
     * 初始化
     */
    public void init() {
        SharedPreferences spSet = getSharedPreferences("Settings", MODE_PRIVATE);
        tip=spSet.getBoolean("tip",true);

        // 隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        videoView=findViewById(R.id.video);
        playButton = findViewById(R.id.playButton);
        backButton = findViewById(R.id.backButton);
        moreButton=findViewById(R.id.moreButton);
        nextButton=findViewById(R.id.nextButton);
        perfButton=findViewById(R.id.pervButton);
        seekBar = findViewById(R.id.seekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        topController=findViewById(R.id.top_controller);
        bottomController=findViewById(R.id.buttom_controller);
        slideDialog=new SlideDialog(this);
        sharedPreferences = getSharedPreferences("Playing", MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tip) lightSensorUtils.registerLight();
    }

    /**
     * 使视频立刻暂停
     */
    @Override
    public void onPause() {
        super.onPause();
        if(tip) lightSensorUtils.unregisterLight();
        if(url==null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if(moviepath!=null) {
                editor.putLong(String.valueOf(movieid),player.getCurrentPosition()); //保存当前进度
            } else {
                editor.putLong(String.valueOf(videoList.get(player.getCurrentMediaItemIndex()).getId()),player.getCurrentPosition()); //保存当前进度
            }
            if (player.isPlaying()) {
                editor.putBoolean("play_situation", true);
                player.pause();
                playButton.setImageResource(R.drawable.play_arrow_fill1_wght400_grad0_opsz48);
            } else {
                editor.putBoolean("play_situation", false);
            }
            editor.apply();
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        // 恢复数据
        boolean playSituation = sharedPreferences.getBoolean("play_situation", false);
        if (playSituation) {
            player.play();
            playButton.setImageResource(R.drawable.pause_fill1_wght400_grad0_opsz48);
        }
    }

    /**
     * 横竖屏切换，返回桌面时储存数据
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("reminder",true); //已提醒
        super.onSaveInstanceState(outState);
    }

    @Override
    public void finish() {
        super.finish();
        player.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
