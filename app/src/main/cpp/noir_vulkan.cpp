#include <jni.h>
#include <vulkan/vulkan.h>

#include <android/log.h>
#include <cstring>
#include <string>

namespace {

constexpr const char *TAG = "NoirVulkan";

struct Runtime {
    VkInstance instance = VK_NULL_HANDLE;
    VkPhysicalDevice physicalDevice = VK_NULL_HANDLE;
    VkDevice device = VK_NULL_HANDLE;
    VkQueue graphicsQueue = VK_NULL_HANDLE;
    uint32_t graphicsQueueFamily = UINT32_MAX;
    uint32_t apiVersion = VK_API_VERSION_1_0;
    VkPhysicalDeviceProperties properties{};
    bool initialized = false;
};

Runtime g_runtime;

void resetRuntime() {
    if (g_runtime.device != VK_NULL_HANDLE) {
        vkDeviceWaitIdle(g_runtime.device);
        vkDestroyDevice(g_runtime.device, nullptr);
    }
    if (g_runtime.instance != VK_NULL_HANDLE) {
        vkDestroyInstance(g_runtime.instance, nullptr);
    }
    g_runtime = Runtime{};
}

bool hasInstanceExtension(const char *name) {
    uint32_t count = 0;
    if (vkEnumerateInstanceExtensionProperties(nullptr, &count, nullptr) != VK_SUCCESS) {
        return false;
    }
    if (count == 0) return false;

    VkExtensionProperties *extensions = new VkExtensionProperties[count];
    VkResult result = vkEnumerateInstanceExtensionProperties(nullptr, &count, extensions);
    bool found = false;
    if (result == VK_SUCCESS) {
        for (uint32_t i = 0; i < count; ++i) {
            if (std::strcmp(extensions[i].extensionName, name) == 0) {
                found = true;
                break;
            }
        }
    }
    delete[] extensions;
    return found;
}

bool hasDeviceExtension(VkPhysicalDevice device, const char *name) {
    uint32_t count = 0;
    if (vkEnumerateDeviceExtensionProperties(device, nullptr, &count, nullptr) != VK_SUCCESS) {
        return false;
    }
    if (count == 0) return false;

    VkExtensionProperties *extensions = new VkExtensionProperties[count];
    VkResult result = vkEnumerateDeviceExtensionProperties(device, nullptr, &count, extensions);
    bool found = false;
    if (result == VK_SUCCESS) {
        for (uint32_t i = 0; i < count; ++i) {
            if (std::strcmp(extensions[i].extensionName, name) == 0) {
                found = true;
                break;
            }
        }
    }
    delete[] extensions;
    return found;
}

bool findGraphicsQueue(VkPhysicalDevice device, uint32_t *outFamily) {
    uint32_t count = 0;
    vkGetPhysicalDeviceQueueFamilyProperties(device, &count, nullptr);
    if (count == 0) return false;

    VkQueueFamilyProperties *families = new VkQueueFamilyProperties[count];
    vkGetPhysicalDeviceQueueFamilyProperties(device, &count, families);

    bool found = false;
    for (uint32_t i = 0; i < count; ++i) {
        if ((families[i].queueFlags & VK_QUEUE_GRAPHICS_BIT) != 0 &&
            families[i].queueCount > 0) {
            *outFamily = i;
            found = true;
            break;
        }
    }

    delete[] families;
    return found;
}

bool createInstance() {
    if (!hasInstanceExtension(VK_KHR_SURFACE_EXTENSION_NAME)) {
        return false;
    }

    VkApplicationInfo app{};
    app.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    app.pApplicationName = "Noir 3D Game Engine";
    app.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    app.pEngineName = "Noir";
    app.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    app.apiVersion = VK_API_VERSION_1_0;

    const char *extensions[2];
    uint32_t extensionCount = 0;
    extensions[extensionCount++] = VK_KHR_SURFACE_EXTENSION_NAME;

    if (hasInstanceExtension("VK_KHR_android_surface")) {
        extensions[extensionCount++] = "VK_KHR_android_surface";
    }

    VkInstanceCreateInfo ci{};
    ci.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    ci.pApplicationInfo = &app;
    ci.enabledExtensionCount = extensionCount;
    ci.ppEnabledExtensionNames = extensions;

    return vkCreateInstance(&ci, nullptr, &g_runtime.instance) == VK_SUCCESS;
}

bool pickPhysicalDevice() {
    uint32_t count = 0;
    if (vkEnumeratePhysicalDevices(g_runtime.instance, &count, nullptr) != VK_SUCCESS || count == 0) {
        return false;
    }

    VkPhysicalDevice *devices = new VkPhysicalDevice[count];
    VkResult result = vkEnumeratePhysicalDevices(g_runtime.instance, &count, devices);
    if (result != VK_SUCCESS) {
        delete[] devices;
        return false;
    }

    bool found = false;
    for (uint32_t i = 0; i < count; ++i) {
        uint32_t queueFamily = UINT32_MAX;
        if (!findGraphicsQueue(devices[i], &queueFamily)) continue;

        VkPhysicalDeviceProperties props{};
        vkGetPhysicalDeviceProperties(devices[i], &props);

        g_runtime.physicalDevice = devices[i];
        g_runtime.graphicsQueueFamily = queueFamily;
        g_runtime.properties = props;
        g_runtime.apiVersion = props.apiVersion;
        found = true;
        break;
    }

    delete[] devices;
    return found;
}

bool createLogicalDevice() {
    if (g_runtime.physicalDevice == VK_NULL_HANDLE ||
        g_runtime.graphicsQueueFamily == UINT32_MAX) {
        return false;
    }

    float priority = 1.0f;
    VkDeviceQueueCreateInfo queueInfo{};
    queueInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueInfo.queueFamilyIndex = g_runtime.graphicsQueueFamily;
    queueInfo.queueCount = 1;
    queueInfo.pQueuePriorities = &priority;

    const char *deviceExtensions[1];
    uint32_t extensionCount = 0;
    if (hasDeviceExtension(g_runtime.physicalDevice, VK_KHR_SWAPCHAIN_EXTENSION_NAME)) {
        deviceExtensions[extensionCount++] = VK_KHR_SWAPCHAIN_EXTENSION_NAME;
    }

    VkPhysicalDeviceFeatures features{};

    VkDeviceCreateInfo deviceInfo{};
    deviceInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
    deviceInfo.queueCreateInfoCount = 1;
    deviceInfo.pQueueCreateInfos = &queueInfo;
    deviceInfo.pEnabledFeatures = &features;
    deviceInfo.enabledExtensionCount = extensionCount;
    deviceInfo.ppEnabledExtensionNames = deviceExtensions;

    if (vkCreateDevice(g_runtime.physicalDevice, &deviceInfo, nullptr, &g_runtime.device) != VK_SUCCESS) {
        return false;
    }

    vkGetDeviceQueue(
        g_runtime.device,
        g_runtime.graphicsQueueFamily,
        0,
        &g_runtime.graphicsQueue
    );

    return g_runtime.graphicsQueue != VK_NULL_HANDLE;
}

std::string apiString(uint32_t version) {
    return std::to_string(VK_VERSION_MAJOR(version)) + "." +
           std::to_string(VK_VERSION_MINOR(version)) + "." +
           std::to_string(VK_VERSION_PATCH(version));
}

} // namespace

extern "C" JNIEXPORT jboolean JNICALL
Java_com_noir_game_engine_NoirNative_vulkanSupported(JNIEnv*, jclass) {
    if (!hasInstanceExtension(VK_KHR_SURFACE_EXTENSION_NAME)) return JNI_FALSE;

    VkApplicationInfo app{};
    app.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    app.pApplicationName = "Noir";
    app.pEngineName = "Noir";
    app.apiVersion = VK_API_VERSION_1_0;

    const char *extensions[] = { VK_KHR_SURFACE_EXTENSION_NAME };

    VkInstanceCreateInfo ci{};
    ci.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    ci.pApplicationInfo = &app;
    ci.enabledExtensionCount = 1;
    ci.ppEnabledExtensionNames = extensions;

    VkInstance instance = VK_NULL_HANDLE;
    if (vkCreateInstance(&ci, nullptr, &instance) != VK_SUCCESS) return JNI_FALSE;

    uint32_t count = 0;
    VkResult result = vkEnumeratePhysicalDevices(instance, &count, nullptr);
    vkDestroyInstance(instance, nullptr);
    return result == VK_SUCCESS && count > 0 ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_noir_game_engine_NoirNative_vulkanStatus(JNIEnv* env, jclass) {
    if (!hasInstanceExtension(VK_KHR_SURFACE_EXTENSION_NAME)) {
        return env->NewStringUTF("Vulkan surface extension unavailable");
    }

    if (!createInstance()) {
        return env->NewStringUTF("Vulkan instance creation failed");
    }

    bool picked = pickPhysicalDevice();
    if (!picked) {
        resetRuntime();
        return env->NewStringUTF("No Vulkan graphics device");
    }

    std::string status =
        "Vulkan " + apiString(g_runtime.apiVersion) +
        " • " + g_runtime.properties.deviceName +
        " • graphics queue ready";

    resetRuntime();
    return env->NewStringUTF(status.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_noir_game_engine_NoirNative_vulkanInitialize(JNIEnv* env, jclass) {
    resetRuntime();

    if (!createInstance() || !pickPhysicalDevice() || !createLogicalDevice()) {
        resetRuntime();
        __android_log_print(ANDROID_LOG_WARN, TAG, "Vulkan device initialization failed; GLES remains active");
        return JNI_FALSE;
    }

    g_runtime.initialized = true;
    __android_log_print(
        ANDROID_LOG_INFO,
        TAG,
        "Vulkan device initialized: %s",
        g_runtime.properties.deviceName
    );
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_noir_game_engine_NoirNative_vulkanShutdown(JNIEnv*, jclass) {
    resetRuntime();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_noir_game_engine_NoirNative_vulkanDeviceReady(JNIEnv*, jclass) {
    return g_runtime.initialized && g_runtime.device != VK_NULL_HANDLE
        ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_noir_game_engine_NoirNative_vulkanDeviceInfo(JNIEnv* env, jclass) {
    if (!g_runtime.initialized || g_runtime.physicalDevice == VK_NULL_HANDLE) {
        return env->NewStringUTF("Vulkan device not initialized");
    }

    std::string info =
        std::string(g_runtime.properties.deviceName) +
        " | API " + apiString(g_runtime.apiVersion) +
        " | graphics queue " + std::to_string(g_runtime.graphicsQueueFamily);

    return env->NewStringUTF(info.c_str());
}
