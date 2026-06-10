# Gradle 子工程

Android 工程位于本目录。完整说明见仓库根目录 [README.md](../README.md)。

## 常用命令

```bash
./gradlew assembleDebug      # 开发构建
./gradlew assembleRelease    # 发布构建
./gradlew clean
```

## 定制入口

| 文件 | 作用 |
|------|------|
| `app/src/main/java/.../MainModule.java` | Hook 逻辑 |
| `app/src/main/resources/META-INF/xposed/scope.list` | 作用域 |
| `app/src/main/resources/META-INF/xposed/java_init.list` | 入口类 |
| `app/build.gradle` | 包名与 SDK 版本 |

详细步骤：[docs/customization.md](../docs/customization.md)
