package com.gamesbykevin.chess.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.Toast;

import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.game.Game;
import com.gamesbykevin.chess.game.GameHelper;
import com.gamesbykevin.chess.opengl.OpenGLSurfaceView;
import com.gamesbykevin.chess.players.PlayerHelper;
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
        Settings,
    }

    //current screen we are on
    private static Screen SCREEN = Screen.Loading;

    //keep track of game time
    private GameTimer timer;

    //our visual progress bar
    private ProgressBar progressBar;

    //container for our game timer
    private TableLayout tableTimer;

    //our array adapter for the list view
    private ArrayAdapter<String> adapter;

    //what is the current selected item
    private int selectedListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //default to loading screen
        SCREEN = Screen.Loading;

        if (DEBUG)
            UtilityHelper.logEvent("onCreate");

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

        //obtain our progress bar
        this.progressBar = findViewById(R.id.simpleProgressBar);

        //obtain our list view
        final ListView listView = findViewById(R.id.listViewHistory);

        //create our data array adapter, use ArrayList so we can make it dynamic
        this.adapter = new ArrayAdapter<String>(this, R.layout.layout_history, new ArrayList<String>()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                final View renderer = super.getView(position, convertView, parent);
                if (position == selectedListItem) {
                    renderer.setBackgroundResource(R.color.colorGray);
                } else  {
                    renderer.setBackgroundResource(R.color.colorLightGray);
                }
                return renderer;
            }

        };

        //update our list view with the adapter
        listView.setAdapter(this.adapter);

        //add our history to the array adapter
        for (int i = 0; i < GAME.getHistory().size(); i++) {
            updateListView(GAME.getHistory().get(i).toString());
        }

        //obtain our game timer container reference
        this.tableTimer = findViewById(R.id.tableGameTimer);

        //display the timer according to the settings
        this.tableTimer.setVisibility(getSharedPreferences().getBoolean(getString(R.string.timer_file_key), true) ? VISIBLE : View.INVISIBLE);

        //add the layouts to our list
        this.layouts = new ArrayList<>();
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameSettings));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutLoadingScreen));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameControls));

        //assign the current screen
        setScreen(getScreen(), true);
    }

    public void updateListView(final int position) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //assign the current item
                selectedListItem = position;

                //position at the latest move
                ListView listView = findViewById(R.id.listViewHistory);
                listView.setSelection(position);
                listView.setVerticalScrollBarEnabled(false);
                listView.setHorizontalScrollBarEnabled(false);

                if (position + 5 < listView.getCount() && position > 5) {
                    listView.smoothScrollToPosition(position + 5);
                } else {
                    listView.smoothScrollToPosition(position);
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    public void updateListView(final String description) {

        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                adapter.add(description);

                //position at the latest move
                updateListView(adapter.getCount() - 1);
            }
        });
    }

    public void updateProgress(final int progress) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        });
    }

    public void displayMessage(final int resid) {
        displayMessage(getString(resid));
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
        getSurfaceView().getRenderer().changeCamera();
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

    public void displayTimer(final boolean visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.tableGameTimer).setVisibility(visible ? VISIBLE : View.INVISIBLE);
            }
        });

    }

    @Override
    protected void onStart() {

        if (DEBUG)
            UtilityHelper.logEvent("onStart");

        //call parent
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        if (DEBUG)
            UtilityHelper.logEvent("onDestroy");

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
        GAME = null;
        SCREEN = null;
    }

    @Override
    protected void onPause() {

        if (DEBUG)
            UtilityHelper.logEvent("onPause");

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

        if (DEBUG)
            UtilityHelper.logEvent("onResume");

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
        setScreen(getScreen(), true);

        //update the progress bar
        updateProgress(0);
    }

    public static Screen getScreen() {
        return SCREEN;
    }

    public void setScreen(final Screen screen, final boolean hide) {

        //default all layouts to hidden
        if (hide) {
            for (int i = 0; i < layouts.size(); i++) {
                setLayoutVisibility(layouts.get(i), false);
            }
        }

        //only display the correct screens
        switch (screen) {

            //show loading screen
            case Loading:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutLoadingScreen), true);
                break;

            //decide which game over screen is displayed
            case Settings:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameSettings), true);
                break;

            //don't re-enable anything
            case Ready:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameControls), true);
                break;

            default:
                throw new RuntimeException("Screen not handled: " + screen.toString());
        }

        //assign screen to view
        this.SCREEN = screen;
    }

    private LinearLayout.LayoutParams getLayoutParams() {

        if (this.layoutParams == null)
            this.layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT);

        return this.layoutParams;
    }

    @Override
    public void onBackPressed() {

        if (DEBUG)
            UtilityHelper.logEvent("onBackPressed");

        //save current game to shared preferences
        GameHelper.saveHistory(this, getGame());

        //call parent
        super.onBackPressed();

        //end the activity
        finish();
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

    public void onClickSettings(View view) {

        //switch our current choice
        toggleSettings(getScreen() != Screen.Settings);
    }

    public void toggleSettings(final boolean visible) {

        if (visible) {
            setScreen(Screen.Settings, getScreen() == Screen.Loading);
        } else {
            setScreen(Screen.Ready, true);
        }

        //we display the positions if we are viewing the settings screen
        GameHelper.displayPositions(GAME, getScreen() == Screen.Settings);
    }
}