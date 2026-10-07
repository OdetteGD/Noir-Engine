#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_noir_game_engine_NoirNative_glesBackendInfo(JNIEnv* env, jclass) {
    return env->NewStringUTF("OpenGL ES 3.x forward renderer");
}
