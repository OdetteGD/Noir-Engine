#include <jni.h>
#include <vulkan/vulkan.h>

extern "C" JNIEXPORT jboolean JNICALL
Java_com_noir_game_engine_NoirNative_vulkanSupported(JNIEnv*, jclass) {
    uint32_t count = 0;
    if (vkEnumerateInstanceExtensionProperties(nullptr, &count, nullptr) != VK_SUCCESS || count == 0) return JNI_FALSE;

    VkApplicationInfo app{};
    app.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    app.pApplicationName = "Noir 3D Engine";
    app.applicationVersion = VK_MAKE_VERSION(1,0,0);
    app.engineName = "Noir";
    app.engineVersion = VK_MAKE_VERSION(1,0,0);
    app.apiVersion = VK_API_VERSION_1_0;

    VkInstanceCreateInfo ci{};
    ci.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    ci.pApplicationInfo = &app;

    VkInstance instance = VK_NULL_HANDLE;
    if (vkCreateInstance(&ci, nullptr, &instance) != VK_SUCCESS) return JNI_FALSE;

    uint32_t physicalCount = 0;
    VkResult result = vkEnumeratePhysicalDevices(instance, &physicalCount, nullptr);
    vkDestroyInstance(instance, nullptr);
    return (result == VK_SUCCESS && physicalCount > 0) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_noir_game_engine_NoirNative_vulkanStatus(JNIEnv* env, jclass) {
    uint32_t count = 0;
    if (vkEnumerateInstanceExtensionProperties(nullptr, &count, nullptr) != VK_SUCCESS || count == 0)
        return env->NewStringUTF("Vulkan loader unavailable");

    VkInstanceCreateInfo ci{};
    ci.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    VkApplicationInfo app{};
    app.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    app.pApplicationName = "Noir 3D Engine";
    app.engineName = "Noir";
    app.apiVersion = VK_API_VERSION_1_0;
    ci.pApplicationInfo = &app;

    VkInstance instance = VK_NULL_HANDLE;
    if (vkCreateInstance(&ci, nullptr, &instance) != VK_SUCCESS)
        return env->NewStringUTF("Vulkan instance creation failed");

    uint32_t physicalCount = 0;
    VkResult result = vkEnumeratePhysicalDevices(instance, &physicalCount, nullptr);
    vkDestroyInstance(instance, nullptr);

    if (result != VK_SUCCESS || physicalCount == 0)
        return env->NewStringUTF("No Vulkan physical device");
    return env->NewStringUTF("Vulkan 1.0 device detected");
}
