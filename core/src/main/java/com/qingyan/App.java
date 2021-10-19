package com.qingyan;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.qingyan.lsp.LSPLoader;
import com.qingyan.lsp.PrePareApk;
import com.qingyan.lsp.VerifyCheck;
import com.qingyan.qpatch_info.QPatchInfo;
import com.qingyan.xp.env.QXpManager;
import com.qingyan.xp.env.XpEnv;

import org.apache.commons.io.IOUtils;
import org.lsposed.lspd.BuildConfig;
import org.lsposed.lspd.yahfa.hooker.YahfaHooker;

import java.io.File;
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
public class App extends HotFixFullApplication implements XpEnv {
    private static final String defaultLspSoName = "liblspd.so";
    private static final String XPOSED_SandHook_Library_Name = "libpatchs.so";
    private static final String TAG = "QBugHook";

    static Context appContext;
    private static ClassLoader appClassLoader;
    private static QPatchInfo config;
    private static ApplicationInfo oriAppInfo;
    private Object version;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        UtilManager.init(base, BuildConfig.DEBUG);
        QXpManager.setEvn(this);
        appContext = base;base.getResources();
        oriAppInfo = new ApplicationInfo(appContext.getApplicationInfo());
        try {
            readConfig();
            PrePareApk prePareApk = PrePareApk.getInstance(appContext, config, oriAppInfo);
            prePareApk.copyOriApk();
//            Log.e(TAG, "libPath: "+ prePareApk.getDefaultBackupLibPath());
            doPrepare(base,prePareApk.getResDir(),prePareApk.getDefaultBackupLibPath());
            appClassLoader =getNewClassloader();
            MyLog.logM("nativeLib= "+prePareApk.getDefaultBackupLibPath()+"\nloaderApp:"+appClassLoader);
            inits();
            VerifyCheck.checkVersion(version);
            makeApplication();
        } catch (Throwable e) {
            MyLog.logM(e);
        }
    }

    public static void inits() throws Throwable {
        Log.e(TAG, "static initializer: \"开始加载App");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Reflection.unseal(appContext);
        }
        String mPackageName = appContext.getPackageName();
        //加载so
        String nativeLibDir = oriAppInfo.nativeLibraryDir;
        String defaultSoPath = nativeLibDir + "/" + XPOSED_SandHook_Library_Name;
        if (!new File(defaultSoPath).exists() && mPackageName.equals("org.qingyan.lsposed.lsp"))
            defaultSoPath = nativeLibDir + "/" + defaultLspSoName;
        ;
        System.load(defaultSoPath);//加载So开始
        YahfaHooker.init();
        initUncaughtException();
        LSPLoader.initAndLoadModules(appContext, appClassLoader);
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


    private void readConfig(){
        try {
            String gstr=IOUtils.toString(appContext.getAssets().open(QPatchInfo.CONFIG_IN_ASSETS), StandardCharsets.UTF_8);
            config= new Gson().fromJson(gstr, QPatchInfo.class);
        } catch (Throwable e) {
            MyLog.logM(e);
        }
    }



    public static ApplicationInfo getsApplicationInfo() {
        return oriAppInfo;
    }

    public static QPatchInfo getPatchConfig(){return config;}

    @Override
    public String getConfig() {
        return new Gson().toJson(config);
    }

    @Override
    public Context getAppContext() {
        return appContext;
    }

    @Override
    public String getTureInstallPath() {
        return oriAppInfo.publicSourceDir;
    }

    @Override
    public void setVersion(Object version) {
        this.version=version;
    }
}
