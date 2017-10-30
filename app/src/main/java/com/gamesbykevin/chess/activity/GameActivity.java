package com.gamesbykevin.chess.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.opengl.OpenGLSurfaceView;
import com.gamesbykevin.chess.players.PlayerVars;
import com.gamesbykevin.chess.util.GameTimer;
import com.gamesbykevin.chess.util.UtilityHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.gamesbykevin.chess.util.UtilityHelper.DEBUG;

public class GameActivity extends BaseActivity implements Disposable {

    //our open GL surface view
    private OpenGLSurfaceView glSurfaceView;

    //Our game manager class
    private static Game GAME;

    //has the activity been paused
    private boolean paused = false;

    //our layout parameters
    private LinearLayout.LayoutParams layoutParams;

    //a list of layouts on the game screen, separate from open gl layout
    private List<ViewGroup> layouts;

    /**
     * Different steps in the game
     */
    public enum Screen {
        Loading,
        Ready,
        GameOver,
    }

    //current screen we are on
    private Screen screen = Screen.Ready;

    //keep track of game time
    private GameTimer timer;

    //our visual progress bar
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //assign to calculate
        PlayerVars.STATUS = PlayerVars.Status.Select;

        //call parent
        super.onCreate(savedInstanceState);

        //create our game manager
        GAME = new Game(this);

        //set the content view
        setContentView(R.layout.activity_game);

        //obtain our open gl surface view object for reference
        this.glSurfaceView = findViewById(R.id.openGLView);

        this.progressBar = findViewById(R.id.simpleProgressBar);

        //add the layouts to our list
        this.layouts = new ArrayList<>();
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameOver));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutLoadingScreen));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameControls));
    }

    public void updateProgress(final int progress) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        });
    }

    public void displayMessage(final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        if (DEBUG)
            UtilityHelper.logEvent(message);
    }

    public OpenGLSurfaceView getSurfaceView() {
        return this.glSurfaceView;
    }

    public void onClickResetCamera(View view) {
        getSurfaceView().getRenderer().resetCamera();
    }

    public static Game getGame() {
        return GAME;
    }

    public GameTimer getTimer() {

        //create timer if null
        if (this.timer == null)
            this.timer = new GameTimer(this);

        //return our timer object
        return this.timer;
    }

    @Override
    protected void onStart() {

        //call parent
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        //call parent
        super.onDestroy();

        //cleanup resources
        if (GAME != null) {
            try {
                GAME.dispose();
            } catch (Exception e) {
                UtilityHelper.handleException(e);
            }
        }

        if (layouts != null) {

            for (ViewGroup view : layouts) {
                if (view != null) {
                    try {
                        view.removeAllViews();
                        view = null;
                    } catch (Exception e) {
                        UtilityHelper.handleException(e);
                    }
                }
            }

            layouts.clear();
            layouts = null;
        }

        if (timer != null) {
            timer.dispose();
            timer = null;
        }

        glSurfaceView = null;
        layoutParams = null;
    }

    @Override
    protected void onPause() {

        //call parent
        super.onPause();

        //pause the game
        getGame().onPause();

        //flag paused true
        this.paused = true;

        //pause the game view
        glSurfaceView.onPause();

        //flag for recycling
        glSurfaceView = null;

        //stop all sound
        stopSound();
    }

    @Override
    protected void onResume() {

        //call parent
        super.onResume();

        //resume theme if the game has started
        super.playTheme();

        //resume the game object
        getGame().onResume();

        //if the game was previously paused we need to re-initialize the views
        if (this.paused) {

            //flag paused false
            this.paused = false;

            //create a new OpenGL surface view
            glSurfaceView = new OpenGLSurfaceView(this);

            //resume the game view
            getSurfaceView().onResume();

            //remove layouts from the parent view
            for (int i = 0; i < layouts.size(); i++) {
                ((ViewGroup)layouts.get(i).getParent()).removeView(layouts.get(i));
            }

            //set the content view for our open gl surface view
            setContentView(getSurfaceView());

            //add the layouts to the current content view
            for (int i = 0; i < layouts.size(); i++) {
                super.addContentView(layouts.get(i), getLayoutParams());
            }

        } else {

            //resume the game view
            getSurfaceView().onResume();

        }

        //determine what screen(s) are displayed
        setScreen(getScreen());
    }

    public Screen getScreen() {
        return this.screen;
    }

    public void setScreen(final Screen screen) {

        //default all layouts to hidden
        for (int i = 0; i < layouts.size(); i++) {
            setLayoutVisibility(layouts.get(i), false);
        }

        //only display the correct screens
        switch (screen) {

            //show loading screen
            case Loading:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutLoadingScreen), true);
                break;

            //decide which game over screen is displayed
            case GameOver:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameOver), true);
                break;

            //don't re-enable anything
            case Ready:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameControls), true);
                break;
        }

        //assign screen to view
        this.screen = screen;
    }

    private LinearLayout.LayoutParams getLayoutParams() {

        if (this.layoutParams == null)
            this.layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT);

        return this.layoutParams;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void onClickMenu(View view) {

        //go back to the main game menu
        startActivity(new Intent(this, MainActivity.class));
    }

    public void onClickLeaderboard(View view) {

        //displayLeaderboardUI(getString(LeaderboardHelper.getResId(getGame().getBoard())));
    }

    public void onClickHome(View view) {

        //start the activity
        startActivity(new Intent(GameActivity.this, MainActivity.class));

        //remove this activity from the back stack
        finish();
    }

    public void onClickShowTimer(View view) {
        //show timer
    }
}