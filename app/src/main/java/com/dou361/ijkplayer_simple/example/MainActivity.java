package com.dou361.ijkplayer_simple.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dou361.ijkplayer_simple.activity.PlayerActivity;
import com.dou361.ijkplayer_simple.example.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_player = (Button) findViewById(R.id.btn_player);
        btn_player.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }
}
