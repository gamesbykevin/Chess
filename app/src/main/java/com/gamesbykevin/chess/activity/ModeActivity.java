package com.gamesbykevin.chess.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gamesbykevin.chess.R;

public class ModeActivity extends PagerActivity {

    private static final int PAGES = 6;

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

        if (CURRENT_PAGE == PAGES - 1) {

            //start replay activity
            startActivity(new Intent(this, ReplayActivity.class));

        } else if (CURRENT_PAGE == PAGES - 2 || CURRENT_PAGE == PAGES - 3) {

            //start game activity
            startActivity(new Intent(this, MultiplayerActivity.class));

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