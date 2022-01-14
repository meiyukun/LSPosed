package com.qingyan.lsp;

import android.content.pm.PackageInfo;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import qingyan.qhot.module.PackageManagerCompat;
import qingyan.util.reflect.ReflectInfo;

/**
 * 主要是对华为手机的兼容
 */
public class KillSigTool {
    private static boolean hasKilled;
    public static void kill(PackageInfo packageInfo){
        if (hasKilled)return;
        hasKilled=true;
        XposedHelpers.findAndHookMethod(ReflectInfo.clz_ApplicationPackageManager, "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                replacePkg(param, packageInfo);
            }
        });
        XposedHelpers.findAndHookMethod(ReflectInfo.clz_ApplicationPackageManager, "getPackageInfoAsUser", String.class, int.class,int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                replacePkg(param, packageInfo);
            }
        });
    }

    private static void replacePkg(XC_MethodHook.MethodHookParam param, PackageInfo packageInfo) {
        String pkgName = (String) param.args[0];
        if (pkgName.equals(packageInfo.packageName)) {
            PackageInfo result = (PackageInfo) param.getResult();
            PackageManagerCompat.fixPackageInfo(packageInfo, result);
        }
    }
}
