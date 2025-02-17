## 📌 关于 CraftEngine
CraftEngine 重新定义了 Minecraft 插件架构，作为下一代自定义内容实现的解决方案。其 JVM 级别的注入提供了前所未有的性能、稳定性和可扩展性。该框架提供了一个代码优先的 API，用于注册原生集成的方块行为和物品交互逻辑。

## 构建

### 🐚 命令行
+ 安装 JDK 21。
+ 打开终端并切换到项目文件夹。
+ 执行 `./gradlew build`，并在 `/target` 文件夹下获取构建产物。

### 💻 IDE
+ 导入项目并执行 Gradle 构建操作。
+ 在 `/target` 文件夹下获取构建产物。

## 安装

### 💻 环境要求
+ 首先，确保您正在运行 [Paper](https://papermc.io/)（或其分支）1.20.1+ 服务器。该插件不支持 Spigot，并且未来也不太可能支持。CraftEngine 包含了超过 10,000 行基于 Paper 版本服务器的 Minecraft 代码。
+ 其次，请使用 JDK 21 来运行服务器。我相信这对您来说非常简单。

### 🔍 如何安装
CraftEngine 提供了两种安装模式：标准安装和 Mod 模式。顾名思义，标准安装是将插件放入您的插件文件夹中，就像任何传统插件一样。下面，我们将详细解释如何使用 Mod 模式进行安装。

### 🔧 安装服务器 Mod
- 下载最新的 [ignite.jar](https://github.com/vectrix-space/ignite/releases) 到您的服务器根目录。
- 选择以下任一操作：
    - 将您的服务器 JAR 重命名为 `paper.jar`，**或**
    - 添加启动参数：`-Dignite.locator=paper -Dignite.paper.jar=./paper-xxx.jar`
- 启动服务器以生成 `/mods` 目录。
- 将最新的 [mod.jar](https://github.com/Xiao-MoMi/craft-engine/releases) 放入 `/mods` 文件夹。
- 将插件的 JAR 文件放入 `/plugins` 文件夹进行安装。
- 执行两次重启：
    1. 第一次重启用于文件初始化。
    2. 第二次重启以激活所有组件。

## 技术概述

### ⚙️ 方块
该插件使用运行时字节码生成技术，在服务器原生级别注册自定义方块，并结合客户端数据包修改以实现视觉同步。此架构提供了：

🧱 自定义原生方块
+ 动态注册方块，完全可控
+ 物理属性：硬度、引燃几率、亮度等所有标准属性。
+ 自定义行为：通过 API 实现树苗、作物、下落的方块等。
+ 原生兼容性：完全保留原生方块机制（例如音符盒、绊线）。

📦 数据包集成
+ 定义自定义矿脉。
+ 生成自定义树木。
+ 配置自定义地形生成。

⚡ 性能优势
+ 比传统的 Bukkit 事件监听器更快、更稳定。
+ 策略性代码注入以最小化开销。

### 🥘 配方
CraftEngine 通过底层注入实现完全可定制的合成系统。与传统插件不同，它在处理 NBT 修改时不会失效，确保配方结果仅与唯一的物品标识符绑定。

### 🪑 家具
该插件使用核心实体来存储家具元数据，同时将碰撞实体和模块组件作为客户端数据包传输。此架构实现了显著的服务器端性能优化，同时支持通过多部分物品集成实现复合家具组装。

### 📝 模板
鉴于插件配置的广泛性和复杂性，实现了模块化模板系统以分隔关键设置。这使得用户可以自定义配置格式，同时显著减少冗余的 YAML 定义。

### 🛠️ 模型
该插件通过配置实现模型继承和纹理覆盖，同时支持从 1.21.4 版本开始的[所有物品模型](https://misode.github.io/assets/item/)。它包含一个版本迁移系统，可以自动将 1.21.4+ 的物品模型降级为旧格式，以实现最大向后兼容性。

## 灵感来源
该项目从以下开源作品中汲取了灵感：
+ [Paper](https://github.com/PaperMC/Paper)
+ [LuckPerms](https://github.com/LuckPerms/LuckPerms)
+ [Fabric](https://github.com/FabricMC/fabric)
+ [packetevents](https://github.com/retrooper/packetevents)
+ [NBT](https://github.com/Querz/NBT)
+ [DataFixerUpper](https://github.com/Mojang/DataFixerUpper)
+ [ViaVersion](https://github.com/ViaVersion/ViaVersion)

### 核心依赖
实施工作依赖于以下基础库：
+ [ignite](https://github.com/vectrix-space/ignite)
+ [cloud-minecraft](https://github.com/Incendo/cloud-minecraft)
+ [rtag](https://github.com/saicone/rtag)
+ [adventure](https://github.com/KyoriPowered/adventure)
+ [byte-buddy](https://github.com/raphw/byte-buddy)

## 如何贡献

### 🔌 新功能与 Bug 修复
如果您的 PR 是关于 Bug 修复的，它很可能会被合并。如果您想提交新功能，请确保提前在 [QQ](https://ti.qq.com/open_qq/index2.html?url=mqqapi%3a%2f%2fuserprofile%2ffriend_profile_card%3fsrc_type%3dweb%26version%3d1.0%26source%3d2%26uin%3d3266959688) 上联系我。

### 🌍 翻译
1. 克隆此仓库。
2. 在 `/bukkit-loader/src/main/resources/translations` 中创建一个新的语言文件。
3. 完成后，提交 **pull request** 以供审核。我们感谢您的贡献！

### 💖 支持开发者
如果您喜欢使用 CraftEngine，请考虑支持开发者！

- **Polymart**: [无]
- **BuiltByBit**: [无]
- **爱发电**: [通过爱发电支持](https://afdian.com/@xiaomomi/)

## CraftEngine API

```kotlin
repositories {
    maven("https://repo.momirealms.net/releases/")
    // 如果你的网络环境受限可以尝试下面的存储库地址
    // maven("https://repo-momi.gtemc.cn/releases/")
}
```
```kotlin
dependencies {
    compileOnly("net.momirealms:craft-engine-core:0.0.16")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.16")
}
```