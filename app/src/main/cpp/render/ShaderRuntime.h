#pragma once

#include "ShaderAsset.h"

namespace noir::render {

bool CompileGLESShader(const ShaderAsset& asset, unsigned int glShaderHandle) noexcept;
bool CreateVulkanShaderModule(const ShaderAsset& asset, void* vkDevice, void* outModule) noexcept;

} // namespace noir::render
