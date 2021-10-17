package com.qingyan;

import static com.qingyan.lsp.LSPLoader.initAndLoadModules;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;


import com.google.gson.Gson;
import com.qingyan.qpatch_info.QPatchInfo;
import com.qingyan.xp.env.QXpManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.lsposed.lspd.BuildConfig;
import org.lsposed.lspd.yahfa.hooker.YahfaHooker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import me.weishu.reflection.Reflection;
import qingyan.qhot.HotFixFullApplication;
import qingyan.util.CrashHandler;
import qingyan.util.MyLog;
import qingyan.util.UtilManager;

/**
 * Created by 青烟
 */
@SuppressLint("UnsafeDynamicallyLoadedCode")
public class App extends HotFixFullApplication {
    private static final String defaultLspSoName = "liblspd.so";
    private static final String XPOSED_SandHook_Library_Name = "libpatchs.so";
    private static final String TAG = "QBugHook";
    private static final String ORI_APK_A_PATH = "base";
    static Context appContext;
    private static ClassLoader appClassLoader;
    private static QPatchInfo config;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        UtilManager.init(base, BuildConfig.DEBUG);
        appContext = base;base.getResources();
        QXpManager.setAppContext(base);
        try {
            readConfig();
            copyOriApk();
            doPrepare(base,getResDir());
            appClassLoader =getNewClassloader();
            inits();
            makeApplication();
        } catch (Throwable e) {
            MyLog.logM(e);
        }
    }

    public static void inits() throws Exception {
        Log.e(TAG, "static initializer: \"开始加载App");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Reflection.unseal(appContext);
        }
        String mPackageName = appContext.getPackageName();
        //加载so
        String nativeLibDir = appContext.getPackageManager().getApplicationInfo(mPackageName, 0).nativeLibraryDir;
        String defaultSoPath = nativeLibDir + "/" + XPOSED_SandHook_Library_Name;
        if (!new File(defaultSoPath).exists() && mPackageName.equals("org.qingyan.lsposed.lsp"))
            defaultSoPath = nativeLibDir + "/" + defaultLspSoName;
        ;
        System.load(defaultSoPath);//加载So开始
        YahfaHooker.init();
        initUncaughtException();
        initAndLoadModules(appContext, appClassLoader);
    }

    private static void initUncaughtException() {
        XposedHelpers.findAndHookMethod(Thread.class, "dispatchUncaughtException", Throwable.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                CrashHandler.getsHandler(appContext).uncaughtException((Thread) param.thisObject, (Throwable) param.args[0]);
            }
        });
        XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                CrashHandler.getsHandler(appContext).setActivity((Activity) param.thisObject);
            }
        });
    }

    private void copyOriApk() throws Throwable {
        File file = new File(getResDir());
        int version = -1;
        try {
            version = appContext.getPackageManager().getPackageArchiveInfo(file.getPath(), 0).versionCode;
        } catch (Throwable ignore) {
        }
        int nowVer = 0;
        try {
            nowVer = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(),0).versionCode;
        } catch (Throwable ignored) {
        }
        if (version < nowVer) {
            InputStream inputStream = appContext.getAssets().open(ORI_APK_A_PATH);
            FileUtils.copyInputStreamToFile(inputStream, file);
        }
    }
    private void readConfig(){
        try {
            String gstr=IOUtils.toString(appContext.getAssets().open(QPatchInfo.CONFIG_IN_ASSETS), StandardCharsets.UTF_8);
            QXpManager.setConfig(gstr);
            config= new Gson().fromJson(gstr, QPatchInfo.class);
        } catch (Throwable e) {
            MyLog.logM(e);
        }
    }
    protected String getResDir() {
        if (config!=null){
            String basePathFormat = config.getBasePathFormat(appContext.getApplicationInfo().nativeLibraryDir);
            if (!basePathFormat.isEmpty())return basePathFormat;
        }
        return new File(appContext.getFilesDir().getPath() + "/ori_backup/" + "data/app/" + appContext.getPackageName() + "/base.apk").getPath();
    }
}
