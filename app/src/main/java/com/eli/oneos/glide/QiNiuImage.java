package com.eli.oneos.glide;

import android.util.Log;

/**
 * Created by Administrator on 2017/9/15.
 */

public class QiNiuImage {
    private final String imageUrl;

    public QiNiuImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageId() {
        if (imageUrl.contains("&")) {
            Log.d("TAG","dog====="+ imageUrl.substring(imageUrl.lastIndexOf("&"),imageUrl.length()-1));
            return imageUrl.substring(imageUrl.lastIndexOf("&"),imageUrl.length()-1);
        } else {
            return imageUrl;
        }
    }
}
