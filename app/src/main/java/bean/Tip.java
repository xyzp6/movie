package bean;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

public class Tip {
    public static void showGreeting(Context context) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 6) {
            Toast.makeText(context, "凌晨的风，吹过了昨夜的忧伤，也吹来了今天的希望。", Toast.LENGTH_SHORT).show();
        } else if (hour < 11) {
            Toast.makeText(context, "生活原本沉闷，但跑起来就有风，早安。", Toast.LENGTH_SHORT).show();
        } else if (hour < 13) {
            Toast.makeText(context, "中午好！一份牵挂，一份祝福送给在乎的人，愿你一生幸福。", Toast.LENGTH_SHORT).show();
        } else if (hour < 18) {
            Toast.makeText(context, "下午好，愿你的心情像这个下午一样温暖。", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "寂静的夜空，送你一堆好梦。", Toast.LENGTH_SHORT).show();
        }
    }

}
