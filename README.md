# Wear AM Launcher —— Galaxy Watch 上启动 Apple Music 的轻量启动壳

## 这是什么

官方 Apple Music 安卓 APK 侧载到 Wear OS 手表后，**不会出现在手表的应用列表里**。
本项目是一个「轻量启动壳」，只负责提供入口：

- 一键拉起 Apple Music（包名 `com.apple.android.music`）
- 「全部应用列表」：列出所有已安装可启动应用，作为侧载应用的备用入口
- 显示 Apple Music 是否已安装
- 内置使用说明

纯 Android 原生 API 实现，零第三方依赖，APK 体积约几十 KB。
不实现播放、DRM、登录，也不修改 Apple Music 本体。

## 环境要求

- 手表：Galaxy Watch 4 及以上（Wear OS），思路适用于 Watch 7（Wear OS 5 / Android 14）
- 构建：JDK 17 + Android SDK（compileSdk 34）

## 构建

### 方式 A：Android Studio（推荐）

1. 用 Android Studio 打开本目录（WearAMLauncher）
2. 等待 Gradle 同步（首次会下载 AGP 8.5.2 与 SDK）
3. Build → Build APK(s)
4. 产物：`app/build/outputs/apk/debug/app-debug.apk`

### 方式 B：命令行 Gradle

```bash
cd WearAMLauncher
gradle :app:assembleDebug
```

若目录未带 wrapper，先执行 `gradle wrapper` 生成。

### 方式 C：无 Gradle 手动构建

需要 `build-tools;34.0.0` 与 `platforms;android-34`：

```bash
ANDROID_SDK=/path/to/sdk ./build_manual.sh
```

## 侧载到手表

1. 下载 Apple Music **APK**（注意不是 App Bundle），可去 APKMirror 找官方版本。
2. 手表开开发者模式：设置 → 关于手表 → 软件 → 连点「软件版本」5 次。
3. 设置 → 开发者选项 → 打开「ADB 调试」和「通过 WLAN 调试」，记下 IP:端口。
4. 免电脑：手机装 Bugjaeger，连接手表，依次安装 WearAMLauncher.apk 与 Apple Music APK。
   电脑：`adb connect <IP>:<端口>` → `adb install WearAMLauncher.apk` → `adb install AppleMusic.apk`
5. 在手表应用列表打开「AM启动器」→ 点「▶ 启动 Apple Music」→ 登录 Apple ID。

## 已知限制

- Apple Music 是手机版 UI，圆形屏边缘可能显示不全（可长按拖动窗口，或在系统里调整显示缩放）。
- 本壳只负责启动入口。
- 若 Apple Music 在后台被系统杀掉：手表 设置 → 电池 → 找到 Apple Music → 设为「不受限制」。

## 常见问题

- Q: 应用列表里看不到「AM启动器」？
  A: 侧载后重启一次手表（launcher 缓存问题）；或用 adb 确认已安装。
- Q: 点「启动」没反应？
  A: Apple Music 没装上或装的是 bundle，改装 APK 版。
- Q: 登录不了 Apple ID？
  A: 在 Apple Music 内登录；如遇风控，先在手机上登录并信任设备。

## 免责声明

本项目仅为启动入口工具，与 Apple Inc. 无任何关联；Apple Music 商标归 Apple 所有。
请使用自己订阅的账号，遵守 Apple 服务条款。
