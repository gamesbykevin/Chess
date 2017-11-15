package com.gamesbykevin.chess.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gamesbykevin.chess.R;

public class ModeActivity extends PagerActivity {

    public ModeActivity() {
        super(Type.ModeSelection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set the total number of pages
        PAGES = 6;

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

        if (CURRENT_PAGE == 5) {

            PAGES = 5;

            //start replay activity
            startActivity(new Intent(this, ReplayActivity.class));

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