package com.gamesbykevin.chess.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.Toast;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.util.UtilityHelper;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.chess.activity.GameActivity.getGame;

public class MainActivity extends BaseActivity {

    //did we prompt the user before exiting the app
    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call parent
        super.onCreate(savedInstanceState);

        //assign main layout
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {

        //call parent
        super.onStart();
    }

    @Override
    protected void onStop() {

        //call parent
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        //call parent
        super.onDestroy();

        //recycle resources
        super.dispose();
    }

    @Override
    protected void onPause() {

        //call parent
        super.onPause();
    }

    @Override
    protected void onResume() {

        //call parent
        super.onResume();

        //resume audio
        super.playMenu();

        //flag prompt false
        exit = false;

        //enable all buttons in container
        enableChildren((ViewGroup)findViewById(R.id.table_menu));
    }

    private void enableChildren(ViewGroup viewGroup) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            //enable the view group
            viewGroup.getChildAt(i).setEnabled(true);

            //recursive call in case there are additional children
            if (viewGroup.getChildAt(i) instanceof ViewGroup)
                enableChildren((ViewGroup)viewGroup.getChildAt(i));
        }
    }

    @Override
    public void onBackPressed() {

        try {

            //if we already prompted the user, exit the app
            if (exit) {

                //no need to bypass login in the future
                //BYPASS_LOGIN = false;

                //sign out of google play services
                //super.signOut();

                //finish activity
                super.finish();

                //close all activities
                ActivityCompat.finishAffinity(this);

            } else {

                //prompt the user if they want to exit
                Toast.makeText(this, getString(R.string.exit_prompt), Toast.LENGTH_SHORT).show();

                //flag that we have prompted the user
                exit = true;
            }

        } catch (Exception e) {

            //handle exception
            UtilityHelper.handleException(e);

            //finish activity anyways
            super.finish();
        }
    }

    public void onClickStart(View view) {

        //is sound enabled in the game?
        SOUND_ENABLED = getBooleanValue(R.string.sound_file_key);

        //start game
        openActivity(view, ModeActivity.class);
    }

    public void onClickTutorial(View view) {
        openActivity(view, TutorialActivity.class);
    }

    public void onClickOptions(View view) {
        openActivity(view, OptionsActivity.class);
    }

    public void onClickLeaderboards(View view) {

        //display all the leader boards
        //super.displayLeaderboardUI(null);
    }

    private void openActivity(View view, Class classObj) {

        //disable view so it can't be clicked twice
        view.setEnabled(false);

        //start activity
        startActivity(new Intent(this, classObj));
    }
}