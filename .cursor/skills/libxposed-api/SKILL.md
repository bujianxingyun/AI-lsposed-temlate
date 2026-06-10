---
name: libxposed-api
description: >-
  LSPosed Modern API (libxposed) development guide. Use when writing Hook code,
  module metadata, XposedService, or migrating from legacy XposedBridge APIs.
---

# libxposed Modern API

基于 [LSPosed Modern API 官方说明](https://github.com/LSPosed/LSPosed/wiki/Develop-Xposed-Modules-Using-Modern-Xposed-API)。API 参考：[libxposed.github.io/api](https://libxposed.github.io/api/index.html)。

本仓库使用 **API 101**。入口继承 `io.github.libxposed.api.XposedModule`，**禁止**使用 Classic `de.robv.android.xposed.*`。

## 与 Legacy XposedBridge 的差异

| 项目 | Legacy | Modern (libxposed) |
|------|--------|-------------------|
| Java 入口注册 | `assets/xposed_init` | `META-INF/xposed/java_init.list` |
| Native 入口 | — | `META-INF/xposed/native_init.list` |
| 模块名/描述 | `xposedmodule` 元数据 | `AndroidManifest` `android:label` / `android:description` |
| 作用域 | 管理器勾选 | `META-INF/xposed/scope.list` + 管理器 |
| 模块配置 | 元数据 | `META-INF/xposed/module.prop` |
| 入口类 | `IXposedHookLoadPackage` 等 | `extends XposedModule` |
| 初始化 | 构造函数 | 框架自动 `attachFramework()`；**勿在 `onModuleLoaded()` 之前初始化** |
| Hook | `XposedHelpers` + `XC_MethodHook` | `hook(executable).intercept(chain -> ...)` |
| 资源 Hook | `XResources` | **不支持** |
| 跨进程配置 | `XSharedPreferences` | `getRemotePreferences()` |
| 跨进程文件 | — | `openRemoteFile()` |
| 框架通信 | 无 | `io.github.libxposed:service` → `XposedService` |

辅助反射库（非框架内置）：[libxposed/helper](https://github.com/libxposed/helper)。

## 依赖

```kotlin
// module/app/build.gradle.kts
compileOnly(libs.libxposed.api)        // 必须 compileOnly，禁止打进 APK
implementation(libs.libxposed.service) // 模块 App 与框架通信时需要
```

## 模块文件

置于 `src/main/resources/META-INF/xposed/`（Gradle 会打包进 APK）：

| 文件 | 说明 |
|------|------|
| `java_init.list` | 入口类 FQCN，每行一个 |
| `scope.list` | 目标包名，每行一个；`system` = system_server |
| `module.prop` | Java properties 格式 |

`module.prop` 字段：

| 键 | 必填 | 说明 |
|----|------|------|
| `minApiVersion` | 是 | 最低 API 版本（本仓库 101） |
| `targetApiVersion` | 是 | 目标 API 版本 |
| `staticScope` | 否 | `true` 时禁止用户对 scope 外应用启用模块 |

## 生命周期

框架回调顺序：`onModuleLoaded` → `onPackageLoaded` → `onPackageReady`（应用 Hook）/ `onSystemServerStarting`（system_server）。

| 回调 | 用途 |
|------|------|
| `onModuleLoaded` | 模块进程加载；可读框架名、版本、`getApiVersion()` |
| `onPackageLoaded` | 包进入进程；**不要在此 Hook** |
| `onPackageReady` | **应用 Hook 入口** |
| `onSystemServerStarting` | system_server Hook；`scope.list` 需含 `system` |

注意：

- 同进程可能加载多包，**必须** `getPackageName()` 过滤
- `getProcessName()` 仅在 `ModuleLoadedParam` 上；`PackageReadyParam` 用 `getApplicationInfo().processName`
- **模块自身 App 不会被 Hook**；配置与框架通信用 `XposedService`

## Hook（拦截链）

OkHttp 风格：`hook(Method|Constructor)` 返回 `HookBuilder`，可设 `setPriority()`、`setExceptionMode()`，再 `.intercept(chain -> ...)`。

```java
@Override
public void onPackageReady(@NonNull PackageReadyParam param) {
    if (!param.getPackageName().equals("com.target.app")) return;
    try {
        Method method = param.getClassLoader()
            .loadClass("com.target.Foo")
            .getDeclaredMethod("bar", String.class);

        hook(method)
            .setPriority(PRIORITY_DEFAULT)
            .setExceptionMode(ExceptionMode.PROTECTIVE)
            .intercept(chain -> {
                // 修改参数后可 chain.proceed() / chain.proceed(newArgs) / chain.proceedWith(thisObj, args)
                return chain.proceed();
            });
    } catch (Throwable t) {
        log(Log.ERROR, TAG, "Hook failed", t);
    }
}
```

- void 方法拦截器返回 `null`
- 框架**不提供** `XposedHelpers`；用反射或 [libxposed/helper](https://github.com/libxposed/helper)

### Invoker

跳过拦截链、调用原实现或从链中指定位置调用：

```java
getInvoker(method).setType(Invoker.Type.ORIGIN).invoke(receiver, args);
getInvoker(method).setType(Invoker.Type.Chain(-50)).invokeSpecial(receiver, args);
getInvoker(constructor).setType(Invoker.Type.ORIGIN).newInstance(args);
getInvoker(constructor).setType(Invoker.Type.Chain.FULL).newInstanceSpecial(declaringClass, args);
```

### deoptimize

Hook System Framework 等方法因 inline 优化不生效时：

```java
deoptimize(method); // 参数为 Executable
```

## 框架通信（XposedService）

模块 App 启动后，通过 [libxposed/service](https://github.com/libxposed/service) 与框架通信：

- 动态申请作用域 `requestScope()`
- 查询框架名称、版本、能力
- **Remote Preferences** — 模块与 Hook 进程共享配置（支持变更监听）
- **Remote Files** — 共享较大文件（`/data/adb/lspd/modules/<user>/<module>/`）

Hook 进程内：`getRemotePreferences(name)`、`openRemoteFile(name)`。

模块 App 内：注册 `XposedServiceHelper.OnServiceListener`，取得 `XposedService` 实例后调用对应 API。

| 方式 | API 代际 | 本仓库 |
|------|----------|--------|
| `XSharedPreferences` | Legacy | ❌ 不用 |
| Remote Preferences | Modern | ✅ |
| Remote Files | Modern | ✅ |

## 不支持的能力

- 资源 Hook（`XResources.setReplacement` 等）
- Zygote 全局注入
- `XposedHelpers` / `IXposedHookLoadPackage` / `assets/xposed_init`

## ProGuard

Release 保留 `XposedModule` 子类及生命周期方法，见 `module/app/proguard-rules.pro`。

## 常见陷阱

| 问题 | 处理 |
|------|------|
| 照搬旧教程 | 整体改用 Modern API，勿混用两套 import |
| 模块未加载 | 核对 `java_init.list` FQCN |
| 在构造函数或字段初始化中 Hook | 等到 `onModuleLoaded` / `onPackageReady` |
| Hook 不生效 | 包名过滤、混淆、对目标方法 `deoptimize()` |
| `ClassNotFoundException` | 使用 `param.getClassLoader()` |
| 模块 App 读不到配置 | 用 Remote Preferences，不要用 `XSharedPreferences` |
