# Zebra EMDK RFID ライブラリのセットアップガイド

このガイドでは、Zebra EMDK for Android RFID APIライブラリの取得と配置方法を説明します。

## 必要なファイル

- `EMDKRFID-9.1.1.aar` (または最新バージョン)

## ステップバイステップ手順

### 1. Zebra Developer Portalへのアクセス

1. [Zebra Support & Downloads](https://www.zebra.com/us/en/support-downloads.html) にアクセス
2. 検索ボックスに「EMDK for Android」と入力
3. 最新の「EMDK for Android」をダウンロード

### 2. RFID APIの抽出

ダウンロードしたパッケージから以下を探します：

```
EMDK-A-[version]/
├── addon/
│   └── EMDKRFID-[version].aar
```

または、以下のURLから直接ダウンロードも可能です：
- [Zebra TechDocs - EMDK Downloads](https://techdocs.zebra.com/emdk-for-android/latest/guide/download/)

### 3. AARファイルの配置

1. プロジェクトの `android/app/libs/` ディレクトリを作成：

```bash
mkdir -p android/app/libs
```

2. `EMDKRFID-9.1.1.aar` をコピー：

```bash
cp /path/to/downloaded/EMDKRFID-9.1.1.aar android/app/libs/
```

3. ファイルが正しく配置されたことを確認：

```bash
ls -la android/app/libs/
# 以下のように表示されるはずです：
# EMDKRFID-9.1.1.aar
```

### 4. build.gradleの確認

`android/app/build.gradle` に以下の行があることを確認：

```gradle
dependencies {
    implementation files('libs/EMDKRFID-9.1.1.aar')
}
```

**異なるバージョンを使用する場合**は、ファイル名を一致させてください：

```gradle
// 例: バージョン 10.0.0 を使用する場合
implementation files('libs/EMDKRFID-10.0.0.aar')
```

### 5. ビルドとテスト

```bash
cd /path/to/zebra-emdk-flutter
flutter clean
flutter pub get
flutter build apk
```

## バージョン互換性

| EMDK Version | Android API Level | 対応デバイス |
|--------------|-------------------|--------------|
| 9.1.x | 26+ | EC30, EC50, EC55, MC33, RFD40 |
| 10.0.x | 26+ | 最新のZebraデバイス |

## トラブルシューティング

### エラー: "Could not find EMDKRFID-9.1.1.aar"

**原因**: AARファイルが正しい場所に配置されていない

**解決策**:
```bash
# ファイルの存在確認
ls android/app/libs/EMDKRFID-9.1.1.aar

# 存在しない場合は再度コピー
cp /path/to/EMDKRFID-9.1.1.aar android/app/libs/
```

### エラー: "Duplicate class found"

**原因**: 複数のバージョンのEMDKライブラリが存在

**解決策**:
```bash
# 古いバージョンを削除
rm android/app/libs/EMDKRFID-*.aar
# 使用するバージョンのみをコピー
cp /path/to/EMDKRFID-9.1.1.aar android/app/libs/
```

### エラー: "API not found" 実行時エラー

**原因**: Zebraデバイス上にEMDKランタイムがインストールされていない

**解決策**:
- Zebra公式からEMDKランタイムAPKをインストール
- または、DataWedgeがインストールされているか確認

## 代替方法: Maven Repositoryの使用（将来的な改善）

現在、Zebra EMDK RFIDはMaven Centralで公開されていないため、手動でAARファイルを配置する必要があります。

将来的にZebraがMavenリポジトリを提供する場合は、以下のように依存関係を追加できます：

```gradle
repositories {
    maven {
        url 'https://zebratech.jfrog.io/artifactory/emdk-releases'
    }
}

dependencies {
    implementation 'com.symbol:emdk-rfid:9.1.1'
}
```

**注意**: これは現時点では利用できません。

## RFD40専用の注意事項

RFD40を使用する場合、以下の点に注意してください：

1. **Bluetoothペアリング**: RFD40はEC50とBluetoothでペアリングする必要があります
2. **権限**: `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `ACCESS_FINE_LOCATION` が必要
3. **DataWedge**: DataWedgeがRFIDをブロックしていないことを確認

## 参考リンク

- [Zebra EMDK for Android - 公式ドキュメント](https://techdocs.zebra.com/emdk-for-android/)
- [RFID API3 Developer Guide](https://techdocs.zebra.com/emdk-for-android/latest/guide/rfid_guide/)
- [RFD40 製品情報](https://www.zebra.com/us/en/products/rfid/rfid-readers/rfd40.html)

## 開発者サポート

問題が発生した場合は、Zebraの開発者サポートにお問い合わせください：
- [Zebra Developer Portal](https://developer.zebra.com/)
- [Zebra Support Community](https://community.zebra.com/)
