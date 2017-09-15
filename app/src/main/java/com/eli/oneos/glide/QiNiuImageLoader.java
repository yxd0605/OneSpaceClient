package com.eli.oneos.glide;

import android.content.Context;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.HttpUrlFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.InputStream;

/**
 * Created by Administrator on 2017/9/15.
 */

public class QiNiuImageLoader implements StreamModelLoader<QiNiuImage> {
    @Override
    public DataFetcher<InputStream> getResourceFetcher(final QiNiuImage model, int width, int height) {
        return new HttpUrlFetcher(new GlideUrl(model.getImageUrl())) {
            @Override
            public String getId() {
                return model.getImageId();
            }
        };
    }
    public static class Factory implements ModelLoaderFactory<QiNiuImage, InputStream> {
        @Override
        public ModelLoader<QiNiuImage, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new QiNiuImageLoader();
        }
        @Override
        public void teardown() { /* no op */ }
    }
}
