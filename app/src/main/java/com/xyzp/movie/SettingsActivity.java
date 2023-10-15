package com.xyzp.movie;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import bean.StatusBar;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private MaterialToolbar materialToolbar;
    private RadioGroup themeradioGroup;
    private RadioButton autoradioButton,brightradioButton,darkradioButton;
    private TextView versiontextView;
    private ImageView tipimageView;
    private Button opaddbutton,lzybutton,mainpicchoicebutton,mainpicdefaultbutton;
    private MaterialSwitch tipswitch;
    private NumberPicker historynp;
    private final String[] historyitems = {"3", "5", "7"};
    private final int[] historynumitems = {3, 5, 7};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置颜色为半透明
        StatusBar statusBar = new StatusBar(SettingsActivity.this);
        statusBar.setColor(R.color.transparent);
        if(this.getApplicationContext().getResources().getConfiguration().uiMode == 0x21) { //深色
            statusBar.setTextColor(true);
        } else if (this.getApplicationContext().getResources().getConfiguration().uiMode == 0x11) { //浅色
            statusBar.setTextColor(false);
        }

        //material 3取色，安卓12起
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_settings);
        init();

        //顶部栏返回按钮事件
        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //设置提示，默认为true
        tipswitch.setChecked(sharedPreferences.getBoolean("tip",true));
        tipswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("tip", true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("tip", false);
                    editor.apply();
                }
            }
        });
        tipimageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.popup_layout, null);
                PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);

                int[] location = new int[2];
                v.getLocationOnScreen(location);
                popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0]+v.getWidth(), location[1]);
            }
        });

        //设置暗色模式，默认为auto
        String theme=sharedPreferences.getString("theme","auto");
        switch (theme) {
            case "auto":
                themeradioGroup.check(R.id.settings_total_theme_auto);
                break;
            case "bright":
                themeradioGroup.check(R.id.settings_total_theme_bright);
                break;
            case "dark":
                themeradioGroup.check(R.id.settings_total_theme_dark);
                break;
        }
        autoradioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("theme", "auto");
                editor.apply();
            }
        });
        brightradioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("theme", "bright");
                editor.apply();
            }
        });
        darkradioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("theme", "dark");
                editor.apply();
            }
        });

        //首页图片选择
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // 处理返回的结果
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            Uri imageUri = data.getData();

                            try {
                                // 创建一个指向应用缓存目录的文件对象
                                File cacheDir = getCacheDir();
                                File imageFile = new File(cacheDir, "bg.png");

                                // 打开输入流
                                InputStream inputStream = getContentResolver().openInputStream(imageUri);

                                // 打开输出流
                                FileOutputStream fos = new FileOutputStream(imageFile);

                                // 创建一个缓冲区
                                byte[] buffer = new byte[1024];
                                int bytesRead;

                                // 将输入流的数据写入输出流
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                }

                                // 关闭流
                                inputStream.close();
                                fos.close();

                                new MaterialAlertDialogBuilder(SettingsActivity.this)
                                        .setTitle("重启应用生效")
                                        .setMessage("迎接更美好的下一次启动")
                                        .setPositiveButton("立即重启", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = getBaseContext().getPackageManager()
                                                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                        })
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // 处理取消按钮的点击事件
                                            }
                                        })
                                        .show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                });
        mainpicchoicebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动图片选择器
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(intent);
            }
        });
        mainpicdefaultbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个指向应用缓存目录的文件对象
                File cacheDir = getCacheDir();
                File imageFile = new File(cacheDir, "bg.png");

                // 检查图片是否存在
                if (imageFile.exists()) {
                    imageFile.delete();
                }

                new MaterialAlertDialogBuilder(SettingsActivity.this)
                        .setTitle("重启应用生效")
                        .setMessage("迎接更美好的下一次启动")
                        .setPositiveButton("立即重启", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
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

        //主页历史页设置
        // 设置 NumberPicker 的取值范围和初始值
        historynp.setMinValue(1);
        historynp.setMaxValue(historyitems.length);
        historynp.setValue(sharedPreferences.getInt("historynum",1));

        // 设置 NumberPicker 的显示内容
        historynp.setDisplayedValues(historyitems);

        // 设置 NumberPicker 的滚动监听器
        historynp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // 处理选项的滚动事件
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("historynum", newVal);
                editor.apply();

            }
        });

        //github链接
        opaddbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/xyzp6/local_movie";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        //蓝奏云链接
        lzybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建一个新的文本clip对象
                ClipData mClipData = ClipData.newPlainText("lyz","bbw4");
                //把clip对象放在剪贴板中
                ClipboardManager mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                mClipboardManager.setPrimaryClip(mClipData);
                Toast.makeText(getApplicationContext(), "密码已复制", Toast.LENGTH_SHORT).show();
                String url = "https://wwyq.lanzouy.com/b048pmqef";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        //版本号填充
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            String versionText = getString(R.string.version_text, versionName);
            versiontextView.setText(versionText);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void init() {
        materialToolbar=findViewById(R.id.settings_top_MaterialToolbarr);
        versiontextView=findViewById(R.id.settings_about_version);
        opaddbutton=findViewById(R.id.settings_about_github);
        lzybutton=findViewById(R.id.settings_about_lanzouyun);
        mainpicchoicebutton=findViewById(R.id.settings_main_selpictures);
        mainpicdefaultbutton=findViewById(R.id.settings_main_defaultpictures);
        themeradioGroup=findViewById(R.id.settings_total_theme);
        autoradioButton=findViewById(R.id.settings_total_theme_auto);
        brightradioButton=findViewById(R.id.settings_total_theme_bright);
        darkradioButton=findViewById(R.id.settings_total_theme_dark);
        tipswitch=findViewById(R.id.settings_total_tip_switch);
        tipimageView=findViewById(R.id.settings_total_tip_info);
        historynp=findViewById(R.id.settings_main_history_number_picker);

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
    }
}
