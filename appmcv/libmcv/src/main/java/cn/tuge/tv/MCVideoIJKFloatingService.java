package cn.tuge.tv;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;

import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Video Floating Windows: IjkVideoView
 */

public class MCVideoIJKFloatingService extends Service {

    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private IjkVideoView videoView;
    private TableLayout mHudView;
    private View displayView;

    private int width           = 480;
    private int height          = 270;
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
        displayView = layoutInflater.inflate(R.layout.video_floating_view_i, null);
        videoView = displayView.findViewById(R.id.video_floating_ijkvideoview);
        mHudView = displayView.findViewById(R.id.hud_view);
        mHudView.setVisibility(View.INVISIBLE);

        //mMediaController = new MediaController(this);
        //videoView.setMediaController(mMediaController);
        videoView.setHudView(mHudView);
        videoView.setBackgroundColor(Color.TRANSPARENT);

//        videoView.getBufferPercentage();
        videoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {


            @Override
            public void onPrepared(IMediaPlayer mediaPlayer) {

//                if (mediaPlayer instanceof IjkMediaPlayer) {
//
//                    ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//                    ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzeduration",1);
//                    ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"analyzemaxduration",100L);
//                    ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 100);
//                    ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);
//                    ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
//                    ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 0);
//                    ((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 10);
//                    //((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
//                    //((IjkMediaPlayer) mediaPlayer).setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "stimeout", "5000000");
//                }



                mediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(IMediaPlayer mp) {
                        mp.start();
                    }
                });

                //mediaPlayer.start();
                //mediaPlayer.setLooping(true);

                //webViewMask.load(blankPage, null);

            }

        });

        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mediaPlayer) {

                /*
                System.out.println("currentVideoUrl = " + currentVideoUrl);
                videoView.setVideoURI(Uri.parse(currentVideoUrl));
                videoView.start();
                */
            }
        });

        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {

                Log.e(MCVActivity.TAG, "what:extra: " + what + ":" + extra);
                return true;
            }
        });

        videoView.setVideoURI(Uri.parse(videoURL));
        videoView.start();

        MCVActivity.ijkvvMap.put(videoid, videoView);
        MCVActivity.ijkvvViewMap.put(videoid, displayView);

        windowManager.addView(displayView, layoutParams);

    }

}
