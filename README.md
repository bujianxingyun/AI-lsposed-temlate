# AI LSPosed 模块模板

基于 [libxposed](https://github.com/libxposed/api) 的 **LSPosed Xposed 模块** 模板，并预置 Cursor 规则与 Skill，便于在 AI 辅助下快速开发 Hook 模块。

- **框架**：LSPosed（libxposed API 101）
- **语言**：Java 17
- **最低 Android 版本**：API 26
- **Gradle 工程目录**：[`module/`](module/)

## 项目结构

```
.
├── README.md
├── LICENSE
├── .github/workflows/build.yml        # CI 构建
├── .cursor/
│   ├── rules/                         # Cursor AI 项目规则
│   └── skills/xposed-framework-api/   # Xposed / LSPosed API 参考
├── docs/customization.md              # 定制清单
├── apksource/                         # 目标 app 源码/反编译代码（只读参考，不参与编译）
│   └── README.md
└── module/                            # Android Gradle 工程
    ├── app/
    │   ├── build.gradle
    │   ├── proguard-rules.pro
    │   └── src/main/
    │       ├── java/com/example/module/MainModule.java
    │       ├── res/drawable/ic_module_icon.png  # 模块图标（可直接替换）
    │       ├── res/values/strings.xml
    │       └── resources/META-INF/xposed/
    │           ├── java_init.list
    │           ├── scope.list
    │           └── module.prop
    ├── gradlew / gradlew.bat
    └── README.md
```

## 快速开始

### 环境要求

- JDK 17（与 Android Studio + AGP 8.2.2 兼容）
- Android SDK（compileSdk 34）
- 已安装 [LSPosed](https://github.com/LSPosed/LSPosed) 的 Root 设备或模拟器

### 构建

```bash
cd module
./gradlew assembleDebug          # 日常开发（无混淆）
./gradlew assembleRelease        # 发布（开启 R8 混淆）
```

Windows：

```bat
cd module
gradlew.bat assembleDebug
```

Release APK：`module/app/build/outputs/apk/release/`

安装后在 LSPosed 管理器中启用模块，勾选作用域并重启目标应用。

## 定制模块

完整清单见 [docs/customization.md](docs/customization.md)。核心步骤：

1. 修改 `app/build.gradle` 中的 `namespace` 和 `applicationId`
2. 重命名 `app/src/main/java/com/example/module` 包目录
3. 在 `MainModule.java` 中设置 `TARGET_PACKAGE` 并实现 `onPackageReady` / `onSystemServerStarting`
4. 更新 `java_init.list`、`scope.list`、`strings.xml`
5. 按需调整 `compileSdk` / `targetSdk`
6. 将目标 app 源码或反编译结果放入 [`apksource/`](apksource/)（见 [apksource/README.md](apksource/README.md)）

## 主要文件说明

| 文件 | 说明 |
|------|------|
| `MainModule.java` | 模块入口，API 101 生命周期 |
| `java_init.list` | libxposed 入口类 FQCN |
| `scope.list` | 默认作用域（`staticScope=false` 可在 LSPosed 中扩展） |
| `module.prop` | libxposed 元数据 |
| `proguard-rules.pro` | Release 混淆保留规则 |
| `apksource/<包名>/` | 目标 app 参考代码，用于分析 Hook 点（不参与编译） |

## 使用 Cursor / AI 开发

- **Rules**（`.cursor/rules/`）：项目结构、API 101 Hook 规范、Gradle 约定、`apksource` 目标 app 分析规范
- **Skill**（`.cursor/skills/xposed-framework-api/`）：LSPosed 与 Classic Xposed API 对照

## 注意事项

- libxposed 依赖：`compileOnly 'io.github.libxposed:api:101.0.1'`（已从 Maven Central 解析，无需 mavenLocal）
- Maven 仓库默认使用**阿里云镜像**（见 `module/settings.gradle`）
- Hook 应写在 `onPackageReady`，不要在 `onPackageLoaded` 中 Hook
- 同进程多包场景务必校验 `getPackageName()`；进程名勿在 `PackageReadyParam` 上调用 `getProcessName()`（应使用 `getApplicationInfo().processName`）

## 参考链接

- [libxposed API](https://libxposed.github.io/api/index.html)
- [libxposed example](https://github.com/libxposed/example)
- [LSPosed Wiki — Modern API](https://github.com/LSPosed/LSPosed/wiki/Develop-Xposed-Modules-Using-Modern-Xposed-API)
