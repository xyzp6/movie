package com.xyzp.movie;


import static bean.DatabaseBackup.REQUEST_CODE_CREATE_FILE;
import static bean.DatabaseBackup.REQUEST_CODE_OPEN_FILE;
import static bean.Tip.showGreeting;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bean.FolderAdapter;
import bean.ListMovieAdapter;
import bean.ListMovieHistoryAdapter;
import bean.DatabaseBackup;
import bean.StatusBar;
import bean.Video;
import bean.VideoProvider;

public class MainActivity extends AppCompatActivity {
    public static final int DELETE_VIDEO_CODE=4;
    private ImageView mainbg;
    private SearchView mainsearchview;
    private SearchBar mainsearchbar;
    private Button historyallbt;
    private LinearProgressIndicator mainsearchlinearprogress;
    private FloatingActionButton fabonline;
    private List<Video> searchlist,movielist,selectvideo=new ArrayList<>();
    private ActivityResultLauncher<String> requestReadPermissionLauncher;
    private RecyclerView recyclerView,searchrecyclerView,historyrecyclerView;
    private RelativeLayout history,searchRelativeLayout;
    private NavigationRailView navigationRailView;
    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar historymaterialToolbar;
    private String folder_path,orientation="auto";
    private VideoProvider provider;
    private SharedPreferences sharedPreferences;
    private FolderAdapter folderAdapter;
    private ListMovieAdapter listMovieAdapter;
    private ListMovieHistoryAdapter listMovieHistoryAdapter;
    private boolean list_horizontal_layout,searchstatus; //布局方式，默认水平;搜索状态，false为未在布局中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置颜色为半透明
        StatusBar statusBar = new StatusBar(MainActivity.this);
        statusBar.setColor(R.color.transparent);
        if(this.getApplicationContext().getResources().getConfiguration().uiMode == 0x21) { //深色
            statusBar.setTextColor(true);
        } else if (this.getApplicationContext().getResources().getConfiguration().uiMode == 0x11) { //浅色
            statusBar.setTextColor(false);
        }

        //material 3取色，安卓12起
        DynamicColors.applyToActivityIfAvailable(this);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            setContentView(R.layout.activity_main);
            navigationRailView = findViewById(R.id.navigation_rail);
            setItemSelectedListener(navigationRailView);
        } else {
            //竖屏
            setContentView(R.layout.activity_main_vertical);
            bottomNavigationView = findViewById(R.id.navigation_bottom);
            setItemSelectedListener(bottomNavigationView);
        }

        //获取传递的值
        Intent intentfp = getIntent();
        folder_path = intentfp.getStringExtra("folder_path");
        if (savedInstanceState != null) {
            folder_path = savedInstanceState.getString("folder_path");
        }

        //权限申请回报
        requestReadPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean granted) {
                        if (granted) { //用户同意，此处为每次activity启动时的绘制
                            //视频数据初始化，防止多次运行造成性能浪费
                            provider = new VideoProvider(MainActivity.this);

                            //存入数据
                            init_data();
                        } else { //用户拒绝
                            Toast.makeText(MainActivity.this, "请授予读取权限", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        init();
        ReadPermission();

        //设置主页背景
        try {
            // 创建一个指向应用缓存目录的文件对象
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, "bg.png");

            // 检查图片是否存在
            if (imageFile.exists()) {
                // 从文件中读取位图数据
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                // 使用位图数据
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                mainbg.setImageDrawable(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置提示
        if (sharedPreferences.getBoolean("tip",true)) {
            GlobalApplication application = (GlobalApplication) getApplication();
            if (!application.hasShownGreeting) {
                showGreeting(this);
                application.hasShownGreeting = true;
            }
        }
        orientation=sharedPreferences.getString("orientation","auto");
        //设置暗色模式
        String theme=sharedPreferences.getString("theme","auto");
        if (theme.equals("auto")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else if (theme.equals("bright")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        //搜索框
        mainsearchbar.inflateMenu(R.menu.searchbar_menu);
        mainsearchbar.setOnMenuItemClickListener(
                menuItem -> {
                    if (menuItem.getItemId() == R.id.searchbar_menu_settings) {
                        Intent intent = new Intent(this, SettingsActivity.class);
                        startActivity(intent);
                    } else if (menuItem.getItemId()==R.id.searchbar_menu_layout) {
                        // 加载自定义布局
                        View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_main_layout, null);
                        TabLayout tabLayout = dialogView.findViewById(R.id.dialog_main_layout_TabLayout);
                        if(list_horizontal_layout) Objects.requireNonNull(tabLayout.getTabAt(1)).select();
                        else Objects.requireNonNull(tabLayout.getTabAt(0)).select();

                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("布局")
                                .setView(dialogView)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        list_horizontal_layout= tabLayout.getSelectedTabPosition() == 1;
                                        init_data();
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("list_horizontal_layout", list_horizontal_layout);
                                        editor.apply();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 处理取消按钮的点击事件
                                    }
                                })
                                .show();
                    } else if (menuItem.getItemId()==R.id.searchbar_menu_delete) {
                        List<Uri> uris=new ArrayList<>();
                        for (Video video:selectvideo) {
                            uris.add(Uri.parse(video.getUri()));
                        }
                        PendingIntent pendingIntent = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            pendingIntent = MediaStore.createDeleteRequest(getContentResolver(), uris);
                        }
                        try {
                            startIntentSenderForResult(pendingIntent.getIntentSender(),DELETE_VIDEO_CODE,null,0,0,0);
                        } catch (IntentSender.SendIntentException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return true;
                });
        mainsearchview.setupWithSearchBar(mainsearchbar);
        mainsearchview.addTransitionListener(new SearchView.TransitionListener() {
            @Override
            public void onStateChanged(@NonNull SearchView searchView, @NonNull SearchView.TransitionState previousState, @NonNull SearchView.TransitionState newState) {
                if (newState == SearchView.TransitionState.SHOWING) {
                    // SearchView 进入
                    searchstatus = true;
                } else if (newState == SearchView.TransitionState.HIDING) {
                    // SearchView 退出
                    searchstatus = false;
                }
            }
        });
        mainsearchview.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            // 显示加载
            mainsearchlinearprogress.setVisibility(View.VISIBLE);
            mainsearchview.setText(mainsearchview.getText());
            // 在子线程中执行耗时操作
            new Thread(new Runnable() {
                @Override
                public void run() {
                    searchlist=init_data(String.valueOf(mainsearchview.getText()));
                    // 在主线程中更新界面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListMovieAdapter listMovieAdapter = new ListMovieAdapter(MainActivity.this, searchlist, true,null,null);
                            //创建线性布局
                            LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
                            //水平方向
                            manager.setOrientation(LinearLayoutManager.VERTICAL);
                            //给RecyclerView设置布局管理器
                            searchrecyclerView.setLayoutManager(manager);
                            searchrecyclerView.setAdapter(listMovieAdapter);
                            // 隐藏加载
                            mainsearchlinearprogress.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();

            return false;
        });

        //历史记录按钮监听
        historymaterialToolbar.setOnMenuItemClickListener(
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
                                    historyallbt.setVisibility(View.GONE);
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
                                    DatabaseBackup.backup(MainActivity.this);
                                }
                            })
                            .show();
                }
                return true;
            });

        //历史记录查看更多
        historyallbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,HistoryActivity.class);
                startActivity(intent);
            }
        });

        //在线播放fab的监听事件
        fabonline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 加载自定义布局
                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_online, null);
                TextInputEditText textInputEditText = dialogView.findViewById(R.id.online_textinput);

                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("请输入一个网络URL进行播放")
                        .setView(dialogView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(Objects.requireNonNull(textInputEditText.getText()).toString().equals("")) {
                                    Toast.makeText(MainActivity.this, "请输入URL", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent intent=new Intent();
                                    switch (orientation) {
                                        case "auto":
                                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                                //横屏
                                                intent = new Intent(MainActivity.this, PlayerActivity.class);
                                            } else {
                                                //竖屏
                                                intent = new Intent(MainActivity.this, PlayerVerticalActivity.class);
                                            }
                                            break;
                                        case "vertical":
                                            intent = new Intent(MainActivity.this, PlayerVerticalActivity.class);
                                            break;
                                        case "horizontal":
                                            intent = new Intent(MainActivity.this, PlayerActivity.class);
                                            break;
                                    }
                                    intent.putExtra("movie_url",textInputEditText.getText().toString());
                                    startActivity(intent);
                                }

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
        });
    }

    /**
     * 首页菜单栏按钮点击
     */
    private void setItemSelectedListener(NavigationBarView navigationBarView){
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            Menu menu = navigationBarView.getMenu();
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_loacl) {
                    item.setIcon(R.drawable.movie_fill1_wght400_grad0_opsz32);
                    history.setVisibility(View.GONE);
                    searchRelativeLayout.setVisibility(View.VISIBLE);
                    return true;
                } else if (itemId == R.id.menu_history) {
                    menu.findItem(R.id.menu_loacl).setIcon(R.drawable.movie_fill0_wght400_grad0_opsz32);
                    searchRelativeLayout.setVisibility(View.GONE);
                    history.setVisibility(View.VISIBLE);
                    init_data_history(); //初始化数据
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 初始化list数据，以dp为单位分配，避免不同分辨率设备下的差异
     */
    public void init_data() {
        if(folder_path==null) { //文件夹页面
            List<String> list = provider.getFolderList();
            folderAdapter = new FolderAdapter(this, list, list_horizontal_layout, new FolderAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String data,int position) {
                    folder_path=data;
                    init_data();
                }
            }, new FolderAdapter.OnItemLongClickListener() {
                @Override
                public void OnItemLongClick(String data,int position) {
                    // 加载自定义布局
                    View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_main_info, null);
                    TextView tvFolderName=dialogView.findViewById(R.id.info_folder_name_value);
                    TextView tvVideoNumber=dialogView.findViewById(R.id.info_video_number_value);
                    TextView tvSize=dialogView.findViewById(R.id.info_size_value);

                    movielist = provider.getMapList(data);
                    tvFolderName.setText(data);
                    tvVideoNumber.setText(String.valueOf(movielist.size()));
                    long size=0;
                    for (Video video:movielist) {
                        size+=video.getSize();
                    }
                    tvSize.setText(formatSize(size));

                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("详细信息")
                            .setView(dialogView)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            });
            //若为水平布局
            if(list_horizontal_layout) {
                //创建线性布局
                LinearLayoutManager manager = new LinearLayoutManager(this);
                //水平方向
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                //给RecyclerView设置布局管理器
                recyclerView.setLayoutManager(manager);
            } else {
                DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
                int screenWidth = displayMetrics.widthPixels;

                Resources resources = getResources();

                int itemWidthInDp = 100; // 项目宽度（以dp为单位）
                float itemWidthInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, itemWidthInDp, resources.getDisplayMetrics());

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    //横屏
                    int dpValue = 72; // 要减去的 dp 值
                    float density = displayMetrics.density;
                    int pixelValue = (int)(dpValue * density); // 将 dp 转换为像素
                    screenWidth -= pixelValue; // 减去 72 dp
                }

                int spanCount = (int) (screenWidth / itemWidthInPixels);

                GridLayoutManager manager = new GridLayoutManager(this, spanCount);
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                //给RecyclerView设置布局管理器
                recyclerView.setLayoutManager(manager);
            }
            recyclerView.setAdapter(folderAdapter);
        } else { //文件夹内
            movielist = provider.getMapList(folder_path);
            listMovieAdapter = new ListMovieAdapter(this, movielist, list_horizontal_layout, new ListMovieAdapter.OnItemClickListener() {
                @Override
                public void OnItemClick(List<Video> list, int position) {
                    if(selectvideo.size()==0) {
                        Intent intent=new Intent();
                        switch (orientation) {
                            case "auto":
                                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    //横屏
                                    intent = new Intent(MainActivity.this, PlayerActivity.class);
                                } else {
                                    //竖屏
                                    intent = new Intent(MainActivity.this, PlayerVerticalActivity.class);
                                }
                                break;
                            case "vertical":
                                intent = new Intent(MainActivity.this, PlayerVerticalActivity.class);
                                break;
                            case "horizontal":
                                intent = new Intent(MainActivity.this, PlayerActivity.class);
                                break;
                        }
                        intent.putExtra("movie_position",position);
                        intent.putExtra("movie_id",list.get(position).getId());
                        intent.putExtra("movie_video_list",(Serializable) list);
                        startActivity(intent);
                    } else { //已有选中的内容
                        View itemView = recyclerView.findViewHolderForAdapterPosition(position).itemView;
                        ColorDrawable colorDrawable = (ColorDrawable) itemView.getBackground();
                        int currentColor = colorDrawable.getColor();
                        if (currentColor == Color.LTGRAY) { //取消选择
                            itemView.setBackgroundColor(Color.TRANSPARENT);
                            selectvideo.remove(list.get(position));
                        } else { //选择
                            itemView.setBackgroundColor(Color.LTGRAY);
                            selectvideo.add(list.get(position));
                        }
                    }
                    if (selectvideo.size()==0) {
                        mainsearchbar.getMenu().clear();
                        mainsearchbar.inflateMenu(R.menu.searchbar_menu);
                    } else {
                        mainsearchbar.getMenu().clear();
                        mainsearchbar.inflateMenu(R.menu.searchbar_menu_select);
                    }
                }
            },new ListMovieAdapter.OnListItemLongClickListener() {
                @Override
                public void OnItemLongClick(List<Video> list,int position) {
                    View itemView = recyclerView.findViewHolderForAdapterPosition(position).itemView;
                    ColorDrawable colorDrawable = (ColorDrawable) itemView.getBackground();
                    int currentColor = colorDrawable.getColor();
                    if (currentColor == Color.LTGRAY) { //取消选择
                        itemView.setBackgroundColor(Color.TRANSPARENT);
                        selectvideo.remove(list.get(position));
                    } else { //选择
                        itemView.setBackgroundColor(Color.LTGRAY);
                        selectvideo.add(list.get(position));
                    }
                    if (selectvideo.size()==0) {
                        mainsearchbar.getMenu().clear();
                        mainsearchbar.inflateMenu(R.menu.searchbar_menu);
                    } else {
                        mainsearchbar.getMenu().clear();
                        mainsearchbar.inflateMenu(R.menu.searchbar_menu_select);
                    }
                }
            });
            //若为水平布局
            if(list_horizontal_layout) {
                //创建线性布局
                LinearLayoutManager manager = new LinearLayoutManager(this);
                //水平方向
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                //给RecyclerView设置布局管理器
                recyclerView.setLayoutManager(manager);
            } else {
                DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
                int screenWidth = displayMetrics.widthPixels;

                Resources resources = getResources();
                int itemWidthInDp = 200; // 项目宽度（以dp为单位）
                float itemWidthInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, itemWidthInDp, resources.getDisplayMetrics());

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    //横屏
                    int dpValue = 72; // 要减去的 dp 值
                    float density = displayMetrics.density;
                    int pixelValue = (int)(dpValue * density); // 将 dp 转换为像素
                    screenWidth -= pixelValue; // 减去 72 dp
                }

                int spanCount = (int) (screenWidth / itemWidthInPixels);

                GridLayoutManager manager = new GridLayoutManager(this, spanCount);
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                //给RecyclerView设置布局管理器
                recyclerView.setLayoutManager(manager);
            }
            recyclerView.setAdapter(listMovieAdapter);
        }
    }
    public void init_data_history() { //历史记录初始化数据
        listMovieHistoryAdapter=new ListMovieHistoryAdapter(MainActivity.this, provider,2*sharedPreferences.getInt("historynum",1)+1);
        //创建线性布局
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);
        //水平方向
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        //给RecyclerView设置布局管理器
        historyrecyclerView.setLayoutManager(manager);
        historyrecyclerView.setAdapter(listMovieHistoryAdapter);

        if (listMovieHistoryAdapter.getItemCount()==0) historyallbt.setVisibility(View.GONE);
        else historyallbt.setVisibility(View.VISIBLE);
    }
    public List<Video> init_data(String input) {
        List<Video> originallist=provider.getloclist();
        List<Video> currentlist=new ArrayList<>();
        for(Video video:originallist) {
            if(video.getTitle().contains(input)) currentlist.add(video);
        }
        return currentlist;
    }

    /**
     * 格式化占用大小的显示
     * @param sizeInBytes 字节数
     * @return 根据数值选择合适的单位
     */
    public String formatSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return sizeInBytes / 1024 + " KB";
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return sizeInBytes / (1024 * 1024) + " MB";
        } else {
            return sizeInBytes / (1024 * 1024 * 1024) + " GB";
        }
    }

    /**
     * 初始化
     */
    public void init() {
        mainbg=findViewById(R.id.main_background);
        recyclerView=findViewById(R.id.recyclerview);
        historyrecyclerView=findViewById(R.id.main_history_list);
        history=findViewById(R.id.main_history);
        historymaterialToolbar=findViewById(R.id.main_history_top_MaterialToolbar);
        mainsearchview=findViewById(R.id.search_view);
        mainsearchbar=findViewById(R.id.search_bar);
        searchrecyclerView=findViewById(R.id.search_recyclerview);
        mainsearchlinearprogress=findViewById(R.id.search_LinearProgressIndicator);
        searchRelativeLayout=findViewById(R.id.search_RelativeLayout);
        fabonline=findViewById(R.id.floating_action_button_online);
        historyallbt=findViewById(R.id.main_history_all);

        sharedPreferences = getSharedPreferences("Settings",MODE_PRIVATE);
        list_horizontal_layout = sharedPreferences.getBoolean("list_horizontal_layout", true);
    }

    /**
     * 申请读取权限
     */
    public void ReadPermission() {
        if (Build.VERSION.SDK_INT >= 33) { //安卓13
            if (!Environment.isExternalStorageManager()) {
                requestReadPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else { //安卓7.0
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                //视频数据初始化，防止多次运行造成性能浪费
                provider = new VideoProvider(MainActivity.this);

                //存入数据
                init_data();
            }
        }
    }

    /**
     * 横竖屏切换时储存文件夹数据
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("folder_path",folder_path);
        super.onSaveInstanceState(outState);
    }

    /**
     * 重写返回方法
     */
    @Override
    public void onBackPressed() {
        //判断当前在文件内或外，是否处于搜索页面
        if (searchstatus) { //搜索
            mainsearchview.hide();
            searchstatus=false;
        } else if (selectvideo.size()!=0) { //文件夹内选中
            selectvideo.clear();
            mainsearchbar.getMenu().clear();
            mainsearchbar.inflateMenu(R.menu.searchbar_menu);
            listMovieAdapter.resetSelectedStates(); //重置背景色
        } else if(!Objects.equals(folder_path, null)) { //文件夹内未选中
            folder_path=null;
            init_data();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 回调数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
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
                init_data_history();
                Toast.makeText(this, "读取成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "取消读取", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == DELETE_VIDEO_CODE) {
            if (resultCode==RESULT_OK) { //刷新并清空选中列表
                provider.Updatelist();
                selectvideo.clear();
                listMovieAdapter.removeSelectedItems(); //更新页面
                Toast.makeText(this, "成功删除", Toast.LENGTH_SHORT).show();
            }
            else {
                // 处理取消删除的情况
                Toast.makeText(this, "取消删除", Toast.LENGTH_SHORT).show();
            }
        } else {
            folder_path = data.getStringExtra("folder_path");
            // 处理返回的结果
            init_data();
        }
    }

}