//package cn.tuge.tv;
//
//import android.app.Service;
//import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.PixelFormat;
//import android.os.Build;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.WindowManager;
//
//import org.xwalk.core.XWalkSettings;
//import org.xwalk.core.XWalkView;
//
///**
// * Browser Floating Windows: CrossWalk
// */
//
//public class MCVBrowserCFloatingService extends Service {
//
//    public static boolean isStarted = false;
//
//    private WindowManager windowManager;
//    private WindowManager.LayoutParams layoutParams;
//
//    private XWalkView webView;
//    private View displayView;
//
//    private int width = 100;
//    private int height = 100;
//    private int x = 0;
//    private int y =0;
//    private String url = "";
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//
//        url = intent.getStringExtra("url");
//        width = intent.getIntExtra("width", 100);
//        height = intent.getIntExtra("height", 100);
//        x = intent.getIntExtra("x", 0);
//        y = intent.getIntExtra("y", 0);
//
//        Log.i(MainActivity.TAG, "url: " + url);
//
//        showFloatingWindow();

//        stopSelf(startId);
//
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    private void showFloatingWindow() {
//
//        isStarted = true;
//
//        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        layoutParams = new WindowManager.LayoutParams();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        } else {
//            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//        }
//        layoutParams.format = PixelFormat.RGBA_8888;
//        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        layoutParams.width = width;
//        layoutParams.height = height;
//        layoutParams.x = x;
//        layoutParams.y = y;
//
//        LayoutInflater layoutInflater = LayoutInflater.from(this);
//        displayView = layoutInflater.inflate(R.layout.browser_floating_view_c, null);
//
//
//        webView = displayView.findViewById(R.id.browser_floating_webview_c);
//
//        XWalkSettings settings = webView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setAllowFileAccess(true);
//        settings.setCacheMode(XWalkSettings.LOAD_NO_CACHE);
//
//        webView.setBackgroundColor(Color.TRANSPARENT);
//        webView.requestFocus();
//        webView.setHorizontalScrollBarEnabled(false);
//        webView.setVerticalScrollBarEnabled(false);
//
//        webView.loadUrl(url);
//
//        windowManager.addView(displayView, layoutParams);
//    }
//
//}
