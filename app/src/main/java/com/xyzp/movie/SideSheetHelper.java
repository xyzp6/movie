package com.xyzp.movie;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.sidesheet.SideSheetBehavior;

import java.util.List;

import bean.Video;

public class SideSheetHelper {
    private Context context;
    private SideSheetBehavior<View> sideSheetBehavior;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private LinearLayout linearLayout;
    private View sideSheetOverlay;
    private ImageView closeIcon;
    private TextView title,details;

    public SideSheetHelper(Activity activity) {
        // 获取 SideSheet 的实例
        context=activity;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            sideSheetBehavior = SideSheetBehavior.from(activity.findViewById(R.id.standard_side_sheet));
        } else {
            //竖屏
            linearLayout=activity.findViewById(R.id.standard_side_sheet);
            bottomSheetBehavior = BottomSheetBehavior.from(activity.findViewById(R.id.standard_side_sheet));
        }
        closeIcon = activity.findViewById(R.id.slide_sheet_close);
        title=activity.findViewById(R.id.slide_sheet_title);
        details=activity.findViewById(R.id.slide_sheet_details);
        sideSheetOverlay = activity.findViewById(R.id.side_sheet_overlay);

        // 为 closeIcon 内部的组件添加点击事件监听器
        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击时关闭 SideSheet
                hideSideSheet();
            }
        });

        // 为 sideSheetOverlay 添加点击事件监听器
        sideSheetOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击时关闭 SideSheet
                hideSideSheet();
            }
        });
    }

    public void showSideSheet(String folder_path_data, List<Video> list) {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            sideSheetBehavior.setState(SideSheetBehavior.STATE_EXPANDED);
        } else {
            //竖屏
            linearLayout.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        sideSheetOverlay.setVisibility(View.VISIBLE);
        title.setText(folder_path_data);
        details.setText("视频数量:"+list.size());
    }
    public void showSideSheet(Video video) {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            sideSheetBehavior.setState(SideSheetBehavior.STATE_EXPANDED);
        } else {
            //竖屏
            linearLayout.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        sideSheetOverlay.setVisibility(View.VISIBLE);
        title.setText(video.getTitle());
        String detailsText = context.getString(R.string.video_details, video.getId(), video.getTitle(), video.getAlbum(), video.getArtist(), video.getDisplayName(), video.getMimeType(), video.getPath(), video.getSize(), video.getDuration(), video.getFolderName());
        details.setText(detailsText);
    }

    public void hideSideSheet() {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            sideSheetBehavior.setState(SideSheetBehavior.STATE_HIDDEN);
        } else {
            //竖屏
            linearLayout.setVisibility(View.GONE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        sideSheetOverlay.setVisibility(View.GONE);
    }
}
