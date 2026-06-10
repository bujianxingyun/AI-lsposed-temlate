package com.example.module;

import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;

/**
 * Xposed 模块入口（libxposed API 101）。
 * <p>
 * 定制步骤：
 * 1. 修改包名与 {@link #TARGET_PACKAGE}
 * 2. 在 {@link #onPackageReady} 或 {@link #onSystemServerStarting} 中添加 Hook
 * 3. 同步更新 scope.list 与 java_init.list
 */
public class MainModule extends XposedModule {

    private static final String TAG = "MainModule";

    /** 要 Hook 的目标包名；多包场景可改为 Set 或从配置读取 */
    private static final String TARGET_PACKAGE = "com.example.target";

    @Override
    public void onModuleLoaded(@NonNull ModuleLoadedParam param) {
        log(Log.INFO, TAG, "loaded in " + param.getProcessName());
    }

    @Override
    public void onPackageReady(@NonNull PackageReadyParam param) {
        if (!matchesTarget(param)) {
            return;
        }

        // 取消注释并替换为目标类/方法
        /*
        try {
            Method method = param.getClassLoader()
                    .loadClass("com.target.Foo")
                    .getDeclaredMethod("bar", String.class);

            hook(method)
                    .setPriority(PRIORITY_DEFAULT)
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        log(Log.INFO, TAG, "hooked: " + param.getPackageName());
                        return chain.proceed();
                    });
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "Hook failed", t);
        }
        */
    }

    @Override
    public void onSystemServerStarting(@NonNull SystemServerStartingParam param) {
        // Hook system_server 时在 scope.list 中添加 system
        /*
        try {
            ClassLoader cl = param.getClassLoader();
            // Method method = ...
            // hook(method).intercept(chain -> chain.proceed());
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "System hook failed", t);
        }
        */
    }

    private static boolean matchesTarget(@NonNull PackageReadyParam param) {
        if (!param.getPackageName().equals(TARGET_PACKAGE)) {
            return false;
        }
        // 同进程多包时，建议同时校验进程名
        return param.getProcessName().equals(TARGET_PACKAGE);
    }
}
