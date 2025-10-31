package com.zebra.rfid.reader

import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val TAG = "MainActivity"
    private val CHANNEL = "com.zebra.rfid/reader"

    // どちらかの実装を使用（インターフェースで抽象化）
    private lateinit var readerAdapter: RFIDReaderAdapter

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Zebra RFID SDKとRFIDReaderManagerが利用可能かチェック
        readerAdapter = if (isRealImplementationAvailable()) {
            Log.i(TAG, "Zebra RFID SDK detected - using real implementation")
            createRealImplementation()
        } else {
            Log.w(TAG, "Zebra RFID SDK not found - using MOCK implementation")
            RFIDReaderManagerMock(this)
        }

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "initializeReader" -> {
                    readerAdapter.initializeReader(
                        onSuccess = { message ->
                            result.success(mapOf("success" to true, "message" to message))
                        },
                        onError = { error ->
                            result.success(mapOf("success" to false, "message" to error))
                        }
                    )
                }
                "startReading" -> {
                    readerAdapter.startReading(
                        onSuccess = {
                            result.success(true)
                        },
                        onError = { error ->
                            result.error("START_READING_ERROR", error, null)
                        }
                    )
                }
                "stopReading" -> {
                    readerAdapter.stopReading(
                        onSuccess = {
                            result.success(true)
                        },
                        onError = { error ->
                            result.error("STOP_READING_ERROR", error, null)
                        }
                    )
                }
                else -> {
                    result.notImplemented()
                }
            }
        }

        // タグ読み取りのコールバック設定
        readerAdapter.onTagRead = { tagId, rssi ->
            runOnUiThread {
                MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
                    .invokeMethod("onTagRead", mapOf("tagId" to tagId, "rssi" to rssi))
            }
        }

        // ステータス変更のコールバック設定
        readerAdapter.onStatusChanged = { status ->
            runOnUiThread {
                MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
                    .invokeMethod("onStatusChanged", mapOf("status" to status))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        readerAdapter.cleanup()
        super.onDestroy()
    }

    /**
     * 実装クラスが利用可能かチェック（Zebra SDK + RFIDReaderManager）
     */
    private fun isRealImplementationAvailable(): Boolean {
        return try {
            // Zebra RFID SDKのクラスをチェック
            Class.forName("com.zebra.rfid.api3.Readers")
            // RFIDReaderManagerクラスをチェック
            Class.forName("com.zebra.rfid.reader.RFIDReaderManager")
            true
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "Real implementation not available: ${e.message}")
            false
        }
    }

    /**
     * リフレクションを使ってRFIDReaderManagerを動的に作成
     */
    private fun createRealImplementation(): RFIDReaderAdapter {
        return try {
            val clazz = Class.forName("com.zebra.rfid.reader.RFIDReaderManager")
            val constructor = clazz.getConstructor(android.content.Context::class.java)
            constructor.newInstance(this) as RFIDReaderAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create real implementation: ${e.message}")
            // フォールバックとしてモック実装を返す
            RFIDReaderManagerMock(this)
        }
    }
}
