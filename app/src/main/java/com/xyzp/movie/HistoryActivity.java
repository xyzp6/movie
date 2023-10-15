package com.xyzp.movie;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;

import bean.ListMovieHistoryAdapter;
import bean.StatusBar;
import bean.VideoProvider;

public class HistoryActivity  extends AppCompatActivity {
    private MaterialToolbar materialToolbar;
    private RecyclerView recyclerView;
    private VideoProvider provider;
    private ListMovieHistoryAdapter listMovieHistoryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置颜色为半透明
        StatusBar statusBar = new StatusBar(HistoryActivity.this);
        statusBar.setColor(R.color.transparent);
        if (this.getApplicationContext().getResources().getConfiguration().uiMode == 0x21) { //深色
            statusBar.setTextColor(true);
        } else if (this.getApplicationContext().getResources().getConfiguration().uiMode == 0x11) { //浅色
            statusBar.setTextColor(false);
        }

        //material 3取色，安卓12起
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_history);
        init();
        init_data();

        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void init() {
        recyclerView=findViewById(R.id.history_list);
        materialToolbar=findViewById(R.id.history_top_MaterialToolbar);

        provider=new VideoProvider(HistoryActivity.this);
    }

    /**
     * 初始化数据
     */
    public void init_data() {
        listMovieHistoryAdapter=new ListMovieHistoryAdapter(HistoryActivity.this, provider,-1);
        //创建线性布局
        LinearLayoutManager manager = new LinearLayoutManager(HistoryActivity.this);
        //水平方向
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        //给RecyclerView设置布局管理器
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(listMovieHistoryAdapter);
    }
}
