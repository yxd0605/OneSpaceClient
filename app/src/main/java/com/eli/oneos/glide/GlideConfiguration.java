package com.eli.oneos.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;

import java.io.File;
import java.io.InputStream;

/**
 * GlideConfiguration
 */

public class GlideConfiguration implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //自定义缓存目录
        File cacheDir = context.getExternalCacheDir();
        builder.setDiskCache(new DiskLruCacheFactory(cacheDir.getPath(), GlideCatchConfig.GLIDE_CARCH_DIR, GlideCatchConfig.GLIDE_CATCH_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(QiNiuImage.class, InputStream.class, new QiNiuImageLoader.Factory());
    }
}
