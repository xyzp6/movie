package bean;

import android.app.Activity;
import android.view.View;

public class StatusBar {
    private Activity activity;

    //初始化activity
    public StatusBar(Activity activity){
        this.activity = activity;
    }

    //将状态栏设置为传入的color
    public void setColor(int color){
        View view = activity.getWindow().getDecorView();
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        activity.getWindow().setStatusBarColor(activity.getResources().getColor(color));
    }

    //设置状态栏字体颜色
    public void setTextColor(boolean isDarkBackground){
        View decor = activity.getWindow().getDecorView();
        if (isDarkBackground) {
            //黑暗背景字体浅色
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            //高亮背景字体深色
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

}
