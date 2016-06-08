package com.dou361.ijkplayer_simple.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dou361.ijkplayer_simple.activity.PlayerActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_player;
    private Button btn_live;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_player = (Button) findViewById(R.id.btn_player);
        btn_live = (Button) findViewById(R.id.btn_live);
        btn_player.setOnClickListener(this);
        btn_live.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_player) {
            Intent intent = new Intent(this, PlayerActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_live) {
            Intent intent = new Intent(this, PlayerLiveActivity.class);
            startActivity(intent);
        }
    }
}
