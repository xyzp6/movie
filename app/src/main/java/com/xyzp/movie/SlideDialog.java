package com.xyzp.movie;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


public class SlideDialog extends Dialog {
    private ImageView slideImageView;
    private TextView slideCurrentTimeTextView;
    private SeekBar slideSeekBar;

    public SlideDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_slide);
        slideImageView = findViewById(R.id.slide_imageview);
        slideCurrentTimeTextView = findViewById(R.id.slide_currentTimeTextView);
        slideSeekBar=findViewById(R.id.slide_seekbar);

        //去除半透明阴影
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = 0.0f;
        getWindow().setAttributes(layoutParams);
    }

    public void setSlideImage(int id) {
        slideImageView.setImageResource(id);
    }
    public void setSlideSeekBar(int progress,int max) {
        slideSeekBar.setProgress(progress);
        slideSeekBar.setMax(max);
    }
    public void setSlideCurrentTime(String text) {
        slideCurrentTimeTextView.setText(text);
    }

    //根据不同需求调节界面
    public void SetVisible(int operationType) {
        if(operationType==1) { //亮度
            slideCurrentTimeTextView.setVisibility(View.GONE);
            slideSeekBar.setVisibility(View.VISIBLE);
            slideImageView.setImageResource(R.drawable.light_mode_fill1_wght400_grad0_opsz32_white);
        }
        else if(operationType==2) { //音量
            slideCurrentTimeTextView.setVisibility(View.GONE);
            slideSeekBar.setVisibility(View.VISIBLE);
            slideImageView.setImageResource(R.drawable.volume_up_fill1_wght400_grad0_opsz32);
        }
        else if (operationType==3) { //进度
            slideCurrentTimeTextView.setVisibility(View.VISIBLE);
            slideSeekBar.setVisibility(View.GONE);
        }
    }

    public void SetVolumeOff() {
        slideImageView.setImageResource(R.drawable.volume_off_fill1_wght400_grad0_opsz32);
    }
}
