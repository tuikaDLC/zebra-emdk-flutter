package com.zebra.rfid.reader

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.random.Random

/**
 * Zebra RFID SDKがない場合のモック実装
 * 開発・テスト用にシミュレートされたタグデータを提供
 */
class RFIDReaderManagerMock(private val context: Context) : RFIDReaderAdapter {
    private val TAG = "RFIDReaderManagerMock"

    private var isReaderInitialized = false
    private var isReading = false
    private val handler = Handler(Looper.getMainLooper())
    private var readingRunnable: Runnable? = null

    var onTagRead: ((String, Int) -> Unit)? = null
    var onStatusChanged: ((String) -> Unit)? = null

    // シミュレート用のタグデータ
    private val mockTags = listOf(
        "E200001CA2150400112233AA" to -45,
        "E200001CA2150400112233BB" to -55,
        "E200001CA2150400112233CC" to -65,
        "E200001CA2150400112233DD" to -75,
        "E200001CA2150400112233EE" to -85,
    )

    /**
     * モックリーダーの初期化
     */
    fun initializeReader(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        Log.d(TAG, "Mock: Initializing reader")

        handler.postDelayed({
            isReaderInitialized = true
            val message = "✅ モックモード: RFIDリーダーをシミュレート中（実機なし）"
            onSuccess(message)
            onStatusChanged?.invoke(message)
            Log.d(TAG, "Mock: Reader initialized")
        }, 500) // 初期化の遅延をシミュレート
    }

    /**
     * モック読み取り開始
     */
    fun startReading(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isReaderInitialized) {
            onError("リーダーが初期化されていません")
            return
        }

        Log.d(TAG, "Mock: Starting to read tags")
        isReading = true
        onSuccess()
        onStatusChanged?.invoke("🔄 モック読み取り中（シミュレーション）")

        // 定期的にランダムなタグを送信
        readingRunnable = object : Runnable {
            override fun run() {
                if (isReading) {
                    // ランダムにタグを選択
                    val randomTag = mockTags.random()
                    val tagId = randomTag.first
                    // RSSIに±5dBmのランダム変動を追加（実際の環境をシミュレート）
                    val baseRssi = randomTag.second
                    val rssi = baseRssi + Random.nextInt(-5, 6)

                    Log.d(TAG, "Mock: Tag read - $tagId with RSSI $rssi dBm")
                    onTagRead?.invoke(tagId, rssi)

                    // 次の読み取りをスケジュール（200-800msのランダム間隔）
                    handler.postDelayed(this, Random.nextLong(200, 800))
                }
            }
        }

        handler.post(readingRunnable!!)
    }

    /**
     * モック読み取り停止
     */
    fun stopReading(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isReaderInitialized) {
            onError("リーダーが初期化されていません")
            return
        }

        Log.d(TAG, "Mock: Stopping tag reading")
        isReading = false
        readingRunnable?.let { handler.removeCallbacks(it) }
        onSuccess()
        onStatusChanged?.invoke("⏸️ 停止（モックモード）")
    }

    /**
     * クリーンアップ
     */
    fun cleanup() {
        Log.d(TAG, "Mock: Cleanup")
        isReading = false
        readingRunnable?.let { handler.removeCallbacks(it) }
        isReaderInitialized = false
    }

    /**
     * バッテリー情報のシミュレート（オプション）
     */
    fun simulateBatteryEvent() {
        val batteryLevel = Random.nextInt(60, 100)
        onStatusChanged?.invoke("🔋 バッテリー: $batteryLevel% (シミュレート)")
    }

    /**
     * トリガーイベントのシミュレート（オプション）
     */
    fun simulateTriggerPress() {
        onStatusChanged?.invoke("🔘 トリガー押下 (シミュレート)")
    }
}
