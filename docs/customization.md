# 模块定制清单

按顺序完成以下步骤，避免遗漏。

## 1. 包名与标识

- [ ] `module/app/build.gradle` — 修改 `namespace`、`applicationId`
- [ ] 重命名 `app/src/main/java/com/example/module/` 目录
- [ ] 更新所有 Java 文件的 `package` 声明
- [ ] `META-INF/xposed/java_init.list` — 写入新的入口类 FQCN

## 2. 模块信息

- [ ] `app/src/main/res/values/strings.xml` — 修改 `app_name`、`xposed_description`
- [ ] `app/build.gradle` — 按需调整 `versionCode`、`versionName`

## 3. Hook 目标

- [ ] 将目标 app 反编译/源码放入 `apksource/<包名>/`（见 [apksource/README.md](../apksource/README.md)）
- [ ] `MainModule.java` — 设置 `TARGET_PACKAGE`
- [ ] `META-INF/xposed/scope.list` — 添加目标包名（每行一个）
- [ ] 在 `onPackageReady` 中实现 Hook（参考注释示例）
- [ ] 若 Hook system_server：scope 添加 `system`，逻辑写在 `onSystemServerStarting`

## 4. SDK 与构建

- [ ] 按需修改 `compileSdk`、`targetSdk`、`minSdk`
- [ ] 开发阶段使用 `assembleDebug`
- [ ] 发布前使用 `assembleRelease` 并真机验证

## 5. LSPosed 启用

- [ ] 安装 APK
- [ ] 在 LSPosed 中启用模块并勾选作用域
- [ ] 重启目标应用或 system_server

## 常见陷阱

| 问题 | 原因 | 处理 |
|------|------|------|
| 模块未加载 | `java_init.list` 类名错误 | 核对 FQCN |
| Hook 不生效 | 未过滤包名/进程名 | 使用 `matchesTarget` 模式 |
| 构建失败 | JDK 版本不对 | 使用 JDK 21 |
| 混淆后崩溃 | ProGuard 规则缺失 | 检查 `proguard-rules.pro` |
