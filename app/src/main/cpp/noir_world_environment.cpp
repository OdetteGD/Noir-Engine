#include <jni.h>
#include <cmath>
#include <cstring>\n#include <cstdio>
#include <string>

namespace {
struct WorldEnvironment {
    float exposure = 1.25f;
    float ambient = 0.42f;
    float skyStrength = 1.0f;
    float sunStrength = 3.0f;
    float fogDensity = 0.015f;
    float cloudStrength = 0.55f;
    bool enabled = true;
};
WorldEnvironment g_env;
}

extern "C" JNIEXPORT void JNICALL
Java_com_noir_game_engine_NoirNative_setWorldEnvironment(
        JNIEnv*, jclass, jfloat exposure, jfloat ambient, jfloat skyStrength,
        jfloat sunStrength, jfloat fogDensity, jfloat cloudStrength, jboolean enabled) {
    g_env.exposure = std::fmax(0.1f, std::fmin(exposure, 4.0f));
    g_env.ambient = std::fmax(0.0f, std::fmin(ambient, 2.0f));
    g_env.skyStrength = std::fmax(0.0f, std::fmin(skyStrength, 4.0f));
    g_env.sunStrength = std::fmax(0.0f, std::fmin(sunStrength, 8.0f));
    g_env.fogDensity = std::fmax(0.0f, std::fmin(fogDensity, 0.25f));
    g_env.cloudStrength = std::fmax(0.0f, std::fmin(cloudStrength, 1.0f));
    g_env.enabled = enabled == JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_noir_game_engine_NoirNative_worldEnvironmentInfo(JNIEnv* env, jclass) {
    char info[256];
    std::snprintf(info, sizeof(info),
        "WorldEnvironment C++ | exposure %.2f | ambient %.2f | sky %.2f | sun %.2f | fog %.3f | clouds %.2f | %s",
        g_env.exposure,g_env.ambient,g_env.skyStrength,g_env.sunStrength,
        g_env.fogDensity,g_env.cloudStrength,g_env.enabled ? "enabled" : "disabled");
    return env->NewStringUTF(info);
}

// Shared reference shaders used by offline/native backends.
// GLES uses GLSL ES 3.0; Vulkan uses the same conceptual inputs when compiled to SPIR-V.
extern "C" JNIEXPORT jstring JNICALL
Java_com_noir_game_engine_NoirNative_worldEnvironmentGlesShader(JNIEnv* env, jclass) {
    static const char* src =
        "#version 300 es\n"
        "precision highp float;\n"
        "in vec3 vDir; out vec4 frag;\n"
        "uniform float uExposure,uAmbient,uSkyStrength,uSunStrength;\n"
        "void main(){vec3 d=normalize(vDir);float h=clamp(d.y*.5+.5,0.,1.);"
        "vec3 sky=mix(vec3(.62,.75,.94),vec3(.08,.18,.34),pow(h,.8));"
        "float sun=pow(max(dot(d,normalize(vec3(-.42,.82,.34))),0.),64.);"
        "vec3 c=(sky*uSkyStrength+vec3(.18,.20,.24)*uAmbient+vec3(1,.78,.55)*sun*uSunStrength)*uExposure;"
        "c=vec3(1.)-exp(-c);frag=vec4(pow(c,vec3(.4545)),1.);}";
    return env->NewStringUTF(src);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_noir_game_engine_NoirNative_worldEnvironmentVulkanShader(JNIEnv* env, jclass) {
    static const char* src =
        "Noir Vulkan WorldEnvironment profile: sky + sun + ambient + fog + clouds; "
        "compile this profile to SPIR-V for the Vulkan renderer pipeline.";
    return env->NewStringUTF(src);
}
