///*
// Copyright (c) 2012 Roman Truba
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
//import android.util.AttributeSet;
//import android.view.View;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//
//import com.eli.oneos.utils.HttpBitmap;
//
//
//public class TouchImageViewLayout extends RelativeLayout {
//    protected ProgressBar mProgressBar;
//    protected TouchImageView mImageView;
//    private HttpBitmap mHttpBitmap;
//
//    protected Context mContext;
//
//    // public TouchImageViewLayout(Context ctx) {
//    // super(ctx);
//    // this.mContext = ctx;
//    // init();
//    // }
//
//    public TouchImageViewLayout(Context ctx, HttpBitmap httpBitmap) {
//        super(ctx);
//        this.mHttpBitmap = httpBitmap;
//        this.mContext = ctx;
//        init();
//    }
//
//    public TouchImageViewLayout(Context ctx, AttributeSet attrs) {
//        super(ctx, attrs);
//        mContext = ctx;
//        init();
//    }
//
//    public TouchImageView getImageView() {
//        return mImageView;
//    }
//
//    protected void init() {
//        LayoutParams params;
//
//        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleInverse);
//        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        params.addRule(RelativeLayout.CENTER_IN_PARENT);
//        params.setMargins(30, 0, 30, 0);
//        mProgressBar.setLayoutParams(params);
//        mProgressBar.setIndeterminate(false);
//        mProgressBar.setMax(100);
//        this.addView(mProgressBar);
//
//        mImageView = new TouchImageView(mContext);
//        params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        mImageView.setLayoutParams(params);
//        mImageView.setOnLoadResultListener(new TouchImageView.onLoadResultCallback() {
//
//            @Override
//            public void onCallback(boolean success) {
//                onLoadOver();
//            }
//        });
//        this.addView(mImageView);
//        // mImageView.setVisibility(View.GONE);
//    }
//
//    public void onLoadOver() {
//        mProgressBar.setVisibility(View.GONE);
//        mImageView.setVisibility(View.VISIBLE);
//    }
//
//    public void setUri(String imageUri) {
//        if (mHttpBitmap != null) {
//            mHttpBitmap.display(mImageView, imageUri);
//        }
//    }
//}
