package com.eli.oneos.model.oneos.scan;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Map;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/11.
 */
public class ScanDeviceManager {
    private Context context;
    private UdpScanDeviceTask mUdpScanTask;
    private TcpScanDeviceTask mTcpScanTask;
    private OnScanDeviceListener mCallback;
    private OnScanDeviceListener mOnScanDeviceListener = new OnScanDeviceListener() {
        @Override
        public void onScanStart() {
            if (null != mCallback) {
                mCallback.onScanStart();
            }
        }

        @Override
        public void onScanning(String mac, String ip) {
            if (null != mCallback) {
                mCallback.onScanning(mac, ip);
            }
        }

        @Override
        public void onScanOver(Map<String, String> mDeviceMap, boolean isInterrupt, boolean isUdp) {
//            if (isUdp && mDeviceMap.size() == 0) {
//                startTcpScanDevice();
//                return;
//            }

            if (null != mCallback) {
                mCallback.onScanOver(mDeviceMap, isInterrupt, isUdp);
            }
        }
    };

    public ScanDeviceManager(Context context, OnScanDeviceListener mCallback) {
        this.context = context;
        this.mCallback = mCallback;
    }

    /**
     * start scanning LAN OneSpace
     */
    public void start() {
        startUdpScanDevice();
    }

    /**
     * stop scanning LAN OneSpace
     */
    public void stop() {
        stopUdpScanDevice();
        stopTcpScanDevice();
    }

    /**
     * start send UDP broadcast and scanning response
     */
    private void startUdpScanDevice() {
        stopUdpScanDevice();
        mUdpScanTask = new UdpScanDeviceTask(mOnScanDeviceListener);
        mUdpScanTask.execute();
    }

    /**
     * start send TCP broadcast and scanning response
     */
    private void startTcpScanDevice() {
        stopTcpScanDevice();
        mTcpScanTask = new TcpScanDeviceTask(mOnScanDeviceListener, context);
        mTcpScanTask.execute();
    }

    /**
     * stop send UDP broadcast
     */
    private void stopUdpScanDevice() {
        if (mUdpScanTask != null && mUdpScanTask.getStatus() == AsyncTask.Status.RUNNING) {
            mUdpScanTask.stopScan();
        }
    }

    /**
     * stop send TCP broadcast
     */
    private void stopTcpScanDevice() {
        if (mTcpScanTask != null && mTcpScanTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTcpScanTask.stopScan();
        }
    }
}
