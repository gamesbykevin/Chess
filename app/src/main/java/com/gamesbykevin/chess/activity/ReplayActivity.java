package com.gamesbykevin.chess.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.game.GameHelper;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;

public class ReplayActivity extends PagerActivity {

    //do we save the replay
    public static boolean SAVE = false;

    private static final int PAGES = 5;

    public ReplayActivity() {
        super(Type.ReplaySelection, PAGES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //inflate content view
        setContentView(R.layout.activity_replay);

        //call parent
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
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

    public void onClickStartReplay(View view) {

        if (SAVE) {

            //determine where to save our game
            GameHelper.saveHistory(getGame(), CURRENT_PAGE);

            //go back kto mode activity
            startActivity(new Intent(this, ModeActivity.class));

        } else {

            //flag we want to watch a replay
            Game.REPLAY = true;

            //start game activity
            startActivity(new Intent(this, GameActivity.class));
        }

        //flag save false
        SAVE = false;
    }
}