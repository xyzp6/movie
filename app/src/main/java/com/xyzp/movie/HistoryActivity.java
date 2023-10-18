package com.xyzp.movie;

import static com.xyzp.movie.MainActivity.DELETE_VIDEO_CODE;
import static bean.DatabaseBackup.REQUEST_CODE_CREATE_FILE;
import static bean.DatabaseBackup.REQUEST_CODE_OPEN_FILE;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import bean.DatabaseBackup;
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
        materialToolbar.setOnMenuItemClickListener(
                menuItem -> {
                    if (menuItem.getItemId() == R.id.history_imports) { //导入
                        DatabaseBackup.restore(this);
                    } else if (menuItem.getItemId() == R.id.history_exports) { //导出
                        DatabaseBackup.backup(this);
                    } else if (menuItem.getItemId() == R.id.history_delete) { //删除
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("确认全部删除")
                                .setMessage("警告！此操作会清空所有历史记录且不可以逆！（不影响本地备份）")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        listMovieHistoryAdapter.removeAllItems();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setNeutralButton("备份", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DatabaseBackup.backup(HistoryActivity.this);
                                    }
                                })
                                .show();
                    }
                    return true;
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

    /**
     * 回调数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_FILE) {
            DatabaseBackup.onActivityResult(this, requestCode, resultCode, data);
            if (resultCode==RESULT_OK) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "取消保存", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_OPEN_FILE) {
            DatabaseBackup.onActivityResult(this, requestCode, resultCode, data);
            if (resultCode==RESULT_OK) {
                init_data();
                Toast.makeText(this, "读取成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "取消读取", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
