# AI LSPosed 模块模板

基于 [libxposed](https://github.com/libxposed/api) 的 **LSPosed Xposed 模块** 模板，并预置 Cursor 规则与 Skill，便于在 AI 辅助下快速开发 Hook 模块。

- **框架**：LSPosed（libxposed API 101）
- **语言**：Java 21 + Kotlin
- **最低 Android 版本**：API 26
- **Gradle 工程目录**：[`module/`](module/)

## 项目结构

```
.
├── README.md
├── LICENSE
├── .github/workflows/build.yml
├── .cursor/
│   ├── rules/
│   └── skills/libxposed-api/
├── apksource/                 # 目标 app 反编译代码（只读参考）
└── module/
    ├── app/
    │   ├── build.gradle.kts
    │   ├── proguard-rules.pro
    │   └── src/main/
    │       ├── java/io/github/libxposed/example/
    │       ├── res/
    │       └── resources/META-INF/xposed/
    ├── gradle/libs.versions.toml
    ├── settings.gradle.kts
    └── gradlew / gradlew.bat
```

## 快速开始

### 环境要求

- JDK 21
- Android SDK（compileSdk 36）
- 已安装 [LSPosed](https://github.com/LSPosed/LSPosed) 的 Root 设备或模拟器

### 构建

```bash
cd module
./gradlew assembleDebug
./gradlew assembleRelease
```

Windows：`gradlew.bat assembleDebug`

Release APK：`module/app/build/outputs/apk/release/`

安装后在 LSPosed 管理器中启用模块，勾选作用域并重启目标应用。

## 定制模块

1. 修改 `app/build.gradle.kts` 中的 `namespace`、`applicationId`
2. 重命名 `app/src/main/java/io/github/libxposed/example/` 包目录，同步 `package` 声明
3. 更新 `java_init.list` 入口类 FQCN
4. 在 `scope.list` 添加目标包名，在 `ModuleMain` 的 `onPackageReady` 中过滤并实现 Hook
5. 修改 `AndroidManifest.xml` 的 `android:label`、`android:description`
6. 将目标 app 反编译结果放入 `apksource/<包名>/` 供分析（不参与编译）

Hook system_server 时：`scope.list` 添加 `system`，逻辑写在 `onSystemServerStarting`。

## 主要文件说明

| 文件 | 说明 |
|------|------|
| `ModuleMain.java` | Xposed 入口，API 101 生命周期 |
| `App.kt` / `MainActivity.kt` | 模块 UI，`XposedService` 演示 |
| `java_init.list` | libxposed 入口类 FQCN |
| `scope.list` | 默认作用域 |
| `module.prop` | libxposed 元数据 |
| `proguard-rules.pro` | Release 混淆保留规则 |
| `apksource/<包名>/` | 目标 app 参考代码（不参与编译） |

## 使用 Cursor / AI 开发

- **Rule**（`.cursor/rules/project-context.mdc`）：项目结构与定制流程
- **Skill**（`.cursor/skills/libxposed-api/`）：libxposed API 参考

## 注意事项

- libxposed API 使用 `compileOnly`，**禁止**打进 APK
- Hook 写在 `onPackageReady`，不要在 `onPackageLoaded` 中 Hook
- 同进程多包场景务必校验 `getPackageName()`

## 参考链接

- [libxposed API](https://libxposed.github.io/api/index.html)
- [libxposed example](https://github.com/libxposed/example)
- [LSPosed Wiki — Modern API](https://github.com/LSPosed/LSPosed/wiki/Develop-Xposed-Modules-Using-Modern-Xposed-API)
