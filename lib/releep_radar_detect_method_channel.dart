import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'releep_radar_detect_platform_interface.dart';

/// An implementation of [ReleepRadarDetectPlatform] that uses method channels.
class MethodChannelReleepRadarDetect extends ReleepRadarDetectPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('releep_radar_detect');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<void> connect({
    required String wifiSsid,
    required String wifiPassword,
    required String radarTcpServiceUrl,
    required int deviceType,
  }) async {
    try {
      await methodChannel.invokeMethod('connect', {
        'wifiSsid': wifiSsid,
        'wifiPassword': wifiPassword,
        'radarTcpServiceUrl': radarTcpServiceUrl,
        'deviceType': deviceType,
      });
    } on PlatformException catch (e) {
      print("Failed to connect: ${e.message}");
    }
  }
}
