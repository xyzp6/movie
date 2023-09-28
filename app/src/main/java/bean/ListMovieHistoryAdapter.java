package bean;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.xyzp.movie.MainActivity;
import com.xyzp.movie.PlayerActivity;
import com.xyzp.movie.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ListMovieHistoryAdapter extends RecyclerView.Adapter<ListMovieHistoryAdapter.MyViewHolder>{
    private final Context context;
    private final VideoProvider videoProvider;
    private final List<String> idlist=new ArrayList<>();
    private final List<Long> timelist=new ArrayList<>();
    private final List<String> titlelist=new ArrayList<>();
    private View inflater;
    //构造方法，传入数据,即把展示的数据源传进来，并且复制给一个全局变量，以后的操作都在该数据源上进行
    public ListMovieHistoryAdapter(Context context,VideoProvider videoProvider){
        this.context = context;
        this.videoProvider=videoProvider;
        SharedPreferences sharedPreferences = context.getSharedPreferences("Playing", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Long) {
                String key = entry.getKey();
                long value = (Long) entry.getValue();
                if(value!=0) {
                    String title=videoProvider.getNameFromId(Integer.parseInt(key));
                    if (!Objects.equals(title, "")) {
                        idlist.add(key);
                        timelist.add(value);
                        titlelist.add(title);
                    }
                }
            }
        }
    }
    //由于RecycleAdapterDome继承自RecyclerView.Adapter,则必须重写onCreateViewHolder()，onBindViewHolder()，getItemCount()
    //onCreateViewHolder()方法用于创建ViewHolder实例，我们在这个方法将item_list_movie.xml布局加载进来
    //然后创建一个ViewHolder实例，并把加载出来的布局传入到构造函数，最后将实例返回
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.item_list_movie_history,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(inflater);
        myViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() { //点击事件
            @Override
            public void onClick(View v) {
                int position = myViewHolder.getBindingAdapterPosition();
                int id=Integer.parseInt(idlist.get(position));
                //传递
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("movie_id",id);
                intent.putExtra("movie_path",videoProvider.getPathFromId(id));
                context.startActivity(intent);
            }
        });
        myViewHolder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = myViewHolder.getBindingAdapterPosition();
                int id=Integer.parseInt(idlist.get(position));
                // 加载自定义布局
                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_history_info, null);
                TextView tvNowTime=dialogView.findViewById(R.id.info_now_time_value);
                TextView tvTotalTime=dialogView.findViewById(R.id.info_total_time_value);
                tvNowTime.setText(formatTime(timelist.get(position)));
                tvTotalTime.setText(formatTime(videoProvider.getDurationFromId(id)));

                AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                        .setTitle(titlelist.get(position))
                        .setView(dialogView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPreferences = context.getSharedPreferences("Playing", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove(String.valueOf(id));
                                editor.apply();
                                idlist.remove(position);
                                timelist.remove(position);
                                titlelist.remove(position);
                                notifyItemRemoved(position);
                            }
                        })
                        .create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        // 设置按钮的字体颜色
                        button.setTextColor(Color.RED);  // 将颜色更改为红色
                    }
                });
                dialog.show();
                return false;
            }
        });
        return myViewHolder;
    }
    //onBindViewHolder()方法用于对RecyclerView子项数据进行赋值，会在每个子项被滚动到屏幕内的时候执行
    //这里我们通过position参数的得到当前项的实例，然后将数据设置到ViewHolder的TextView即可
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //将数据和控件绑定
        int id=Integer.parseInt(idlist.get(position));
        holder.nametextView.setText(titlelist.get(position));
        holder.timetextView.setText(formatTime(timelist.get(position)));

        // 在后台线程中加载真正的图像
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                        context.getContentResolver(),
                        id,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null);

                // 在主线程中更新 ImageView
                holder.imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        holder.imageView.setImageBitmap(thumbnail);
                    }
                });
            }
        }).start();
    }
    //getItemCount()告诉RecyclerView一共有多少个子项，直接返回数据源的长度。
    @Override
    public int getItemCount() {
        //返回Item总条数
        return idlist.size();
    }

    public void removeAllItems() { //清空item
        SharedPreferences sharedPreferences = context.getSharedPreferences("Playing", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        idlist.clear();
        timelist.clear();
        titlelist.clear();
        notifyDataSetChanged();
    }

    //内部类，绑定控件
    class MyViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout relativeLayout;
        TextView nametextView,timetextView;
        ImageView imageView;
        public MyViewHolder(View itemView) {//这个view参数就是recyclerview子项的最外层布局
            super(itemView);
            relativeLayout=(RelativeLayout) itemView.findViewById(R.id.item_history);
            nametextView = (TextView) itemView.findViewById(R.id.history_name);
            timetextView=(TextView) itemView.findViewById(R.id.history_current_time);
            imageView = (ImageView) itemView.findViewById(R.id.history_picture);
        }
    }

    /**
     * 规范化时长
     */
    private String formatTime(long timeInMilliseconds) {
        long seconds = timeInMilliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}