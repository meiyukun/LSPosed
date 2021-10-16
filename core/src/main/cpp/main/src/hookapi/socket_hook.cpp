//
// Created by Lenovo on 2021-06-11.
//

#include "lsp_hook.h"
#include <netinet/in.h>
#include <arpa/inet.h>
#include "../jni/yahfa.h"
#include <logging.h>
#include <context.h>
using namespace lspd;
namespace QyHook::SocketHook{
//    typedef struct sockaddr_in sockaddr_in;
    int  (*ori_connect)(int __fd, const struct sockaddr* __addr, socklen_t __addr_length)= nullptr;
    int fake_connect(int __fd, const struct sockaddr* __addr, socklen_t __addr_length){
        sockaddr_in* inAddr=(sockaddr_in*)__addr;
        auto port=inAddr->sin_port;
        auto addr=inAddr->sin_addr;
        char buff[INET_ADDRSTRLEN] ;
        const char* ret4=inet_ntop(AF_INET,&addr,buff, sizeof(buff));
        LOGE("端口号：%d,地址：%s，返回值:%s",port,buff,ret4);
//        if (!ret4){
            char buff6[INET6_ADDRSTRLEN];
            const char* ret6=inet_ntop(AF_INET6,&addr,buff6, sizeof(buff6));
            LOGE("666:端口号：%d,地址：%s，返回值:%s",port,buff6,ret6);
//        }
        return ori_connect(__fd,__addr,__addr_length);
    }
    void hookSocket() {
//        socket(AF_LOCAL, SOCK_STREAM, 0);
//        connect();
//        inet_aton();
        if(!ori_connect)
         HookFunction((void*)connect,(void*)fake_connect, reinterpret_cast<void **>(&ori_connect));
    }

}
extern "C"
JNIEXPORT void JNICALL
Java_com_qingyan_natives_LspNative_socketHook(JNIEnv *env, jclass clazz) {
    QyHook::SocketHook::hookSocket();
}