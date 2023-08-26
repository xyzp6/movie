package com.xyzp.movie;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.BaseKeyListener;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import bean.StatusBar;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private NavigationView navigationView;
    private MaterialToolbar materialToolbar;
    private TextView versiontextView,opaddtextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置颜色为半透明
        StatusBar statusBar = new StatusBar(SettingsActivity.this);
        statusBar.setColor(R.color.translucent);
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

        //navigationView点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 更新菜单项的选中状态
                item.setChecked(true);
                // 处理选中事件
                return true;
            }
        });

        Linkify.addLinks(opaddtextView, Linkify.WEB_URLS);
        opaddtextView.setMovementMethod(LinkMovementMethod.getInstance());
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
        navigationView=findViewById(R.id.settings_navigationView);
        materialToolbar=findViewById(R.id.settings_top_MaterialToolbarr);
        versiontextView=findViewById(R.id.settings_about_version);
        opaddtextView=findViewById(R.id.settings_about_openaddress);

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
    }
}
