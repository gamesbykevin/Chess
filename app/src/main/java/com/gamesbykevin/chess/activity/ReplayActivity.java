package com.gamesbykevin.chess.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.game.GameHelper;
import com.gamesbykevin.chess.services.AchievementHelper;
import com.gamesbykevin.chess.services.EventHelper;

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

        //only show when the user makes a selection
        findViewById(R.id.layoutLoadingScreen).setVisibility(View.INVISIBLE);

        //display the pager icons
        findViewById(R.id.listPageContainer).setVisibility(View.VISIBLE);

        //re-enable any disabled children
        enableChildren((ViewGroup)findViewById(R.id.customPager));
    }

    @Override
    protected void onDestroy() {

        //call parent
        super.onDestroy();
    }

    public void onClickStartReplay(View view) {

        //display the loading page for now
        findViewById(R.id.layoutLoadingScreen).setVisibility(View.VISIBLE);

        //hide the pager icons
        findViewById(R.id.listPageContainer).setVisibility(View.INVISIBLE);

        //disable view after we click it
        if (view != null)
            view.setEnabled(false);

        if (SAVE) {

            //achievements
            AchievementHelper.trackAchievementSaveReplay(this);

            //determine where to save our game
            boolean result = GameHelper.saveHistory(getGame(), CURRENT_PAGE);

            //notify user, if replay was saved
            if (result) {
                displayMessage(R.string.save_replay_success);

                //update event
                EventHelper.trackEventGameSavedReplay(this);
            }

            //go back to mode activity
            startActivity(new Intent(this, ModeActivity.class));

        } else {

            //achievements
            AchievementHelper.trackAchievementWatchReplay(this);

            //update event
            EventHelper.trackEventGameWatchReplay(this);

            //flag we want to watch a replay
            Game.REPLAY = true;

            //start game activity
            startActivity(new Intent(this, GameActivity.class));
        }

        //flag save false
        SAVE = false;
    }
}