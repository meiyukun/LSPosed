package org.lsposed.lspd.nativebridge.cocos2d;

import android.util.ArraySet;

public class Cocos2dUtil {
    private static final ArraySet<Cocos2dEvalListener> listeners=new ArraySet<>();
    public static void registerEvalStringListener(Cocos2dEvalListener listener){
        synchronized (listeners){
            if (listeners.isEmpty())hookCocos2dEval();
            listeners.add(listener);
        }
    }
    private static native void hookCocos2dEval();
    private static String onEvalString(String ori){
        String ret=ori;
        synchronized (listeners){
            for (Cocos2dEvalListener listener : listeners) {
                ret = listener.onEvalString(ori);
            }
        }
        return ret;
    }
}
