//package www.glinkwin.com.glink.ssudp;
//
//import android.content.Context;
//import android.os.Handler;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by Administrator on 2016/3/12.
// */
//public class DeviceList {
//
//    public List<Map<String, Object>> devices = new ArrayList<Map<String, Object>>();
//
//    static private DeviceList singleton = null;
//
//    private DeviceList() {
//    }
//
//    public static DeviceList getInstance() {
//        if (singleton == null) {
//            singleton = new DeviceList();
//        }
//        return singleton;
//    }
//
//
//    public void deviceDelete(int index) {
//
//        SSUDPRequest client = SSUDPManager.clientArrayList.get(index);
//        if (client == null)
//            return;
//
//        DataBase.getInstance().deleteDevice(client.clientCfg.strcid);
//
////        FilePathManager.getInstance().deleteUsrFolder(client.clientCfg.strcid);
//
//        SSUDPManager.getInstance().deleteClient(SSUDPManager.clientArrayList.get(index));
//
//        devices.remove(index);
//    }
//
//    public void bindClient(Context ctx, Handler handle, int index) {
//
//        synchronized (this) {
//
//            Map<String, Object> itmMap = devices.get(index);
//
//            String strcid = (String) itmMap.get("cid");
//            String strpwd = (String) itmMap.get("pwd");
//            String struuid = (String) itmMap.get("conuuid");
//
//            int msgid = Integer.parseInt(itmMap.get("msgid").toString());
//
//            itmMap.put("remotemsgid", "0");
//            itmMap.put("connect_sts", "0");
//            itmMap.put("ble_connect_sts", "0");
//
//            for (int i = 0; i < SSUDPManager.clientArrayList.size(); i++) {
//                if (strcid.equals(SSUDPManager.clientArrayList.get(i).clientCfg.strcid)) {
//                    return;
//                }
//            }
//
//            ClientConfig config = new ClientConfig(strcid, strpwd, struuid);
//            SSUDPRequest client = new SSUDPRequest(config, ctx);
//
//            client.msgid = msgid;
//            client.remoteMsgid = msgid;
//            client.registerMsgHandle(handle);
//
//            SSUDPManager.getInstance().addClient(client);
//
//        }
//    }
//
//    /**
//     * 添加一个新的设备
//     **/
//    public void deviceAdd(Context ctx,
//                          Handler handle,
//                          String deviceName,
//                          String strcid,
//                          String strpwd) {
//        synchronized (this) {
//
//            /**初始化目录**/
////            FilePathManager.getInstance().initUsrPath(strcid);
//
//            /**保存到数据库**/
//            String struuid = DataBase.getInstance().addDevice(strcid, deviceName, strpwd);
//
//
//            /**读出纪录到item**/
//            Map<String, Object> itmMap = DataBase.getInstance().getDevicesItem(strcid);
//
//            /**初始化额外数据**/
//            itmMap.put("remotemsgid", "0");
//            itmMap.put("connect_sts", "0");
//            itmMap.put("ble_connect_sts", "0");
//
//            /**开始运行client实例**/
//            ClientConfig config = new ClientConfig(strcid, strpwd, struuid);
//            SSUDPRequest client = new SSUDPRequest(config, ctx);
//
//            client.msgid = 0;
//            client.remoteMsgid = 0;
//            client.strname = deviceName;
//
//            client.registerMsgHandle(handle);
//
//            SSUDPManager.getInstance().addClient(client);
//
//            devices.add(itmMap);
//        }
//    }
//
//
//}
