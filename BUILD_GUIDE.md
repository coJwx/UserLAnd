# UserLAnd 构建和发布指南

## 目录
1. [本地构建](#本地构建)
2. [GitHub Actions 自动构建](#github-actions-自动构建)
3. [发布到 GitHub Releases](#发布到-github-releases)
4. [签名配置（可选）](#签名配置可选)

---

## 本地构建

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 11
- Android SDK 30

### 构建步骤

#### 1. 克隆仓库
```bash
git clone https://github.com/YOUR_USERNAME/UserLAnd.git
cd UserLAnd
git submodule update --init --recursive
```

#### 2. 构建 Debug APK
```bash
./gradlew assembleDebug
```
APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`

#### 3. 构建 Release APK（未签名）
```bash
./gradlew assembleRelease
```
APK 输出位置：`app/build/outputs/apk/release/app-release-unsigned.apk`

#### 4. 运行测试
```bash
# 单元测试
./gradlew testDebugUnitTest

# 代码格式检查
./gradlew ktlint
```

---

## GitHub Actions 自动构建

本项目已配置 GitHub Actions 工作流（`.github/workflows/build.yml`），支持：

### 自动触发条件
- 推送到 `master` 或 `main` 分支
- 创建以 `v` 开头的标签（如 `v2.9.0`）
- 手动触发（Workflow dispatch）

### 构建产物
每次构建完成后，可以在 GitHub Actions 页面下载：
- Debug APK（已签名，可直接安装测试）
- Release APK（需签名才能安装）

---

## 发布到 GitHub Releases

### 方式一：自动发布（推荐）

1. **更新版本号**
   编辑 `app/build.gradle`：
   ```gradle
   defaultConfig {
       versionName "2.9.0"  // 修改版本号
       // versionCode 会自动生成，无需修改
   }
   ```

2. **提交并打标签**
   ```bash
   git add .
   git commit -m "Release v2.9.0"
   git tag v2.9.0
   git push origin master --tags
   ```

3. **自动发布**
   - GitHub Actions 会自动检测到标签推送
   - 构建完成后自动创建 Release
   - APK 文件会自动上传到 Release 页面

### 方式二：手动上传

1. 本地构建 Release APK
2. 前往 GitHub 仓库 → Releases → Draft a new release
3. 填写版本号和更新说明
4. 上传 APK 文件
5. 点击 Publish release

---

## 签名配置（可选）

如需发布可安装的 Release APK，需要配置签名。

### 方式一：GitHub Secrets（推荐用于 CI）

1. **生成签名密钥**（只需执行一次）
   ```bash
   keytool -genkey -v -keystore userland.keystore -alias userland -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Base64 编码密钥文件**
   ```bash
   base64 userland.keystore | pbcopy  # Mac
   base64 userland.keystore -w 0 | xclip -selection clipboard  # Linux
   certutil -encode userland.keystore tmp.b64 && findstr /v /c:- tmp.b64 > encoded.txt  # Windows
   ```

3. **设置 GitHub Secrets**
   进入仓库 Settings → Secrets and variables → Actions → New repository secret：
   - `SIGNING_KEY`: Base64 编码的 keystore 文件内容
   - `ALIAS`: 密钥别名（如 `userland`）
   - `KEY_STORE_PASSWORD`: keystore 密码
   - `KEY_PASSWORD`: 密钥密码

4. **触发构建**
   推送标签后，GitHub Actions 会自动签名 APK

### 方式二：本地签名

```bash
# 签名 APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore userland.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  userland

# 优化 APK
zipalign -v 4 app-release-unsigned.apk app-release-signed.apk
```

---

## 快速发布清单

```bash
# 1. 确保代码已提交
git status

# 2. 更新版本号（编辑 app/build.gradle）
vim app/build.gradle

# 3. 提交版本更新
git add app/build.gradle
git commit -m "Bump version to v2.9.0"

# 4. 打标签并推送
git tag v2.9.0
git push origin master --tags

# 5. 等待 GitHub Actions 完成，检查 Releases 页面
```

---

## 常见问题

### Q: 构建时提示 assets 缺失？
A: 项目配置了自动下载 assets，首次构建时会自动从 GitHub 下载。

### Q: 如何跳过测试加速构建？
A: 使用 `-x test` 参数：
```bash
./gradlew assembleDebug -x test
```

### Q: 如何修改应用 ID？
A: 编辑 `app/build.gradle`：
```gradle
defaultConfig {
    applicationId "com.yourname.userland"  // 修改为你自己的 ID
}
```

---

## 相关文件

| 文件 | 说明 |
|------|------|
| `app/build.gradle` | 构建配置，包含版本号 |
| `.github/workflows/build.yml` | GitHub Actions 工作流 |
| `BUILD_GUIDE.md` | 本指南 |
