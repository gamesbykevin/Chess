package com.gamesbykevin.chess.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gamesbykevin.chess.R;

import static com.gamesbykevin.chess.activity.MultiplayerActivity.MULTI_PLAYER;

public class ModeActivity extends PagerActivity {

    private static final int PAGES = 5;

    public ModeActivity() {
        super(Type.ModeSelection, PAGES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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

        //flag multi-player false
        MULTI_PLAYER = false;

        if (CURRENT_PAGE == PAGES - 1) {

            //start replay activity
            startActivity(new Intent(this, ReplayActivity.class));

        } else if (CURRENT_PAGE == PAGES - 2) {// || CURRENT_PAGE == PAGES - 3) {

            //flag multi-player true
            MULTI_PLAYER = true;

            //start game activity
            startActivity(new Intent(this, GameActivity.class));

        } else {

            //start game activity
            startActivity(new Intent(this, GameActivity.class));
        }
    }

    @Override
    public void onBackPressed() {

        //start game activity
        startActivity(new Intent(this, MainActivity.class));

        //close this activity
        finish();
    }
}