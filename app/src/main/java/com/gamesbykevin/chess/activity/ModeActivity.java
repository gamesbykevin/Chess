package com.gamesbykevin.chess.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gamesbykevin.chess.R;

public class ModeActivity extends PagerActivity {

    public ModeActivity() {
        super(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set the total number of pages
        PAGES = 5;

        //inflate content view
        setContentView(R.layout.activity_mode);

        //call parent
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {

        //call parent
        super.onPause();
    }

    @Override
    public void onStart() {

        //call parent
        super.onStart();
    }

    @Override
    public void onResume() {

        //call parent
        super.onResume();

        //resume audio
        super.playMenu();
    }

    @Override
    protected void onDestroy() {

        //call parent
        super.onDestroy();
    }

    public void onClickStartGame(View view) {

        //start game activity
        startActivity(new Intent(this, GameActivity.class));
    }
}