package com.xyzp.movie;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.PopupMenu;

import androidx.media3.common.PlaybackParameters;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import bean.Video;

public class MovieMenu implements PopupMenu.OnMenuItemClickListener {
    private final PlayerActivity playerActivity;
    private final ExoPlayer player;
    private int checkedItem,CurrentMediaItemIndex;
    private List<Video> list;
    private List<String> audioList,subtitlelist;
    private DefaultTrackSelector trackSelector;
    public MovieMenu(PlayerActivity playerActivity,ExoPlayer player,int checkedItem,List<Video> list,List<String> audioList,List<String> subtitlelist,DefaultTrackSelector trackSelector) {
        this.playerActivity = playerActivity;
        this.player=player;
        this.checkedItem=checkedItem;
        this.list=list;
        this.audioList=audioList;
        this.subtitlelist=subtitlelist;
        this.trackSelector=trackSelector;
        CurrentMediaItemIndex=player.getCurrentMediaItemIndex();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.speed) {
            // 处理速度菜单项的点击事件
            String[] items = {"0.5x", "1x", "2x", "5x"};
            float[] items_num = {0.5F, 1, 2, 5};

            // 加载自定义布局
            View dialogView = LayoutInflater.from(playerActivity).inflate(R.layout.dialog_speed, null);
            NumberPicker numberPicker = dialogView.findViewById(R.id.dialog_number_picker);

            // 设置 NumberPicker 的取值范围和初始值
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(items.length);
            numberPicker.setValue(checkedItem);

            // 设置 NumberPicker 的显示内容
            numberPicker.setDisplayedValues(items);

            // 设置 NumberPicker 的滚动监听器
            numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    // 处理选项的滚动事件
                    // newVal 参数表示当前选中的选项的索引 + 1
                    checkedItem = newVal;
                }
            });

            new MaterialAlertDialogBuilder(playerActivity)
                    .setTitle("倍速")
                    .setView(dialogView)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //修改倍速
                            PlaybackParameters playbackParameters = new PlaybackParameters(items_num[checkedItem - 1]);
                            player.setPlaybackParameters(playbackParameters);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 处理取消按钮的点击事件
                        }
                    })
                    .show();
        } else if(id==R.id.movie_item) {
            // 处理菜单项的点击事件
            String[] items = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                items[i] = list.get(i).getTitle();
            }

            // 加载自定义布局
            View dialogView = LayoutInflater.from(playerActivity).inflate(R.layout.dialog_speed, null);
            NumberPicker numberPicker = dialogView.findViewById(R.id.dialog_number_picker);

            // 设置 NumberPicker 的取值范围和初始值
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(items.length);
            numberPicker.setValue(CurrentMediaItemIndex+1);

            // 设置 NumberPicker 的显示内容
            numberPicker.setDisplayedValues(items);

            new MaterialAlertDialogBuilder(playerActivity)
                    .setTitle("列表")
                    .setView(dialogView)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //修改
                            CurrentMediaItemIndex=numberPicker.getValue();
                            player.seekTo(CurrentMediaItemIndex-1,0);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 处理取消按钮的点击事件
                        }
                    })
                    .show();
        } else if(id==R.id.audio_track) {
            // 处理菜单项的点击事件
            String[] items = new String[audioList.size()];
            for (int i = 0; i < audioList.size(); i++) {
                items[i] = audioList.get(i);
            }

            // 加载自定义布局
            View dialogView = LayoutInflater.from(playerActivity).inflate(R.layout.dialog_speed, null);
            NumberPicker numberPicker = dialogView.findViewById(R.id.dialog_number_picker);

            // 设置 NumberPicker 的取值范围和初始值
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(items.length);
            numberPicker.setValue(1);

            // 设置 NumberPicker 的显示内容
            numberPicker.setDisplayedValues(items);

            new MaterialAlertDialogBuilder(playerActivity)
                    .setTitle("音频轨道")
                    .setView(dialogView)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //修改
                            trackSelector.setParameters(
                                    trackSelector.getParameters().buildUpon()
                                            .setPreferredAudioLanguage(items[numberPicker.getValue()-1]));
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 处理取消按钮的点击事件
                        }
                    })
                    .show();
        } else if(id==R.id.subtitle_track) {
            // 处理菜单项的点击事件
            String[] items = new String[subtitlelist.size()];
            for (int i = 0; i < subtitlelist.size(); i++) {
                items[i] = subtitlelist.get(i);
            }

            // 加载自定义布局
            View dialogView = LayoutInflater.from(playerActivity).inflate(R.layout.dialog_speed, null);
            NumberPicker numberPicker = dialogView.findViewById(R.id.dialog_number_picker);

            // 设置 NumberPicker 的取值范围和初始值
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(items.length);
            numberPicker.setValue(1);

            // 设置 NumberPicker 的显示内容
            numberPicker.setDisplayedValues(items);

            new MaterialAlertDialogBuilder(playerActivity)
                    .setTitle("字幕轨道")
                    .setView(dialogView)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //修改
                            trackSelector.setParameters(
                                    trackSelector.getParameters().buildUpon()
                                            .setPreferredTextLanguage(items[numberPicker.getValue()-1]));
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 处理取消按钮的点击事件
                        }
                    })
                    .show();
        }
        return true;
    }
}