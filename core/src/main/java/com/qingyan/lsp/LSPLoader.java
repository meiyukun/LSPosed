package com.qingyan.lsp;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.os.Environment;

import android.text.TextUtils;
import android.util.Log;

import com.qingyan.App;

import qingyan.util.MyLog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.lsposed.lspd.hooker.LoadedApkCstrHooker;
import org.lsposed.lspd.service.ConfigFileManager;
import org.lsposed.lspd.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dalvik.system.DelegateLastClassLoader;
import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import qingyan.util.reflect.RefUtil;
import qingyan.util.reflect.ReflectInfo;

public class LSPLoader {

    private static final String TAG ="BugHook";
    private static final String DIR_BASE = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final AtomicBoolean hasInited = new AtomicBoolean(false);
    private static Context appContext;
    private static final String shouldLoadModuleConfigAP = "qingyan/Qpatch_info/modules.txt";
    private static final String XPOSED_MODULE_FILE_PATH = "xposed_config/modules.list";

    private static final String XPOSED_MODULE_FILE_NAME_PREFIX = "libpatchq";

    public static void initAndLoadModules(Context context,ClassLoader appLoader) {
        if (!hasInited.compareAndSet(false, true)) {
            MyLog.logM(TAG, "Has been init");
            return;
        }
        if (context == null) {
            MyLog.logM(TAG, "Try to init with context null");
            return;
        }
        appContext = context;


        List<String> modulePathList = new ArrayList<>();
//        loadAllInstalledModule(context);


        boolean isForceLoadExt=false;
        try {
            File qingyanLoadConif = new File(context.getExternalFilesDir("qingyan"), "ext.txt");
            if (!qingyanLoadConif.exists()){
                qingyanLoadConif.createNewFile();
            }
            isForceLoadExt="1".equals(FileUtils.readFileToString(qingyanLoadConif,StandardCharsets.UTF_8));
        } catch (Exception ignore) {
        }
        if (!isForceLoadExt)
            modulePathList.addAll(getXposedModulesFromLibPath(context)) ;
        if (modulePathList.isEmpty())
            modulePathList.addAll(getShouldLoadQYModules(context));
        MyLog.logM(TAG,"modules="+modulePathList);
        for (String modulePath : modulePathList) {
            if (!TextUtils.isEmpty(modulePath)) {
//                LSPLoader.loadModule(modulePath, null, context.getApplicationInfo(), context.getClassLoader());

                boolean result=XposedInit.loadModule("module",modulePath, ConfigFileManager.loadModule(modulePath));
                if (!result){
                    Log.e(TAG, "initAndLoadModules fail : "+modulePath );
                }

            }
        }
        LoadedApk loadedApk = (LoadedApk) ReflectInfo.obj_LoadedApk;
        String processName = (String) XposedHelpers.getObjectField(ReflectInfo.obj_appBindData, "processName");
        XC_LoadPackage.LoadPackageParam lpparam = new XC_LoadPackage.LoadPackageParam(
                XposedBridge.sLoadedPackageCallbacks);
        lpparam.packageName = context.getPackageName();
        lpparam.processName = processName;
        lpparam.classLoader = appLoader;
        lpparam.appInfo = loadedApk.getApplicationInfo();
        lpparam.isFirstApplication =true;
//                XposedInit.loadedModules.add(modulePath);
        XposedInit.loadedPackagesInProcess.add(context.getPackageName());
        XC_LoadPackage.callAll(lpparam);
        XposedHelpers.findAndHookConstructor(LoadedApk.class,
                ActivityThread.class, ApplicationInfo.class, CompatibilityInfo.class,
                ClassLoader.class, boolean.class, boolean.class, boolean.class,
                new LoadedApkCstrHooker());
    }

    private static List<String> getXposedModulesFromLibPath(Context context) {
        String libPath = App.getsApplicationInfo().nativeLibraryDir;
//        MyLog.d(TAG, "Current loaded module libPath ----> " + libPath);
        ArrayList<String> inLibPathList=new ArrayList<>();
        File libFileParent = new File(libPath);
        if (!libFileParent.exists()) {
            return inLibPathList;
        }

        File[] childFileList = libFileParent.listFiles();
        if (childFileList != null && childFileList.length > 0) {
            for (File libFile : childFileList) {
                String fileName = libFile.getName();
                if (fileName.startsWith(XPOSED_MODULE_FILE_NAME_PREFIX)) {
                    MyLog.logM( "add xposed modules from libPath, this lib path is --> " + libFile);
                    inLibPathList.add(libFile.getAbsolutePath());
                }
            }
        }
        return inLibPathList;
    }
    private static List<String> getShouldLoadQYModules(Context context){
        List<String> list=new ArrayList<>();
        try {
            final ArrayList<String> packageNames= App.getConfig().defaultModuleList;
            for (String packageName:packageNames){
                String apkPath= context.getPackageManager().getApplicationInfo(packageName, 0).publicSourceDir;
                list.add(apkPath);
            }
        } catch (Exception e) {
            Utils.logE("",e);
        }
        return list;
    }



    public static Context getAppContext() {
        return appContext;
    }
}
