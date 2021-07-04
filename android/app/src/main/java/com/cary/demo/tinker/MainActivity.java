
package com.cary.demo.tinker;

import android.Manifest;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import io.flutter.embedding.android.FlutterActivity;

public class MainActivity extends FlutterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 必须给存储权限啊，要不然tinker的补丁无法写入本地文件夹
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
    }
}
