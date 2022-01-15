
#ifndef MYNATIVEUTILS_H
#define MYNATIVEUTILS_H
#include "../../include/base/object.h"
#include "../../include/native_util.h"
#include "../symbol_cache.h"
#include <jni.h>

namespace QyTool{
    extern JavaVM *sJvm;
    JNIEnv *get_env(int *attach);
    void del_env();
    void RegisterQyTool(JNIEnv *env);
}
#endif