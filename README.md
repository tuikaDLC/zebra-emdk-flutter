# Zebra RFID Reader App

Zebra EC50 + RFD40を使用してRFIDタグを読み取り、RSSI（受信信号強度）を測定するFlutterアプリケーション。

## 🎯 機能

- ✅ 複数のRFIDタグの同時読み取り
- ✅ 各タグのRSSI値測定
- ✅ RSSI値に基づく距離推定
- ✅ リアルタイムでタグ情報を表示
- ✅ 色分けされたRSSI表示（近い: 緑、遠い: 赤）
- ✅ タグの自動ソート（信号強度順）
- ✅ **モック実装で実機なしでも開発可能** 🆕

## 📱 対応デバイス

- **ハンドヘルド端末**: Zebra EC50
- **RFIDリーダー**: Zebra RFD40 (Bluetooth接続)

## 💻 必要な環境

- Flutter 3.0以上
- Android SDK 26以上
- **Zebra RFID SDK for Android** (オプション - 実機で動作させる場合のみ)

## ⚡ クイックスタート（SDKなしでも開始可能）

Zebra SDKがなくても、モック実装ですぐに開発を始められます：

```bash
# リポジトリをクローン
git clone <repository-url>
cd zebra-emdk-flutter

# 依存関係をインストール
flutter pub get

# モックモードで実行（Zebra実機不要）
flutter run
```

アプリは自動的にモックモードで起動し、シミュレートされたRFIDタグデータを表示します。

## 📦 セットアップ

### オプション A: モック実装で開発（推奨・初心者向け）

Zebra SDKがなくても、すぐに開発を始められます：

```bash
# 1. 依存関係をインストール
flutter pub get

# 2. アプリを実行（自動的にモックモードで起動）
flutter run
```

**モックモードでできること：**
- ✅ UIの開発とテスト
- ✅ シミュレートされたRFIDタグデータの表示
- ✅ RSSI表示ロジックの確認
- ✅ 距離推定機能のテスト

**モックモードの制限：**
- ❌ 実際のRFIDタグは読み取れません（シミュレーションのみ）

---

### オプション B: 実機で動作（Zebra SDK使用）

実機でRFIDタグを読み取る場合：

#### 1. Zebra RFID SDK の取得

詳細は **[LIBRARY_SETUP.md](LIBRARY_SETUP.md)** を参照してください。

簡易手順：
1. [Zebra RFID SDK for Android](https://www.zebra.com/us/en/support-downloads/software/rfid-software/rfid-sdk-for-android.html) をダウンロード
2. `zebra-rfid-sdk-*.aar` を抽出

#### 2. ライブラリの配置

```bash
mkdir -p android/app/libs
cp /path/to/zebra-rfid-sdk-*.aar android/app/libs/
```

#### 3. 実装クラスの有効化

```bash
cd android/app/src/main/kotlin/com/zebra/rfid/reader/
mv RFIDReaderManager.kt.template RFIDReaderManager.kt
```

#### 4. ビルドと実行

```bash
flutter clean
flutter pub get
flutter run
```

アプリは自動的にZebra SDKを検出し、実機モードで動作します。

---

## 🚀 使用方法

### モックモードの場合

1. **リーダー初期化**: アプリ起動後、「リーダー初期化」ボタンをタップ
2. **読み取り開始**: 「読み取り開始」ボタンでシミュレーション開始
3. **タグ表示**: シミュレートされたタグがリアルタイムで表示されます
4. **停止**: 「停止」ボタンで読み取りを停止
5. **クリア**: 「クリア」ボタンでタグリストをクリア

### 実機モードの場合

#### 1. デバイスの準備

1. EC50デバイスにRFD40をBluetoothでペアリング
2. RFD40が正しく接続されていることを確認

#### 2. アプリの操作

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

## 📂 プロジェクト構造

```
zebra-emdk-flutter/
├── lib/
│   └── main.dart                              # Flutterアプリのメインコード
├── android/
│   ├── app/
│   │   ├── build.gradle                       # Android依存関係設定
│   │   ├── libs/                              # Zebra SDKを配置（オプション）
│   │   │   └── zebra-rfid-sdk-*.aar          # ← SDKファイルをここに配置
│   │   └── src/main/
│   │       ├── AndroidManifest.xml            # 権限設定
│   │       └── kotlin/com/zebra/rfid/reader/
│   │           ├── MainActivity.kt            # Method Channelブリッジ
│   │           ├── RFIDReaderAdapter.kt       # インターフェース
│   │           ├── RFIDReaderManagerMock.kt   # モック実装（常に有効）
│   │           └── RFIDReaderManager.kt.template  # 実機実装（要有効化）
│   └── build.gradle
├── README.md                                  # このファイル
├── LIBRARY_SETUP.md                           # SDK詳細セットアップガイド
└── pubspec.yaml                               # Flutter依存関係
```

**重要なファイル：**
- `RFIDReaderManagerMock.kt` - デフォルトで有効、実機なしで動作
- `RFIDReaderManager.kt.template` - SDK入手後に `.template` を削除して有効化

## 技術詳細

### アーキテクチャ

```
┌──────────────────────────┐
│      Flutter UI          │
│      (main.dart)         │
└───────────┬──────────────┘
            │ Method Channel
            │ "com.zebra.rfid/reader"
┌───────────┴──────────────┐
│      MainActivity        │
│  (自動検出してアダプター選択) │
└───────────┬──────────────┘
            │
    ┌───────┴────────┐
    │                │
┌───┴──────────┐  ┌─┴──────────────────┐
│ Mock実装      │  │ 実機実装            │
│ (デフォルト)   │  │ (SDK必要)          │
│              │  │                    │
│ シミュレート  │  │ RFIDReaderManager  │
│ されたタグ    │  │ ↓                  │
│ データを生成  │  │ Zebra RFID SDK     │
│              │  │ (*.aar)            │
└──────────────┘  └────────────────────┘
```

**アダプター選択ロジック:**
- Zebra SDKクラスが見つかる → 実機実装を使用
- Zebra SDKが見つからない → モック実装を使用（自動フォールバック）

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
