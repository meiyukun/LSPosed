package com.qingyan.lsp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qingyan.qpatch_info.QPatchInfo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import qingyan.util.MyLog;
import qingyan.util.encrypt.FileRandomEncrypt;
import qingyan.util.encrypt.XORUtils;

public final class PrePareModules {
    private static final String XPOSED_MODULE_FILE_NAME_PREFIX = "libpatchq";
    private static final String XPOSED_MODULE_FILE_APATH = "qmodules";
    private static PrePareModules ins;
    private final Context appContext;
    private final File moduleBaseDir;
    private final ArrayList<File> releasedModules = new ArrayList<>();
    private boolean hasPrepared = false;

    public synchronized static PrePareModules getInstance(Context appContext) {
        if (ins != null) return ins;
        return ins = new PrePareModules(appContext);
    }

    private PrePareModules(@NonNull Context appContext) {
        this.appContext = appContext;
        this.moduleBaseDir = new File(appContext.getFilesDir().getPath() + "/qy_mods");
        if (moduleBaseDir.exists()&&moduleBaseDir.isFile()){
            moduleBaseDir.delete();
            moduleBaseDir.mkdirs();
        }
    }

    public void releaseModules(boolean force) throws Throwable {
        if (hasPrepared && !force) return;
        String[] list = appContext.getAssets().list(XPOSED_MODULE_FILE_APATH);
        if (list.length <= 0)
            throw new FileNotFoundException("assets:" + XPOSED_MODULE_FILE_APATH + ":没有找到任何模块");
        synchronized (releasedModules) {
            releasedModules.clear();
            for (String s : list) {
                try {
                    File module = new File(moduleBaseDir, XPOSED_MODULE_FILE_NAME_PREFIX + releasedModules.size() + ".s");
                    InputStream inputStream = appContext.getAssets().open(XPOSED_MODULE_FILE_APATH + "/" + s);
//                    Log.e("BugHook", "inputStream的available= "+inputStream.available()+";"+module.getPath()+"的大小:"+module.length(),new Exception() );
                    /*如果模块已经存在了*/
                    if (!force && module.exists() && module.length() == inputStream.available()) {
                        inputStream.close();
                        releasedModules.add(module);
                        continue;
                    }
                    FileUtils.copyToFile(inputStream, module);
                    releasedModules.add(module);
                    handleEncrypt(module);
                } catch (Throwable e) {
                    MyLog.logM(e);
                }
            }
            hasPrepared = true;
            if (releasedModules.isEmpty()) throw new Exception("没有成功释放任何模块，检查模块目录");
        }
    }
    private void handleEncrypt(@NonNull File mayEncryptFile){
        try {
            new ZipFile(mayEncryptFile);/*如果以zip可以直接打开说明没加密*/
        } catch (IOException ie) {
            try {
                FileRandomEncrypt.decrypt(mayEncryptFile);
            } catch (Throwable e) {
                MyLog.logM("解密失败:"+mayEncryptFile.getPath(),e );
                FileUtils.deleteQuietly(mayEncryptFile);
                releasedModules.remove(mayEncryptFile);
            }
        }
    }
    public List<File> getReleasedModules() {
        return releasedModules;
    }
    public void deleteAll(){
        synchronized (releasedModules){
            hasPrepared=false;
            FileUtils.deleteQuietly(moduleBaseDir);
            releasedModules.clear();
        }
    }
}
