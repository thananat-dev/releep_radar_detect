
import 'releep_radar_detect_platform_interface.dart';

class ReleepRadarDetect {
  Future<String?> getPlatformVersion() {
    return ReleepRadarDetectPlatform.instance.getPlatformVersion();
  }



  Future<void> connect({
    required String wifiSsid,
    required String wifiPassword,
    required String radarTcpServiceUrl,
    required int deviceType,
  }) {
    return ReleepRadarDetectPlatform.instance.connect(
      wifiSsid: wifiSsid,
      wifiPassword: wifiPassword,
      radarTcpServiceUrl: radarTcpServiceUrl,
      deviceType: deviceType,
    );
  }
}
