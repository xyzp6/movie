package com.xyzp.movie;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.method.BaseKeyListener;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import bean.StatusBar;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private MaterialToolbar materialToolbar;
    private RadioGroup themeradioGroup;
    private RadioButton autoradioButton,brightradioButton,darkradioButton;
    private TextView versiontextView;
    private ImageView tipimageView;
    private Button opaddbutton,mainpicchoicebutton,mainpicdefaultbutton;
    private MaterialSwitch tipswitch;
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

        int screenBrightness = 0;
        try {
            screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            throw new RuntimeException(e);
        }
        float brightnessValue = screenBrightness / 255.0f;
        System.out.println(brightnessValue);


//        ContentResolver contentResolver = getContentResolver();
//        int defVal = 125;
//        System.out.println(Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, defVal));

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
                                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                Drawable drawable = Drawable.createFromStream(inputStream, imageUri.toString());

                                // 将 Drawable 转换为 Bitmap
                                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                                // 将 Bitmap 转换为字节数组
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                byte[] byteArray = byteArrayOutputStream.toByteArray();

                                // 将字节数组编码为 Base64 字符串
                                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                                // 将编码后的字符串存储在 SharedPreferences 中
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("mainbg", encodedImage);
                                editor.apply();
                            } catch (FileNotFoundException e) {
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
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("mainbg", "");
                editor.apply();
            }
        });

        //开源代码链接
        opaddbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/xyzp6/movie";
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
        opaddbutton=findViewById(R.id.settings_about_openaddress);
        mainpicchoicebutton=findViewById(R.id.settings_main_selpictures);
        mainpicdefaultbutton=findViewById(R.id.settings_main_defaultpictures);
        themeradioGroup=findViewById(R.id.settings_total_theme);
        autoradioButton=findViewById(R.id.settings_total_theme_auto);
        brightradioButton=findViewById(R.id.settings_total_theme_bright);
        darkradioButton=findViewById(R.id.settings_total_theme_dark);
        tipswitch=findViewById(R.id.settings_total_tip_switch);
        tipimageView=findViewById(R.id.settings_total_tip_info);

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
    }
}
