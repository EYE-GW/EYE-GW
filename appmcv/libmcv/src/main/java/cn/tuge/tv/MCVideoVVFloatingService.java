package cn.tuge.tv;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

/**
 * Video Floating Windows: VideoView
 */

public class MCVideoVVFloatingService extends Service {

    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private VideoView videoView;
    private View displayView;

    private int width           = 100;
    private int height          = 100;
    private int x               = 0;
    private int y               = 0;
    private String videoURL     = "";
    private String videoid      = "";

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

        videoURL    = intent.getStringExtra("videoURL");
        width       = intent.getIntExtra("width", 100);
        height      = intent.getIntExtra("height", 100);
        x           = intent.getIntExtra("x", 0);
        y           = intent.getIntExtra("y", 0);
        videoid     = intent.getStringExtra("videoid");

        showFloatingWindow();

        stopSelf(startId);

        return super.onStartCommand(intent, flags, startId);
    }

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
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.x = x;
        layoutParams.y = y;

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        displayView = layoutInflater.inflate(R.layout.video_floating_view_v, null);

        videoView = displayView.findViewById(R.id.video_floating_videoview);
        //mMediaController = new MediaController(this);
        //videoView.setMediaController(mMediaController);
        videoView.setBackgroundColor(Color.TRANSPARENT);
//        videoView.getBufferPercentage();

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });


        videoView.setVideoURI(Uri.parse(videoURL));
        videoView.start();

        MCVActivity.vvMap.put(videoid, videoView);
        MCVActivity.vvViewMap.put(videoid, displayView);

        windowManager.addView(displayView, layoutParams);

    }

}
