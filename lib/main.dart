import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Zebra RFID Reader',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      home: const RFIDReaderPage(),
    );
  }
}

class RFIDReaderPage extends StatefulWidget {
  const RFIDReaderPage({super.key});

  @override
  State<RFIDReaderPage> createState() => _RFIDReaderPageState();
}

class _RFIDReaderPageState extends State<RFIDReaderPage> {
  static const platform = MethodChannel('com.zebra.rfid/reader');

  List<RFIDTag> _tags = [];
  bool _isReading = false;
  bool _isInitialized = false;
  String _statusMessage = '初期化されていません';

  @override
  void initState() {
    super.initState();
    _setupMethodCallHandler();
    _requestPermissions();
  }

  void _setupMethodCallHandler() {
    platform.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'onTagRead':
          _handleTagRead(call.arguments);
          break;
        case 'onStatusChanged':
          _handleStatusChanged(call.arguments);
          break;
        default:
          print('Unknown method: ${call.method}');
      }
    });
  }

  Future<void> _requestPermissions() async {
    await Permission.bluetoothConnect.request();
    await Permission.bluetoothScan.request();
    await Permission.location.request();
  }

  Future<void> _initializeReader() async {
    try {
      final result = await platform.invokeMethod('initializeReader');
      setState(() {
        _isInitialized = result['success'];
        _statusMessage = result['message'];
      });
    } on PlatformException catch (e) {
      setState(() {
        _statusMessage = 'エラー: ${e.message}';
      });
    }
  }

  Future<void> _startReading() async {
    try {
      await platform.invokeMethod('startReading');
      setState(() {
        _isReading = true;
        _statusMessage = '読み取り中...';
      });
    } on PlatformException catch (e) {
      setState(() {
        _statusMessage = 'エラー: ${e.message}';
      });
    }
  }

  Future<void> _stopReading() async {
    try {
      await platform.invokeMethod('stopReading');
      setState(() {
        _isReading = false;
        _statusMessage = '停止';
      });
    } on PlatformException catch (e) {
      setState(() {
        _statusMessage = 'エラー: ${e.message}';
      });
    }
  }

  Future<void> _clearTags() async {
    setState(() {
      _tags.clear();
    });
  }

  void _handleTagRead(dynamic arguments) {
    final tagId = arguments['tagId'] as String;
    final rssi = arguments['rssi'] as int;
    final distance = _estimateDistance(rssi);

    setState(() {
      final existingIndex = _tags.indexWhere((tag) => tag.tagId == tagId);
      if (existingIndex >= 0) {
        _tags[existingIndex] = RFIDTag(
          tagId: tagId,
          rssi: rssi,
          distance: distance,
          lastSeen: DateTime.now(),
        );
      } else {
        _tags.add(RFIDTag(
          tagId: tagId,
          rssi: rssi,
          distance: distance,
          lastSeen: DateTime.now(),
        ));
      }
      // RSSI値で降順ソート（近い順）
      _tags.sort((a, b) => b.rssi.compareTo(a.rssi));
    });
  }

  void _handleStatusChanged(dynamic arguments) {
    setState(() {
      _statusMessage = arguments['status'] as String;
    });
  }

  // RSSIから距離を推定（簡易計算）
  String _estimateDistance(int rssi) {
    if (rssi > -50) return '< 0.5m';
    if (rssi > -60) return '0.5-1m';
    if (rssi > -70) return '1-2m';
    if (rssi > -80) return '2-4m';
    return '> 4m';
  }

  Color _getRssiColor(int rssi) {
    if (rssi > -50) return Colors.green;
    if (rssi > -60) return Colors.lightGreen;
    if (rssi > -70) return Colors.orange;
    if (rssi > -80) return Colors.deepOrange;
    return Colors.red;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Zebra RFID Reader'),
        elevation: 2,
      ),
      body: Column(
        children: [
          _buildControlPanel(),
          _buildStatusBar(),
          Expanded(child: _buildTagList()),
        ],
      ),
    );
  }

  Widget _buildControlPanel() {
    return Container(
      padding: const EdgeInsets.all(16),
      color: Colors.grey[100],
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: _isInitialized ? null : _initializeReader,
                  icon: const Icon(Icons.settings_input_antenna),
                  label: const Text('リーダー初期化'),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: _isInitialized && !_isReading ? _startReading : null,
                  icon: const Icon(Icons.play_arrow),
                  label: const Text('読み取り開始'),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    backgroundColor: Colors.green,
                    foregroundColor: Colors.white,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: _isReading ? _stopReading : null,
                  icon: const Icon(Icons.stop),
                  label: const Text('停止'),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    backgroundColor: Colors.red,
                    foregroundColor: Colors.white,
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: ElevatedButton.icon(
                  onPressed: _tags.isEmpty ? null : _clearTags,
                  icon: const Icon(Icons.clear_all),
                  label: const Text('クリア'),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatusBar() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      color: _isReading ? Colors.green[50] : Colors.grey[200],
      child: Row(
        children: [
          Icon(
            _isReading ? Icons.radar : Icons.radio_button_unchecked,
            color: _isReading ? Colors.green : Colors.grey,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              _statusMessage,
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: _isReading ? Colors.green[900] : Colors.grey[700],
              ),
            ),
          ),
          Text(
            '${_tags.length} タグ検出',
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _buildTagList() {
    if (_tags.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.wifi_tethering, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              'タグが検出されていません',
              style: TextStyle(fontSize: 16, color: Colors.grey[600]),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      itemCount: _tags.length,
      padding: const EdgeInsets.all(8),
      itemBuilder: (context, index) {
        final tag = _tags[index];
        return Card(
          margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
          child: ListTile(
            leading: CircleAvatar(
              backgroundColor: _getRssiColor(tag.rssi),
              child: Text(
                '${tag.rssi}',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            title: Text(
              tag.tagId,
              style: const TextStyle(
                fontFamily: 'monospace',
                fontWeight: FontWeight.bold,
              ),
            ),
            subtitle: Text(
              '推定距離: ${tag.distance} | ${_formatTimestamp(tag.lastSeen)}',
            ),
            trailing: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '${tag.rssi} dBm',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: _getRssiColor(tag.rssi),
                  ),
                ),
                Text(
                  tag.distance,
                  style: const TextStyle(fontSize: 12),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  String _formatTimestamp(DateTime time) {
    final now = DateTime.now();
    final diff = now.difference(time);
    if (diff.inSeconds < 60) return '${diff.inSeconds}秒前';
    if (diff.inMinutes < 60) return '${diff.inMinutes}分前';
    return '${diff.inHours}時間前';
  }
}

class RFIDTag {
  final String tagId;
  final int rssi;
  final String distance;
  final DateTime lastSeen;

  RFIDTag({
    required this.tagId,
    required this.rssi,
    required this.distance,
    required this.lastSeen,
  });
}
