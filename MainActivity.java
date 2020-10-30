package cn.tuge.tv.appmcv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import cn.tuge.tv.MCVActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MCVActivity.class);
        startActivity(intent);
    }
}