package bean;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.xyzp.movie.PlayerActivity;
import com.xyzp.movie.R;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListMovieAdapter extends RecyclerView.Adapter<ListMovieAdapter.MyViewHolder>{
    private Context context;
    private int cnt=0;
    private boolean list_horizontal_layout;
    private List<Video> list;
    private List<Boolean> selectedStates;
    private View inflater;
    private OnItemClickListener listener;
    private OnListItemLongClickListener longListener;
    //构造方法，传入数据,即把展示的数据源传进来，并且复制给一个全局变量，以后的操作都在该数据源上进行
    public ListMovieAdapter(){}
    public ListMovieAdapter(Context context, List<Video> list, boolean list_horizontal_layout, OnItemClickListener listener, OnListItemLongClickListener longListener){
        this.context = context;
        this.list = list;
        this.list_horizontal_layout=list_horizontal_layout;
        this.listener=listener;
        this.longListener=longListener;
        this.selectedStates = new ArrayList<>(Collections.nCopies(list.size(), false));
    }
    public interface OnItemClickListener {
        void OnItemClick(List<Video> list ,int position);
    }
    public interface OnListItemLongClickListener {
        void OnItemLongClick(List<Video> list ,int position);
    }
    //由于RecycleAdapterDome继承自RecyclerView.Adapter,则必须重写onCreateViewHolder()，onBindViewHolder()，getItemCount()
    //onCreateViewHolder()方法用于创建ViewHolder实例，我们在这个方法将item_list_movie.xml布局加载进来
    //然后创建一个ViewHolder实例，并把加载出来的布局传入到构造函数，最后将实例返回
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
        if (list_horizontal_layout) {
            inflater = LayoutInflater.from(context).inflate(R.layout.item_list_movie,parent,false);
            MyViewHolder myViewHolder = new MyViewHolder(inflater);
            myViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() { //点击事件
                @Override
                public void onClick(View v) {
                    int position = myViewHolder.getBindingAdapterPosition();
                    //传递
                    if(listener!=null) {
                        listener.OnItemClick(list,position);
                        if(cnt!=0) { //有选中项
                            if (selectedStates.get(position)) {
                                selectedStates.set(position,false);
                                cnt--;
                            } else {
                                selectedStates.set(position,true);
                                cnt++;
                            }
                        }
                    }
                }
            });
            myViewHolder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = myViewHolder.getBindingAdapterPosition();
                    // 传递
                    if (longListener != null) {
                        longListener.OnItemLongClick(list,position);
                        if (selectedStates.get(position)) {
                            selectedStates.set(position,false);
                            cnt--;
                        } else {
                            selectedStates.set(position,true);
                            cnt++;
                        }
                    }
                    return true;
                }
            });
            return myViewHolder;
        } else {
            inflater = LayoutInflater.from(context).inflate(R.layout.item_list_movie_grid,parent,false);
            MyViewHolder myViewHolder = new MyViewHolder(inflater);
            myViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() { //点击事件
                @Override
                public void onClick(View v) {
                    int position = myViewHolder.getBindingAdapterPosition();
                    //传递
                    if(listener!=null) {
                        listener.OnItemClick(list,position);
                        if(cnt!=0) { //有选中项
                            if (selectedStates.get(position)) {
                                selectedStates.set(position,false);
                                cnt--;
                            } else {
                                selectedStates.set(position,true);
                                cnt++;
                            }
                        }
                    }
                }
            });
            myViewHolder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = myViewHolder.getBindingAdapterPosition();
                    // 传递
                    if (longListener != null) {
                        longListener.OnItemLongClick(list,position);
                        if (selectedStates.get(position)) {
                            selectedStates.set(position,false);
                            cnt--;
                        } else {
                            selectedStates.set(position,true);
                            cnt++;
                        }
                    }
                    return true;
                }
            });
            return myViewHolder;
        }
    }
    //onBindViewHolder()方法用于对RecyclerView子项数据进行赋值，会在每个子项被滚动到屏幕内的时候执行
    //这里我们通过position参数的得到当前项的实例，然后将数据设置到ViewHolder的TextView即可
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //将数据和控件绑定
        holder.textView.setText(list.get(position).getTitle());

        // 根据选中状态设置背景颜色
        if (selectedStates.get(position)) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // 在后台线程中加载真正的图像
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                            context.getContentResolver(),
                            list.get(holder.getLayoutPosition()).getId(),
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
        return list.size();
    }

    public void resetSelectedStates() { //返回时重置选中状态
        for (int i = 0; i < selectedStates.size(); i++) {
            if (selectedStates.get(i)) {
                selectedStates.set(i, false);
                notifyItemChanged(i);
            }
        }
        cnt=0;
    }

    public void removeSelectedItems() {
        for (int i = selectedStates.size() - 1; i >= 0; i--) {
            if (selectedStates.get(i)) {
                // 从列表中移除item
                list.remove(i);
                selectedStates.remove(i);
                // 通知adapter更新
                notifyItemRemoved(i);
            }
        }
    }


    //内部类，绑定控件
    class MyViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout relativeLayout;
        LinearLayout linearLayout;
        TextView textView;
        ImageView imageView;
        public MyViewHolder(View itemView) {//这个view参数就是recyclerview子项的最外层布局
            super(itemView);
            if (list_horizontal_layout) relativeLayout=(RelativeLayout) itemView.findViewById(R.id.item_list);
            else linearLayout=(LinearLayout) itemView.findViewById(R.id.item_list);
            textView = (TextView) itemView.findViewById(R.id.item_list_name);
            imageView = (ImageView) itemView.findViewById(R.id.item_list_picture);
        }
    }
}