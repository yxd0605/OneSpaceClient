//package www.glinkwin.com.glink.ssudp;
//
//
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//import www.onespace.com.ones.ui.FilePathManager;
//
//
///***************************************************************
// * Copyright (C) 2015 盛耀微电子科技有限公司
// * www.glinkwin.com
// * Author :wxj@glinkwin.com
// * Version:1.0
// * Date:2015-11-10
// * History:
// ***************************************************************/
//
//public class DataBase {
//    private String pushdbname;
//    private String pathname;
//    private SQLiteDatabase db;
//    private SQLiteDatabase msgdb;
//    public List<Map<String, Object>> mDevices;
//    static private DataBase singleton = null;
//
//    private DataBase() {
//        init();
//    }
//
//    public static DataBase getInstance() {
//
//        if (singleton == null) {
//            singleton = new DataBase();
//        }
//
//        return singleton;
//    }
//
//
//    private boolean openmsg() {
//        msgdb = SQLiteDatabase.openOrCreateDatabase(pushdbname, null);
//        return true;
//    }
//
//    private boolean closemsg() {
//        msgdb.close();
//        return true;
//    }
//
//    private boolean open() {
//        db = SQLiteDatabase.openOrCreateDatabase(pathname, null);
//        return true;
//    }
//
//    private boolean close() {
//        db.close();
//        return true;
//    }
//
//    private int execSQL(String str) {
//        db.execSQL(str);
//        return 0;
//    }
//
//    public int deleteDevice(String strcid) {
//        synchronized (this) {
//            open();
//            db.beginTransaction();
//            try {
//                execSQL(String.format("delete from device where cid like '%s'", strcid));
//                db.setTransactionSuccessful();
//            } finally {
//                db.endTransaction();
//            }
//            close();
//        }
//        return 0;
//    }
//
//    public int updatePushMsg(String strcid, String date, String strupdate) {
//        String strsql = "UPDATE pushmsg SET " + strupdate + " WHERE cid like '%s' and date=%s";
//        strsql = String.format(strsql, strcid, date);
//
//        synchronized (this) {
//            openmsg();
//            msgdb.beginTransaction();
//            try {
//                msgdb.execSQL(strsql);
//                msgdb.setTransactionSuccessful();
//            } finally {
//                msgdb.endTransaction();
//            }
//            closemsg();
//        }
//        return 0;
//    }
//
//
//    public int updateDevice(String strcid, String strupdate) {
//        /*
//        string cid='12121212',  int    fps=15
//        */
//        String strsql = "UPDATE device SET " + strupdate + " WHERE cid like '%s'";
//        strsql = String.format(strsql, strcid);
//
//        synchronized (this) {
//            open();
//            db.beginTransaction();
//            try {
//                execSQL(strsql);
//                db.setTransactionSuccessful();
//            } finally {
//                db.endTransaction();
//            }
//            close();
//        }
//        return 0;
//    }
//
//    public String addDevice(String strcid, String name, String pwd) {
//        String struuid = createConnectUUID();
//        synchronized (this) {
//            insert(strcid, name, pwd, 0, 0, 38, 26, 25, 0, 0, 0, 0, "", struuid);
//        }
//        return struuid;
//    }
//
//
//    public List<Map<String, Object>> getPushMsgList(String cid) {
//
//        List<Map<String, Object>> msglst = new ArrayList<Map<String, Object>>();
//
//        synchronized (this) {
//
//            openmsg();
//
//            String[] columnsname = {"cid", "date", "filedate", "read", "isdown", "isdel", "type", "msgText", "imageName", "videoName"};
//            String[] columns = {"*"};
//
//            Cursor cursor = msgdb.query("pushmsg", columns, "cid like '" + cid + "' and isdel=0", null, null, null, null);
//
//            while (cursor.moveToNext()) {
//
//                Map<String, Object> itmMap = new HashMap<String, Object>();
//                for (int i = 0; i < columnsname.length; i++) {
//                    itmMap.put(columnsname[i], cursor.getString(cursor.getColumnIndex(columnsname[i])));
//                }
//                msglst.add(itmMap);
//            }
//            closemsg();
//        }
//
//        return msglst;
//    }
//
//
//    public Map<String, Object> getDevicesItem(String cid) {
//
//        Map<String, Object> itmMap = new HashMap<String, Object>();
//        open();
//        String[] columnsname = {"cid", "name", "pwd", "conuuid", "msgid"};
//        String[] columns = {"*"};
//
//        Cursor cursor = db.query("device", columns, "cid like '" + cid + "'", null, null, null, null);
//        cursor.moveToNext();
//        for (int i = 0; i < columnsname.length; i++) {
//            itmMap.put(columnsname[i], cursor.getString(cursor.getColumnIndex(columnsname[i])));
//        }
//        close();
//        return itmMap;
//    }
//
//
//    public List<Map<String, Object>> getDevices() {
//
//        List<Map<String, Object>> devices = new ArrayList<Map<String, Object>>();
//
//        synchronized (this) {
//            open();
//            String[] columnsname = {"cid", "name", "pwd", "conuuid", "msgid"};
//            String[] columns = {"*"};
//
//            Cursor cursor = db.query("device", columns, null, null, null, null, null);
//            while (cursor.moveToNext()) {
//                Map<String, Object> itmMap = new HashMap<String, Object>();
//                for (int i = 0; i < columnsname.length; i++) {
//                    itmMap.put(columnsname[i], cursor.getString(cursor.getColumnIndex(columnsname[i])));
//                }
//
//                devices.add(itmMap);
//            }
//            close();
//        }
//        mDevices = devices;
//        return devices;
//    }
//
//    private String createConnectUUID() {
//        byte[] id = new byte[16];
//        (new Random()).nextBytes(id);
//        return String.format("%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
//                id[0], id[1], id[2], id[3], id[4], id[5], id[6], id[7],
//                id[8], id[9], id[10], id[11], id[12], id[13], id[14], id[15]);
//    }
//
//    private void init() {
//        String devicedb = "CREATE TABLE IF NOT EXISTS device " +
//                "(cid TEXT PRIMARY KEY," +
//                "name TEXT," +
//                "pwd TEXT," +
//                "ip INTEGER," +
//                "port INTEGER," +
//                "maxQp INTEGER," +
//                "minQp INTEGER," +
//                "fps INTEGER," +
//                "quality INTEGER," +
//                "routate INTEGER," +
//                "msgid INTEGER," +
//                "devtype INTEGER," +
//                "bkimage TEXT," +
//                "conuuid TEXT)";
//
//        pathname = FilePathManager.getInstance().deviceDatabaseName;
//
//        db = SQLiteDatabase.openOrCreateDatabase(pathname, null);
//
//        db.beginTransaction();
//        try {
//            db.execSQL(devicedb);
//            db.setTransactionSuccessful();
//        } finally {
//            db.endTransaction();
//        }
//        db.close();
//
//
//        String pushmsgdb = "CREATE TABLE IF NOT EXISTS pushmsg " +
//                "(date INTEGER PRIMARY KEY," +
//                "cid TEXT," +
//                "filedate INTEGER," +
//                "read INTEGER," +
//                "isdown INTEGER," +
//                "isdel INTEGER," +
//                "type INTEGER," +
//                "vaddr INTEGER," +
//                "msgText TEXT," +
//                "imageName TEXT," +
//                "videoName TEXT)";
//
//        pushdbname = FilePathManager.getInstance().pushMsgDatabaseName;
//        Log.e("pushdbname", pushdbname);
//        msgdb = SQLiteDatabase.openOrCreateDatabase(pushdbname, null);
//        msgdb.beginTransaction();
//        try {
//            msgdb.execSQL(pushmsgdb);
//            msgdb.setTransactionSuccessful();
//        } finally {
//            msgdb.endTransaction();
//        }
//        msgdb.close();
//
//    }
//
//    private int insert(
//            String cid,
//            String name,
//            String pwd,
//            int ip,
//            int port,
//            int maxQp,
//            int minQp,
//            int fps,
//            int quality,
//            int routate,
//            int msgid,
//            int devtype,
//            String bkimage,
//            String conuuid) {
//
//        String strformat = "INSERT INTO device (" +
//                "'cid'," +
//                "'name'," +
//                "'pwd'," +
//                "'ip'," +
//                "'port'," +
//                "'maxQp'," +
//                "'minQp'," +
//                "'fps'," +
//                "'quality'," +
//                "'routate'," +
//                "'msgid'," +
//                "'devtype'," +
//                "'bkimage'," +
//                "'conuuid') VALUES ('%s','%s','%s','%d','%d','%d','%d','%d','%d','%d','%d','%d','%s','%s')";
//
//        String strSql = String.format(strformat, cid, name, pwd, ip, port, maxQp, minQp, fps, quality, routate, msgid, devtype, bkimage, conuuid);
//
//        open();
//
//        db.beginTransaction();
//        try {
//            execSQL(strSql);
//            db.setTransactionSuccessful();
//        } finally {
//            db.endTransaction();
//        }
//        close();
//        return 0;
//    }
//
//    public int pushMsgInsert(
//            String cid,
//            int filedate,
//            int read,
//            int isdown,
//            int isdel,
//            int type,
//            int date,
//            int vaddr,
//            String msgText,
//            String imageName,
//            String videoName) {
//        /**
//         String strformat =
//         "INSERT INTO pushmsg ("+
//         "'date',"+
//         "'cid',"+
//         "'filedate',"+
//         "'read',"+
//         "'isdown',"+
//         "'isdel',"+
//         "'type',"+
//         "'vaddr',"+
//         "'msgText',"+
//         "'imageName',"+
//         "'videoName') VALUES ("+date+",'"+cid+"',"+filedate+","+read+","+isdown+","+isdel
//         +","+type+","+vaddr+",'"+msgText+"','"+imageName+"','"+videoName+"') " +
//         "where not exists (select cid,date from pushmsg where cid like '"+cid+"' and date="+date+")";
//         **/
//
//        String strformat =
//                "INSERT INTO pushmsg (" +
//                        "date,cid,filedate,read,isdown,isdel,type,vaddr,msgText,imageName,videoName) " +
//                        "select " + date + ",'" + cid + "'," + filedate + "," + read + "," + isdown + "," + isdel
//                        + "," + type + "," + vaddr + ",'" + msgText + "','" + imageName + "','" + videoName + "' " +
//                        "where not exists (select cid,date from pushmsg where cid like '" + cid + "' and date=" + date + ")";
//
//
//        //Log.i("sql",strformat);
//        //String strSql = String.format(strformat,date,cid,filedate,read,isdown,isdel,type,vaddr,msgText,imageName,videoName);
//        openmsg();
//        msgdb.execSQL(strformat);
//        closemsg();
//        return 0;
//    }
//
//    //501000000f000000000000001dd0035700000000
//    //501000000f00000000000000bde9035700000000
//    //5010000029000000415649200ed30357ee020000
//
//}
//
//
//
