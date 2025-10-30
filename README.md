# Zebra RFID Reader App

Zebra EC50 + RFD40を使用してRFIDタグを読み取り、RSSI（受信信号強度）を測定するFlutterアプリケーション。

## 機能

- ✅ 複数のRFIDタグの同時読み取り
- ✅ 各タグのRSSI値測定
- ✅ RSSI値に基づく距離推定
- ✅ リアルタイムでタグ情報を表示
- ✅ 色分けされたRSSI表示（近い: 緑、遠い: 赤）
- ✅ タグの自動ソート（信号強度順）

## 対応デバイス

- **ハンドヘルド端末**: Zebra EC50
- **RFIDリーダー**: Zebra RFD40 (Bluetooth接続)

## 必要な環境

- Flutter 3.0以上
- Android SDK 26以上
- Zebra EMDK for Android RFID Library (バージョン 9.1.1以上)

## セットアップ

### 1. Zebra EMDK RFIDライブラリの取得

1. [Zebra Support Portal](https://www.zebra.com/us/en/support-downloads.html)にアクセス
2. "EMDK for Android" を検索
3. 最新版の RFID API をダウンロード
4. `EMDKRFID-9.1.1.aar` ファイルを抽出

### 2. ライブラリの配置

```bash
mkdir -p android/app/libs
cp /path/to/EMDKRFID-9.1.1.aar android/app/libs/
```

**重要**: `android/app/build.gradle` の以下の行でバージョンが一致していることを確認してください：

```gradle
implementation files('libs/EMDKRFID-9.1.1.aar')
```

### 3. Flutterパッケージのインストール

```bash
flutter pub get
```

### 4. ビルドと実行

```bash
flutter run
```

または、リリースビルド：

```bash
flutter build apk --release
```

## 使用方法

### 1. デバイスの準備

1. EC50デバイスにRFD40をBluetoothでペアリング
2. RFD40が正しく接続されていることを確認

### 2. アプリの操作

1. **リーダー初期化**: アプリ起動後、「リーダー初期化」ボタンをタップ
2. **読み取り開始**: 「読み取り開始」ボタンをタップしてスキャン開始
3. **タグ検出**: 検出されたタグがリアルタイムで表示されます
4. **停止**: 「停止」ボタンで読み取りを停止
5. **クリア**: 「クリア」ボタンで検出済みタグのリストをクリア

### RSSI値と距離の目安

| RSSI範囲 | 推定距離 | 表示色 |
|----------|---------|--------|
| > -50 dBm | < 0.5m | 緑 |
| -50 ~ -60 dBm | 0.5-1m | 薄緑 |
| -60 ~ -70 dBm | 1-2m | オレンジ |
| -70 ~ -80 dBm | 2-4m | 濃いオレンジ |
| < -80 dBm | > 4m | 赤 |

**注意**: 実際の距離は環境要因（金属、水分、干渉など）により変動します。

## プロジェクト構造

```
zebra-emdk-flutter/
├── lib/
│   └── main.dart                 # Flutterアプリのメインコード
├── android/
│   ├── app/
│   │   ├── build.gradle          # Android依存関係設定
│   │   ├── libs/
│   │   │   └── EMDKRFID-9.1.1.aar  # Zebra EMDKライブラリ
│   │   └── src/main/
│   │       ├── AndroidManifest.xml  # 権限設定
│   │       └── kotlin/com/zebra/rfid/reader/
│   │           ├── MainActivity.kt          # Method Channelブリッジ
│   │           └── RFIDReaderManager.kt     # RFID制御ロジック
│   └── build.gradle
└── pubspec.yaml                  # Flutter依存関係
```

## 技術詳細

### アーキテクチャ

```
┌─────────────────┐
│  Flutter UI     │
│  (main.dart)    │
└────────┬────────┘
         │ Method Channel
         │ "com.zebra.rfid/reader"
┌────────┴────────┐
│  MainActivity   │
│  (Kotlin)       │
└────────┬────────┘
         │
┌────────┴────────────┐
│ RFIDReaderManager   │
│ - initializeReader  │
│ - startReading      │
│ - stopReading       │
└────────┬────────────┘
         │
┌────────┴────────────┐
│  Zebra EMDK API     │
│  (EMDKRFID.aar)     │
└─────────────────────┘
```

### Method Channel API

#### Flutter → Android

- `initializeReader()`: RFIDリーダーの初期化
- `startReading()`: タグ読み取り開始
- `stopReading()`: タグ読み取り停止

#### Android → Flutter (コールバック)

- `onTagRead`: タグ検出時のコールバック
  ```dart
  {
    "tagId": "E200001234567890ABCDEF12",
    "rssi": -65
  }
  ```

- `onStatusChanged`: ステータス変更時のコールバック
  ```dart
  {
    "status": "読み取り中..."
  }
  ```

### RFID設定

RFIDReaderManager.ktで以下を設定：

- **RFパワー**: 270 (27.0 dBm)
- **セッション**: SESSION_S0
- **RSSI取得**: 有効（peakRSSI）
- **動的パワー最適化**: 無効（一貫したRSSI測定のため）

## トラブルシューティング

### リーダーが検出されない

1. RFD40がBluetoothでペアリングされているか確認
2. アプリに Bluetooth と Location 権限が付与されているか確認
3. デバイスを再起動してみる

### RSSI値が不安定

- 周囲の金属や電波干渉を減らす
- タグの向きを変えてみる
- アンテナパワーを調整（RFIDReaderManager.ktの`setAntennaRfConfig`）

### ビルドエラー

- EMDK AARファイルが正しく配置されているか確認
- `flutter clean` → `flutter pub get` を実行
- Android Studioでプロジェクトを同期

## 権限

アプリは以下の権限を必要とします：

- `BLUETOOTH` / `BLUETOOTH_ADMIN`: Bluetooth通信
- `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN`: Android 12以上のBluetooth
- `ACCESS_FINE_LOCATION`: RFIDリーダーのスキャン
- `com.symbol.emdk.permission.EMDK`: Zebra EMDK使用

## 参考資料

- [Zebra EMDK for Android Documentation](https://techdocs.zebra.com/emdk-for-android/)
- [RFID API Developer Guide](https://techdocs.zebra.com/emdk-for-android/latest/guide/rfid_guide/)
- [Flutter Platform Channels](https://docs.flutter.dev/development/platform-integration/platform-channels)

## ライセンス

このプロジェクトはサンプルコードです。商用利用の際は適切なライセンスを設定してください。

## 開発者

Created for Zebra EC50 + RFD40 RFID reading with RSSI measurement.
