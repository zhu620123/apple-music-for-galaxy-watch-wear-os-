package com.operit.wearamlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    public static final String APPLE_MUSIC_PACKAGE = "com.apple.android.music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton btnLaunch = findViewById(R.id.btn_launch);
        View btnAll = findViewById(R.id.btn_all);
        View btnHelp = findViewById(R.id.btn_help);
        TextView tvStatus = findViewById(R.id.tv_status);

        boolean installed = isInstalled(APPLE_MUSIC_PACKAGE);
        if (installed) {
            tvStatus.setText("✓ Apple Music 已安装");
            tvStatus.setBackgroundResource(R.drawable.bg_status_ok);
            tvStatus.setTextColor(0xFF7CE39B);
        } else {
            tvStatus.setText("✗ 未检测到 Apple Music");
            tvStatus.setBackgroundResource(R.drawable.bg_status_err);
            tvStatus.setTextColor(0xFFFF8B9C);
        }

        btnLaunch.setOnClickListener(v -> launchPackage(APPLE_MUSIC_PACKAGE));
        btnAll.setOnClickListener(v -> startActivity(new Intent(this, AllAppsActivity.class)));
        btnHelp.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("使用说明")
                .setMessage("1. 先把官方 Apple Music APK 侧载到手表；\n"
                        + "2. 点「启动 Apple Music」；\n"
                        + "3. 没反应就去「全部应用列表」里找；\n"
                        + "4. Apple Music 是手机版 UI，在圆形屏边缘可能显示不全，属正常现象；\n"
                        + "5. 首次使用请在 Apple Music 内登录你的 Apple ID。")
                .setPositiveButton("知道了", null)
                .show());
    }

    private boolean isInstalled(String pkg) {
        try {
            getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void launchPackage(String pkg) {
        PackageManager pm = getPackageManager();
        Intent launch = pm.getLaunchIntentForPackage(pkg);
        if (launch == null) {
            // 兜底：在该包内找任意 MAIN/LAUNCHER 入口
            Intent query = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setPackage(pkg);
            List<ResolveInfo> list = pm.queryIntentActivities(query, 0);
            if (!list.isEmpty()) {
                ResolveInfo ri = list.get(0);
                launch = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setComponent(new ComponentName(
                                ri.activityInfo.packageName, ri.activityInfo.name));
            }
        }
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(launch);
            } catch (Exception e) {
                toast("启动失败：" + e.getMessage());
            }
        } else {
            toast("没找到 Apple Music，请先侧载官方 APK");
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}