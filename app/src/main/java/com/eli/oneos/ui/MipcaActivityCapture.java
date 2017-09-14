package com.eli.oneos.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.db.greendao.UserSettings;
import com.eli.oneos.model.oneos.OneOSInfo;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.receiver.NetworkStateManager;
import com.eli.oneos.widget.TitleBackLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
;

import net.cifernet.cmapi.CMAPI;
import net.cifernet.cmapi.protocal.ResultListener;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

import zxing.camera.CameraManager;
import zxing.decoding.CaptureActivityHandler;
import zxing.decoding.InactivityTimer;
import zxing.decoding.RGBLuminanceSource;
import zxing.decoding.Utils;
import zxing.view.ViewfinderView;


/**
 * Initial the camera
 */
public class MipcaActivityCapture extends BaseActivity implements Callback {

    private static final int CAMERA_OK = 111;
    private static final int REQUEST_CODE = 555;
    private String photo_path;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private TextView picQrView;
    private Bitmap scanBitmap;
    private static final int BUILD_VPN_REQUEST_CODE = 909;
    private static final long VIBRATE_DURATION = 200L;
    private String token = null;
    private String domain = null;

    private static final String UTF_8 = "UTF-8";

    public static final String TAG = MipcaActivityCapture.class.getSimpleName();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        initSystemBarStyle();

        Log.d(TAG, "-----------MipcaActivityCapture onCreate-----------");
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

        NetworkStateManager.getInstance().addNetworkStateChangedListener(mNetworkListener);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_qr);

        picQrView = (TextView) findViewById(R.id.qr_pic);
        picQrView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent innerIntent = new Intent(); // "android.intent.action.GET_CONTENT"
                if (Build.VERSION.SDK_INT < 19) {
                    innerIntent.setAction(Intent.ACTION_GET_CONTENT);
                } else {
                    innerIntent.setAction(Intent.ACTION_PICK);
                }

                innerIntent.setType("image/*");

                Intent wrapperIntent = Intent.createChooser(innerIntent, getResources().getString(R.string.scan_selected_path));

                MipcaActivityCapture.this
                        .startActivityForResult(wrapperIntent, REQUEST_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_OK:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //这里已经获取到了摄像头的权限，想干嘛干嘛了可以

                } else {
                    //这里是拒绝给APP摄像头权限，给个提示什么的说明一下都可以。
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);

        //系统版本在6.0之下，不需要动态获取权限。
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(MipcaActivityCapture.this,
                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //先判断有没有权限 ，没有就在这里进行权限的申请
                ActivityCompat.requestPermissions(MipcaActivityCapture.this,
                        new String[]{android.Manifest.permission.CAMERA}, CAMERA_OK);

            }
        }

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        inactivityTimer.shutdown();
        NetworkStateManager.getInstance().removeNetworkStateChangedListener(mNetworkListener);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    if (data != null) {
                        if (data.getDataString().contains("content")) {
                            photo_path = getRealPathFromURI(data.getData());
                        } else {
                            photo_path = data.getDataString().replace("file://", "");
                        }
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Result result = scanningImage(photo_path);
                            if (result == null) {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.scan_failed), 0).show();
                                Looper.loop();
                            } else {
                                // 数据返回
                                String results = decodeData(result.getText());
                                Log.d("123result", results);
                                String resultString = recode(results.toString());

                                if (resultString.indexOf("token") != -1 && resultString.indexOf("ip") != -1) {
                                    Log.d(TAG, "resultIntent = " + resultString);
                                    try {
                                        JSONObject json = new JSONObject(resultString);
                                        domain = json.getString("ip");
                                        token = json.getString("token");

                                        if (domain.endsWith("cifernet.net") || domain.endsWith("memenet.net")) {
                                            final String mAccount = json.getString("mn");
                                            final String mPass = json.getString("mp");
                                            new Thread() {
                                                @Override
                                                public void run() {
                                                    CMAPI.getInstance().login(MipcaActivityCapture.this, mAccount, mPass, BUILD_VPN_REQUEST_CODE, new ResultListener() {
                                                        @Override
                                                        public void onError(int i) {
                                                            Log.d(TAG, "onError i========================" + i);
                                                        }
                                                    });

                                                }
                                            }.start();
                                        } else {
                                            Log.d(TAG, "token===" + token);
                                            Log.d(TAG, "domain===" + domain);
                                            gotoMainActivity();
                                        }


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    Looper.prepare();
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.scan_failed), 0).show();
                                    Looper.loop();
                                }
                            }
                        }
                    }).start();
                    break;
            }

        }

    }


    private NetworkStateManager.OnNetworkStateChangedListener mNetworkListener = new NetworkStateManager.OnNetworkStateChangedListener() {
        @Override
        public void onChanged(boolean isAvailable, boolean isWifiAvailable) {

        }

        @Override
        public void onSSUDPChanged(boolean isConnect) {
        }

        @Override
        public void onStatusConnection(int statusCode) {
            Log.d(TAG, "statusCode=" + statusCode);
            if (statusCode == NetworkStateManager.STATUS_CODE_ESTABLISHED) {
                Log.d(TAG, "onEstablished====");
                gotoMainActivity();
                dismissLoading();

            } else if (statusCode == NetworkStateManager.STATUS_CODE_DISCONNECTED) {
                Log.d(TAG, "onDisconnected====");
                String activityName = getRunningActivityName();
                Log.d(TAG, "activityName == " + activityName);
                if (activityName.indexOf("MipcaActicityCapture") != -1) {
                    showLoading(R.string.failed_login);
                }
            } else {
                Log.d(TAG, "onConecting====");
                viewfinderView.setVisibility(View.INVISIBLE);
                CameraManager.get().stopPreview();
                hasSurface = false;
            }
        }

    };


    public String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }


    // TODO: 解析部分图片
    protected Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        // DecodeHintType 和EncodeHintType
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小

        int sampleSize = (int) (options.outHeight / (float) 300);

        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);

        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

        return null;

    }

    private String recode(String str) {
        String formart = "";
        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
            if (ISO) {
                formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
            } else {
                formart = str;
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return formart;
    }


    /**
     * 处理扫描结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String results = decodeData(result.getText());
        String resultString = results;
        if (resultString.indexOf("token") != -1 && resultString.indexOf("ip") != -1) {
            Log.d(TAG, "resultIntent = " + resultString);

            try {
                JSONObject json = new JSONObject(resultString);
                domain = json.getString("ip");
                token = json.getString("token");

                if (domain.endsWith("cifernet.net") || domain.endsWith("memenet.net")) {
                    final String mAccount = json.getString("mn");
                    final String mPass = json.getString("mp");
                    new Thread() {
                        @Override
                        public void run() {
                            CMAPI.getInstance().login(MipcaActivityCapture.this, mAccount, mPass, BUILD_VPN_REQUEST_CODE, new ResultListener() {
                                @Override
                                public void onError(int i) {
                                    Log.d(TAG, "onError i========================" + i);
                                }
                            });

                        }

                    }.start();
                } else {
                    Log.d(TAG, "token===" + token);
                    Log.d(TAG, "domain===" + domain);
                    gotoMainActivity();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else {
            Toast.makeText(MipcaActivityCapture.this, R.string.scan_failed, Toast.LENGTH_LONG).show();

            handler.quitSynchronously();
            handler = null;

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler = new CaptureActivityHandler(MipcaActivityCapture.this, decodeFormats, characterSet);
                }
            }, 1000);


        }
    }


    /**
     * 分享设备，通过二维码直接进入主界面
     */
    private void gotoMainActivity() {
        Log.e(TAG, ">>>>>>>>>>>>>>> gotoMainActivity <<<<<<<<<<<<<<<");

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setLanIp(domain);
        deviceInfo.setLanPort("80");
        deviceInfo.setDomain(Constants.DOMAIN_DEVICE_LAN);
        UserSettings userSettings;
        UserInfo userInfos = UserInfoKeeper.getUserInfo("guest", "abcdefg");
        UserInfo userInfo = null;
        long id;
        if (null == userInfos) {
            userInfo = new UserInfo((long) 999999, "guest", "abcdefg", "1234567890", 0, 9999, 9999, Constants.DOMAIN_DEVICE_LAN, System.currentTimeMillis(), false, true);
            id = UserInfoKeeper.insert(userInfo);
            userSettings = UserSettingsKeeper.insertDefault(id, "guest");
        } else {
            userInfo = new UserInfo((long) 999999, "guest", "abcdefg", "1234567890", 0, 9999, 9999, Constants.DOMAIN_DEVICE_LAN, System.currentTimeMillis(), false, true);
//            UserInfoKeeper.update(userInfo);
            id = userInfos.getId();
            userSettings = UserSettingsKeeper.getSettings(id);
        }

        LoginSession loginSession = new LoginSession(userInfo, deviceInfo, userSettings, token, false, 9);
        OneOSInfo info = new OneOSInfo("4.0.0", "one2017", false, "h2n2", "20170906");
        loginSession.setOneOSInfo(info);
        LoginManage loginManager = LoginManage.getInstance();
        loginManager.setLoginSession(loginSession);

        Intent resultIntent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("isQrShare", "1");
        resultIntent.putExtras(bundle);
        startActivity(resultIntent);
        MipcaActivityCapture.this.finish();
    }

    /**
     * 获取当前activity名称
     */
    private String getRunningActivityName() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }

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

    Handler mHandler = new Handler();

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}