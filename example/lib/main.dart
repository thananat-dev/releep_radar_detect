import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:releep_radar_detect/releep_radar_detect.dart';

void main() {
  runApp(MaterialApp(home: const MyApp()));
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final TextEditingController _ssidController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  static const MethodChannel _channel = MethodChannel('releep_radar_detect');

  Future<void> _connect() async {
    try {
      final String wifiSsid = _ssidController.text;
      final String wifiPassword = _passwordController.text;

      // ตัวอย่างค่าส่ง (เติมให้เอง)
      final String radarTcpServiceUrl = "34.223.29.230,8884";
      final int deviceType = 0; // สมมติเลือก deviceType = 0 (ASSURE)

      int status = await _channel.invokeMethod('connect', {
        'wifiSsid': wifiSsid,
        'wifiPassword': wifiPassword,
        'radarTcpServiceUrl': radarTcpServiceUrl,
        'deviceType': deviceType,
      });
      var snackBar = SnackBar(
          content: Text(
        "sdsfdsfsdfs",
      ));

// Find the ScaffoldMessenger in the widget tree
// and use it to show a SnackBar.
      ScaffoldMessenger.of(context).showSnackBar(snackBar);
    } on PlatformException catch (e) {
      print('Connect failed: ${e.message}');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: _ssidController,
              decoration: const InputDecoration(
                labelText: 'WiFi SSID',
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _passwordController,
              decoration: const InputDecoration(
                labelText: 'WiFi Password',
              ),
              // obscureText: true,
            ),
            const SizedBox(height: 32),
            ElevatedButton(
              onPressed: _connect,
              child: const Text('Connect'),
            ),
          ],
        ),
      ),
    );
  }
}
