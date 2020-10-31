package cn.tuge.tv;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Browser Floating Windows: WebView
 */

public class MCVBrowserWVFloatingService extends Service {

    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private WebView webView;
    private View displayView;

    private int width           = 100;
    private int height          = 100;
    private int x               = 0;
    private int y               = 0;
    private int isRequestFocus  = 0;
    private String url          = "";
    private String browserid    = "";

    // 键值事件，0：交给系统处理，1：交给页面处理
    private int keyEventMode = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        url = intent.getStringExtra("url");
        browserid = intent.getStringExtra("browserid");
        width = intent.getIntExtra("width", 100);
        height = intent.getIntExtra("height", 100);
        x = intent.getIntExtra("x", 0);
        y = intent.getIntExtra("y", 0);
        isRequestFocus = intent.getIntExtra("isRequestFocus", 0);

        Log.i(MCVActivity.TAG, "url: " + url);

        showFloatingWindow();

        stopSelf(startId);

        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("JavascriptInterface")
    private void showFloatingWindow() {

        isStarted = true;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        if (isRequestFocus != 0) {
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        } else {
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.x = x;
        layoutParams.y = y;

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        displayView = layoutInflater.inflate(R.layout.browser_floating_view, null);

        webView = displayView.findViewById(R.id.browser_floating_webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.requestFocus();
        webView.addJavascriptInterface(this, "MCV"); // MCV: Meow Cloud Video
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    keyDownEvent(event.getKeyCode());
                }

                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {

                Log.i(MCVActivity.TAG, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                Log.i(MCVActivity.TAG, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                Log.i(MCVActivity.TAG, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                Log.i(MCVActivity.TAG, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

                return super.shouldOverrideKeyEvent(view, event);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 防止点击页面链接启动Android默认浏览器
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i(MCVActivity.TAG, "WebView errorCode: " + error.getErrorCode() + " description: " + error.getDescription() + " failingUrl: " + request.getUrl());
                }

                super.onReceivedError(view, request, error);
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

        webView.loadUrl(url);

        MCVActivity.wvMap.put(browserid, webView);
        MCVActivity.wvViewMap.put(browserid, displayView);

        windowManager.addView(displayView, layoutParams);
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
            Log.e(MCVActivity.TAG, "", e);
        }
    }

//    ========== 窗口视频操作接口 START ======================================================================================

    /**
     * 窗口播放JS接口
     *
     * @param url 媒体资源地址
     * @param x 窗口x坐标
     * @param y 窗口y坐标
     * @param width 窗口宽
     * @param height 窗口高
     * @param type 播放器类型
     *
     * @return videoid浮动视频的唯一标识
     */
    @JavascriptInterface
    public String playVideo(int width, int height, int x, int y, String url, int type) {

        String videoid = UUID.randomUUID().toString();

        Intent intent;
        if (type != 0) {
            intent = new Intent(this, MCVideoIJKFloatingService.class);
        } else {
            intent = new Intent(this, MCVideoVVFloatingService.class);
        }

        intent.putExtra("width", width);
        intent.putExtra("height", height);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("videoURL", url);
        intent.putExtra("videoid", videoid);

        startService(intent);

        return videoid;
    }

    @JavascriptInterface
    public String playVideo(int width, int height, int x, int y, String url) {

        return playVideo(width, height, x, y, url, 0);
    }

    /**
     * 关闭浮动视频
     *
     * @param videoid
     */
    @JavascriptInterface
    public void stopVideo(String videoid) {

        try {
            MCVActivity.ijkvvMap.get(videoid).stopPlayback();
        } catch (Exception e) {
            MCVActivity.vvMap.get(videoid).stopPlayback();
        }

    }

    /**
     * 切换浮动视频
     *
     * @param videoid
     * @param url
     */
    @JavascriptInterface
    public void switchVideo(String videoid, String url) {
        try {
            MCVActivity.ijkvvMap.get(videoid).setVideoURI(Uri.parse(url));
            MCVActivity.ijkvvMap.get(videoid).start();
        } catch (Exception e) {
            MCVActivity.vvMap.get(videoid).setVideoURI(Uri.parse(url));
            MCVActivity.vvMap.get(videoid).start();
        }

    }


//    ========== 窗口视频操作接口 END   ======================================================================================

    @JavascriptInterface
    public void killProcess() {


        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appList) {

            Log.i(MCVActivity.TAG,"PID: " + appProcessInfo.pid + ", Name: " + appProcessInfo.processName);

            if ("com.skyworthdigital.app.VodBrowser".equals(appProcessInfo.processName)) {

//                android.os.Process.killProcess(appProcessInfo.pid);
//                am.killBackgroundProcesses(appProcessInfo.processName);

                try {
                    Log.i(MCVActivity.TAG, "adb shell am force-stop " + appProcessInfo.processName);
                    Runtime.getRuntime().exec("adb shell am force-stop " + appProcessInfo.processName);
                } catch (IOException e) {
                    Log.e(MCVActivity.TAG, "", e);
                }
            }
        }

    }

    /**
     * 设置键值事件模式
     * @param mode 0：交给系统处理，1：交给页面处理
     */
    @JavascriptInterface
    public void setKeyEventMode(int mode) {

        keyEventMode = mode;
    }

    @JavascriptInterface
    @Override
    public void onDestroy() {

        super.onDestroy();

//        System.gc();
//        System.exit(0);
    }
}
