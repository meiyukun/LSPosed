#include "../../include/base/object.h"
#include <jni.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_qingyan_natives_LspNative_setAccessible(JNIEnv* env,jclass clazz, jlong address,jlong length) {
_make_rwx((void*)address,
           length);
}