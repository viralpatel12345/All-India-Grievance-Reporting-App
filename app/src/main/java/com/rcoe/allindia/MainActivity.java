package com.rcoe.allindia;

import de.hdodenhof.circleimageview.CircleImageView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class MainActivity extends BaseActivity {

    private int i=0,backpressed=0;
    private CircularProgressBar circularProgressBar;
    private CircleImageView civ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        civ = findViewById(R.id.profile_image);
        civ.setImageResource(R.drawable.logo);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        logic();

    }

    public void logic() {
        if (i <= 100) {
            Handler h = new Handler();
            h.postDelayed(new Runnable() {

                public void run() {

                    circularProgressBar.setProgress(i);
                    i=i+2;
                    logic();
                }
            }, 1);
        }
        else {
            if(backpressed != 1)
            {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }

        }
    }

    @Override
    public void onBackPressed()
    {
        backpressed=1;
        finish();
    }

}
