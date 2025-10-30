package com.zebra.rfid.reader

import android.content.Context
import android.util.Log
import com.zebra.rfid.api3.*

class RFIDReaderManager(private val context: Context) {
    private val TAG = "RFIDReaderManager"

    private var readers: Readers? = null
    private var reader: RFIDReader? = null
    private var isReaderInitialized = false

    var onTagRead: ((String, Int) -> Unit)? = null
    var onStatusChanged: ((String) -> Unit)? = null

    /**
     * RFIDリーダーの初期化
     */
    fun initializeReader(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        try {
            if (readers == null) {
                readers = Readers(context, ENUM_TRANSPORT.ALL)
            }

            val availableReaders = readers?.GetAvailableRFIDReaderList()

            if (availableReaders.isNullOrEmpty()) {
                onError("利用可能なRFIDリーダーが見つかりません")
                return
            }

            // 最初のリーダーを使用（通常はRFD40）
            val readerDevice = availableReaders[0]
            reader = readerDevice.rfidReader

            if (reader != null) {
                reader?.connect()
                configureReader()
                setupEventHandlers()
                isReaderInitialized = true
                onSuccess("RFIDリーダーが正常に初期化されました: ${readerDevice.name}")
            } else {
                onError("RFIDリーダーの初期化に失敗しました")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Initialize error: ${e.message}", e)
            onError("初期化エラー: ${e.message}")
        }
    }

    /**
     * リーダーの設定
     */
    private fun configureReader() {
        try {
            reader?.let { rfidReader ->
                // RFモードの設定
                rfidReader.Config.Antennas.setAntennaRfConfig(1, 270, 270)

                // タグレポートの設定 - RSSIを含める
                val reportConfig = rfidReader.Config.TagReportConfig
                reportConfig.enableRSSI(true)  // RSSI有効化
                reportConfig.enablePeakRSSI(true)  // ピークRSSI有効化
                reportConfig.enableTagSeenCount(true)
                reportConfig.enableFirstSeenTimeStamp(false)
                reportConfig.enableLastSeenTimeStamp(false)

                rfidReader.Config.TagReportConfig = reportConfig

                // セッション設定
                rfidReader.Config.setSession(SESSION.SESSION_S0)

                // 動的パワー最適化を無効化（一貫したRSSI測定のため）
                rfidReader.Config.DPOState = DYNAMIC_POWER_OPTIMIZATION.DISABLE

                Log.d(TAG, "Reader configured successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Configuration error: ${e.message}", e)
        }
    }

    /**
     * イベントハンドラーの設定
     */
    private fun setupEventHandlers() {
        reader?.Events?.addEventsListener(object : RfidEventsListener {
            override fun eventReadNotify(rfidReadEvents: RfidReadEvents?) {
                rfidReadEvents?.let { events ->
                    val tags = events.readEventData
                    tags?.forEach { tagData ->
                        val tagId = tagData.tagID
                        val peakRssi = tagData.peakRSSI.toInt()  // RSSI値を取得

                        Log.d(TAG, "Tag detected: $tagId, RSSI: $peakRssi dBm")
                        onTagRead?.invoke(tagId, peakRssi)
                    }
                }
            }

            override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents?) {
                rfidStatusEvents?.let { events ->
                    val statusEvent = events.StatusEventData
                    val status = when(statusEvent.StatusEventType) {
                        STATUS_EVENT_TYPE.DISCONNECTION_EVENT -> "リーダーが切断されました"
                        STATUS_EVENT_TYPE.BATTERY_EVENT -> "バッテリー: ${statusEvent.batteryData?.level}%"
                        STATUS_EVENT_TYPE.TEMPERATURE_ALARM_EVENT -> "温度警告"
                        STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT -> {
                            if (statusEvent.HandheldTriggerEventData.handheldEvent == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                                "トリガー押下"
                            } else {
                                "トリガー解放"
                            }
                        }
                        else -> "ステータス更新: ${statusEvent.StatusEventType}"
                    }

                    Log.d(TAG, "Status: $status")
                    onStatusChanged?.invoke(status)
                }
            }
        })

        // イベント通知を有効化
        reader?.Events?.setReaderDisconnectEvent(true)
        reader?.Events?.setBatteryEvent(true)
        reader?.Events?.setHandheldEvent(true)
        reader?.Events?.setAttachTagDataWithReadEvent(false)
    }

    /**
     * 読み取り開始
     */
    fun startReading(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isReaderInitialized) {
            onError("リーダーが初期化されていません")
            return
        }

        try {
            reader?.Actions?.Inventory?.perform()
            onSuccess()
            onStatusChanged?.invoke("読み取り中...")
            Log.d(TAG, "Started reading")
        } catch (e: Exception) {
            Log.e(TAG, "Start reading error: ${e.message}", e)
            onError("読み取り開始エラー: ${e.message}")
        }
    }

    /**
     * 読み取り停止
     */
    fun stopReading(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isReaderInitialized) {
            onError("リーダーが初期化されていません")
            return
        }

        try {
            reader?.Actions?.Inventory?.stop()
            onSuccess()
            onStatusChanged?.invoke("停止")
            Log.d(TAG, "Stopped reading")
        } catch (e: Exception) {
            Log.e(TAG, "Stop reading error: ${e.message}", e)
            onError("読み取り停止エラー: ${e.message}")
        }
    }

    /**
     * クリーンアップ
     */
    fun cleanup() {
        try {
            reader?.let {
                if (it.isConnected) {
                    it.disconnect()
                }
            }
            readers?.Dispose()
            isReaderInitialized = false
            Log.d(TAG, "Cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup error: ${e.message}", e)
        }
    }
}
