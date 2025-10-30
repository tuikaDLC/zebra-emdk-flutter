package com.zebra.rfid.reader

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.zebra.rfid/reader"
    private lateinit var rfidReaderManager: RFIDReaderManager

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        rfidReaderManager = RFIDReaderManager(this)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "initializeReader" -> {
                    rfidReaderManager.initializeReader(
                        onSuccess = { message ->
                            result.success(mapOf("success" to true, "message" to message))
                        },
                        onError = { error ->
                            result.success(mapOf("success" to false, "message" to error))
                        }
                    )
                }
                "startReading" -> {
                    rfidReaderManager.startReading(
                        onSuccess = {
                            result.success(true)
                        },
                        onError = { error ->
                            result.error("START_READING_ERROR", error, null)
                        }
                    )
                }
                "stopReading" -> {
                    rfidReaderManager.stopReading(
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
        rfidReaderManager.onTagRead = { tagId, rssi ->
            runOnUiThread {
                MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
                    .invokeMethod("onTagRead", mapOf("tagId" to tagId, "rssi" to rssi))
            }
        }

        // ステータス変更のコールバック設定
        rfidReaderManager.onStatusChanged = { status ->
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
        rfidReaderManager.cleanup()
        super.onDestroy()
    }
}
