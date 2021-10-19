package com.qingyan.lsp;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;

import com.qingyan.natives.LspNative;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.lsposed.lspd.BuildConfig;
import org.lsposed.lspd.yahfa.hooker.YahfaHooker;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedInit;
import qingyan.util.MyLog;
import qingyan.util.UtilManager;

public class App extends Application{
    public final static String TAG="BugHook";
    static {
        System.loadLibrary("patchs");
//        Yahfa.init(Build.VERSION.SDK_INT);
        YahfaHooker.init();
        XposedInit.startsSystemServer=false;
    }
    @Override
    protected void attachBaseContext(Context base) {
        UtilManager.init(base, BuildConfig.DEBUG);
        super.attachBaseContext(base);
        StringBuilder nativePath = new StringBuilder();
        String installedLibDir=getApplicationInfo().nativeLibraryDir;
        String abiStr=installedLibDir.substring(installedLibDir.lastIndexOf('/')+1);
//        if (is64)abiStr="arm64-v8a"; else abiStr="armeabi-v7a";
        nativePath.append(installedLibDir).append(File.pathSeparator);
        nativePath.append(getPackageCodePath()).append("!/lib/").append(abiStr).append(File.pathSeparator);
        MyLog.logM(TAG,"nativeLib="+nativePath);
//        File cacheDexFilePath=getExternalFilesDir("dex");
//        File dexFile=new File(cacheDexFilePath,"classes.dex");
//        File appDexFile=new File(cacheDexFilePath,"app.dex");
////        FileUtils.copyFile(new File(getPackageCodePath()),dexFile);
//        initQySetFramePath(dexFile.getPath());
//        ModuleClass.doHook(appDexFile.getPath());
        try {
            ApplicationInfo greenOneInfo=getPackageManager().getApplicationInfo("com.qingyan.greenone",0);
            String greenOnePath = greenOneInfo.publicSourceDir;
            String aPath=getExternalFilesDir("")+"/a.txt";
            String bPath=getExternalFilesDir("")+"/b.txt";
            LspNative.openHook(aPath,bPath);
            File aFile=new File(aPath);
            aFile.createNewFile();
            String s = FileUtils.readFileToString(aFile, StandardCharsets.UTF_8);
//            LSPLoader.loadModule(greenOnePath,null,getPackageManager().getApplicationInfo(getPackageName(),0),App.class.getClassLoader());
//            AppClass.doMain();
            MainActivity.class.newInstance();
            Method test1 = App.class.getDeclaredMethod("test1");
            Method test2 = App.class.getDeclaredMethod("test2");
            Method test3 = App.class.getDeclaredMethod("test3");
//            findAndHookMethod(App.class, "test1", new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    param.setResult("哈哈");
//                }
//            });
//            Yahfa.backupAndHookNative(test1,test2,test3);
            Log.e(TAG, "test1="+test1()+"; test2="+test2()+  ";test3="+test3());
//            test1();
//            test3();
        } catch (Exception e) {
            e.printStackTrace();
        }

        findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.e(TAG, "beforeHookedMethod: "+"onCreate" );
            }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e(TAG, "afterHookedMethod: "+"onCreate" );
                    }
                }
        );
    }
    public static int cal(int a,int b){
        return a+b;
    }
    public static String test1(){return "1";}
    public static String test2(){return "2";}
    public static String test3(){return "3";}
}
