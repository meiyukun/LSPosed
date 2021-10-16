//
// Created by Lenovo on 2021-06-10.
//

#ifndef LSPOSED_LSP_HOOK_H
#define LSPOSED_LSP_HOOK_H

namespace QyHook{
    void* DlSyms(const char* libName,const char* sym);
    namespace OpenHook{
        void hookPath(const char* oriPath, const char* newPath);
    }
    namespace SocketHook{
        void hookSocket();
    }
    namespace BinderHook{
        void hook();
    }
}
#endif //LSPOSED_LSP_HOOK_H
