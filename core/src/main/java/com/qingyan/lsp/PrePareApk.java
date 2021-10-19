package com.qingyan.lsp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.qingyan.qpatch_info.QPatchInfo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import qingyan.util.BuildCompat;
import qingyan.util.reflect.RefUtil;

public final class PrePareApk {
    private static final String ORI_APK_A_PATH = "base";
    private final Context appContext;
    private final QPatchInfo config;
    private final ApplicationInfo oriAppInfo;
    private static PrePareApk sIns;
    volatile private boolean hasFinish = false;
    private final File defaultBaseDir;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public synchronized static PrePareApk getInstance(Context appContext, QPatchInfo config, ApplicationInfo oriApplicationInfo) {
        if (sIns != null) return sIns;
        return sIns = new PrePareApk(appContext, config, oriApplicationInfo);
    }

    private PrePareApk(@NonNull Context appContext, QPatchInfo config, ApplicationInfo oriApplicationInfo) {
        this.appContext = appContext;
        this.config = config;
        this.oriAppInfo = oriApplicationInfo;
        this.defaultBaseDir=new File(appContext.getFilesDir().getPath() + "/ori_backup/" + "data/app/" + appContext.getPackageName());
        if (defaultBaseDir.exists()&&defaultBaseDir.isFile()){
            FileUtils.deleteQuietly(defaultBaseDir);
            defaultBaseDir.mkdirs();
        }
    }

    public void copyOriApk() throws Throwable {
        File file = new File(getResDir());
        file.getParentFile().mkdirs();
        int version = -1;
        try {
            version = appContext.getPackageManager().getPackageArchiveInfo(file.getPath(), 0).versionCode;
        } catch (Throwable ignore) {
        }
        int nowVer = 0;
        try {
            nowVer = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0).versionCode;
        } catch (Throwable ignored) {
        }
        startWaitThread();
        if (version < nowVer) {
            InputStream inputStream = appContext.getAssets().open(ORI_APK_A_PATH);
            FileUtils.copyInputStreamToFile(inputStream, file);
            releaseLib(true);
        } else {
            releaseLib(false);
        }
        hasFinish = true;
    }
    private void startWaitThread(){
        new Thread(() -> {
            try {
                Thread.sleep(25000);
                if (!hasFinish)
                    handler.post(() -> {
                        Toast.makeText(appContext, "首次加载需释放资源，请耐心等待", Toast.LENGTH_LONG).show();
                    });
            } catch (InterruptedException ignored) { }
        }).start();
    }
    @NonNull
    public String getDefaultBackupLibPath() {
        return new File(defaultBaseDir, "lib/" + getABI()).getPath();
    }

    private void releaseLib(boolean forceRelease) throws Throwable {
        ZipFile zipArchive = new ZipFile(getResDir());
        String abi = getABI();
        Enumeration<? extends ZipEntry> entries = zipArchive.entries();
        while (entries.hasMoreElements()){
            ZipEntry zipEntry = entries.nextElement();
            String entry = zipEntry.getName();
            if (entry.startsWith("lib/" + abi)) {
                File soFile = new File(defaultBaseDir, entry);
                if (forceRelease || !soFile.exists()) {
                    InputStream inputStream = zipArchive.getInputStream(zipEntry);
                    FileUtils.copyInputStreamToFile(inputStream,soFile);
                }
            }
        }
        zipArchive.close();
    }

    private String abi = null;

    @NonNull
    private String getABI() {
        if (abi != null) return abi;
        try {
            abi = (String) RefUtil.on(ApplicationInfo.class).F("primaryCpuAbi").get(oriAppInfo);
        } catch (Throwable e) {
        }
        if (abi != null) return abi;
        abi = BuildCompat.getMayUsingABI(oriAppInfo.publicSourceDir);
        if (abi != null) return abi;
        String nativeLibraryDir = oriAppInfo.nativeLibraryDir;
        if (nativeLibraryDir.contains("arm64")) {
            abi = "arm64-v8a";
        } else {
            abi = "armeabi-v7a";
        }
        return abi;
    }

    @NonNull
    public String getResDir() {
        if (config != null) {
            String basePathFormat = config.getBasePathFormat(appContext.getApplicationInfo().nativeLibraryDir);
            if (!basePathFormat.isEmpty()) return basePathFormat;
        }
        return getDefaultBackupApkPath();
    }

    @NonNull
    private String getDefaultBackupApkPath() {
        return new File(defaultBaseDir, "base.apk").getPath();
    }

}
