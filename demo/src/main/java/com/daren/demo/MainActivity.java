package com.daren.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.daren.videorecord.MediaRecorderActivity;
import com.daren.videorecord.VConstant;
import com.daren.videorecord.VideoInit;
import com.daren.videorecord.VideoPlayerActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RECORD_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.ttttt).setOnClickListener(this);

        VideoInit.init(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, MediaRecorderActivity.class);
        startActivityForResult(intent,RECORD_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK){
            String videoPath = data.getStringExtra(VConstant.RECORD_VIDEO_PATH);

            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra(VConstant.RECORD_VIDEO_PATH,videoPath);
        }
    }


}
