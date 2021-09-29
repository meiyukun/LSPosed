
#ifndef INLINEHOOKLSPTEST_QY_HOOK_H
#define INLINEHOOKLSPTEST_QY_HOOK_H
namespace lspd{
    inline int HookFunction(void *original, void *replace, void **backup);
}
#endif