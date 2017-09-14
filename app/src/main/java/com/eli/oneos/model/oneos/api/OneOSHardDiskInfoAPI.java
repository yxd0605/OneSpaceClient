package com.eli.oneos.model.oneos.api;

import android.util.Log;

import com.eli.oneos.R;
import com.eli.oneos.constant.HttpErrorNo;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.http.OnHttpListener;
import com.eli.oneos.model.http.RequestBody;
import com.eli.oneos.model.oneos.OneOSHardDisk;
import com.eli.oneos.model.oneos.user.LoginSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * OneSpace OS Get Hard Disk information
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/03/29.
 */
public class OneOSHardDiskInfoAPI extends OneOSBaseAPI {
    private static final String TAG = OneOSHardDiskInfoAPI.class.getSimpleName();

    private OnHDInfoListener listener;
    private String username = null;
    private OneOSHardDisk hardDisk1, hardDisk2;

    public OneOSHardDiskInfoAPI(LoginSession loginSession) {
        super(loginSession);
        username = loginSession.getUserInfo().getName();
    }

    public void setOnHDInfoListener(OnHDInfoListener listener) {
        this.listener = listener;
    }

    public void query(OneOSHardDisk hd1, OneOSHardDisk hd2) {
        if (null == hd1) {
            hd1 = new OneOSHardDisk();
        }
        if (null == hd2) {
            hd2 = new OneOSHardDisk();
        }
        this.hardDisk1 = hd1;
        this.hardDisk2 = hd2;
        url = genOneOSAPIUrl(OneOSAPIs.SYSTEM_SYS);
        Map<String, Object> params = new HashMap<>();

        httpUtils.postJson(url,new RequestBody("hdinfo", session, params), new OnHttpListener<String>() {
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                // super.onFailure(t, errorNo, strMsg);
                Log.e(TAG, "Response Data: " + errorNo + " : " + strMsg);
                if (listener != null) {
                    listener.onFailure(url, errorNo, strMsg);
                }
            }

            @Override
            public void onSuccess(String result) {
                // super.onSuccess(result);
                Log.d(TAG, "Response Data:" + result);
                if (listener != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        boolean ret = json.getBoolean("result");
                        if (ret) {
//                            {"result":true, "info":{"mode":"basic","count":2,"errno":0,"dev1":"/dev/sda","dev2":"/dev/sdb"}
//                                ,"hds":[
//                                {
//                                    "name":"/dev/sda",
//                                        "info":{
//                                    "Device Model":"    TOSHIBA DT01ACA200",
//                                            "Serial Number":"   54ODYV8GS",
//                                            "LU WWN Device Id":"5 000039 ffac5e47f",
//                                            "Firmware Version":"MX4OABB0",
//                                            "User Capacity":"   2,000,398,934,016 bytes [2.00 TB]",
//                                            "Sector Sizes":"    512 bytes logical, 4096 bytes physical",
//                                            "ATA Version is":"  8",
//                                            "ATA Standard is":" ATA-8-ACS revision 4",
//                                            "SMART support is":"Available - device has SMART capability.",
//                                            "SMART support is":"Enabled",
//                                            "slot":0,
//                                            "end":"end"
//                                }
//                                }
//                                , ...
//                                ]
//                            }
                            JSONObject infoJson = json.getJSONObject("info");
                            String mode = infoJson.getString("mode");
                            int count = infoJson.getInt("count");
                            if (count > 0) {
                                String hd1Name = hardDisk1.getName();
                                String hd2Name = hardDisk2.getName();

                                JSONArray jArray = json.getJSONArray("hds");
                                for (int i = 0; i < jArray.length(); i++) {
                                    json = jArray.getJSONObject(i);
                                    String name = json.getString("name");
                                    json = json.getJSONObject("info");
                                    String model = json.getString("Device Model").trim();
                                    String serial = json.getString("Serial Number").trim();
                                    String capacity = json.getString("User Capacity").trim();

                                    if (null != hd1Name && hd1Name.equals(name)) {
                                        hardDisk1.setModel(model);
                                        hardDisk1.setSerial(serial);
                                        hardDisk1.setCapacity(capacity);
                                    } else if (null != hd2Name && hd2Name.equals(name)) {
                                        hardDisk2.setModel(model);
                                        hardDisk2.setSerial(serial);
                                        hardDisk2.setCapacity(capacity);
                                    } else {
                                        hardDisk1.setName(name);
                                        if (i == 0) {
                                            hardDisk1.setModel(model);
                                            hardDisk1.setSerial(serial);
                                            hardDisk1.setCapacity(capacity);
                                        } else {
                                            hardDisk2.setModel(model);
                                            hardDisk2.setSerial(serial);
                                            hardDisk2.setCapacity(capacity);
                                        }
                                    }
                                }
                            } else {
                                hardDisk1 = null;
                                hardDisk2 = null;
                            }

                            listener.onSuccess(url, mode, hardDisk1, hardDisk2);
                        } else {
                            // {"errno":-1,"msg":"list error","result":false}
                            int errorNo = json.getInt("errno");
                            String msg = json.has("msg") ? json.getString("msg") : null;
                            listener.onFailure(url, errorNo, msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure(url, HttpErrorNo.ERR_JSON_EXCEPTION, context.getResources().getString(R.string.error_json_exception));
                    }
                }
            }
        });

        if (listener != null) {
            listener.onStart(url);
        }
    }

    public interface OnHDInfoListener {
        void onStart(String url);

        void onSuccess(String url, String model, OneOSHardDisk hd1, OneOSHardDisk hd2);

        void onFailure(String url, int errorNo, String errorMsg);
    }
}
