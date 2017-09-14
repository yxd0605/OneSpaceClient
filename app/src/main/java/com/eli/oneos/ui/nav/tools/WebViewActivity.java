package com.eli.oneos.ui.nav.tools;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.eli.oneos.R;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.TitleBackLayout;

public class WebViewActivity extends BaseActivity {
    private static final String TAG = WebViewActivity.class.getSimpleName();

    private String mLoadUrl = null;
    private String mTitle = null;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        initSystemBarStyle();

        Intent intent = getIntent();
        if (intent != null) {
            mLoadUrl = intent.getStringExtra("Url");
            mTitle = intent.getStringExtra("Title");
        }

        if (EmptyUtils.isEmpty(mLoadUrl)) {
            this.finish();
            return;
        }

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Init View By ID
     */
    private void initView() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(mTitle);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.loadUrl(mLoadUrl);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                mProgressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                // Uri uri = Uri.parse(url);
                // Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // startActivity(intent);

                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                Log.d(TAG, ">>>> Download, Url=" + url + "; Agent=" + userAgent + "; Content="
                        + contentDisposition + "; MimeType=" + mimetype + "; Len=" + contentLength
                        + "; Guess FileName=" + fileName);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                ToastHelper.showToast(R.string.start_download_file);
            }
        });

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true); // 支持js
        settings.setUseWideViewPort(true); // 将图片调整到适合webview的大小
        settings.setSupportZoom(true); // 支持缩放
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); // 支持内容重新布局
        settings.supportMultipleWindows(); // 多窗口
        // settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        settings.setAllowFileAccess(true); // 设置可以访问文件
        settings.setNeedInitialFocus(true); // 当webview调用requestFocus时为webview设置节点
        settings.setBuiltInZoomControls(true); // 设置支持缩放
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过JS打开新窗口
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        settings.setLoadsImagesAutomatically(true); // 支持自动加载图片
        settings.setDisplayZoomControls(false); // 显示缩放工具
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();// 返回上一页面
                return true;
            } else {
                onBackPressed();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
