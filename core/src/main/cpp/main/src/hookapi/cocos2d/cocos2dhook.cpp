#include "../MyNativeUtils.h"
#include "cocos2dhook.h"
#include <cstring>
#include <context.h>
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_qingyan_natives_LspNative_setAccessible(JNIEnv* env,jclass clazz, jlong address,jlong length) {
//_make_rwx((void*)address,
//           length);
//}
//__arm__             armeabi
//__arm__             armeabi-v7
//        __aarch64__         arm64-v8a
//        __i386__            x86
//        __x86_64__          x86_64
using namespace lspd;
using namespace QyTool;
namespace cocos2djs{

    jclass cocos2djsClz;
    const char* cocos2djsClzStr="org.lsposed.lspd.nativebridge.cocos2d.Cocos2dUtil";
    const char* LIB_COCOS2DJS_NAME="libcocos2djs.so";
    jmethodID onEvalStringId;
    const char* SYM_64="_ZN2se12ScriptEngine10evalStringEPKclPNS_5ValueES2_";
    const char* SYM_32="_ZN2se12ScriptEngine10evalStringEPKciPNS_5ValueES2_";
    void * (*ori_dlopen)(const char* path,int flag,void *a,void *b)= nullptr;
    void * (*ori_evalString)(void* a1,const char *code,size_t size,void *a4,const char *path)= nullptr;

    void* evalString(void* a1,const char *code,size_t size,void *a4,const char *path){
        LOGE("修改前 sizeCal= %d, size = %d ,pointer = %p,path = %s", strlen(code), size,code,path);
        int attah;
        JNIEnv *mEnv= get_env(&attah);
//        LOGE("js:=\n%s",code);
        auto jsUtf=mEnv->NewStringUTF(code);
        auto pathUtf=mEnv->NewStringUTF(path);
//        LOGE("方法ID = %d",onEvalStringId);
        jstring retstr= (jstring)mEnv->CallStaticObjectMethod(cocos2djsClz,onEvalStringId,pathUtf,jsUtf);
        const char* retCstr=mEnv->GetStringUTFChars(retstr, nullptr);
        size_t afterLen= strlen(retCstr);
        LOGE("修改后：len=%d , pointer = %p",afterLen,retCstr);
        if (size<=0){
//            afterLen=size;
            LOGE("内容：$s ,\n修改内容:%s",code,retCstr);
        }
//        strcpy((char *)code,retCstr);
        if (attah)del_env();
//        return ori_evalString(a1,code, size,a4,path);

        return ori_evalString(a1,retCstr, afterLen,a4,path);
    }

    void* fake_dlopen(const char* path,int flag,void *a,void *b){
        LOGE("dlopen:%s",path);
        const char* sym;
#ifdef __aarch64__
        sym=SYM_64;
#else
#ifdef __x86_64__
        sym=SYM_64;
#else
        sym=SYM_32;
#endif
#endif
        auto x=strstr(path,LIB_COCOS2DJS_NAME);
        void* ret= ori_dlopen(path,flag,a,b);
        if (x!= nullptr){
            auto symbol=Dlsym(ret, sym);
            if (symbol){
                HookFunction(symbol,(void *)evalString,reinterpret_cast<void **>(&ori_evalString));
                UnhookFunction((void *)ori_dlopen);
            }
        }
        return ret;
    }
    extern "C"
    void hookCocos2dEval( JNIEnv* env,
                          jclass clz){
        LOGE("hookCocos2dEval invoking!");
        cocos2djsClz = clz;
        onEvalStringId=env->GetStaticMethodID(clz,"onEvalString","(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        HookFunction(symbol_cache->do_dlopen,(void *)fake_dlopen,reinterpret_cast<void **>(&ori_dlopen));
    }
}


static JNINativeMethod gMethodss[] = {
         {"hookCocos2dEval", "()V", (void *)cocos2djs::hookCocos2dEval}
//         {"onEvalString","(Ljava/lang/String;)Ljava/lang/String;",(void *)onEvalString}

};
namespace QyTool{
        using namespace cocos2djs;
        void RegisterCocos2dHook(JNIEnv *env){
            RegisterNativeMethodsInternal(env,cocos2djsClzStr,gMethodss,
                                          sizeof(gMethodss));
    }

}

