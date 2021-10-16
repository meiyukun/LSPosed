//
// Created by LENOVO on 2021/7/8.
//
#include "lsp_hook.h"
#include <context.h>
using namespace lspd;
namespace QyHook::BinderHook{
//    void*  (*ori_getContextObject)(void*, unsigned int,int64_t*)= nullptr;
//    void* fake_getContextObject(void* c, int p,int64_t* d){
//        LOGE("调用getContextObject,c=%p,p=%d,d=%d",c,p,*d);
//        return ori_getContextObject(c,p,d);
//    }
//    void*  (*ori_defaultServiceManager)(void*,void*)= nullptr;
//    void* fake_defaultServiceManager(void* a,void* b){
//        LOGE("调用了defaultServiceManager,a=%p,b=%d,c=%d",a,b);
//        return ori_defaultServiceManager(a,b);
//    }
//    void hook(){
//        auto sym_getContextObject = DlSyms("libbinder.so",
//                                           "_ZN7android12ProcessState16getContextObjectERKNS_2spINS_7IBinderEEE");
//        auto sym_defaultServiceManager = DlSyms("libbinder.so",
//                                           "_ZN7android21defaultServiceManagerEv");
//
//        LOGE("sym_getContextObj=%p,_defaultServiceManager=%p", sym_getContextObject,sym_defaultServiceManager);
////        HookFunction(sym_getContextObject, (void*)fake_getContextObject, reinterpret_cast<void **>(&ori_getContextObject));
////        HookFunction(sym_defaultServiceManager, (void*)fake_defaultServiceManager, reinterpret_cast<void **>(&ori_defaultServiceManager));
////        auto de=ori_defaultServiceManager();
//        return;
//    }
}