package in.techaddicts.eligius;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SocialmediaActivity extends AppCompatActivity {

    public void linkedin_method(View view){
        Intent y1 = new Intent(this,LinkedinActivity.class);
        startActivity(y1);
    }

    public void instagram_method(View view){
        Intent y2 = new Intent(this,InstagramActivity.class);
        startActivity(y2);
    }

    public void facebook_method(View view){
        Intent y3 = new Intent(this,FacebookActivity.class);
        startActivity(y3);
    }

    public void github_method(View view){
        Intent y4 = new Intent(this,GithubActivity.class);
        startActivity(y4);
    }

    public void techaddicts_method(View view){
        Intent y5 = new Intent(this,TechaddictsActivity.class);
        startActivity(y5);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socialmedia);
    }
}