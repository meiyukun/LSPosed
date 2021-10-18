package com.qingyan.lsp;

import de.robv.android.xposed.IXposedHookLoadPackage;
import qingyan.util.reflect.RefUtil;

public class VerifyCheck {
    public static void checkVersion(Object o) throws Exception {
        Class<?> c= (Class<?>) o;
        if (!(IXposedHookLoadPackage.class.isAssignableFrom(c))){
            throw new Exception();
        }
        Class<?> fieldClass = (Class<?>) RefUtil.on(c).F(c.getSimpleName()).get(null);
        if (fieldClass!=c)throw new Exception();
    }
}
