package com.nanda.bookslibrary.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.nanda.bookslibrary.R;
import com.nanda.bookslibrary.base.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(HomeActivity.getCallingIntent(SplashActivity.this));
                finish();
            }
        }, 1000);
    }
}
