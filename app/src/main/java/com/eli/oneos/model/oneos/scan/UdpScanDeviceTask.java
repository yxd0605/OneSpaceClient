package com.eli.oneos.model.oneos.scan;

import android.os.AsyncTask;
import android.util.Log;

import com.eli.oneos.utils.Utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class UdpScanDeviceTask extends AsyncTask<Void, String, String[]> {
    private static final String TAG = UdpScanDeviceTask.class.getSimpleName();

    private static final int CLIENT_PORT = 6000;
    private static final int SERVER_PORT = 7979;
    private static final int TIME_SCAN = 2000;
    private static final String DATA = "scan";
    private static final int MAX_LENGTH = 40;

    /**
     * Device Map<Mac, IP>
     */
    private Map<String, String> mDeviceMap = new HashMap<String, String>();
    private String dataStr;
    private boolean isInterrupt = false;
    private OnScanDeviceListener mListener;

    public UdpScanDeviceTask(OnScanDeviceListener mListener) {
        this.dataStr = DATA;
        this.mListener = mListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null) {
            mListener.onScanStart();
        }
        mDeviceMap.clear();
        this.isInterrupt = false;
    }

    @Override
    protected String[] doInBackground(Void... params) {
        DatagramPacket sendPacket = null;
        DatagramSocket udpSocket = null;

        try {
            byte[] sendBuffer = new byte[MAX_LENGTH];
            // udpSocket = new DatagramSocket(CLIENT_PORT);
            udpSocket = new DatagramSocket(null);
            udpSocket.setReuseAddress(true);
            udpSocket.bind(new InetSocketAddress(CLIENT_PORT));

            sendPacket = new DatagramPacket(sendBuffer, MAX_LENGTH);
            byte[] data = dataStr.getBytes();
            sendPacket.setData(data);
            sendPacket.setLength(data.length);
            sendPacket.setPort(SERVER_PORT);

            InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
            sendPacket.setAddress(broadcastAddr);
            udpSocket.setBroadcast(true);
            udpSocket.send(sendPacket);
            udpSocket.setSoTimeout(2000);

            byte[] receiveBuffer = new byte[MAX_LENGTH];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, MAX_LENGTH);

            long start = System.currentTimeMillis();
            while (!isInterrupt && (System.currentTimeMillis() - start < TIME_SCAN)) {
                udpSocket.receive(receivePacket);
                final String ip = receivePacket.getAddress().getHostAddress().toString();
                if (Utils.isAvaliableIp(ip)) { // Mac: 84:5D:D7:02:00:0D ; Len: 17
                    String deviceMac = new String(receivePacket.getData()).substring(0, 17).toUpperCase();
                    Log.d(TAG, "Scanning: IP= " + ip + "  Mac:" + deviceMac);
                    if (!mDeviceMap.containsKey(deviceMac)) {
                        publishProgress(deviceMac, ip);
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (udpSocket != null) {
                udpSocket.close();
                udpSocket = null;
            }
        }
        // Logged.e(TAG, "----doInBackground over----");

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        // if (mDeviceMap.size() <= 1) {
        // mIpText.setText(values[1]);
        // }
        Log.d(TAG, "Store: IP= " + values[1] + "  Mac:" + values[0]);
        mDeviceMap.put(values[0], values[1]);
        // checkDeviceState(values[0], values[1]);
        if (mListener != null) {
            mListener.onScanning(values[0], values[1]);
        }
    }

    @Override
    protected void onPostExecute(String[] result) {
        Log.d(TAG, "---on Post Execute---");
        super.onPostExecute(result);
        // checkScanResult();
        if (mListener != null) {
            mListener.onScanOver(mDeviceMap, isInterrupt, true);
        }
    }

    public void stopScan() {
        this.mListener = null;
        this.isInterrupt = true;
    }

}
