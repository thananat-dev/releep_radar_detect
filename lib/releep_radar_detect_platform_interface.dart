import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'releep_radar_detect_method_channel.dart';

abstract class ReleepRadarDetectPlatform extends PlatformInterface {
  /// Constructs a ReleepRadarDetectPlatform.
  ReleepRadarDetectPlatform() : super(token: _token);

  static final Object _token = Object();

  static ReleepRadarDetectPlatform _instance = MethodChannelReleepRadarDetect();

  /// The default instance of [ReleepRadarDetectPlatform] to use.
  ///
  /// Defaults to [MethodChannelReleepRadarDetect].
  static ReleepRadarDetectPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ReleepRadarDetectPlatform] when
  /// they register themselves.
  static set instance(ReleepRadarDetectPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> connect({
    required String wifiSsid,
    required String wifiPassword,
    required String radarTcpServiceUrl,
    required int deviceType,
  }) {
    throw UnimplementedError('connect() has not been implemented.');
  }
}
