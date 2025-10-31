package com.zebra.rfid.reader

/**
 * RFIDリーダーのアダプターインターフェース
 * 実装とモックの両方で使用
 */
interface RFIDReaderAdapter {
    var onTagRead: ((String, Int) -> Unit)?
    var onStatusChanged: ((String) -> Unit)?

    fun initializeReader(onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun startReading(onSuccess: () -> Unit, onError: (String) -> Unit)
    fun stopReading(onSuccess: () -> Unit, onError: (String) -> Unit)
    fun cleanup()
}
