package com.cary.demo.tinker.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;
import java.lang.reflect.Field;

import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.loader.ApplicationInfoLoader;
import io.flutter.embedding.engine.loader.FlutterApplicationInfo;
import io.flutter.embedding.engine.loader.FlutterLoader;

public class FlutterPatch {
    private static final String TAG = "FlutterPatch";


    private FlutterPatch() {
    }

    public static String getLibPath(Context context) {
        String libPath = findLibraryFromTinker(context, "lib" + File.separator + getCpuABI(), "libapp.so");
        if (!TextUtils.isEmpty(libPath) && libPath.equals("libapp.so")) {
            return null;
        }
        return libPath;
    }

    public static void flutterPatchInit(Context context) {

        try {
            String tinkerLibPath = findLibraryFromTinker(context, "lib" + File.separator + getCpuABI(), "libapp.so");
            Log.e(TAG, "tinker libPath:   " + tinkerLibPath);

            FlutterLoader flutterLoader = FlutterInjector.instance().flutterLoader();

            Field applicationInfoFiled = flutterLoader.getClass().getDeclaredField("flutterApplicationInfo");
            applicationInfoFiled.setAccessible(true);
            Object flutterApplicationInfo = applicationInfoFiled.get(flutterLoader);

            Log.e(TAG, "get flutterApplicationInfo success" );

            Class<?> listenerInfoClz = Class.forName("io.flutter.embedding.engine.loader.FlutterApplicationInfo");
            Field aotSharedLibraryNameFiled = listenerInfoClz.getDeclaredField("aotSharedLibraryName");
            aotSharedLibraryNameFiled.setAccessible(true);
            String oldLibPath = (String) aotSharedLibraryNameFiled.get(flutterApplicationInfo);
            Log.e(TAG, "oldLibPath::"+oldLibPath);

            aotSharedLibraryNameFiled.set(flutterApplicationInfo, tinkerLibPath);

            Log.e(TAG, "flutter patch is loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "exception::"+e.getLocalizedMessage());
        }
    }

    /**
     *
     * 插桩方法
     * 此方法不可修改，否则不会成功
     *
     * @param obj
     */
    public static void hook(Object obj) {
        if (obj instanceof Context) {

            Context context = (Context) obj;
            TinkerLog.i(TAG, "find FlutterMain");

            flutterPatchInit(context);
        } else {

            TinkerLog.i(TAG, "Object: " + obj.getClass().getName());
        }

    }

    public static String findLibraryFromTinker(Context context, String relativePath, String libName) throws UnsatisfiedLinkError {
        final Tinker tinker = Tinker.with(context);

        libName = libName.startsWith("lib") ? libName : "lib" + libName;
        libName = libName.endsWith(".so") ? libName : libName + ".so";
        String relativeLibPath = relativePath + File.separator + libName;

        TinkerLog.i(TAG, "flutterPatchInit() called   " + tinker.isTinkerLoaded() + " " + tinker.isEnabledForNativeLib());

        if (tinker.isEnabledForNativeLib() && tinker.isTinkerLoaded()) {
            TinkerLoadResult loadResult = tinker.getTinkerLoadResultIfPresent();
            if (loadResult.libs == null) {
                return libName;
            }
            for (String name : loadResult.libs.keySet()) {
                if (!name.equals(relativeLibPath)) {
                    continue;
                }
                String patchLibraryPath = loadResult.libraryDirectory + "/" + name;
                File library = new File(patchLibraryPath);
                if (!library.exists()) {
                    continue;
                }

                boolean verifyMd5 = tinker.isTinkerLoadVerify();
                if (verifyMd5 && !SharePatchFileUtil.verifyFileMd5(library, loadResult.libs.get(name))) {
                    tinker.getLoadReporter().onLoadFileMd5Mismatch(library, ShareConstants.TYPE_LIBRARY);
                } else {
                    TinkerLog.i(TAG, "findLibraryFromTinker success:" + patchLibraryPath);
                    return patchLibraryPath;
                }
            }
        }

        return libName;
    }

    /**
     * 获取最优abi
     *
     * @return
     */
    public static String getCpuABI() {

        if (Build.VERSION.SDK_INT >= 21) {
            for (String cpu : Build.SUPPORTED_ABIS) {
                if (!TextUtils.isEmpty(cpu)) {
                    TinkerLog.i(TAG, "cpu abi is:" + cpu);
                    return cpu;
                }
            }
        } else {
            TinkerLog.i(TAG, "cpu abi is:" + Build.CPU_ABI);
            return Build.CPU_ABI;
        }

        return "";
    }
}
