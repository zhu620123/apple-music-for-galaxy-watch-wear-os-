package com.operit.wearamlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllAppsActivity extends Activity {

    private final List<ResolveInfo> apps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent query = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> raw = getPackageManager().queryIntentActivities(query, 0);

        String self = getPackageName();
        for (ResolveInfo ri : raw) {
            if (!ri.activityInfo.packageName.equals(self)) {
                apps.add(ri);
            }
        }
        Collections.sort(apps, (a, b) -> {
            String la = a.loadLabel(getPackageManager()).toString();
            String lb = b.loadLabel(getPackageManager()).toString();
            return la.compareToIgnoreCase(lb);
        });

        ListView lv = new ListView(this);
        lv.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return apps.size();
            }

            @Override
            public Object getItem(int i) {
                return apps.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View v, ViewGroup parent) {
                TextView tv = (v instanceof TextView)
                        ? (TextView) v
                        : new TextView(AllAppsActivity.this);
                tv.setText(apps.get(i).loadLabel(getPackageManager()));
                tv.setTextSize(16);
                tv.setPadding(24, 26, 24, 26);
                return tv;
            }
        });
        lv.setOnItemClickListener((parent, view, i, id) -> {
            ResolveInfo ri = apps.get(i);
            Intent launch = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClassName(ri.activityInfo.packageName, ri.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(launch);
            } catch (Exception e) {
                Toast.makeText(this, "无法启动：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        setContentView(lv);
    }
}
