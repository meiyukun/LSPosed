package com.qingyan.natives;

public class LspNative {
    public static native void openHook(String oriPath,String newPath);
    public static native void socketHook();
    public static native void setAccessible(long address,long length);
}
