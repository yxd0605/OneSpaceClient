package com.eli.oneos.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.glide.GlideCatchUtil;
import com.eli.oneos.glide.QiNiuImage;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.nav.progress.ProgressModelLoader;
import com.eli.oneos.utils.FileUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.preview.HackyViewPager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PictureViewActivity extends Activity {
    private static final String TAG = PictureViewActivity.class.getSimpleName();

    private ArrayList<OneOSFile> mPicList = new ArrayList<>();
    private ArrayList<File> mLocalPicList = new ArrayList<>();

    protected LoginSession mLoginSession = null;
    private TextView mCurTxt, mTotalTxt;
    private RelativeLayout mTitleLayout;
    protected Animation mSlideInAnim, mSlideOutAnim;

    private int startIndex = 0;
    private int curPositon;
    private boolean isLocalPicture = false;
    private OnClickListener onBackListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_viewer_picture);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            startIndex = bundle.getInt("StartIndex", 0);
            isLocalPicture = bundle.getBoolean("IsLocalPicture");
            startIndex = startIndex >= 0 ? startIndex : 0;
            if (isLocalPicture) {
                mLocalPicList = (ArrayList<File>) intent.getSerializableExtra("PictureList");
            } else {
                mPicList = (ArrayList<OneOSFile>) intent.getSerializableExtra("PictureList");
            }
        }
        Log.d(TAG, "---Start Index: " + startIndex);

        mLoginSession = LoginManage.getInstance().getLoginSession();

        initView();
    }

    private void initView() {
        mSlideInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top);
        mSlideOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);

        mTitleLayout = (RelativeLayout) findViewById(R.id.layout_title);
        mCurTxt = (TextView) findViewById(R.id.text_index);
        mTotalTxt = (TextView) findViewById(R.id.text_total);
        mTitleLayout.setVisibility(View.VISIBLE);
        ImageButton ivBtn = (ImageButton) findViewById(R.id.btn_back);
        ivBtn.setOnClickListener(onBackListener);
        TextView mBackTxt = (TextView) findViewById(R.id.txt_title_back);
        mBackTxt.setOnClickListener(onBackListener);

        if (isLocalPicture ? mLocalPicList.size() > 0 : mPicList.size() > 0) {
            // GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(this, isLocalPicture ? mLocalPicList : mPicList, isLocalPicture, mLoginSession, httpBitmap);
            // GalleryViewPager mViewPager = (GalleryViewPager) this.findViewById(R.id.switch_viewer);

            HackyPagerAdapter pagerAdapter = new HackyPagerAdapter(this, isLocalPicture ? mLocalPicList : mPicList, isLocalPicture, mLoginSession);
            pagerAdapter.setOnItemChangedListener(new OnItemChangedListener() {
                @Override
                public void onItemChanged(int curPos) {
                    setIndicatorTxt(curPos + 1, isLocalPicture ? mLocalPicList.size() : mPicList.size());
                }
            });
            ViewPager mViewPager = (HackyViewPager) findViewById(R.id.switch_viewer);
            mViewPager.setOffscreenPageLimit(1);
            mViewPager.setAdapter(pagerAdapter);
            mViewPager.setPageMargin(100);
            mViewPager.setCurrentItem(startIndex);
        } else {
            ToastHelper.showToast(R.string.app_exception);
            finish();
        }
    }

    private void setIndicatorTxt(int curIndex, int total) {
        if (curIndex <= total) {
            mCurTxt.setText(String.valueOf(curIndex));
            mTotalTxt.setText(String.valueOf(total));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        this.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "----On Low Memory---");
        Glide.get(this).clearMemory();
    }

    public class HackyPagerAdapter extends PagerAdapter {
        private Context context;
        private List<?> mList;
        private boolean isLocalPic = false;
        private LoginSession mLoginSession = null;
        private OnItemChangedListener listener;

        public HackyPagerAdapter(Context context, List<?> list, boolean isLocalPicture, LoginSession loginSession) {
            this.context = context;
            this.mList = list;
            this.isLocalPic = isLocalPicture;
            this.mLoginSession = loginSession;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
//            ImageView photoView = new ImageView(container.getContext());
            RelativeLayout rootView = new RelativeLayout(container.getContext());
            final PhotoView photoView = new PhotoView(rootView.getContext());
            photoView.setScaleType(ImageView.ScaleType.CENTER);


            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    mTitleLayout.clearAnimation();
                    if (mTitleLayout.isShown()) {
                        mTitleLayout.startAnimation(mSlideOutAnim);
                        mSlideOutAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mTitleLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    } else {
                        mTitleLayout.startAnimation(mSlideInAnim);
                        mTitleLayout.setVisibility(View.VISIBLE);
                    }
                    // mTitleLayout.setVisibility(mTitleLayout.isShown() ? View.GONE : View.VISIBLE);
                }
            });
            if (isLocalPic) {
                File file = (File) mList.get(position);
                if (FileUtils.isGifFile(file.getName())) {
                    Glide.with(context).load(file).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            /*.placeholder(R.drawable.ic_circle_progress_sm)*/.fitCenter().crossFade(250).error(R.drawable.icon_file_pic_default).into(photoView);
                } else {
                    Glide.with(context).load(file).diskCacheStrategy(DiskCacheStrategy.RESULT)
                    /*.placeholder(R.drawable.ic_circle_progress_sm)*/.fitCenter().crossFade(250).error(R.drawable.icon_file_pic_default).into(photoView);
                }
            } else {
                Log.d(TAG,"cache==================="+context.getExternalCacheDir());
                Log.d(TAG,"cache size============"+ GlideCatchUtil.getInstance().getCacheSize());
                final OneOSFile file = (OneOSFile) mList.get(position);
                final String url = OneOSAPIs.genDownloadUrl(mLoginSession, file);
                final String thumburl = OneOSAPIs.genThumbnailUrl(mLoginSession, file);
                if (file.isGif()) {
                    Glide.with(context).load(url).asGif().diskCacheStrategy(DiskCacheStrategy.ALL)
                            /*.placeholder(R.drawable.ic_circle_progress_sm)*/.fitCenter().crossFade(250).error(R.drawable.icon_file_pic_default)
                            .thumbnail(1f).into(photoView);

                } else {

//                    Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.RESULT)
//                    /*.placeholder(R.drawable.ic_circle_progress_sm)*/.fitCenter().crossFade(250).error(R.drawable.icon_file_pic_default).into(photoView);

                    if (OneOSAPIs.isOneSpaceX1()) {
                        Glide.with(context).load(new QiNiuImage(url)).diskCacheStrategy(DiskCacheStrategy.ALL)
                    /*.placeholder(R.drawable.ic_circle_progress_sm)*/.fitCenter().crossFade(250).error(R.drawable.icon_file_pic_default).into(photoView);
                    } else {
                        Glide.with(context).load(thumburl).listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                // Log.e("onException", e.toString() + "  model:" + model + " isFirstResource: " + isFirstResource);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                Glide.with(context).using(new ProgressModelLoader(new ProgressHandler(context))).load(new QiNiuImage(url).getImageUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true)
                                        .fitCenter().error(R.drawable.icon_file_pic_default).into(photoView);
                                return false;
                            }
                        }).diskCacheStrategy(DiskCacheStrategy.ALL)
                    /*.placeholder(R.drawable.ic_circle_progress_sm)*/.fitCenter().error(R.drawable.icon_file_pic_default).into(photoView);
                    }


                }
            }

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, final int position, Object object) {
            super.setPrimaryItem(container, position, object);
            if (listener != null) {
                curPositon = position;
                TextView textView = (TextView) findViewById(R.id.progress_textView);
                textView.setVisibility(View.GONE);
                listener.onItemChanged(position);
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void setOnItemChangedListener(OnItemChangedListener listener) {
            this.listener = listener;
        }

        //add picture progress
        private class ProgressHandler extends Handler {

            private WeakReference<Context> mContext;

            public ProgressHandler(Context context) {
                super(Looper.getMainLooper());
                mContext = new WeakReference<>(context);
            }


            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                final Context context = mContext.get();

                if (context != null) {
                    switch (msg.what) {
                        case 1:
                            int percent = msg.arg1;
                            String downloadUrl = (String) msg.obj;
                            OneOSFile file = (OneOSFile) mList.get(curPositon);
                            String url = OneOSAPIs.genDownloadUrl(mLoginSession, file);
                            TextView textView = (TextView) findViewById(R.id.progress_textView);
//                            Log.d(TAG, "percent======" + percent);
//                            Log.d(TAG, "position===" + curPositon);

                            if (downloadUrl.equals(url)) {
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(String.valueOf(percent) + "%");
                                if (percent ==100 || percent < 0) {
                                    textView.setText("100%");
                                    textView.setVisibility(View.GONE);
                                }
                            }

                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }


    public interface OnItemChangedListener {
        void onItemChanged(int currentPosition);
    }
}
