package com.eli.oneos.model.oneos.scan;

import java.util.Map;

public interface OnScanDeviceListener {
	void onScanStart();

	void onScanning(String mac, String ip);

	void onScanOver(Map<String, String> mDeviceMap, boolean isInterrupt, boolean isUdp);
}
