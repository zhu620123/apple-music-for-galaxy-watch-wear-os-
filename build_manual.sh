#!/usr/bin/env bash
# 无 Gradle 手动构建脚本（自动适配 aapt2 / 系统 aapt）
# x86_64：需 ANDROID_SDK（build-tools;34.0.0 + platforms;android-34）
# aarch64：aapt2 仅有 x86_64 版，需 apt install aapt zipalign，脚本会自动降级
# 用法：ANDROID_SDK=/path/to/sdk ./build_manual.sh

SDK="${ANDROID_SDK:-}"
BT="${SDK:+$SDK/build-tools/34.0.0}"
PLATFORM="${SDK:+$SDK/platforms/android-34/android.jar}"
APP="app/src/main"
OUT="build-manual"

rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex"

USE_AAPT2=0
if [ -n "$BT" ] && [ -x "$BT/aapt2" ] && "$BT/aapt2" version >/dev/null 2>&1; then
  USE_AAPT2=1
fi

if [ "$USE_AAPT2" = "1" ]; then
  echo "[1/5] aapt2 编译资源..."
  "$BT/aapt2" compile --dir "$APP/res" -o "$OUT/res.zip"
  "$BT/aapt2" link -o "$OUT/unsigned.apk" -I "$PLATFORM" \
    --manifest "$APP/AndroidManifest.xml" -R "$OUT/res.zip" \
    --java "$OUT/gen" --auto-add-overlay \
    --min-sdk-version 26 --target-sdk-version 34 \
    --version-code 1 --version-name 1.0
else
  echo "[1/5] aapt(系统版) 编译资源..."
  command -v aapt >/dev/null 2>&1 || { echo "缺少 aapt（apt install aapt）"; exit 1; }
  # 旧版 aapt 需要 manifest 带 package 属性，且文件名必须是 AndroidManifest.xml；生成临时副本，不动源文件
  cp "$APP/AndroidManifest.xml" "$OUT/AndroidManifest.xml"
  sed -i 's|<manifest xmlns:android="http://schemas.android.com/apk/res/android">|<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.operit.wearamlauncher">|' \
    "$OUT/AndroidManifest.xml"
  aapt package -f -m -S "$APP/res" -M "$OUT/AndroidManifest.xml" \
    -I "$PLATFORM" -J "$OUT/gen" -F "$OUT/unsigned.apk" \
    --min-sdk-version 26 --target-sdk-version 34 \
    --version-code 1 --version-name 1.0
fi

echo "[2/5] javac 编译 Java..."
javac --release 17 -cp "$PLATFORM" -d "$OUT/classes" \
  "$OUT"/gen/com/operit/wearamlauncher/R.java \
  "$APP"/java/com/operit/wearamlauncher/*.java

echo "[3/5] d8 生成 dex..."
"$BT/d8" --lib "$PLATFORM" --release \
  --output "$OUT/dex" "$OUT"/classes/com/operit/wearamlauncher/*.class

echo "[4/5] 打包 + zipalign..."
cp "$OUT/unsigned.apk" "$OUT/unsigned2.apk"
(cd "$OUT" && zip -q -j unsigned2.apk dex/classes.dex)
ZIPALIGN=""
if [ "$USE_AAPT2" = "1" ] && [ -n "$BT" ] && [ -x "$BT/zipalign" ]; then
  ZIPALIGN="$BT/zipalign"
else
  ZIPALIGN="$(command -v zipalign 2>/dev/null || true)"
fi
if [ -n "$ZIPALIGN" ]; then
  "$ZIPALIGN" -f 4 "$OUT/unsigned2.apk" "$OUT/aligned.apk"
else
  cp "$OUT/unsigned2.apk" "$OUT/aligned.apk"
  echo "（无 zipalign，跳过对齐，不影响 adb install）"
fi

echo "[5/5] 签名..."
KEYSTORE="${KEYSTORE:-$OUT/debug.keystore}"
if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -v -keystore "$KEYSTORE" \
    -alias androiddebugkey -storepass android -keypass android \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" > /dev/null 2>&1
fi
"$BT/apksigner" sign --ks "$KEYSTORE" \
  --ks-pass pass:android --key-pass pass:android \
  --out "$OUT/WearAMLauncher.apk" "$OUT/aligned.apk"

echo "完成：$PWD/$OUT/WearAMLauncher.apk"