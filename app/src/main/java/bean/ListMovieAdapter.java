package bean;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListMovieAdapter extends RecyclerView.Adapter<ListMovieAdapter.MyViewHolder>{
    private Context context;
    private boolean list_horizontal_layout;
    private List<Video> list;
    private View inflater;
    private OnListItemLongClickListener longListener;
    //构造方法，传入数据,即把展示的数据源传进来，并且复制给一个全局变量，以后的操作都在该数据源上进行
    public ListMovieAdapter(Context context, List<Video> list,boolean list_horizontal_layout,OnListItemLongClickListener longListener){
        this.context = context;
        this.list = list;
        this.list_horizontal_layout=list_horizontal_layout;
        this.longListener=longListener;
    }
    public interface OnListItemLongClickListener {
        void OnItemLongClick(Video video);
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
                    int position = myViewHolder.getAdapterPosition();
                    //传递
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("movie_position",position);
                    intent.putExtra("movie_id",list.get(position).getId());
                    intent.putExtra("movie_video_list",(Serializable) list);
                    context.startActivity(intent);
                }
            });
            myViewHolder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = myViewHolder.getAdapterPosition();
                    // 传递
                    if (longListener != null) {
                        longListener.OnItemLongClick(list.get(position));
                    }
                    return true;
                }
            });
            return myViewHolder;
        }
        else {
            inflater = LayoutInflater.from(context).inflate(R.layout.item_list_movie_grid,parent,false);
            MyViewHolder myViewHolder = new MyViewHolder(inflater);
            myViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() { //点击事件
                @Override
                public void onClick(View v) {
                    int position = myViewHolder.getAdapterPosition();
                    //传递
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("movie_position",position);
                    intent.putExtra("movie_id",list.get(position).getId());
                    intent.putExtra("movie_video_list",(Serializable) list);
                    context.startActivity(intent);
                }
            });
            myViewHolder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = myViewHolder.getAdapterPosition();
                    // 传递
                    if (longListener != null) {
                        longListener.OnItemLongClick(list.get(position));
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
        holder.imageView.setImageBitmap(MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), list.get(position).getId(), MediaStore.Video.Thumbnails.MINI_KIND, null));
    }
    //getItemCount()告诉RecyclerView一共有多少个子项，直接返回数据源的长度。
    @Override
    public int getItemCount() {
        //返回Item总条数
        return list.size();
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