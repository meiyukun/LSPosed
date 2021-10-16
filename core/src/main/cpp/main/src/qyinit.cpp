//
// Created by Lenovo on 2021-05-30.
//
#include <jni.h>
#include <fcntl.h>
#include "logging.h"
#include "jni/yahfa.h"
#include "symbol_cache.h"
#include "context.h"
#include "../include/base/object.h"
#include <dlfcn.h>
#include <lsp_hook.h>
using namespace lspd;
using namespace QyHook;
JNIEXPORT int JNICALL JNI_OnLoad(JavaVM* vm,void *unused) {
    JNIEnv *env;
    LOGE( "qingyanModeLsp加载开始");
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    auto context=Context::GetInstance();
    context->InitLess(env);
//    QyHook::BinderHook::hook();
    return JNI_VERSION_1_6;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_qingyan_natives_LspNative_openHook(JNIEnv *env, jclass clazz, jstring ori_path,
                                            jstring new_path) {
    using namespace QyHook;
    auto ori=env->GetStringUTFChars(ori_path, nullptr);
    auto newP=env->GetStringUTFChars(new_path, nullptr);
    OpenHook::hookPath(ori,newP);
}