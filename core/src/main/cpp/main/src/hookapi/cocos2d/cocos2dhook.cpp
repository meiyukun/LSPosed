#include "../MyNativeUtils.h"
#include "cocos2dhook.h"
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_qingyan_natives_LspNative_setAccessible(JNIEnv* env,jclass clazz, jlong address,jlong length) {
//_make_rwx((void*)address,
//           length);
//}
using namespace lspd;

void hookCocos2dEval(){

}

static JNINativeMethod gMethods[] = {
         {"hookCocos2dEval", "()V", (void *)hookCocos2dEval}
//         {"onEvalString","(Ljava/lang/String;)Ljava/lang/String;",(void *)onEvalString}

};
namespace QyTool{

        void RegisterCocos2dHook(JNIEnv *env){
//        REGISTER_LSP_NATIVE_METHODS(LspNative);
            RegisterNativeMethodsInternal(env,"org.lsposed.lspd.nativebridge.cocos2d.Cocos2dUtil",gMethods,
                                          sizeof(gMethods));
    }

}

