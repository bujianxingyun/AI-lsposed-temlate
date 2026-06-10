# apksource — 目标应用参考代码

本目录用于放置 **Hook 目标应用** 的源代码或反编译结果，供开发与 AI 分析类名、方法签名、调用链时使用。

**注意**：此目录内容仅作阅读参考，不参与 `module/` 的 Gradle 编译。

## 推荐目录结构

按目标应用包名分子目录，便于与 `scope.list`、`MainModule.TARGET_PACKAGE` 对应：

```
apksource/
├── README.md
└── com.example.target/          # 目标应用包名
    ├── NOTES.md                 # 可选：版本号、反编译工具、关注的功能点
    └── ...                      # 源码或 jadx/apktool 输出
```

多目标时并列放置：

```
apksource/
├── com.app.one/
└── com.app.two/
```

## 常见来源

| 类型 | 工具/方式 | 说明 |
|------|-----------|------|
| 反编译 Java/Kotlin | [jadx](https://github.com/skylot/jadx) | 最常用，便于查类与方法 |
| 反编译 Smali | apktool | 资源改包时使用；Hook 分析以 jadx 为主 |
| 官方/泄露源码 | 直接复制 | 注意版权与合规 |

## 与模块配置的对应关系

定制 Hook 时，保持以下信息一致：

| 模块配置 | apksource |
|----------|-----------|
| `scope.list` 中的包名 | 子目录名 / 应用 `applicationId` |
| `MainModule.TARGET_PACKAGE` | 主 Hook 目标包名 |
| Hook 的类全名 | 在反编译代码中搜索确认 |

## 使用 Cursor / AI 时

分析 Hook 点时，优先在 `apksource/<包名>/` 中查找：

1. 目标类全限定名（注意混淆后的短类名）
2. 方法名与参数类型（Hook 需精确匹配 overload）
3. 调用时机（Activity、Service、网络层等）

编写 `MainModule` 中的 Hook 代码时，**不要**复制目标 app 的类到 `module/app`；仅引用字符串形式的类名，在运行时通过 `param.getClassLoader().loadClass(...)` 加载。

## Git 与体积

反编译工程可能很大。若不想提交到仓库，可在根目录 `.gitignore` 中增加：

```gitignore
# 忽略具体目标 app 目录（保留本 README）
apksource/com.example.target/
```

或忽略整个目录内容（仅本地保留）：

```gitignore
apksource/**/*
!apksource/README.md
```

## 合规提示

请确保你对目标应用代码的分析与 Hook 符合当地法律及应用许可协议；勿将受版权保护的完整源码公开分发。
