# Zebra RFID SDK セットアップガイド

Zebra EC50 + RFD40 でRFIDタグを読み取るには、**Zebra RFID SDK for Android**が必要です。

## 重要な変更点

実は2つの異なるSDKがあります：

| SDK | 用途 | このプロジェクトでの推奨度 |
|-----|------|---------------------------|
| **RFID SDK for Android** | RFID専用 | ✅ **推奨** - 最新かつRFID特化 |
| EMDK for Android | 汎用エンタープライズ | ⚠️ レガシー |

このプロジェクトは**RFID SDK for Android**を使用します（最新バージョン: v2.0.2.125、2024年3月リリース）。

---

## 📥 RFID SDKのダウンロード方法

### オプション1: Zebra公式サイトから直接ダウンロード（推奨）

1. **Zebraサポートページにアクセス**:
   ```
   https://www.zebra.com/us/en/support-downloads/software/rfid-software/rfid-sdk-for-android.html
   ```

2. **ページの手順**:
   - 「Downloads」タブをクリック
   - 最新版「RFID SDK for Android」を選択
   - 「Download」ボタンをクリック
   - ログインが必要な場合は、無料でアカウント作成

3. **ダウンロードファイル**:
   - `RFIDSDK-Android-v2.0.2.125.zip` (または最新版)

### オプション2: Zebra Developer Portalから

1. **Zebra Developer Portalにアクセス**:
   ```
   https://developer.zebra.com/
   ```

2. 検索バーに「RFID SDK for Android」と入力

3. SDK Downloadsセクションからダウンロード

### オプション3: TechDocsから

1. **TechDocsにアクセス**:
   ```
   https://techdocs.zebra.com/dcs/rfid/android/
   ```

2. ドキュメント内のDownloadリンクを探す

---

## 📦 SDKの抽出と配置

### 1. ZIPファイルを解凍

```bash
unzip RFIDSDK-Android-v2.0.2.125.zip
```

解凍後の構造（例）:
```
RFIDSDK-Android-v2.0.2.125/
├── libs/
│   ├── symbology-api-<version>.aar
│   └── zebra-rfid-sdk-<version>.aar          # ← これが必要
├── samples/
│   └── RFIDMobileApp/
├── docs/
└── README.md
```

### 2. AARファイルを見つける

SDKには以下のファイルが含まれます：
- `zebra-rfid-sdk-<version>.aar` - メインのRFID SDK
- `symbology-api-<version>.aar` - シンボロジー定義（オプション）

### 3. プロジェクトに配置

```bash
# プロジェクトのlibsディレクトリを作成
mkdir -p android/app/libs

# AARファイルをコピー
cp RFIDSDK-Android-v2.0.2.125/libs/zebra-rfid-sdk-*.aar android/app/libs/

# オプション: シンボロジーAPIも使う場合
cp RFIDSDK-Android-v2.0.2.125/libs/symbology-api-*.aar android/app/libs/
```

### 4. build.gradleの確認

`android/app/build.gradle` を開いて、以下を確認：

```gradle
dependencies {
    // Zebra RFID SDK
    implementation fileTree(dir: 'libs', include: ['*.aar'])

    // または個別指定
    // implementation files('libs/zebra-rfid-sdk-2.0.2.125.aar')
}
```

---

## 🚀 ライブラリなしで開発を始める方法

**重要**: 実機がなくてもアプリのUIや基本動作を開発できます！

### モック実装を使用

プロジェクトにはモック実装が含まれています（後述）。これにより：

✅ Zebra実機なしでUIを開発・テスト可能
✅ RFIDライブラリなしでビルド可能
✅ シミュレートされたタグデータで動作確認

```bash
# モックモードでビルド
flutter run --dart-define=MOCK_MODE=true
```

---

## 🔍 バージョン確認

### 現在インストールされているバージョンを確認

AARファイルの中身を確認：

```bash
# AARファイルの内容を確認
unzip -l android/app/libs/zebra-rfid-sdk-*.aar | grep version
```

または、ビルド時のログで確認：

```bash
./gradlew :app:dependencies | grep zebra
```

---

## 📋 バージョン互換性

| RFID SDK Version | Android API Level | 対応デバイス |
|------------------|-------------------|--------------|
| v2.0.2.125 (2024) | 26+ (Android 8.0+) | RFD40, RFD90, RFD8500 |
| v2.1.x | 26+ | 最新のZebraデバイス |

---

## ❓ ダウンロードできない場合

### 解決策1: Zebraサポートに問い合わせ

```
Email: developer@zebra.com
電話: Zebra各国サポートセンター
Web: https://www.zebra.com/us/en/about-zebra/contact-zebra.html
```

### 解決策2: GitHub Sampleから学ぶ

Zebraの公式サンプルアプリを参考にする：
```
https://github.com/zebra-technologies
```

サンプルプロジェクトには正しいライブラリバージョンの情報が含まれています。

### 解決策3: モック実装で開発

実機が手に入るまで、モック実装を使用して開発を進められます（詳細は下記）。

---

## 🧪 モック実装を使った開発

ライブラリがなくても開発を始められるように、モック実装を用意しています。

### モードの切り替え

```kotlin
// RFIDReaderManager.kt に自動判定機能があります
// ライブラリがない場合は自動的にモックモードに切り替わります
```

### モックモードでできること

- ✅ UIの開発とテスト
- ✅ タグ表示ロジックの確認
- ✅ RSSI計算とソート機能のテスト
- ✅ ダミーデータでの動作検証

### モックモードの制限

- ❌ 実際のRFIDタグは読み取れません
- ❌ Zebra固有の機能（バッテリー情報など）は利用不可

---

## 🔧 トラブルシューティング

### エラー: "Could not resolve dependency"

**原因**: AARファイルが配置されていない

**解決策**:
```bash
# ファイルの存在確認
ls -la android/app/libs/*.aar

# ない場合は上記の手順に従ってダウンロード・配置
```

### エラー: "Duplicate class found"

**原因**: 複数バージョンのSDKが混在

**解決策**:
```bash
# 古いバージョンを削除
rm android/app/libs/*.aar

# 最新版のみをコピー
cp /path/to/latest/zebra-rfid-sdk-*.aar android/app/libs/
```

### エラー: "API not found" （実行時）

**原因**: デバイスにZebra RFID Serviceがインストールされていない

**解決策**:
1. Zebra公式サイトから「RFID Service」APKをダウンロード
2. デバイスにインストール
3. アプリを再起動

---

## 📚 参考リンク

### 公式ドキュメント
- [RFID SDK for Android - TechDocs](https://techdocs.zebra.com/dcs/rfid/android/)
- [RFID Developer Guide](https://techdocs.zebra.com/dcs/rfid/android/latest/tutorials/rfiddevguide/)
- [API Reference](https://techdocs.zebra.com/dcs/rfid/android/latest/api/)

### サンプルコード
- [Zebra GitHub - RFID Samples](https://github.com/zebra-technologies)
- [公式サンプルアプリ](https://techdocs.zebra.com/dcs/rfid/android/latest/samples/)

### サポート
- [Zebra Developer Community](https://developer.zebra.com/community)
- [TechDocs Portal](https://techdocs.zebra.com/)

---

## ✅ セットアップ確認チェックリスト

完了したら✓を付けてください：

- [ ] Zebra RFID SDK for Android をダウンロード
- [ ] `zebra-rfid-sdk-*.aar` を `android/app/libs/` に配置
- [ ] `android/app/build.gradle` の dependencies を確認
- [ ] `flutter pub get` を実行
- [ ] `flutter build apk` でビルド成功を確認
- [ ] （実機がある場合）実機でアプリを起動してテスト

または

- [ ] モック実装を使用して開発を開始
- [ ] 後でライブラリを追加する予定

---

## 🎯 次のステップ

### ライブラリを入手できた場合
```bash
flutter pub get
flutter run
```

### まだライブラリがない場合
```bash
# モックモードで開発を開始
flutter run --dart-define=MOCK_MODE=true
```

質問がある場合は、Zebraサポートまたは開発者コミュニティにお問い合わせください。
