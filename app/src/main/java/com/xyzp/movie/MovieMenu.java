package com.xyzp.movie;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.PopupMenu;

import androidx.media3.common.C;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import bean.Video;

public class MovieMenu implements PopupMenu.OnMenuItemClickListener {
    private final PlayerActivity playerActivity;
    private final ExoPlayer player;
    private final List<Video> list;
    private final Set<TrackGroup> audioSet,subtitleSet;
    private final DefaultTrackSelector trackSelector;
    private int checkedItem,CurrentMediaItemIndex;
    private TrackSelectionOverride trackSelectionOverride=null;
    public MovieMenu(PlayerActivity playerActivity,ExoPlayer player,int checkedItem,List<Video> list,Set<TrackGroup> audioSet,Set<TrackGroup> subtitleSet,DefaultTrackSelector trackSelector) {
        this.playerActivity = playerActivity;
        this.player=player;
        this.checkedItem=checkedItem;
        this.list=list;
        this.audioSet=audioSet;
        this.subtitleSet=subtitleSet;
        this.trackSelector=trackSelector;
        CurrentMediaItemIndex=player.getCurrentMediaItemIndex();
    }

    @Override
    @androidx.media3.common.util.UnstableApi
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
            int i=0;
            TrackGroup[] items = new TrackGroup[audioSet.size()];
            String[] Stringitems = new String[audioSet.size()];
            for (TrackGroup trackGroup : audioSet) {
                items[i]=trackGroup;
                Stringitems[i]=trackGroup.getFormat(0).label;
                if (Stringitems[i]==null) Stringitems[i]="单一轨道";
                i++;
            }

            // 加载自定义布局
            View dialogView = LayoutInflater.from(playerActivity).inflate(R.layout.dialog_speed, null);
            NumberPicker numberPicker = dialogView.findViewById(R.id.dialog_number_picker);

            // 设置 NumberPicker 的取值范围和初始值
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(items.length);
            numberPicker.setValue(1);

            // 设置 NumberPicker 的显示内容
            numberPicker.setDisplayedValues(Stringitems);

            new MaterialAlertDialogBuilder(playerActivity)
                    .setTitle("音频轨道")
                    .setView(dialogView)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            trackSelectionOverride=new TrackSelectionOverride(items[numberPicker.getValue()-1],0);
                            //修改
                            trackSelector.setParameters(
                                    trackSelector.getParameters().buildUpon()
                                            .setOverrideForType(trackSelectionOverride));
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
            int i=0;
            TrackGroup[] items = new TrackGroup[subtitleSet.size()];
            String[] Stringitems = new String[subtitleSet.size()];
            for (TrackGroup trackGroup : subtitleSet) {
                items[i]=trackGroup;
                Stringitems[i]=trackGroup.getFormat(0).label;
                i++;
            }

            // 加载自定义布局
            View dialogView = LayoutInflater.from(playerActivity).inflate(R.layout.dialog_speed, null);
            NumberPicker numberPicker = dialogView.findViewById(R.id.dialog_number_picker);

            // 设置 NumberPicker 的取值范围和初始值
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(items.length);
            numberPicker.setValue(1);

            // 设置 NumberPicker 的显示内容
            if (Stringitems.length==0) {
                String[] nullStringitems=new String[1];
                nullStringitems[0]="无字幕";
                numberPicker.setDisplayedValues(nullStringitems);
            } else {
                numberPicker.setDisplayedValues(Stringitems);
            }

            new MaterialAlertDialogBuilder(playerActivity)
                    .setTitle("字幕轨道")
                    .setView(dialogView)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            trackSelectionOverride=new TrackSelectionOverride(items[numberPicker.getValue()-1],0);
                            //修改
                            trackSelector.setParameters(
                                    trackSelector.getParameters().buildUpon()
                                            .setOverrideForType(trackSelectionOverride));
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