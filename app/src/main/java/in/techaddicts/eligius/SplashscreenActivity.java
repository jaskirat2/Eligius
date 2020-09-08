package in.techaddicts.eligius;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashscreenActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 3500;

    Animation topAnim, rightToUp, leftToUp;
    ImageView image;
    TextView t1, t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);  // This single line is to remove status bar
        setContentView(R.layout.activity_splashscreen);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        rightToUp = AnimationUtils.loadAnimation(this, R.anim.right_to_up_anim);
        leftToUp = AnimationUtils.loadAnimation(this,R.anim.left_to_down_anim);

        image = findViewById(R.id.abc);
        t1 = findViewById(R.id.textView4);
        t2 = findViewById(R.id.textView22);

        image.setAnimation(topAnim);
        t1.setAnimation(rightToUp);
        t2.setAnimation(leftToUp);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashscreenActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_SCREEN);
    }
}