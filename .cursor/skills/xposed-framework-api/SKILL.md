---
name: xposed-framework-api
description: >-
  Guides Xposed and LSPosed module development. LSPosed section covers libxposed API
  (XposedModule, onPackageLoaded, @XposedHooker). Xposed section covers classic API
  (IXposedHookLoadPackage, XposedHelpers, XResources). Use when writing or debugging
  Xposed/LSPosed modules, hooking methods, or referencing api.xposed.info / libxposed docs.
---

# Xposed / LSPosed Module Development

**This repo uses LSPosed (libxposed API 101).** Use the API 101 section first; API 100 is legacy.

| | LSPosed | Xposed (Classic) |
|---|---------|------------------|
| Framework | LSPosed and forks | Original Xposed / EdXposed |
| API | [libxposed](https://libxposed.github.io/api/index.html) | [api.xposed.info](https://api.xposed.info/reference/packages.html) |
| Entry | `extends XposedModule` | `implements IXposedHookLoadPackage` |
| Hook | `hook(method, Hooker)` / `chain.proceed()` | `XposedHelpers.findAndHookMethod()` |
| Resources | Not supported | `XResources.setReplacement()` |
| Zygote | Not supported | `IXposedHookZygoteInit` |

---

## LSPosed (libxposed)

Official docs: [libxposed.github.io/api](https://libxposed.github.io/api/index.html)

### Setup

```gradle
compileOnly 'io.github.libxposed:api:100'      // this repo; 101.0.1 for latest
compileOnly project(":libxposed-compat")        // @XposedHooker annotations
```

### Module Files (`src/main/resources/META-INF/xposed/`)

| File | Purpose |
|------|---------|
| `java_init.list` | Entry class FQCN, one per line |
| `scope.list` | Target packages; `system` for system_server |
| `module.prop` | `minApiVersion`, `targetApiVersion`, optional `staticScope`, `exceptionMode` |

Name/description via `android:label` / `android:description`.

### Scope

Always filter by `getPackageName()` — callbacks may fire for other packages in the same process. `getProcessName()` is on `ModuleLoadedParam` only; in `onPackageReady` use `getApplicationInfo().processName`.

- `system` — virtual package for system_server
- `android` — valid (some components run outside system_server)
- `com.android.providers.settings` — not a valid scope; use `system` and filter in callback

### API 100 — This Repo

```java
public class MainModule extends XposedModule {
    public MainModule(XposedInterface base, ModuleLoadedParam param) {
        super(base, param);
    }

    @Override
    public void onPackageLoaded(@NonNull PackageLoadedParam param) {
        if (!param.getPackageName().equals("com.target.app")) return;
        try {
            Class<?> clazz = param.getClassLoader().loadClass("com.target.Foo");
            Method method = clazz.getDeclaredMethod("bar", String.class);
            hook(method, MyHooker.class);
        } catch (Throwable t) {
            log("Hook failed", t);
        }
    }

    @XposedHooker
    private static class MyHooker implements Hooker {
        @BeforeInvocation
        public static void before(@NonNull BeforeHookCallback cb) {
            cb.setArg(0, "modified");
            // cb.returnAndSkip(result);  // skip original
        }
        @AfterInvocation
        public static void after(@NonNull AfterHookCallback cb) {
            cb.setResult("new result");
        }
    }
}
```

**Lifecycle (API 100):**

| Callback | When |
|----------|------|
| Constructor | Module loaded; `isSystemServer()`, `getProcessName()` |
| `onPackageLoaded` | Package in process; `getPackageName()`, `getClassLoader()`, `isFirstPackage()` |
| `onSystemServerLoaded` | system_server ready; `getClassLoader()` |

**Hook APIs:** `hook()`, `hookBefore()`, `hookAfter()` — optional `priority`.

**Preferences (API 100):** `getSharedPreferences(name, mode)`

**ProGuard:** keep `XposedModule` entry and `@XposedHooker` classes — see `module/app/proguard-rules.pro`.

### API 101 — Latest (Interceptor Chain)

No constructor. Initialize in `onModuleLoaded()`. Hook in `onPackageReady()`:

```java
public class MainModule extends XposedModule {
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
                    Object result = chain.proceed();
                    return result;
                });
        } catch (Throwable t) {
            log(Log.ERROR, "MyModule", "Hook failed", t);
        }
    }
}
```

**Lifecycle (API 101):** `onModuleLoaded` → `onPackageLoaded` → `onPackageReady` (hook here) / `onSystemServerStarting`

**Key APIs:**
- `chain.proceed()` / `chain.proceed(newArgs)` / `chain.proceedWith(thisObject, args)`
- `getInvoker(method).setType(Invoker.Type.ORIGIN).invoke(...)` — call original, skip hooks
- `getRemotePreferences(group)` — replaces `XSharedPreferences`
- `deoptimize(executable)` — fix inline optimization blocking hooks
- No zygote injection; modules load only in scoped processes

### LSPosed ↔ Classic Mapping

| Classic Xposed | LSPosed |
|----------------|---------|
| `handleLoadPackage` | `onPackageLoaded` / `onPackageReady` |
| `lpparam.packageName` | `param.getPackageName()` |
| `lpparam.classLoader` | `param.getClassLoader()` |
| `lpparam.isFirstApplication` | `param.isFirstPackage()` |
| `beforeHookedMethod` | `@BeforeInvocation` / `chain.proceed()`前 |
| `afterHookedMethod` | `@AfterInvocation` / `chain.proceed()`后 |
| `param.setResult()` | `cb.returnAndSkip()` / `cb.setResult()` |
| `XposedBridge.log()` | `log(...)` |

---

## Xposed (Classic)

Official docs: [api.xposed.info](https://api.xposed.info/reference/packages.html)

For legacy modules, resource replacement, zygote hooks, or `XposedHelpers` shortcuts.

### Entry Points

Implement in main class; register in `assets/xposed_init` or `META-INF/xposed/java_init.list`.

| Interface | When | Use For |
|-----------|------|---------|
| `IXposedHookLoadPackage` | App loaded (before `Application.onCreate`) | Per-app hooks |
| `IXposedHookZygoteInit` | Zygote startup | System-wide hooks |
| `IXposedHookInitPackageResources` | Resources initialized | Resource replacement |

```java
public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.target.app")) return;
        XposedHelpers.findAndHookMethod(
            "com.target.Foo", lpparam.classLoader, "bar",
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = "modified";
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.setResult("new result");
                }
            }
        );
    }
}
```

### Hooking Essentials

```java
// Hook
XposedHelpers.findAndHookMethod(className, classLoader, methodName, paramTypes..., callback);
XposedHelpers.findAndHookConstructor(className, classLoader, callback);

// Replace method entirely
XposedHelpers.findAndHookMethod(clazz, "method", XC_MethodReplacement.returnConstant(true));

// Reflection
XposedHelpers.findClass(name, classLoader);
XposedHelpers.get/setObjectField(obj, "field", value);
XposedHelpers.callMethod(obj, "method", args);
```

**Param types:** `int.class` for primitive int (not `Integer.class`). Last arg is always callback.

**MethodHookParam:** `thisObject`, `args`, `result`, `throwable`, `method` — use `setResult()` / `setThrowable()` to skip original.

**Priority:** higher runs first in `before`, last in `after`.

### Resource Replacement

Only in classic Xposed — hook `handleInitPackageResources`:

```java
resparam.res.setReplacement("com.target.app", "string", "app_name", "New Name");
resparam.res.hookLayout("com.target.app", "layout", "main", new XC_LayoutInflated() {
    @Override
    protected void handleLayoutInflated(LayoutInflatedParam liparam) {
        // modify liparam.view
    }
});
```

### Zygote Hooks

```java
// classLoader = null for framework classes
XResources.setSystemWideReplacement("android", "string", "yes", "Yeah");
```

### Preferences & Logging

```java
XSharedPreferences pref = new XSharedPreferences("com.my.module", "prefs");
pref.makeWorldReadable(); pref.reload();

XposedBridge.log("message");
XposedBridge.log(throwable);
```

### Common Pitfalls

| Problem | Solution |
|---------|----------|
| `ClassNotFoundError` | Use target app's `classLoader` |
| Hook not firing | Check package name, ProGuard, obfuscation |
| Wrong overload | Specify exact parameter types |
| Resource ignored | Use `handleInitPackageResources`, not `handleLoadPackage` |
| Multi-app process | Check `isFirstApplication` / `isFirstPackage` |
