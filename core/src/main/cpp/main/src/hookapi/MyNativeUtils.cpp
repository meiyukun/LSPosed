#include "MyNativeUtils.h"
#include "cocos2d/cocos2dhook.h"
#include "lsp_hook.h"
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_qingyan_natives_LspNative_setAccessible(JNIEnv* env,jclass clazz, jlong address,jlong length) {
//_make_rwx((void*)address,
//           length);
//}
using namespace lspd;
LSP_DEF_NATIVE_METHOD(void, LspNative, setAccessible, jlong address,jlong length) {
    _make_rwx((void*)address,
              length);
}
LSP_DEF_NATIVE_METHOD(void, LspNative, openHook, jstring oriPath,jstring newPath) {
    auto oripath=env->GetStringUTFChars(oriPath, nullptr);
    auto newpath=env->GetStringUTFChars(newPath, nullptr);
    QyHook::OpenHook::hookPath(oripath,newpath);
}

static JNINativeMethod gMethods[] = {
        LSP_NATIVE_METHOD(LspNative, setAccessible, "(JJ)V"),
        LSP_NATIVE_METHOD(LspNative, openHook, "(Ljava/lang/String;Ljava/lang/String;)V"),
};
namespace QyTool{
    JavaVM *sJvm;
    void RegisterQyTool(JNIEnv *env){
            env->GetJavaVM(&sJvm);
            REGISTER_LSP_NATIVE_METHODS(LspNative);
            RegisterCocos2dHook(env);
   }
    void del_env() {
        sJvm->DetachCurrentThread();
    }
    JNIEnv *get_env(int *attach) {
        if (sJvm == NULL) return NULL;
        *attach = 0;
        JNIEnv *jni_env = NULL;
        int status = sJvm->GetEnv((void **)&jni_env, JNI_VERSION_1_6);
        if (status == JNI_EDETACHED || jni_env == NULL) {
            status = sJvm->AttachCurrentThread(&jni_env, NULL);
            if (status < 0) {
                jni_env = NULL;
            } else {
                *attach = 1;
            }
        }
        return jni_env;
    }
    jobject new_global_object(jobject obj) {
        int attach = 0;
        JNIEnv *env = get_env(&attach);
        jobject ret = env->NewGlobalRef(obj);
        if (attach == 1) {
            del_env();
        }
        return ret;
    }
}

