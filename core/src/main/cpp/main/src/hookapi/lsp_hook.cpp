//
// Created by Lenovo on 2021-06-10.
//

#include <dlfcn.h>

#include "lsp_hook.h"
#include <map>
#include "../jni/yahfa.h"
#include <logging.h>
#include <context.h>
using namespace std;
using namespace lspd;
namespace QyHook{

    void* DlSyms(const char* libName,const char* sym){
        using namespace lspd;
        const SandHook::ElfImg libcElf=SandHook::ElfImg(libName);
        return Dlsym(libcElf,sym);
    }
    namespace OpenHook{
        static map<const char* ,const char*> relocateMap;
        static bool hasHooked= false;
        int  (*ori_Openat)(int,const char *, int...)= nullptr;
        int  (*ori_execl)(const char *path, const char *arg,void* d)= nullptr;

        int  (*ori_Open)(const char *, int...)= nullptr;
        int fakeOpenAt(int dirfd, const char* pathname, int flags){
            LOGE("openat：%s",pathname);
            for(auto & it : relocateMap){
                if (strcmp(pathname,it.first)==0){
                    pathname=it.second;
                    LOGE("openAt已经替换到:%s 到 %s",it.first,it.second);
                    break;
                }
            }
            return ori_Openat(dirfd,pathname,flags);
        }
        int fakeExecl(const char *path, const char *arg,void* d){
            LOGE("调用了fakeExecl");
            LOGE("execl：path = %s ; arg = %s",path,arg);
            return ori_execl(path,arg,d);
        }
        void hookExecl(){
            auto __execl=DlSyms("libc.so","popen");
            LOGE("hook Execl成功？=%p", __execl);
            HookFunction(__execl,(void*) fakeExecl, reinterpret_cast<void **>(&ori_execl));
        }
        void hookPath(const char* oriPath, const char* newPath){
            if(!hasHooked){
                hasHooked= true;
                auto openAtAddr=DlSyms("libc.so","__openat");
                HookFunction(openAtAddr,(void*)fakeOpenAt, reinterpret_cast<void **>(&ori_Openat));
            }
            relocateMap[oriPath]=newPath;
        }
    }
}
