package com.eli.oneos.ui.nav.tools;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.api.OneOSMemenetUserInfoAPI;
import com.eli.oneos.model.oneos.api.OneOSTokenAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;

import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.TitleBackLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * Created by client1 on 2017/8/27.
 */

public class ShareQRActivity extends BaseActivity {
    private static final String TAG = ShareQRActivity.class.getSimpleName();


    private LoginSession mLoginSession;
    private TitleBackLayout mTitleLayout;
    private ImageView mImageView;
    private String savePath;
    private TextView shareView;
    private String token, mAccount, mPass, ip = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_capture);
        initSystemBarStyle();

        mLoginSession = LoginManage.getInstance().getLoginSession();
        savePath = mLoginSession.getDownloadPath() + "/OneSpace_capture.jpg";
        initView();
    }


    @Override
    public void onResume() {
        super.onResume();
        getToken();
//        getQrInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "----OnDestroy-----");
        File file = new File(savePath);
        if (file.exists()) {
            Log.d(TAG, " File =" + file);
            Boolean result = file.delete();
            Log.d(TAG, "deleteFile " + result);
        } else
            Log.d(TAG, "file is not exist");
    }


    public void initView() {
        mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.scan_share_title);
        mImageView = (ImageView) findViewById(R.id.qrcode_bitmap);

        shareView = (TextView) findViewById(R.id.qr_share);
        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareCapture();
            }
        });

    }

    private void shareCapture() {

//        String imagePath = Environment.getExternalStorageDirectory() + File.separator + "test.jpg";
        //由文件得到uri
//        Uri imageUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", new File(savePath));

        Uri imageUri = Uri.fromFile(new File(savePath));
        Log.d("share", "uri:" + imageUri);  //输出：file:///storage/emulated/0/test.jpg

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(shareIntent);

    }

    private void getQrInfo() {
//        String name = mLoginSession.getUserInfo().getName();
//        String pass = mLoginSession.getUserInfo().getPwd();


        OneOSMemenetUserInfoAPI MemenetUserInfoAPI = new OneOSMemenetUserInfoAPI(mLoginSession);
        MemenetUserInfoAPI.setListener(new OneOSMemenetUserInfoAPI.MemenetUserInfoListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, String memenetAccount, String memenetPassWord, String domain) {
                mAccount = memenetAccount;
                mPass = memenetPassWord;
                ip = domain;
                String qrInfo = "{\"token\": \"" + token + "\", \"ip\": \"" + ip + "\",\"mn\": \"" + mAccount + "\",\"mp\": \"" + mPass + "\"}";
                Log.d(TAG, "getQrInfo==" + qrInfo);
                String qrBase64 = encodeData(qrInfo);
                mImageView.setImageBitmap(createQRCode(qrBase64, 250));
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                Log.d(TAG, "errorMsg=" + errorMsg);
                mAccount = null;
                mPass = null;
                ip = mLoginSession.getIp();
                String qrInfo = "{\"token\": \"" + token + "\", \"ip\": \"" + ip + "\",\"mn\": \"" + mAccount + "\",\"mp\": \"" + mPass + "\"}";
                Log.d(TAG, "getQrInfo==" + qrInfo);
                String qrBase64 = encodeData(qrInfo);
                mImageView.setImageBitmap(createQRCode(qrBase64, 250));
            }
        });
        MemenetUserInfoAPI.getMemenetUserInfo();


    /*
    if(ip.endsWith("cifernet.net")||ip.endsWith("memenet.net"))

    {

        SharedPreferences sp = getSharedPreferences("MemenetInfo", MODE_PRIVATE + MODE_APPEND);
        Map<String, ?> allContent = sp.getAll();
        String domain, password;
        for (final Map.Entry<String, ?> entry : allContent.entrySet()) {
            domain = entry.getValue().toString().split("======")[1];
            password = entry.getValue().toString().split("======")[0];
            if (domain.equals(ip)) {
                mAccount = entry.getKey();
                mPass = password;
            }
        }
    }
*/
        //  {"name": "admin", "p": "123456", "ip": "563027262833186.cifernet.net","mn": "100122676","mp": "ZElU5V1Y0y"}
        String qrInfo = "{\"token\": \"" + token + "\", \"ip\": \"" + ip + "\",\"mn\": \"" + mAccount + "\",\"mp\": \"" + mPass + "\"}";
        Log.d(TAG, "getQrInfo==" + qrInfo);
        String qrBase64 = encodeData(qrInfo);
        Log.d(TAG, "qrBase64==" + qrBase64);
        mImageView.setImageBitmap(createQRCode(qrBase64, 250));

    }


    private void getToken() {
        OneOSTokenAPI tokenAPI = new OneOSTokenAPI(mLoginSession);
        tokenAPI.OnGetTokenListener(new OneOSTokenAPI.OnGetTokenListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, String mtoken) {
                token = mtoken;
                Log.d(TAG, "token == " + token);
                if (token.isEmpty()) {
                    ToastHelper.showToast(R.string.scan_get_failed);
                } else
                    getQrInfo();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                ToastHelper.showToast(R.string.scan_get_failed);
            }
        });
        tokenAPI.getToken();
    }


    private static final String UTF_8 = "UTF-8";

    /**
     * 对给定的字符串进行base64解码操作
     */
    public static String decodeData(String inputData) {
        try {
            if (null == inputData) {
                return null;
            }
            return new String(Base64.decodeBase64(inputData.getBytes(UTF_8)), UTF_8);
        } catch (UnsupportedEncodingException e) {

        }

        return null;
    }

    /**
     * 对给定的字符串进行base64加密操作
     */
    public static String encodeData(String inputData) {
        try {
            if (null == inputData) {
                return null;
            }
            return new String(Base64.encodeBase64(inputData.getBytes(UTF_8)), UTF_8);
        } catch (UnsupportedEncodingException e) {

        }

        return null;
    }


    private Bitmap createQRCode(String str, int widthAndHeight) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
            return bitMatrix2Bitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }


    private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] rawData = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = Color.WHITE;
                if (matrix.get(i, j)) {
                    color = Color.BLACK;
                }
                rawData[i + (j * w)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);

        saveBitmap(this, bitmap);
        return bitmap;
    }


    public String saveBitmap(Context context, Bitmap mBitmap) {
        File filePic;
        savePath = mLoginSession.getDownloadPath() + "/OneSpace_capture.jpg";
        Log.d(TAG, "savepath=" + savePath);
        try {
            filePic = new File(savePath);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        System.out.println(filePic.getAbsolutePath());
        return filePic.getAbsolutePath();
    }


}