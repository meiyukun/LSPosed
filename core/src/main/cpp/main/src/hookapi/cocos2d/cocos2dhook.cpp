#include "../MyNativeUtils.h"
#include "cocos2dhook.h"
#include <string.h>
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_qingyan_natives_LspNative_setAccessible(JNIEnv* env,jclass clazz, jlong address,jlong length) {
//_make_rwx((void*)address,
//           length);
//}
using namespace lspd;
namespace cocos2djs{
    const char* LIB_COCOS2DJS_NAME="libcocos2djs.so";
    const char* SYM_64="_ZN2se12ScriptEngine10evalStringEPKclPNS_5ValueES2_";
    const char* SYM_32="_ZN2se12ScriptEngine10evalStringEPKciPNS_5ValueES2_";
    void * (*ori_dlopen)(const char* path,int flag,void *a,void *b)= nullptr;

    void* fake_dlopen(const char* path,int flag,void *a,void *b){
        const char* sym;
#ifdef __aarch64__
        LOGE("我是64位");
        sym=SYM_64;
#else
        sym=SYM_32;
#endif
        auto x=strstr(path,LIB_COCOS2DJS_NAME);
        if (x!= nullptr){

        }
        return ori_dlopen(path,flag,a,b);
    }
    void hookCocos2dEval(){
        HookFunction(symbol_cache->do_dlopen,(void *)fake_dlopen,reinterpret_cast<void **>(&ori_dlopen));
    }
}

void doTest() {
}


static JNINativeMethod gMethods[] = {
         {"hookCocos2dEval", "()V", (void *)cocos2djs::hookCocos2dEval}
//         {"onEvalString","(Ljava/lang/String;)Ljava/lang/String;",(void *)onEvalString}

};
namespace QyTool{

        void RegisterCocos2dHook(JNIEnv *env){
//        REGISTER_LSP_NATIVE_METHODS(LspNative);
            RegisterNativeMethodsInternal(env,"org.lsposed.lspd.nativebridge.cocos2d.Cocos2dUtil",gMethods,
                                          sizeof(gMethods));
    }

}

