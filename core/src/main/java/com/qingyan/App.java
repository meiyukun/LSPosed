package com.qingyan;

import static com.qingyan.lsp.LSPLoader.initAndLoadModules;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;


import androidx.annotation.NonNull;

import com.android.zipflinger.ZipArchive;
import com.google.gson.Gson;
import com.qingyan.qpatch_info.QPatchInfo;
import com.qingyan.xp.env.QXpManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.lsposed.lspd.BuildConfig;
import org.lsposed.lspd.yahfa.hooker.YahfaHooker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import me.weishu.reflection.Reflection;
import qingyan.qhot.HotFixFullApplication;
import qingyan.util.BuildCompat;
import qingyan.util.CrashHandler;
import qingyan.util.MyLog;
import qingyan.util.UtilManager;
import qingyan.util.reflect.RefUtil;

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
    private static ApplicationInfo oriAppInfo;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        UtilManager.init(base, true);
        QXpManager.setAppContext(base);
        appContext = base;base.getResources();
        oriAppInfo = new ApplicationInfo(appContext.getApplicationInfo());
        try {
            readConfig();
            copyOriApk();
            Log.e(TAG, "libPath: "+getDefaultBackupLibPath() );
            doPrepare(base,getResDir(),getDefaultBackupLibPath());
            appClassLoader =getNewClassloader();
            MyLog.logM("nativeLib= "+getDefaultBackupLibPath()+"\nloaderApp:"+appClassLoader);
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
        String nativeLibDir = oriAppInfo.nativeLibraryDir;
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
    private static void releaseLib(boolean forceRelease)throws Throwable{
        String baseDir = getDefaultBaseDir();
        ZipArchive zipArchive = new ZipArchive(Paths.get(getResDir()));
        String abi = getABI();
        for (String entry : zipArchive.listEntries()) {
            if (entry.startsWith("lib/"+abi)){
                File soFile = new File(baseDir, entry);
                if (forceRelease||!soFile.exists()){
                    soFile.getParentFile().mkdirs();
                    ByteBuffer content = zipArchive.getContent(entry);
                    FileOutputStream fileOutputStream = new FileOutputStream(soFile);
                    fileOutputStream.getChannel().write(content);
                    fileOutputStream.close();
                }
            }
        }
        zipArchive.close();
    }
    private static void copyOriApk() throws Throwable {
        File file = new File(getResDir());
        file.getParentFile().mkdirs();
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
            releaseLib(true);
        }else {
            releaseLib(false);
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
    private static String getDefaultBaseDir(){
        return new File(appContext.getFilesDir().getPath() + "/ori_backup/" + "data/app/" + appContext.getPackageName()).getPath();

    }
    @NonNull
    private static String getDefaultBackupApkPath(){
        return new File(getDefaultBaseDir(),"base.apk").getPath();
    }
    private static String abi=null;
    @NonNull private static String getABI(){
        if (abi!=null)return abi;
        try {
            abi= (String) RefUtil.on(ApplicationInfo.class).F("primaryCpuAbi").get(oriAppInfo);
        } catch (Throwable e) {
        }
        if (abi!=null)return abi;
        abi= BuildCompat.getMayUsingABI(oriAppInfo.publicSourceDir);
        if (abi!=null)return abi;
        String nativeLibraryDir = oriAppInfo.nativeLibraryDir;
        if (nativeLibraryDir.contains("arm64")){
            abi="arm64-v8a";
        }else {
            abi = "armeabi-v7a";
        }
        return abi;
    }
    @NonNull
    private static String getDefaultBackupLibPath(){
        return new File(getDefaultBaseDir(),"lib/"+getABI()).getPath();
    }
    @NonNull
    private static String getResDir() {
        if (config!=null){
            String basePathFormat = config.getBasePathFormat(appContext.getApplicationInfo().nativeLibraryDir);
            if (!basePathFormat.isEmpty())return basePathFormat;
        }
        return getDefaultBackupApkPath();
    }


    public static ApplicationInfo getsApplicationInfo() {
        return oriAppInfo;
    }

    public static QPatchInfo getConfig(){return config;}
}
