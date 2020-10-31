package cn.tuge.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;

/**
 * MCV Terminal Software
 */
public class MCVActivity extends Activity {

    public static final String TAG = "MeowCloudVideo";

    // 终端软件版本编号
    private String VERSION_CODE = "0";

    // 应用加载画面
    private ImageView appLoadingView;

    // RFB Layer
    private WebView webViewRFB;
    // Web Layer
//    private XWalkView webView;
    private WebView webView;
    // Mask Layer
    private WebView webViewMask;
    // Video Layer
    private VideoView videoView;
    //private MediaController mMediaController;

    // 浏览器入口页面URL
    private String indexPage = "";
    // 浏览器入口本地页面URL
    private String localPage = "";
    // RFB入口页面URL
    private String rfbPage = "";
    // 加载等待中的页面URL
    private String loadingPage = "";
    // 空白页面的URL
    private String blankPage = "";
    // bridge页面URL，全屏播放时层叠在播放器上层，实现从页面控制播放器
    private String bridgePage = "";

    // 屏幕的宽高
    private int screenWidth = 1920;
    private int screenHeight = 1080;

    // 检测终端软件版本的URL
    private String _url_version_check = "http://webapp.mapg.cn/update/versioninfo.json";

    // 键值事件，0：交给系统处理，1：交给页面处理
    private int keyEventMode = 0;

    // 存放浮动视频与视频窗口的索引与对象
    public static Map<String, IjkVideoView> ijkvvMap = new HashMap<>();
    public static Map<String, View> ijkvvViewMap = new HashMap<>();
    public static Map<String, VideoView> vvMap = new HashMap<>();
    public static Map<String, View> vvViewMap = new HashMap<>();
    // 存放浮动浏览器窗口的索引与对象
    public static Map<String, WebView> wvMap = new HashMap<>();
    public static Map<String, View> wvViewMap = new HashMap<>();


    private WindowManager windowManager;

//    =========================================================================================================

    public static int playerCapacity = 16;
    public static Map<String, IjkVideoView> playerPool = new HashMap<>(playerCapacity);
    public static Queue<String> idlePlayers = new LinkedBlockingQueue<>(playerCapacity);
    public static int[] videoWindowIDs = {
            R.id.videoWindow1,
            R.id.videoWindow2,
            R.id.videoWindow3,
            R.id.videoWindow4,
            R.id.videoWindow5,
            R.id.videoWindow6,
            R.id.videoWindow7,
            R.id.videoWindow8,
            R.id.videoWindow9,
            R.id.videoWindow10,
            R.id.videoWindow11,
            R.id.videoWindow12,
            R.id.videoWindow13,
            R.id.videoWindow14,
            R.id.videoWindow15,
            R.id.videoWindow16,
    };

    public static int playerCapacity2 = 16;
    public static Map<String, VideoView> playerPool2 = new HashMap<>(playerCapacity);
    public static Queue<String> idlePlayers2 = new LinkedBlockingQueue<>(playerCapacity);
    public static int[] videoWindowIDs2 = {
            R.id.videoWindow17,
            R.id.videoWindow18,
            R.id.videoWindow19,
            R.id.videoWindow20,
            R.id.videoWindow21,
            R.id.videoWindow22,
            R.id.videoWindow23,
            R.id.videoWindow24,
            R.id.videoWindow25,
            R.id.videoWindow26,
            R.id.videoWindow27,
            R.id.videoWindow28,
            R.id.videoWindow29,
            R.id.videoWindow30,
            R.id.videoWindow31,
            R.id.videoWindow32,
    };

    public static List<String> playerids = new ArrayList<>(playerCapacity + playerCapacity2);

//    =========================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.mcv_activity);

        // 应用加载画面
        appLoadingView = findViewById(R.id.appLoading);

        // 初始化全局参数
        initParams();

        // 初始化浏览器
        initBrowserRFB();
        initBrowserMask();
        initBrowserFLB();
        initBrowser();

        // 初始化播放器
        initMediaPlayer();

        // 检测最新的软件版本
//        showUpdateApkDialog();
    }

//    @Override
//    protected void onXWalkReady() {
//
///*
//
//        XWalkPreferences.setValue("enable-javascript", true);
//        XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);
//        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);
//        XWalkPreferences.setValue(XWalkPreferences.PROFILE_NAME, true);
//        XWalkPreferences.setValue(XWalkPreferences.ENABLE_THEME_COLOR, true);
//        XWalkPreferences.setValue(XWalkPreferences.SPATIAL_NAVIGATION, true);
//        XWalkPreferences.setValue(XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true);
//*/
//
//    }

    /**
     * 初始化参数
     */
    private void initParams() {

        /*
        // 获取全屏幕的宽高值, 支持Android 4.4
        windowManager = this.getWindowManager();
        screenWidth = windowManager.getDefaultDisplay().getWidth();
        screenHeight = windowManager.getDefaultDisplay().getHeight();
        */

        // 支持Android 7.1
        DisplayMetrics dm = new DisplayMetrics();
        windowManager = this.getWindowManager();
        windowManager.getDefaultDisplay().getRealMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        Log.i(TAG, "screenWidth = " + screenWidth);
        Log.i(TAG, "screenHeight = " + screenHeight);

        WindowManager.LayoutParams wlp = this.getWindow().getAttributes();
        wlp.width = screenWidth;
        wlp.height = screenHeight;
        this.getWindow().setAttributes(wlp);


        VERSION_CODE = String.valueOf(this.getResources().getString(R.string.versionCode));
        _url_version_check = String.valueOf(this.getResources().getString(R.string.urlVersionCheck));

        indexPage = String.valueOf(this.getResources().getString(R.string.indexPage));
        localPage = this.getResources().getString(R.string.localPage);
        loadingPage = String.valueOf(this.getResources().getString(R.string.loadingPage));
        bridgePage = String.valueOf(this.getResources().getString(R.string.bridgePage));
        rfbPage = String.valueOf(this.getResources().getString(R.string.rfbPage));
        blankPage = String.valueOf(this.getResources().getString(R.string.blankPage));

    }

    /**
     * 初始化播放器
     */
    private void initMediaPlayer() {

        videoView = findViewById(R.id.videoView);

        //mMediaController = new MediaController(this);
        //videoView.setMediaController(mMediaController);
//        videoView.setBackgroundColor(0);
        videoView.setBackgroundColor(Color.TRANSPARENT);
//        videoView.getBufferPercentage();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                Log.i(TAG, "In onPrepared() Duration:" + mediaPlayer.getDuration() + "/" + videoView.getDuration());
                sendVideoDuration(mediaPlayer.getDuration());

                mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        mp.start();
                    }
                });

//                mediaPlayer.start();
//                mediaPlayer.setLooping(true);

                webViewMask.loadUrl(blankPage);
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                /*
                System.out.println("currentVideoUrl = " + currentVideoUrl);
                videoView.setVideoURI(Uri.parse(currentVideoUrl));
                videoView.start();
                */
            }
        });


//      =============================================================================================

        for (int i = 0; i < playerCapacity; i++) {

            String playerid = UUID.randomUUID().toString();

//            IjkVideoView videoView = new IjkVideoView(this);
            IjkVideoView videoView = findViewById(videoWindowIDs[i]);

            playerPool.put(playerid, videoView);
            idlePlayers.offer(playerid);

        }

        for (int i = 0; i < playerCapacity2; i++) {

            String playerid = UUID.randomUUID().toString();

            VideoView videoView = findViewById(videoWindowIDs2[i]);

            playerPool2.put(playerid, videoView);
            idlePlayers2.offer(playerid);

        }

//        =============================================================================================


//        videoView.setVideoURI(Uri.parse("http://172.16.1.208:2015/v4s/v4s.mcv"));
//        videoView.start();
//
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
////        } else {
////            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
////        }
////        layoutParams.format = PixelFormat.RGBA_8888;
//        layoutParams.width = 480;
//        layoutParams.height = 270;
//        layoutParams.leftMargin = 100;
//        layoutParams.topMargin = 100;
////        layoutParams.x = 100;
////        layoutParams.y = 100;
////        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
////        layoutParams.type = WindowManager.LayoutParams.FIRST_SUB_WINDOW;
////        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
////        layoutParams.token = ;
//        IjkVideoView ivideoView = new IjkVideoView(this);
//
//        container.addView(ivideoView, 0, layoutParams);
//
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////            videoView.setZ(0.f);
////        }
//
//
//
////
//
//
//        ivideoView.setVideoURI(Uri.parse("http://172.16.1.208:2015/v3s/v3s.mcv"));
//        ivideoView.start();

    }



    /**
     * RFB层浏览器
     */
    private void initBrowserRFB() {

        webViewRFB = findViewById(R.id.webView_rfb);

        WebSettings settings = webViewRFB.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

//        webViewRFB.setBackgroundColor(0);
        webViewRFB.setBackgroundColor(Color.TRANSPARENT);
        webViewRFB.setHorizontalScrollBarEnabled(false);
        webViewRFB.setVerticalScrollBarEnabled(false);

        webViewRFB.setWebChromeClient(new WebChromeClient());
        webViewRFB.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webView.requestFocus();
                super.onPageStarted(view, url, favicon);
            }
        });

        webViewRFB.loadUrl(blankPage);

    }

//    /**
//     * Web层浏览器
//     */
//    @SuppressLint("JavascriptInterface")
//    private void initBrowser() {
//
//        webView = (XWalkView) findViewById(R.id.webView);
//
//        XWalkSettings settings = webView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setAllowFileAccess(true);
//        settings.setCacheMode(XWalkSettings.LOAD_NO_CACHE);
//
//        //webView.setBackgroundColor(0);
//        webView.setBackgroundColor(Color.TRANSPARENT);
//        //webView.setZOrderOnTop(true);
//        webView.requestFocus();
//        webView.addJavascriptInterface(this, "MMP"); // MMP: Meow Media Player
//        webView.addJavascriptInterface(this, "MCV"); // MCV: Meow Cloud Video
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.setVerticalScrollBarEnabled(false);
//        webView.setUIClient(new XWalkUIClient(webView) {
//
//            @Override
//            public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
//                appLoadingView.setVisibility(View.GONE);
//                super.onPageLoadStopped(view, url, status);
//            }
//        });
//        webView.setResourceClient(new XWalkResourceClient(webView) {
//
//            @Override
//            public void onReceivedResponseHeaders(XWalkView view, XWalkWebResourceRequest request, XWalkWebResourceResponse response) {
//
//                super.onReceivedResponseHeaders(view, request, response);
//            }
//
//            @Override
//            public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
//
//                Log.i(TAG, "SslError: " + error);
//                callback.onReceiveValue(true);
////                super.onReceivedSslError(view, callback, error);
//            }
//
//
//
//            @Override
//            public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
//
//                Log.i(TAG, "XWalkView errorCode: " + errorCode + " description: " + description + " failingUrl: " + failingUrl);
//
//                webView.loadUrl(localPage);
//
//                //super.onReceivedLoadError(view, errorCode, description, failingUrl);
//            }
//        });
//        //webView.clearCache(true);
//
//
//        webView.loadUrl(indexPage);
//
//
//    }

    /**
     * Web层浏览器
     */
    @SuppressLint("JavascriptInterface")
    private void initBrowser() {

        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        //webView.setBackgroundColor(0);
        webView.setBackgroundColor(Color.TRANSPARENT);
        //webView.setZOrderOnTop(true);
        webView.requestFocus();
        webView.addJavascriptInterface(this, "MMP"); // MMP: Meow Media Player
        webView.addJavascriptInterface(this, "MCV"); // MCV: Meow Cloud Video
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 防止点击页面链接启动Android默认浏览器
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                appLoadingView.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i(TAG, "WebView errorCode: " + error.getErrorCode() + " description: " + error.getDescription() + " failingUrl: " + request.getUrl());
                }
                webView.loadUrl(localPage);

//                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                }
                handler.proceed();

//                super.onReceivedSslError(view, handler, error);
            }
        });

        webView.loadUrl(indexPage);
    }

    /**
     * 向javascript传送键值
     *
     * @param keyCode
     */
    private void keyDownEvent(int keyCode) {
        try {
            webView.evaluateJavascript(String.format("javascript:keyDownEvent(%s)", keyCode), null);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    /**
     * 向javascript传送视频的总时长
     *
     * @param videoDuration
     */
    private void sendVideoDuration(int videoDuration) {
        try {
            webView.evaluateJavascript(String.format("javascript:receiveVideoDuration(%s)", videoDuration), null);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }


    /**
     * Mask层浏览器
     */
    private void initBrowserMask() {

        webViewMask = findViewById(R.id.webView_mask);

        WebSettings settings = webViewMask.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

//        webViewMask.setBackgroundColor(0);
        webViewMask.setBackgroundColor(Color.TRANSPARENT);
        webViewMask.setHorizontalScrollBarEnabled(false);
        webViewMask.setVerticalScrollBarEnabled(false);

        webViewMask.setWebChromeClient(new WebChromeClient());
        webViewMask.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webView.requestFocus();
                super.onPageStarted(view, url, favicon);
            }
        });

        webViewMask.loadUrl(blankPage);

    }

    /**
     * Floating层浏览器
     */
    private void initBrowserFLB() {


    }


    @JavascriptInterface
    @Override
    public void onDestroy() {

        stopVideo();
        stopAllVideo();

        ijkvvMap.clear();
        ijkvvViewMap.clear();
        vvMap.clear();
        vvViewMap.clear();
        wvMap.clear();
        wvViewMap.clear();
        playerids.clear();
        playerPool.clear();
        playerPool2.clear();
        idlePlayers.clear();
        idlePlayers2.clear();

        super.onDestroy();

        System.gc();
        System.exit(0);
    }


//    /**
//     * 所有按键的行为都交给Web页面处理，不使用操作系统的默认行为
//     *
//     * @param keyCode
//     * @param event
//     * @return
//     */
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        Log.i(TAG, "APP: keyCode=" + keyCode);
//        Log.i(TAG, "APP: KeyEvent=" + event);
//
//        keyDownEvent(keyCode);
//
//        // 当处于首页时，返回按键失效
//        /*
//        if (keyCode == 4 && webView.getUrl().equals(indexPage)) {
//            Log.i(TAG, webView.getUrl());
//            return false;
//        }
//        */
//
//        /*
//        System.out.println("APP: WebView -> URL: " + webView.getUrl());
//
//        if (!webView.getUrl().equals(this.blankPage_stream)) {
//            if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
//
//                // 如果从全屏播放点击返回按键，则跳转至触发视频播放的页面
//                if (webView.getUrl().equals(bridgePage)) {
//                    webView.goBack();
//                    isPlayingForFull = false;
//                }
//
//                webView.goBack();
//                videoView.stopPlayback();
//
//                return true;
//            }
//        }
//        */
//        return super.onKeyDown(keyCode, event);
//        //return false;
//    }

    /**
     * 将所有的键值送到javascript中处理
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        Log.i(TAG, "dispatchKeyEvent: " + event);
        Log.i(TAG, "keyCode: " + event.getKeyCode());
        Log.i(TAG, "KeyEventMode: " + keyEventMode);

        if (keyEventMode != 0) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                keyDownEvent(event.getKeyCode());
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                    || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return super.dispatchKeyEvent(event);
            }

            return true;
        }

        return super.dispatchKeyEvent(event);
    }


    /**
     * 全屏播放JS接口
     *
     * @param url         媒体资源地址
     * @param loadingPage 加载页面地址
     * @param blankPage   播放中的Mask页面地址
     */
    @JavascriptInterface
    public void playVideo(String url, String loadingPage, String blankPage) {

        initParams();

        if (loadingPage != null && !"".equals(loadingPage)) {

            this.loadingPage = loadingPage;
        }

        if (blankPage != null && !"".equals(blankPage)) {

            this.blankPage = blankPage;
        }

        new AsyncBrowserTask().execute(this.loadingPage, "Mask");
//        new AsyncVideoTask().execute(url, "0", "0", String.valueOf(screenWidth), String.valueOf(screenHeight));
        new AsyncVideoTask().execute(url);

    }

    /**
     * 全屏播放JS接口
     *
     * @param url 媒体资源地址
     */
    @JavascriptInterface
    public void playVideo(String url) {

        playVideo(url, null, null);

    }

    /**
     * 全屏播放JS接口
     *
     * @param url         媒体资源地址
     * @param loadingPage 加载页面地址
     */
    @JavascriptInterface
    public void playVideo(String url, String loadingPage) {

        playVideo(url, loadingPage, null);

    }

    /**
     * 当前是否有视频正在播放
     *
     */
    @JavascriptInterface
    public boolean isPlaying() {

        return videoView.isPlaying();

    }

    /**
     * 开始或继续播放
     */
    @JavascriptInterface
    public void startVideo() {

        videoView.start();

    }

    /**
     * 停止播放
     */
    @JavascriptInterface
    public void stopVideo() {

        videoView.stopPlayback();

    }


    /**
     * 暂停播放
     */
    @JavascriptInterface
    public void pauseVideo() {

        if (videoView.canPause()) {
            videoView.pause();
        }
    }

    /**
     * 重头播放
     */
    @JavascriptInterface
    public void resumeVideo() {

        videoView.resume();
    }

    /**
     * 获取当前视频时间，如果获取不到返回0
     * 返回毫秒数
     */
    @JavascriptInterface
    public int getVideoCurrentPosition() {

        Log.i(TAG, "CurrentPosition: " + videoView.getCurrentPosition());
        return videoView.getCurrentPosition();
    }

    /**
     * 从指定的位置开始播放视频
     *
     * @param msec         毫秒数
     * @param positionType 位置类型，0：后退，1：前进
     */
    @JavascriptInterface
    public void seekToVideo(int msec, int positionType) {

        int seekToMsec = 0;

        if (positionType == 1) {
            seekToMsec = videoView.getCurrentPosition() + msec;
        } else {
            seekToMsec = videoView.getCurrentPosition() - msec;
        }

        Log.i(TAG, "Seek To Msec: " + seekToMsec);

        videoView.seekTo(seekToMsec);
    }


    /**
     * Mask层页面加载JS接口
     *
     * @param url 页面地址
     */
    @JavascriptInterface
    public void loadPageMask(String url) {

        Log.i(TAG, "APP: loadPageMask URL: " + url);

        if (url == null || "".equals(url)) {

            url = this.blankPage;
        }

        new AsyncBrowserTask().execute(url, "Mask");

    }

    /**
     * RFB层页面加载JS接口
     *
     * @param url 页面地址
     */
    @JavascriptInterface
    public void loadPageData(String url) {

        Log.i(TAG, "APP: loadPageData URL: " + url);

        if (url == null || "".equals(url)) {

            url = this.blankPage;
        }

        new AsyncBrowserTask().execute(url, "RFB");

    }


//    ========== Floating/Window Layer Player Interface START ======================================================================================

    /**
     * 窗口播放JS接口
     *
     * @param width     窗口宽
     * @param height    窗口高
     * @param x         窗口x坐标
     * @param y         窗口y坐标
     * @param url       媒体资源地址
     * @param type      播放器类型: 0软解，！0硬解
     * @param z         窗口z坐标: 0在最底层，!0在最上层
     *
     * @return playerid 视频窗口的唯一标识
     */
    @JavascriptInterface
    public String playVideo(int width, int height, int x, int y, String url, int type, int z) {

//        Log.i(TAG, "Play Video Time: " + System.currentTimeMillis());

        String playerid = "";

        if (z != 0) {

            playerid = UUID.randomUUID().toString();
            playerids.add(playerid);

            String finalPlayerid = playerid;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Intent intent;
                    if (type != 0) {
                        intent = new Intent(MCVActivity.this, MCVideoVVFloatingService.class);
                    } else {
                        intent = new Intent(MCVActivity.this, MCVideoIJKFloatingService.class);
                    }

                    intent.putExtra("width", width);
                    intent.putExtra("height", height);
                    intent.putExtra("x", x);
                    intent.putExtra("y", y);
                    intent.putExtra("z", z);
                    intent.putExtra("videoURL", url);
                    intent.putExtra("videoid", finalPlayerid);

                    startService(intent);
                }
            }).start();

        } else {

            if (type != 0) {
                playerid = idlePlayers2.poll();
                new AsyncWindowVideoTask2().execute(String.valueOf(width), String.valueOf(height), String.valueOf(x), String.valueOf(y), url, playerid, "0");
            } else {
                playerid = idlePlayers.poll();
                new AsyncWindowVideoTask().execute(String.valueOf(width), String.valueOf(height), String.valueOf(x), String.valueOf(y), url, playerid, "0");
            }

            playerids.add(playerid);
        }

        return playerid;
    }

    @JavascriptInterface
    public String playVideo(int width, int height, int x, int y, String url) {

        return playVideo(width, height, x, y, url, 0, 0);
    }

    @JavascriptInterface
    public String playVideo(int width, int height, int x, int y, String url, int type) {

        return playVideo(width, height, x, y, url, type, 0);
    }

    /**
     * 关闭所有的浮动视频
     *
     */
    @JavascriptInterface
    public void stopAllVideo() {

        for (String playerid : playerids) {
            stopVideo(playerid);
        }
        playerids.clear();
    }

    /**
     * 关闭浮动视频
     *
     * @param playerid
     */
    @JavascriptInterface
    public void stopVideo(String playerid) {

        new AsyncWindowVideoTask().execute("0", "0", "0", "0", "", playerid, "2");
        new AsyncWindowVideoTask2().execute("0", "0", "0", "0", "", playerid, "2");

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    ijkvvMap.get(playerid).stopPlayback();

                    windowManager.removeView(ijkvvViewMap.get(playerid));

                    ijkvvViewMap.remove(playerid);
                    ijkvvMap.remove(playerid);

                } catch (Exception e) {

                    Log.e(TAG, "stopVideo:playerid: " + playerid, e);

                    try {
                        vvMap.get(playerid).stopPlayback();

                        windowManager.removeView(vvViewMap.get(playerid));

                        vvViewMap.remove(playerid);
                        vvMap.remove(playerid);

                    } catch (Exception e2) {

                    }

                }

            }
        }).start();

    }

    /**
     * 切换浮动视频
     *
     * @param playerid
     * @param url
     */
    @JavascriptInterface
    public void switchVideo(String playerid, String url) {

        new AsyncWindowVideoTask().execute("0", "0", "0", "0", url, playerid, "1");
        new AsyncWindowVideoTask2().execute("0", "0", "0", "0", url, playerid, "1");

        try {
            ijkvvMap.get(playerid).setVideoURI(Uri.parse(url));
            ijkvvMap.get(playerid).start();
        } catch (Exception e) {

            Log.e(TAG, "switchVideo:playerid:url: " + playerid + ":" + url, e);

            try {
                vvMap.get(playerid).setVideoURI(Uri.parse(url));
                vvMap.get(playerid).start();
            } catch (Exception e2) {

            }
        }

    }


//    ========== Floating/Window Layer Player Interface END   ======================================================================================


//    ========== Floating Layer Browser Interface START ======================================================================================

    /**
     * Floating Layer Browser页面加载JS接口
     *
     * @param url 页面地址
     * @param isRequestFocus 是否请求获取焦点
     */
    @JavascriptInterface
    public String loadPageFLB(String url, int isRequestFocus) {

        String browserid = UUID.randomUUID().toString();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(MCVActivity.this, MCVBrowserWVFloatingService.class);
                intent.putExtra("width", screenWidth);
                intent.putExtra("height", screenHeight);
                intent.putExtra("x", 0);
                intent.putExtra("y", 0);
                intent.putExtra("url", url);
                intent.putExtra("browserid", browserid);
                intent.putExtra("isRequestFocus", isRequestFocus);

                startService(intent);
            }
        }).start();

        return browserid;

    }

    @JavascriptInterface
    public String loadPageFLB(String url) {

        return loadPageFLB(url, 0);
    }


    /**
     * 关闭浮动浏览器窗口
     *
     * @param browserid
     */
    @JavascriptInterface
    public void closeFLB(String browserid) {

        new Thread(new Runnable() {
            @Override
            public void run() {

            windowManager.removeView(wvViewMap.get(browserid));

            wvViewMap.remove(browserid);
            wvMap.remove(browserid);

            }
        }).start();
    }


//    ========== Floating Layer Browser Interface END   ======================================================================================


    /**
     * 设置键值事件模式
     * @param mode 0：交给系统处理，1：交给页面处理
     */
    @JavascriptInterface
    public void setKeyEventMode(int mode) {

        keyEventMode = mode;
    }


    /**
     * 弹出窗口，应用于调试
     *
     * @param txt
     */
    @JavascriptInterface
    public void showDialog(String txt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(txt);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        /*
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        */

        AlertDialog alertDialog = builder.create();
        //alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * @param param 参数格式：packageName|className|key0=value0,key1=value1,...,keyn=valuen
     */
    @JavascriptInterface
    public void gotoApp(String param) {

        Log.i(TAG, "gotoApp param: ".concat(param));

        String[] params = param.split("[|]");

        //ComponentName cn = new ComponentName(params[0], params[1]);

        Intent intent = new Intent();
        intent.setClassName(params[0], params[1]);
        //intent.setComponent(cn);

        if (params.length > 2) {
            String[] intentParams = params[2].split("[,]");
            for (String s : intentParams) {
                String[] ss = s.split("[=]");
                intent.putExtra(ss[0], ss[1]);
            }
        }

        startActivity(intent);
    }

    /**
     * 获取WIFI信号强度
     * 返回0表示WIFI未连接
     *
     * @return
     */
    @JavascriptInterface
    public int readWiFiRssi() {

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        if (wi.getIpAddress() != 0 || WifiInfo.getDetailedStateOf(wi.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
            //正常接入到无线网络
            return wi.getRssi();
        }

        return 0;
    }

    /**
     * 读取终端ID
     *
     * @return
     */
    @JavascriptInterface
    public int readTerminalId() {

       return 0;
    }

    /**
     * 异步任务线程: 全屏播放：播放视频
     */
    private class AsyncVideoTask extends AsyncTask<String, Void, Void> {

        private String videoUrl = "";

        @Override
        protected void onPostExecute(Void aVoid) {

            Log.i(TAG, "Full Screen:Video URL: " + videoUrl);

//            videoView.stopPlayback();

//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.width = screenWidth;
//            layoutParams.height = screenHeight;
//            layoutParams.leftMargin = 0;
//            layoutParams.topMargin = 0;

//            videoView.setLayoutParams(layoutParams);
//
//            container.removeView(videoView);
//            container.addView(videoView, 1);

            videoView.setVideoURI(Uri.parse(videoUrl));
            videoView.start();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected Void doInBackground(String... strings) {

            videoUrl = strings[0];

            return null;
        }
    }

    /**
     * 异步任务线程: 窗口播放
     */
    private class AsyncWindowVideoTask extends AsyncTask<String, Void, Void> {

        private int width           = 480;
        private int height          = 270;
        private int x               = 0;
        private int y               = 0;
        private String videoUrl     = "";
        private String playerid     = "";
        private int event           = 0;

        @Override
        protected void onPostExecute(Void aVoid) {

            Log.i(TAG, "Window Video URL: " + videoUrl);
            Log.i(TAG, "Window Video Event: " + event);

            try {

                IjkVideoView videoWindow = playerPool.get(playerid);

                if (event == 1) {
                    // Switch

                    videoWindow.setVideoURI(Uri.parse(videoUrl));
                    videoWindow.start();

                } else if (event == 2) {
                    // Stop

                    videoWindow.stopPlayback();
                    videoWindow.removeAllViews();
                    idlePlayers.offer(playerid);

                } else {
                    // Play

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.width = width;
                    layoutParams.height = height;
                    layoutParams.leftMargin = x;
                    layoutParams.topMargin = y;

                    videoWindow.setLayoutParams(layoutParams);

                    videoWindow.setVideoURI(Uri.parse(videoUrl));
                    videoWindow.start();

                }

            } catch (Exception e) {
                Log.e(TAG, "AsyncWindowVideoTask happen error.", e);
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected Void doInBackground(String... strings) {

            width       = Integer.valueOf(strings[0]);
            height      = Integer.valueOf(strings[1]);
            x           = Integer.valueOf(strings[2]);
            y           = Integer.valueOf(strings[3]);
            videoUrl    = strings[4];
            playerid    = strings[5];
            event       = Integer.valueOf(strings[6]);

            return null;
        }
    }

    private class AsyncWindowVideoTask2 extends AsyncTask<String, Void, Void> {

        private int width           = 480;
        private int height          = 270;
        private int x               = 0;
        private int y               = 0;
        private String videoUrl     = "";
        private String playerid     = "";
        private int event           = 0;

        @Override
        protected void onPostExecute(Void aVoid) {

            Log.i(TAG, "Window2 Video URL: " + videoUrl);
            Log.i(TAG, "Window2 Video Event: " + event);

            try {

                VideoView videoWindow = playerPool2.get(playerid);

                if (event == 1) {
                    // Switch

                    videoWindow.setVideoURI(Uri.parse(videoUrl));
                    videoWindow.start();

                } else if (event == 2) {
                    // Stop

                    videoWindow.stopPlayback();
                    idlePlayers2.offer(playerid);

                } else {
                    // Play

                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.width = width;
                    layoutParams.height = height;
                    layoutParams.leftMargin = x;
                    layoutParams.topMargin = y;

                    videoWindow.setLayoutParams(layoutParams);

                    videoWindow.setVideoURI(Uri.parse(videoUrl));
                    videoWindow.start();

                }

            } catch (Exception e) {
                Log.e(TAG, "AsyncWindowVideoTask happen error.", e);
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected Void doInBackground(String... strings) {

            width       = Integer.valueOf(strings[0]);
            height      = Integer.valueOf(strings[1]);
            x           = Integer.valueOf(strings[2]);
            y           = Integer.valueOf(strings[3]);
            videoUrl    = strings[4];
            playerid    = strings[5];
            event       = Integer.valueOf(strings[6]);

            return null;
        }
    }

    /**
     * 浏览器的异步任务线程
     */
    private class AsyncBrowserTask extends AsyncTask<String, Void, WebView> {

        private String url = "";
        private String layer = "Mask";
        private boolean isBack = false;

        @Override
        protected void onPostExecute(WebView webView) {

            webView.loadUrl(url);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected WebView doInBackground(String... strings) {

            url = strings[0];
            layer = strings[1];
            if (layer.equals("RFB")) {
                return webViewRFB;
            }
            if (layer.equals("Web")) {
                return webView;
            }

            return webViewMask;
//            return null;
        }
    }

    /**
     * 检测最新版本，显示升级APK提示框
     * 升级方式：强制升级
     */
    private void showUpdateApkDialog() {

        // 启动APK升级任务线程
        new UpdateApkTask().execute();

    }

    /**
     * APK升级任务线程
     */
    private class UpdateApkTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPostExecute(String strings) {

            if (strings == null || strings.equals("")) {
                return;
            }

            // 跳转至安装界面
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + strings)),
                    "application/vnd.android.package-archive");
            startActivity(intent);

        }

        @Override
        protected void onProgressUpdate(Void... values) {

            /*
            // 弹出提示框
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.update_apk);
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);

            alertDialog.show();

            Window win = alertDialog.getWindow();
            win.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = win.getAttributes();
            //lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            //lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.width = screenWidth;
            lp.height = screenHeight;
            win.setAttributes(lp);
            */

        }

        @Override
        protected String doInBackground(Void... values) {

            // 检测版本
            String versionCode = "";
            String versionName = "";
            String downloadUrl = "";

            HttpURLConnection urlConnection = null;
            JsonReader reader = null;
            try {

                URL url = new URL(_url_version_check + "?versionCode=" + VERSION_CODE);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("version_code") && reader.peek() != JsonToken.NULL) {
                        versionCode = reader.nextString().trim();
                    } else if (name.equals("version_name") && reader.peek() != JsonToken.NULL) {
                        versionName = reader.nextString().trim();
                    } else if (name.equals("download_url") && reader.peek() != JsonToken.NULL) {
                        downloadUrl = reader.nextString().trim();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();

            } catch (Exception e) {
                Log.d(TAG, "", e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "", e);
                }

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            if (versionCode == null || versionCode.equals("")) {
                return "";
            }

            // 如果终端软件版本号与服务端提供的版本号不一致，则触发软件升级
            // 此处不判断版本号的高低，可处理升级回滚事件
            if (VERSION_CODE.equalsIgnoreCase(versionCode)) {
                return "";
            }

            // 通知弹出提示框
            publishProgress();

            // 下载安装包
            FileOutputStream out = null;
            try {

                URL url = new URL(downloadUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                if (in != null) {

                    File file = new File(Environment.getExternalStorageDirectory(), versionName);
                    // 如果存在同命安装包文件，则删除后再下载
                    if (file.exists()) {
                        file.delete();
                    }
                    out = new FileOutputStream(file);

                    byte[] buf = new byte[1024];
                    int ch = -1;
                    while ((ch = in.read(buf)) != -1) {
                        out.write(buf, 0, ch);
                    }

                }
                out.flush();

            } catch (Exception e) {
                Log.d(TAG, "", e);
                return "";

            } finally {

                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.d(TAG, "", e);
                    }
                }

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return versionName;
        }
    }


}
