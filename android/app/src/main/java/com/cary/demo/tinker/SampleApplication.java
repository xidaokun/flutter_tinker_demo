package com.cary.demo.tinker;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import androidx.multidex.MultiDex;

import com.cary.demo.tinker.util.FlutterPatch;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import io.flutter.FlutterInjector;
import io.flutter.app.FlutterApplication;

public class SampleApplication extends FlutterApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FlutterInjector.instance().flutterLoader().startInitialization(this);
        Bugly.init(SampleApplication.this, "9c09326a9e", false);
        FlutterPatch.hook(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);
        // 安装tinker
        Beta.installTinker();
    }
}
