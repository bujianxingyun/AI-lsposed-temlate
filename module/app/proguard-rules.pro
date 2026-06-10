-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations

-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public (...);
    public void onModuleLoaded(...);
    public void onPackageLoaded(...);
    public void onPackageReady(...);
    public void onSystemServerStarting(...);
}
-keep,allowoptimization,allowobfuscation @io.github.libxposed.api.annotations.* class * {
    @io.github.libxposed.api.annotations.BeforeInvocation;
    @io.github.libxposed.api.annotations.AfterInvocation;
}

-repackageclasses
-allowaccessmodification
