package com.magnetic.hackathon.dogeideasapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Created by dliu on 4/20/16.
 */
public class SplashScreen extends Activity {
    private static int SPLASH_TIME_OUT = 3000;
    private static int ANIM_TIME_OUT = 2000;

    Animation animFadein;
    Animation animFadeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final ImageView image = (ImageView)findViewById(R.id.logo);

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        animFadeout = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);

        image.startAnimation(animFadein);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                image.startAnimation(animFadeout);
            }

        }, SPLASH_TIME_OUT);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, ANIM_TIME_OUT + SPLASH_TIME_OUT);
    }
}
