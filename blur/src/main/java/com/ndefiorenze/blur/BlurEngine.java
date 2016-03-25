package com.ndefiorenze.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * created on: 2016-03-24
 *
 * @author Nicola De Fiorenze
 */
public class BlurEngine {

    public static final int RADIUS = 12;
    public static final int SCALE_FACTOR = 8;
    private static final String TAG = BlurEngine.class.getName();
    private static BlurEngine instance = null;
    private final Context context;
    private final Handler handler = new Handler();
    long sum = 0;
    long count = 0;
    private StatusListener statusListener;
    private boolean run = false;
    private Runnable runnableCode;
    private Repeat repeat;

    private ImageView destination;

    private BlurEngine(Context context) {
        this.context = context;
    }

    public static BlurEngine getInstance(Context context) {
        if (instance == null) {
            instance = new BlurEngine(context);
        }
        return instance;
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    private void blurView(View source, ImageView destination) {
        long startMs = System.currentTimeMillis();
        long totalTime = 0;
        if (run) {
            float scaleFactor = SCALE_FACTOR;
            float radius = RADIUS;
            try {
                Bitmap overlay = Bitmap.createBitmap((int) (source.getMeasuredWidth() / scaleFactor),
                        (int) (source.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(overlay);
                canvas.translate(-source.getLeft() / scaleFactor, -source.getTop() / scaleFactor);
                canvas.scale(1 / scaleFactor, 1 / scaleFactor);
                Paint paint = new Paint();
                paint.setFlags(Paint.FILTER_BITMAP_FLAG);
                source.draw(canvas);
                overlay = FastBlur.doBlur(overlay, (int) radius, true);
                destination.setImageDrawable(new BitmapDrawable(context.getResources(), overlay));
                totalTime = System.currentTimeMillis() - startMs;
                Log.d(TAG, "Time for blur(): " + totalTime + "ms");
            } catch (Exception e) {
                Log.e(TAG, "blur: ", e);
            }
            sum += totalTime;
            count++;

            if (statusListener != null)
                statusListener.finish(totalTime, sum / count);

            if (sum < 0) {
                sum = 0;
                count = 1;
            }

            if (repeat == Repeat.YES)
                handler.postDelayed(runnableCode, 2 * (sum / count));
            Log.d(TAG, "finish() called with: " + "milliseconds = [" + totalTime + "] med= [" + (sum / count) + "]");
        }
    }

    public void blur(final View source, final ImageView destination) {
        this.destination = destination;
        runnableCode = new Runnable() {
            @Override
            public void run() {
                blurView(source, destination);
            }
        };
    }

    public void stop() {
        run = false;
        destination.setImageResource(android.R.color.transparent);
    }

    public void start() {
        start(Repeat.YES);
    }

    public void start(Repeat repeat) {
        if (!run) {
            this.repeat = repeat;
            run = true;
            handler.post(runnableCode);
        }
    }

    public boolean isRunning(){
        return run;
    }

    public enum Repeat {YES, NO}

    public interface StatusListener {
        void finish(long milliseconds, long averageTime);
    }
}
