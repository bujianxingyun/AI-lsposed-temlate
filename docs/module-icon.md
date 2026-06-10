# 模块图标

`module/app/src/main/res/drawable/ic_module_icon.png` 为模块在 LSPosed 管理器与系统设置中显示的图标。

**注意**：`res/` 目录下只能放置 `.xml` / `.png` 等资源文件，不要在此目录放 `README` 等非资源文件。

## 更换图标

1. 准备一张 **正方形** PNG（建议 512×512 或 192×192）
2. 替换 `module/app/src/main/res/drawable/ic_module_icon.png`（保持文件名不变）
3. 重新构建并安装 APK：`gradlew assembleDebug`

也可使用各密度 `mipmap` 目录（显示更清晰）：

```
res/
├── drawable/ic_module_icon.png          # 通用（当前方式）
├── mipmap-mdpi/ic_module_icon.png       # 48×48
├── mipmap-hdpi/ic_module_icon.png       # 72×72
├── mipmap-xhdpi/ic_module_icon.png      # 96×96
├── mipmap-xxhdpi/ic_module_icon.png     # 144×144
└── mipmap-xxxhdpi/ic_module_icon.png    # 192×192
```

若改用 `mipmap`，请将 `AndroidManifest.xml` 中的 `@drawable/ic_module_icon` 改为 `@mipmap/ic_module_icon`。
