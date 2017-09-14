///*
// Copyright (c) 2013 Roman Truba
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial
// portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
// TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//package com.eli.oneos.widget.preview;
//
//import android.content.Context;
//import android.net.Uri;
//import android.util.Log;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.eli.oneos.R;
//import com.eli.oneos.constant.Constants;
//import com.eli.oneos.constant.OneOSAPIs;
//import com.eli.oneos.model.oneos.OneOSFile;
//import com.eli.oneos.model.oneos.user.LoginSession;
//import com.eli.oneos.utils.FileUtils;
//import com.eli.oneos.utils.HttpBitmap;
//
//import java.io.File;
//import java.util.List;
//
//public class GalleryPagerAdapter extends BasePagerAdapter {
//    private static final String TAG = GalleryPagerAdapter.class.getSimpleName();
//
//    private static final long MAX_PICTURE_SIZE = 1024 * 1024 * 20;
//
//    private Context mContext;
//    private boolean isLocalPic = false;
//    private LoginSession mLoginSession = null;
//
//    private HttpBitmap finalBitmap;
//
//    public GalleryPagerAdapter(Context context, List<?> resources) {
//        super(context, resources);
//        this.mContext = context;
//    }
//
//    public GalleryPagerAdapter(Context context, List<?> resources, boolean isLocalPicture, LoginSession loginSession, HttpBitmap finalBitmap) {
//        super(context, resources);
//        this.mContext = context;
//        this.isLocalPic = isLocalPicture;
//        this.mLoginSession = loginSession;
//        this.finalBitmap = finalBitmap;
//    }
//
//    @Override
//    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//        super.setPrimaryItem(container, position, object);
//        ((GalleryViewPager) container).mCurrentView = ((TouchImageViewLayout) object).getImageView();
//        // ((TouchImageViewLayout) object).setUri(getPictureUri(position));
//    }
//
//    @Override
//    public Object instantiateItem(ViewGroup collection, final int position) {
//        final TouchImageViewLayout ivLayout = new TouchImageViewLayout(mContext, finalBitmap);
//        ivLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        if (Constants.DISPLAY_IMAGE_WITH_GLIDE) {
//            ivLayout.getImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
//            if (isLocalPic) {
//                File file = (File) mResources.get(position);
//                if (FileUtils.isGifFile(file.getName())) {
//                    Glide.with(mContext).load(Uri.fromFile(file)).asGif().diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .error(R.drawable.icon_file_pic_default).into(ivLayout.getImageView());
//                } else {
//                    Glide.with(mContext).load(Uri.fromFile(file)).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .error(R.drawable.icon_file_pic_default).into(ivLayout.getImageView());
//                }
//            } else {
//                OneOSFile file = (OneOSFile) mResources.get(position);
//                if (file.isGif()) {
//                    Glide.with(mContext).load(OneOSAPIs.genDownloadUrl(mLoginSession, file)).asGif().diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .error(R.drawable.icon_file_pic_default).into(ivLayout.getImageView());
//                } else {
//                    Glide.with(mContext).load(OneOSAPIs.genDownloadUrl(mLoginSession, file)).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .error(R.drawable.icon_file_pic_default).into(ivLayout.getImageView());
//                }
////                Glide.with(mContext).load(OneOSAPIs.genDownloadUrl(mLoginSession, info)).
////                        into(new ImageViewTarget<GlideDrawable>(ivLayout.getImageView()) {
////                            @Override
////                            protected void setResource(GlideDrawable resource) {
////                                ivLayout.getImageView().setImageDrawable(resource);
////                                ivLayout.onLoadOver();
////                            }
////                        });
//            }
//        } else {
//            ivLayout.setUri(getPictureUri(position));
//        }
////        ivLayout.setUri(getPictureUri(position));
//        collection.addView(ivLayout, 0);
//
//        return ivLayout;
//    }
//
//    private String getPictureUri(int position) {
//        String uri;
//        if (isLocalPic) {
//            Log.d(TAG, "Add Touch Local ImageView Layout");
//            File file = (File) mResources.get(position);
//            if (file.length() <= MAX_PICTURE_SIZE) {
//                uri = file.getAbsolutePath();
//            } else {
//                uri = " "; // for skip this picture
//            }
//        } else {
//            OneOSFile info = (OneOSFile) mResources.get(position);
//            if (info.getSize() <= MAX_PICTURE_SIZE) {
//                uri = OneOSAPIs.genDownloadUrl(mLoginSession, info);
//            } else {
//                uri = " "; // for skip this picture
//            }
//        }
//        Log.d(TAG, "===Display Image, Index = " + position + "; Uri = " + uri);
//        return uri;
//    }
//}
