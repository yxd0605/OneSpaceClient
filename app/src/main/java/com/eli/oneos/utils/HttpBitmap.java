package com.eli.oneos.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.widget.preview.TouchImageView;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.display.Displayer;

public class HttpBitmap {
    private static final String TAG = HttpBitmap.class.getSimpleName();

    private static HttpBitmap instance = new HttpBitmap();
    private FinalBitmap finalBitmap = null;

    public static HttpBitmap getInstance() {
        return instance;
    }

    private HttpBitmap() {
        Log.d(TAG, "HttpBitmap Instruct");
        initFinalBitmap();
    }

    private void initFinalBitmap() {
        Log.d(TAG, "Init Final Bitmap");
        Context context = MyApplication.getAppContext();
        final Bitmap mFailBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_pic);

        finalBitmap = FinalBitmap.create(context);
        finalBitmap.configLoadfailImage(mFailBitmap);
        // finalBitmap.configLoadingImage(mLoadBitmap);
        finalBitmap.configBitmapLoadThreadSize(3);
        finalBitmap.configDiskCacheSize(1024 * 1024 * 100);
        finalBitmap.configMemoryCachePercent(0.3f);
        finalBitmap.configDisplayer(new Displayer() {

            @Override
            public void loadFailDisplay(View imageView, Bitmap bitmap) {
                Log.e(TAG, "===>>>>FinalBitmap: Load Picture Failed");
                if (imageView instanceof TouchImageView) {
                    TouchImageView touchView = (TouchImageView) imageView;
                    touchView.onLoadOver(false, mFailBitmap);
                } else if (imageView instanceof ImageView) {
                    ImageView mView = (ImageView) imageView;
//                    mView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    mView.setImageBitmap(bitmap);
                }
                // else if (imageView instanceof TimeImageView) {
                // TimeImageView mView = (TimeImageView) imageView;
                // mView.setImageBitmap(bitmap);
                // }
            }

            @Override
            public void loadCompletedisplay(View imageView, Bitmap bitmap, BitmapDisplayConfig config) {
                if (imageView instanceof TouchImageView) {
                    TouchImageView touchView = (TouchImageView) imageView;
                    touchView.onLoadOver(true, bitmap);
                } else if (imageView instanceof ImageView) {
//                    Logged.d(TAG, "===>>>>FinalBitmap: Load Picture Success");
                    ImageView mView = (ImageView) imageView;
//                    mView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mView.setImageBitmap(bitmap);
                }
                // else {
                // Logged.d(TAG, "===>>>>FinalBitmap: Load Picture Success");
                // ImageView mView = (ImageView) imageView;
                // mView.setImageBitmap(config.getLoadfailBitmap());
                // }
            }
        });
    }

    public void display(View imageView, String uri) {
        if (finalBitmap != null) {
            finalBitmap.display(imageView, uri);
        }
    }

    public void onPause() {
        if (finalBitmap != null) {
            finalBitmap.onPause();
        }
    }

    public void onResume() {
        if (finalBitmap != null) {
            finalBitmap.onResume();
        }
    }

    public void onDestroy() {
        if (finalBitmap != null) {
            finalBitmap.onDestroy();
        }
    }
}
