package com.ndefiorenze.blursample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ndefiorenze.blur.BlurEngine;

public class MainActivity extends AppCompatActivity {

    public static final int ANIMATION_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewGroup blurView = (ViewGroup) findViewById(R.id.blur_view);
        final View blueBox = findViewById(R.id.blue_box);
        final View redBox = findViewById(R.id.red_box);
        final TextView timeTextView = (TextView) findViewById(R.id.time_text_view);

        ImageView blurDestinationView = (ImageView) findViewById(R.id.blur_content);

        initAnimations(blurView, blueBox, redBox);


        final BlurEngine blurEngine = BlurEngine.getInstance(this);
        blurEngine.blur(blurView, blurDestinationView);
        blurEngine.setStatusListener(new BlurEngine.StatusListener() {
            @Override
            public void finish(long milliseconds, long averageTime) {
                timeTextView.setText("Blurred in "+String.format("%03d", milliseconds)+"ms with an average of "+String.format("%03d", averageTime)+"ms");
            }
        });

        Button blurButton = (Button) findViewById(R.id.start_blur_button);
        blurButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!blurEngine.isRunning()) {
                    blurEngine.start(BlurEngine.Repeat.YES);
                } else {
                    blurEngine.stop();
                }
            }
        });
    }

    private void initAnimations(final ViewGroup blurView, final View blueBox, final View redBox) {
        blurView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                final float topY = blueBox.getY();
                final float bottomY = redBox.getY();

                final Handler handlerBlueBox = new Handler();
                handlerBlueBox.post(new Runnable() {
                    private Runnable runnable;
                    float bottom = bottomY;
                    float top = 0;
                    boolean up = true;

                    @Override
                    public void run() {
                        runnable = this;

                        blueBox.animate().translationY(up ? top : bottom).rotation(up ? 90 : -90).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                up = !up;
                                handlerBlueBox.post(runnable);
                            }
                        });
                    }
                });

                final Handler handlerRedBox = new Handler();
                handlerRedBox.post(new Runnable() {
                    private Runnable runnable;
                    float bottom = 0;
                    float top = -bottomY + topY;
                    boolean up = false;

                    @Override
                    public void run() {
                        runnable = this;

                        redBox.animate().translationY(up ? top : bottom).scaleX(up ? 2 : -2).scaleY(up ? -2 : 2).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                up = !up;
                                handlerRedBox.post(runnable);
                            }
                        });
                    }
                });
                blurView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }


}
