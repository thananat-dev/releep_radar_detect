import 'package:flutter_test/flutter_test.dart';
import 'package:releep_radar_detect/releep_radar_detect.dart';
import 'package:releep_radar_detect/releep_radar_detect_platform_interface.dart';
import 'package:releep_radar_detect/releep_radar_detect_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockReleepRadarDetectPlatform
    with MockPlatformInterfaceMixin
    implements ReleepRadarDetectPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<void> connect({required String wifiSsid, required String wifiPassword, required String radarTcpServiceUrl, required int deviceType}) {
    // TODO: implement connect
    throw UnimplementedError();
  }
}

void main() {
  final ReleepRadarDetectPlatform initialPlatform = ReleepRadarDetectPlatform.instance;

  test('$MethodChannelReleepRadarDetect is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelReleepRadarDetect>());
  });

  test('getPlatformVersion', () async {
    ReleepRadarDetect releepRadarDetectPlugin = ReleepRadarDetect();
    MockReleepRadarDetectPlatform fakePlatform = MockReleepRadarDetectPlatform();
    ReleepRadarDetectPlatform.instance = fakePlatform;

    expect(await releepRadarDetectPlugin.getPlatformVersion(), '42');
  });

  // เพิ่มการทดสอบ connect
  test('connect', () async {
    ReleepRadarDetect releepRadarDetectPlugin = ReleepRadarDetect();
    MockReleepRadarDetectPlatform fakePlatform = MockReleepRadarDetectPlatform();
    ReleepRadarDetectPlatform.instance = fakePlatform;

    await releepRadarDetectPlugin.connect(
      wifiSsid: "Test_SSID",
      wifiPassword: "password123",
      radarTcpServiceUrl: "http://test.url",
      deviceType: 1,
    );
    // ตรวจสอบว่า connect ถูกเรียกแล้วหรือไม่ ถ้าจำเป็นต้องทดสอบเพิ่มเติม
    // เช่นใช้ mock หรือ spy เพื่อตรวจสอบการเรียก
  });
}
