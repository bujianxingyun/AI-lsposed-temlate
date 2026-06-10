# AI LSPosed 模块模板

基于 [libxposed](https://github.com/libxposed/api) 的 **LSPosed Xposed 模块** 模板，并预置 Cursor 规则与 Skill，便于在 AI 辅助下快速开发 Hook 模块。

- **框架**：LSPosed（libxposed API 100）
- **语言**：Java 21
- **最低 Android 版本**：API 31
- **Gradle 工程目录**：[`module/`](module/)

## 项目结构

```
.
├── README.md                          # 本文件
├── .cursor/
│   ├── rules/                         # Cursor AI 项目规则
│   │   ├── project-context.mdc        # 项目上下文（始终生效）
│   │   ├── lsposed-hooking.mdc        # Hook 编写规范
│   │   ├── xposed-metadata.mdc        # 模块元数据配置
│   │   └── android-gradle.mdc         # Gradle 构建约定
│   └── skills/
│       └── xposed-framework-api/      # Xposed / LSPosed API 参考
├── docs/                              # 扩展文档（可选）
└── module/                            # Android Gradle 工程
    ├── app/                           # 模块 APK
    │   ├── build.gradle
    │   ├── proguard-rules.pro
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       ├── java/com/example/module/
    │       │   └── MainModule.java    # 模块入口（extends XposedModule）
    │       ├── res/values/strings.xml # 模块名称与描述
    │       └── resources/META-INF/xposed/
    │           ├── java_init.list     # 入口类 FQCN
    │           ├── scope.list         # Hook 作用域
    │           └── module.prop        # libxposed 元数据
    ├── libxposed-compat/              # @XposedHooker 注解兼容层
    ├── gradlew / gradlew.bat
    └── README.md                      # Gradle 子工程说明
```

## 快速开始

### 环境要求

- JDK 21
- Android SDK（compileSdk 34）
- 已安装 [LSPosed](https://github.com/LSPosed/LSPosed) 的 Root 设备或模拟器

### 构建

```bash
cd module
./gradlew assembleRelease        # Linux / macOS
gradlew.bat assembleRelease      # Windows
```

Release APK 输出路径：`module/app/build/outputs/apk/release/`。

安装后在 LSPosed 管理器中启用模块，勾选作用域并重启目标应用。

## 定制模块

以下路径均相对于 `module/` 目录。

1. **修改包名**：在 `app/build.gradle` 中更新 `namespace` 和 `applicationId`。
2. **重命名包目录**：将 `app/src/main/java/com/example/module` 改为与你的包名一致的目录结构。
3. **编写 Hook 逻辑**：
   - 在 `MainModule.java` 的 `onPackageLoaded` 或 `onSystemServerLoaded` 中添加逻辑。
   - 更新 `app/src/main/resources/META-INF/xposed/java_init.list`，确保指向你的 `XposedModule` 实现类 FQCN。
4. **配置作用域**：在 `app/src/main/resources/META-INF/xposed/scope.list` 中列出目标应用包名（每行一个）；Hook system_server 时使用 `system`。
5. **调整编译参数**：按需修改 `app/build.gradle` 中的 `compileSdk`、`targetSdkVersion` 等。

修改包名时，请同步更新 `java_init.list` 与 Java 源文件的 `package` 声明，保持一致。

## 主要文件说明

| 文件 | 说明 |
|------|------|
| `MainModule.java` | 模块主入口，继承 `XposedModule` |
| `java_init.list` | 声明 libxposed 加载的入口类 |
| `scope.list` | 定义模块生效的作用域（应用包名） |
| `module.prop` | libxposed 元数据（`minApiVersion`、`targetApiVersion`、`staticScope`） |
| `proguard-rules.pro` | Release 混淆规则，保留入口类与 `@XposedHooker` |
| `strings.xml` | 通过 `android:label` / `android:description` 设置模块名称与描述 |

## 使用 Cursor / AI 开发

本模板为 AI 辅助开发做了预配置：

- **Rules**（`.cursor/rules/`）：约定项目结构、Hook 写法、元数据与 Gradle 配置，减少 AI 生成 Classic Xposed 代码或错误依赖的概率。
- **Skill**（`.cursor/skills/xposed-framework-api/`）：LSPosed 与 Classic Xposed API 对照及示例，编写 Hook 时可让 AI 优先参考。

在 Cursor 中打开本仓库即可自动加载上述配置。

## 注意事项

- 本模板使用 **libxposed API 100**，请参考 [libxposed 官方文档](https://libxposed.github.io/api/index.html) 了解高级用法。
- libxposed 依赖必须使用 `compileOnly`，**不要**用 `implementation` 引入，避免将 Xposed API 打包进 APK。
- libxposed 不支持 Classic Xposed 的资源替换（`XResources`）与 Zygote 注入；Hook 请使用 `XposedModule` + `@XposedHooker`。
- 同一进程可能加载多个包，Hook 前务必用 `getPackageName()` / `getProcessName()` 过滤。
- Hook 逻辑应包在 `try/catch (Throwable)` 中，失败时使用 `log("...", t)` 记录。

## 参考链接

- [libxposed API](https://libxposed.github.io/api/index.html)
- [libxposed GitHub](https://github.com/libxposed/api)
- [LSPosed](https://github.com/LSPosed/LSPosed)
- [module/README.md](module/README.md) — Gradle 子工程简要说明
