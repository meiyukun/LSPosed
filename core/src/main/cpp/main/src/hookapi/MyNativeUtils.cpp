#include "MyNativeUtils.h"
#include "cocos2d/cocos2dhook.h"
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
static JNINativeMethod gMethods[] = {
        LSP_NATIVE_METHOD(LspNative, setAccessible, "(JJ)V")
};
namespace QyTool{
    void RegisterQyTool(JNIEnv *env){
//        try {
            REGISTER_LSP_NATIVE_METHODS(LspNative);
//            RegisterCocos2dHook(env);
//        } catch (...) {
//            LOGE("%s",...)
//
//        }
//        RegisterNativeMethodsInternal(env,"org.lsposed.lspd.nativebridge.cocos2d.Cocos2dUtil");
    }
}

